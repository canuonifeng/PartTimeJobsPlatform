package com.parttime.platform.service;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.QuestionBankQuestionMapper;
import com.parttime.platform.pojo.cmd.QuestionCreateCmd;
import com.parttime.platform.pojo.cmd.QuestionListCmd;
import com.parttime.platform.pojo.cmd.QuestionUpdateCmd;
import com.parttime.platform.pojo.entity.QuestionBankQuestion;
import com.parttime.platform.pojo.vo.QuestionVO;
import com.parttime.platform.service.impl.QuestionBankQuestionServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionBankQuestionServiceTest {

    @Mock
    private QuestionBankQuestionMapper questionBankQuestionMapper;

    @InjectMocks
    private QuestionBankQuestionServiceImpl service;

    private QuestionCreateCmd.OptionDTO option(String key, String label) {
        QuestionCreateCmd.OptionDTO o = new QuestionCreateCmd.OptionDTO();
        o.setKey(key);
        o.setLabel(label);
        return o;
    }

    private QuestionCreateCmd baseCreateCmd(String type, String answer, List<QuestionCreateCmd.OptionDTO> options) {
        QuestionCreateCmd cmd = new QuestionCreateCmd();
        cmd.setBankId(1L);
        cmd.setQuestionType(type);
        cmd.setStem("题干");
        cmd.setOptions(options);
        cmd.setAnswer(answer);
        return cmd;
    }

    private QuestionBankQuestion question(Long id, String status) {
        QuestionBankQuestion q = new QuestionBankQuestion();
        q.setId(id);
        q.setBankId(1L);
        q.setQuestionType("SINGLE_CHOICE");
        q.setStem("题干");
        q.setOptionsJson("[]");
        q.setAnswer("A");
        q.setStatus(status);
        q.setSortOrder(0);
        return q;
    }

    @Test
    void list_shouldReturnQuestionsByBankId() {
        when(questionBankQuestionMapper.findByBankId(1L)).thenReturn(List.of(question(1L, "DRAFT")));
        QuestionListCmd cmd = new QuestionListCmd();
        cmd.setBankId(1L);

        List<QuestionVO> result = service.list(cmd);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void list_nullBankId_shouldThrow() {
        QuestionListCmd cmd = new QuestionListCmd();
        assertThatThrownBy(() -> service.list(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("题库ID不能为空");
    }

    @Test
    void create_singleChoice_shouldInsertDraft() {
        QuestionCreateCmd cmd = baseCreateCmd("SINGLE_CHOICE", "A",
                List.of(option("A", "选项A"), option("B", "选项B")));

        QuestionVO result = service.create(cmd);

        assertThat(result.getStatus()).isEqualTo("DRAFT");
        assertThat(result.getQuestionType()).isEqualTo("SINGLE_CHOICE");
        verify(questionBankQuestionMapper).insert(any(QuestionBankQuestion.class));
    }

    @Test
    void create_multipleChoice_shouldInsertDraft() {
        QuestionCreateCmd cmd = baseCreateCmd("MULTIPLE_CHOICE", "A,B",
                List.of(option("A", "选项A"), option("B", "选项B"), option("C", "选项C")));

        QuestionVO result = service.create(cmd);

        assertThat(result.getStatus()).isEqualTo("DRAFT");
        verify(questionBankQuestionMapper).insert(any(QuestionBankQuestion.class));
    }

    @Test
    void create_judge_shouldInsertDraft() {
        QuestionCreateCmd cmd = baseCreateCmd("JUDGE", "TRUE",
                List.of(option("TRUE", "正确"), option("FALSE", "错误")));

        QuestionVO result = service.create(cmd);

        assertThat(result.getStatus()).isEqualTo("DRAFT");
        verify(questionBankQuestionMapper).insert(any(QuestionBankQuestion.class));
    }

    @Test
    void create_invalidType_shouldThrow() {
        QuestionCreateCmd cmd = baseCreateCmd("INVALID", "A",
                List.of(option("A", "选项A"), option("B", "选项B")));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("题型不合法");
        verify(questionBankQuestionMapper, never()).insert(any());
    }

    @Test
    void create_noOptions_shouldThrow() {
        QuestionCreateCmd cmd = baseCreateCmd("SINGLE_CHOICE", "A", List.of());

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("选项数需在2~6个之间");
        verify(questionBankQuestionMapper, never()).insert(any());
    }

    @Test
    void create_tooManyOptions_shouldThrow() {
        List<QuestionCreateCmd.OptionDTO> options = List.of(
                option("A", "a"), option("B", "b"), option("C", "c"),
                option("D", "d"), option("E", "e"), option("F", "f"), option("G", "g"));
        QuestionCreateCmd cmd = baseCreateCmd("SINGLE_CHOICE", "A", options);

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("选项数需在2~6个之间");
        verify(questionBankQuestionMapper, never()).insert(any());
    }

    @Test
    void create_multipleChoiceOneAnswer_shouldThrow() {
        QuestionCreateCmd cmd = baseCreateCmd("MULTIPLE_CHOICE", "A",
                List.of(option("A", "选项A"), option("B", "选项B")));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("多选题至少2个正确答案");
        verify(questionBankQuestionMapper, never()).insert(any());
    }

    @Test
    void create_multipleChoiceAnswerNotInOptions_shouldThrow() {
        QuestionCreateCmd cmd = baseCreateCmd("MULTIPLE_CHOICE", "A,B,C",
                List.of(option("A", "选项A"), option("B", "选项B")));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("答案不在选项中");
        verify(questionBankQuestionMapper, never()).insert(any());
    }

    @Test
    void create_singleChoiceAnswerNotInOptions_shouldThrow() {
        QuestionCreateCmd cmd = baseCreateCmd("SINGLE_CHOICE", "C",
                List.of(option("A", "选项A"), option("B", "选项B")));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("答案不在选项中");
        verify(questionBankQuestionMapper, never()).insert(any());
    }

    @Test
    void create_judgeAnswerNotTrueOrFalse_shouldThrow() {
        QuestionCreateCmd cmd = baseCreateCmd("JUDGE", "YES",
                List.of(option("TRUE", "正确"), option("FALSE", "错误")));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("判断题答案必须为 TRUE 或 FALSE");
        verify(questionBankQuestionMapper, never()).insert(any());
    }

    @Test
    void create_blankStem_shouldThrow() {
        QuestionCreateCmd cmd = baseCreateCmd("SINGLE_CHOICE", "A",
                List.of(option("A", "选项A"), option("B", "选项B")));
        cmd.setStem("  ");

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("题干不能为空");
        verify(questionBankQuestionMapper, never()).insert(any());
    }

    @Test
    void update_shouldUpdateSuccessfully() {
        QuestionUpdateCmd cmd = new QuestionUpdateCmd();
        cmd.setId(1L);
        cmd.setBankId(1L);
        cmd.setQuestionType("SINGLE_CHOICE");
        cmd.setStem("新题干");
        QuestionUpdateCmd.OptionDTO optA = new QuestionUpdateCmd.OptionDTO();
        optA.setKey("A");
        optA.setLabel("选项A");
        QuestionUpdateCmd.OptionDTO optB = new QuestionUpdateCmd.OptionDTO();
        optB.setKey("B");
        optB.setLabel("选项B");
        cmd.setOptions(List.of(optA, optB));
        cmd.setAnswer("A");
        when(questionBankQuestionMapper.findById(1L)).thenReturn(Optional.of(question(1L, "DRAFT")));

        QuestionVO result = service.update(cmd);

        assertThat(result.getStem()).isEqualTo("新题干");
        verify(questionBankQuestionMapper).update(any(QuestionBankQuestion.class));
    }

    @Test
    void update_notFound_shouldThrow() {
        QuestionUpdateCmd cmd = new QuestionUpdateCmd();
        cmd.setId(999L);
        when(questionBankQuestionMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("题目不存在");
        verify(questionBankQuestionMapper, never()).update(any());
    }

    @Test
    void publish_draftToPublished_shouldUpdateStatus() {
        when(questionBankQuestionMapper.findById(1L)).thenReturn(Optional.of(question(1L, "DRAFT")));

        service.publish(1L);

        verify(questionBankQuestionMapper).updateStatus(1L, "PUBLISHED");
    }

    @Test
    void publish_alreadyPublished_shouldThrow() {
        when(questionBankQuestionMapper.findById(1L)).thenReturn(Optional.of(question(1L, "PUBLISHED")));

        assertThatThrownBy(() -> service.publish(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅草稿状态可发布");
        verify(questionBankQuestionMapper, never()).updateStatus(anyLong(), any());
    }

    @Test
    void offline_publishedToOffline_shouldUpdateStatus() {
        when(questionBankQuestionMapper.findById(1L)).thenReturn(Optional.of(question(1L, "PUBLISHED")));

        service.offline(1L);

        verify(questionBankQuestionMapper).updateStatus(1L, "OFFLINE");
    }

    @Test
    void offline_draft_shouldThrow() {
        when(questionBankQuestionMapper.findById(1L)).thenReturn(Optional.of(question(1L, "DRAFT")));

        assertThatThrownBy(() -> service.offline(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅已发布状态可下线");
        verify(questionBankQuestionMapper, never()).updateStatus(anyLong(), any());
    }

    @Test
    void delete_draft_shouldDelete() {
        when(questionBankQuestionMapper.findById(1L)).thenReturn(Optional.of(question(1L, "DRAFT")));

        service.delete(1L);

        verify(questionBankQuestionMapper).deleteById(1L);
    }

    @Test
    void delete_offline_shouldDelete() {
        when(questionBankQuestionMapper.findById(1L)).thenReturn(Optional.of(question(1L, "OFFLINE")));

        service.delete(1L);

        verify(questionBankQuestionMapper).deleteById(1L);
    }

    @Test
    void delete_published_shouldThrow() {
        when(questionBankQuestionMapper.findById(1L)).thenReturn(Optional.of(question(1L, "PUBLISHED")));

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅草稿或已下线");
        verify(questionBankQuestionMapper, never()).deleteById(anyLong());
    }
}
