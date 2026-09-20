package com.parttime.platform.service;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.TrainingCertificationMapper;
import com.parttime.platform.mapper.TrainingCourseMapper;
import com.parttime.platform.pojo.cmd.TrainingCourseCmd;
import com.parttime.platform.pojo.entity.TrainingCertification;
import com.parttime.platform.pojo.entity.TrainingCourse;
import com.parttime.platform.pojo.vo.TrainingCourseVO;
import com.parttime.platform.service.impl.TrainingCourseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingCourseServiceTest {

    @Mock
    private TrainingCourseMapper trainingCourseMapper;

    @Mock
    private TrainingCertificationMapper trainingCertificationMapper;

    @InjectMocks
    private TrainingCourseServiceImpl service;

    private TrainingCertification certification() {
        TrainingCertification cert = new TrainingCertification();
        cert.setId(1L);
        cert.setName("数据标注技能认证");
        return cert;
    }

    @Test
    void create_shouldCreateDraftCourse() {
        TrainingCourseCmd cmd = new TrainingCourseCmd();
        cmd.setCertificationId(1L);
        cmd.setTitle("数据标注入门");
        cmd.setPassScore(80);
        when(trainingCertificationMapper.findById(1L)).thenReturn(Optional.of(certification()));

        TrainingCourseVO result = service.create(cmd);

        assertThat(result.getStatus()).isEqualTo("DRAFT");
        assertThat(result.getPassScore()).isEqualTo(80);
        assertThat(result.getCertificationName()).isEqualTo("数据标注技能认证");
        verify(trainingCourseMapper).insert(any(TrainingCourse.class));
    }

    @Test
    void create_missingCertification_shouldThrow() {
        TrainingCourseCmd cmd = new TrainingCourseCmd();
        cmd.setCertificationId(999L);
        cmd.setTitle("课程");
        when(trainingCertificationMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("关联认证不存在");
    }

    @Test
    void publish_shouldSetPublishedStatus() {
        TrainingCourse course = new TrainingCourse();
        course.setId(1L);
        course.setCertificationId(1L);
        when(trainingCourseMapper.findById(1L)).thenReturn(Optional.of(course));
        when(trainingCertificationMapper.findById(1L)).thenReturn(Optional.of(certification()));

        service.publish(1L);

        verify(trainingCourseMapper).updateStatus(1L, "PUBLISHED");
    }

    @Test
    void publish_missingCertification_shouldThrow() {
        TrainingCourse course = new TrainingCourse();
        course.setId(1L);
        course.setCertificationId(999L);
        when(trainingCourseMapper.findById(1L)).thenReturn(Optional.of(course));
        when(trainingCertificationMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.publish(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("关联认证不存在");
    }

    @Test
    void delete_publishedCourse_shouldReject() {
        TrainingCourse course = new TrainingCourse();
        course.setId(1L);
        course.setStatus("PUBLISHED");
        when(trainingCourseMapper.findById(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已发布");
    }

    @Test
    void delete_draftCourse_shouldDelete() {
        TrainingCourse course = new TrainingCourse();
        course.setId(1L);
        course.setStatus("DRAFT");
        when(trainingCourseMapper.findById(1L)).thenReturn(Optional.of(course));

        service.delete(1L);

        verify(trainingCourseMapper).deleteById(anyLong());
    }
}
