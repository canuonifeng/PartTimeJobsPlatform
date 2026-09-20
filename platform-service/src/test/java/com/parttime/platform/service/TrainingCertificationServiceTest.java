package com.parttime.platform.service;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.TrainingCertificationMapper;
import com.parttime.platform.pojo.cmd.TrainingCertificationCmd;
import com.parttime.platform.pojo.entity.TrainingCertification;
import com.parttime.platform.pojo.vo.TrainingCertificationVO;
import com.parttime.platform.service.impl.TrainingCertificationServiceImpl;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingCertificationServiceTest {

    @Mock
    private TrainingCertificationMapper trainingCertificationMapper;

    @InjectMocks
    private TrainingCertificationServiceImpl service;

    @Test
    void list_shouldReturnAllCertifications() {
        TrainingCertification cert = new TrainingCertification();
        cert.setId(1L);
        cert.setName("数据标注技能认证");
        cert.setCode("ANNOTATION_BASIC");
        cert.setTaskType("ANNOTATION");
        cert.setStatus("ACTIVE");
        when(trainingCertificationMapper.findAll()).thenReturn(List.of(cert));

        List<TrainingCertificationVO> result = service.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("数据标注技能认证");
        assertThat(result.get(0).getTaskType()).isEqualTo("ANNOTATION");
    }

    @Test
    void create_duplicateCode_shouldThrow() {
        TrainingCertificationCmd cmd = new TrainingCertificationCmd();
        cmd.setName("认证");
        cmd.setCode("ANNOTATION_BASIC");
        cmd.setTaskType("ANNOTATION");
        when(trainingCertificationMapper.findByCode("ANNOTATION_BASIC"))
                .thenReturn(Optional.of(new TrainingCertification()));

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("认证编码已存在");
    }

    @Test
    void create_shouldPersistActiveCertification() {
        TrainingCertificationCmd cmd = new TrainingCertificationCmd();
        cmd.setName("数据标注技能认证");
        cmd.setCode("ANNOTATION_BASIC");
        cmd.setTaskType("ANNOTATION");
        cmd.setValidDays(30);
        when(trainingCertificationMapper.findByCode(any())).thenReturn(Optional.empty());

        TrainingCertificationVO result = service.create(cmd);

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getValidDays()).isEqualTo(30);
        verify(trainingCertificationMapper).insert(any(TrainingCertification.class));
    }

    @Test
    void toggle_shouldDisableCertification() {
        TrainingCertification cert = new TrainingCertification();
        cert.setId(1L);
        cert.setStatus("ACTIVE");
        when(trainingCertificationMapper.findById(1L)).thenReturn(Optional.of(cert));

        service.toggle(1L, "DISABLED");

        verify(trainingCertificationMapper).updateStatus(1L, "DISABLED");
    }
}
