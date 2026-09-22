package com.parttime.platform.service;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.QuestionBankMapper;
import com.parttime.platform.pojo.cmd.QuestionBankCreateCmd;
import com.parttime.platform.pojo.cmd.QuestionBankUpdateCmd;
import com.parttime.platform.pojo.entity.QuestionBank;
import com.parttime.platform.pojo.vo.QuestionBankVO;
import com.parttime.platform.service.impl.QuestionBankServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionBankServiceTest {

    @Mock
    private QuestionBankMapper questionBankMapper;

    @InjectMocks
    private QuestionBankServiceImpl service;

    private QuestionBank bank(Long id, String name, String status) {
        QuestionBank b = new QuestionBank();
        b.setId(id);
        b.setName(name);
        b.setStatus(status);
        return b;
    }

    @Test
    void list_shouldReturnBanksWithCount() {
        when(questionBankMapper.findAll()).thenReturn(List.of(bank(1L, "安全题库", "ACTIVE")));
        when(questionBankMapper.countByBankId(1L)).thenReturn(5);

        List<QuestionBankVO> result = service.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("安全题库");
        assertThat(result.get(0).getCount()).isEqualTo(5);
    }

    @Test
    void create_shouldInsertActiveBank() {
        QuestionBankCreateCmd cmd = new QuestionBankCreateCmd();
        cmd.setName("安全题库");
        cmd.setDescription("安全培训");
        when(questionBankMapper.findByName("安全题库")).thenReturn(Optional.empty());

        QuestionBankVO result = service.create(cmd);

        assertThat(result.getName()).isEqualTo("安全题库");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(questionBankMapper).insert(any(QuestionBank.class));
    }

    @Test
    void create_duplicateName_shouldThrow() {
        QuestionBankCreateCmd cmd = new QuestionBankCreateCmd();
        cmd.setName("安全题库");
        when(questionBankMapper.findByName("安全题库")).thenReturn(Optional.of(bank(1L, "安全题库", "ACTIVE")));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("题库名称已存在");
        verify(questionBankMapper, never()).insert(any());
    }

    @Test
    void update_shouldRenameSuccessfully() {
        QuestionBankUpdateCmd cmd = new QuestionBankUpdateCmd();
        cmd.setId(1L);
        cmd.setName("新题库名");
        when(questionBankMapper.findById(1L)).thenReturn(Optional.of(bank(1L, "旧题库名", "ACTIVE")));
        when(questionBankMapper.findByName("新题库名")).thenReturn(Optional.empty());

        QuestionBankVO result = service.update(cmd);

        assertThat(result.getName()).isEqualTo("新题库名");
        verify(questionBankMapper).update(any(QuestionBank.class));
    }

    @Test
    void update_duplicateNameExcludingSelf_shouldThrow() {
        QuestionBankUpdateCmd cmd = new QuestionBankUpdateCmd();
        cmd.setId(1L);
        cmd.setName("其他题库");
        when(questionBankMapper.findById(1L)).thenReturn(Optional.of(bank(1L, "旧题库名", "ACTIVE")));
        when(questionBankMapper.findByName("其他题库")).thenReturn(Optional.of(bank(2L, "其他题库", "ACTIVE")));

        assertThatThrownBy(() -> service.update(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("题库名称已存在");
        verify(questionBankMapper, never()).update(any());
    }

    @Test
    void toggle_activeToDisabled_shouldUpdateStatus() {
        when(questionBankMapper.findById(1L)).thenReturn(Optional.of(bank(1L, "安全题库", "ACTIVE")));

        service.toggle(1L);

        verify(questionBankMapper).updateStatus(1L, "DISABLED");
    }

    @Test
    void toggle_disabledToActive_shouldUpdateStatus() {
        when(questionBankMapper.findById(1L)).thenReturn(Optional.of(bank(1L, "安全题库", "DISABLED")));

        service.toggle(1L);

        verify(questionBankMapper).updateStatus(1L, "ACTIVE");
    }
}
