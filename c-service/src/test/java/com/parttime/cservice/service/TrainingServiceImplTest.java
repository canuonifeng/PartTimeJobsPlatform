package com.parttime.cservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.cservice.mapper.TrainingCertificationMapper;
import com.parttime.cservice.mapper.TrainingCourseMapper;
import com.parttime.cservice.mapper.WorkerCertificationMapper;
import com.parttime.cservice.mapper.WorkerTrainingRecordMapper;
import com.parttime.cservice.pojo.cmd.ExamSubmitCmd;
import com.parttime.cservice.pojo.entity.TrainingCertification;
import com.parttime.cservice.pojo.entity.TrainingCourse;
import com.parttime.cservice.pojo.entity.WorkerCertification;
import com.parttime.cservice.pojo.entity.WorkerTrainingRecord;
import com.parttime.cservice.pojo.vo.ExamResultVO;
import com.parttime.cservice.pojo.vo.TrainingCourseDetailVO;
import com.parttime.cservice.pojo.vo.TrainingCourseVO;
import com.parttime.cservice.pojo.vo.WorkerCertificationVO;
import com.parttime.cservice.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
class TrainingServiceImplTest {

    @Mock
    private TrainingCourseMapper trainingCourseMapper;

    @Mock
    private TrainingCertificationMapper trainingCertificationMapper;

    @Mock
    private WorkerTrainingRecordMapper workerTrainingRecordMapper;

    @Mock
    private WorkerCertificationMapper workerCertificationMapper;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private TrainingCourse course() {
        TrainingCourse course = new TrainingCourse();
        course.setId(1L);
        course.setCertificationId(1L);
        course.setTitle("数据标注入门");
        course.setExamJson("[{\"question\":\"标注问题\",\"options\":[\"A\",\"B\"],\"answer\":0},"
                + "{\"question\":\"第二题\",\"options\":[\"A\",\"B\"],\"answer\":1}]");
        course.setPassScore(60);
        return course;
    }

    private TrainingCertification certification() {
        TrainingCertification cert = new TrainingCertification();
        cert.setId(1L);
        cert.setName("数据标注技能认证");
        cert.setCode("ANNOTATION_BASIC");
        cert.setTaskType("ANNOTATION");
        cert.setStatus("ACTIVE");
        cert.setValidDays(30);
        return cert;
    }

    @BeforeEach
    void setUp() throws Exception {
        java.lang.reflect.Field field = TrainingServiceImpl.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(trainingService, objectMapper);
    }

    @Test
    void listCourses_shouldReturnCoursesWithMyStatus() {
        when(trainingCourseMapper.findPublished()).thenReturn(List.of(course()));
        when(trainingCertificationMapper.findByIds(List.of(1L))).thenReturn(List.of(certification()));
        when(workerTrainingRecordMapper.findByWorkerId(100L)).thenReturn(List.of());
        when(workerCertificationMapper.findByWorkerId(100L)).thenReturn(List.of());

        List<TrainingCourseVO> result = trainingService.listCourses(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMyStatus()).isEqualTo("NOT_STARTED");
        assertThat(result.get(0).getCertificationName()).isEqualTo("数据标注技能认证");
        assertThat(result.get(0).getCertified()).isFalse();
    }

    @Test
    void listCourses_certifiedWhenWorkerHoldsActiveCert() {
        WorkerCertification wc = new WorkerCertification();
        wc.setWorkerId(100L);
        wc.setCertificationId(1L);
        wc.setStatus("ACTIVE");
        wc.setExpiresAt(LocalDateTime.now().plusDays(10));
        when(trainingCourseMapper.findPublished()).thenReturn(List.of(course()));
        when(trainingCertificationMapper.findByIds(List.of(1L))).thenReturn(List.of(certification()));
        when(workerTrainingRecordMapper.findByWorkerId(100L)).thenReturn(List.of());
        when(workerCertificationMapper.findByWorkerId(100L)).thenReturn(List.of(wc));

        List<TrainingCourseVO> result = trainingService.listCourses(100L);

        assertThat(result.get(0).getCertified()).isTrue();
    }

    @Test
    void getCourseDetail_shouldStripAnswersFromQuestions() {
        when(trainingCourseMapper.findPublishedById(1L)).thenReturn(Optional.of(course()));
        when(trainingCertificationMapper.findById(1L)).thenReturn(Optional.of(certification()));
        when(workerTrainingRecordMapper.findByWorkerAndCourse(100L, 1L)).thenReturn(Optional.empty());
        when(workerCertificationMapper.findByWorkerAndCert(100L, 1L)).thenReturn(Optional.empty());

        TrainingCourseDetailVO result = trainingService.getCourseDetail(100L, 1L);

        assertThat(result.getQuestions()).hasSize(2);
        assertThat(result.getQuestions().get(0).getQuestion()).isEqualTo("标注问题");
        assertThat(result.getQuestions().get(0).getOptions()).containsExactly("A", "B");
        // 答案字段不能暴露给前端（ExamQuestionVO 无 answer 属性）
        assertThat(result.getQuestions().get(0).getClass().getDeclaredFields())
                .extracting(java.lang.reflect.Field::getName)
                .doesNotContain("answer", "correctAnswer");
    }

    @Test
    void startCourse_shouldInsertInProgressRecordWhenNoneExists() {
        when(trainingCourseMapper.findPublishedById(1L)).thenReturn(Optional.of(course()));
        when(workerTrainingRecordMapper.findByWorkerAndCourse(100L, 1L)).thenReturn(Optional.empty());

        trainingService.startCourse(100L, 1L);

        verify(workerTrainingRecordMapper).insert(any(WorkerTrainingRecord.class));
    }

    @Test
    void startCourse_shouldSkipWhenRecordExists() {
        when(trainingCourseMapper.findPublishedById(1L)).thenReturn(Optional.of(course()));
        WorkerTrainingRecord existing = new WorkerTrainingRecord();
        existing.setId(9L);
        existing.setStatus("IN_PROGRESS");
        when(workerTrainingRecordMapper.findByWorkerAndCourse(100L, 1L)).thenReturn(Optional.of(existing));

        trainingService.startCourse(100L, 1L);

        verify(workerTrainingRecordMapper, never()).insert(any());
    }

    @Test
    void submitExam_passed_shouldMarkCompletedAndGrantCertification() {
        TrainingCourse c = course();
        when(trainingCourseMapper.findPublishedById(1L)).thenReturn(Optional.of(c));
        WorkerTrainingRecord record = new WorkerTrainingRecord();
        record.setId(9L);
        record.setWorkerId(100L);
        record.setCourseId(1L);
        record.setStatus("IN_PROGRESS");
        when(workerTrainingRecordMapper.findByWorkerAndCourse(100L, 1L)).thenReturn(Optional.of(record));
        when(trainingCertificationMapper.findById(1L)).thenReturn(Optional.of(certification()));
        when(workerCertificationMapper.findByWorkerAndCert(100L, 1L)).thenReturn(Optional.empty());

        ExamSubmitCmd cmd = new ExamSubmitCmd();
        cmd.setAnswers(List.of(0, 1));
        ExamResultVO result = trainingService.submitExam(100L, 1L, cmd);

        assertThat(result.getPassed()).isTrue();
        assertThat(result.getScore()).isEqualTo(100);
        assertThat(record.getStatus()).isEqualTo("COMPLETED");
        verify(workerTrainingRecordMapper).update(record);
        verify(workerCertificationMapper).insert(any(WorkerCertification.class));
    }

    @Test
    void submitExam_failed_shouldNotGrantCertification() {
        TrainingCourse c = course();
        when(trainingCourseMapper.findPublishedById(1L)).thenReturn(Optional.of(c));
        WorkerTrainingRecord record = new WorkerTrainingRecord();
        record.setId(9L);
        record.setStatus("IN_PROGRESS");
        when(workerTrainingRecordMapper.findByWorkerAndCourse(100L, 1L)).thenReturn(Optional.of(record));

        ExamSubmitCmd cmd = new ExamSubmitCmd();
        cmd.setAnswers(List.of(1, 0));
        ExamResultVO result = trainingService.submitExam(100L, 1L, cmd);

        assertThat(result.getPassed()).isFalse();
        assertThat(result.getScore()).isEqualTo(0);
        assertThat(record.getStatus()).isEqualTo("FAILED");
        verify(workerCertificationMapper, never()).insert(any());
        verify(workerCertificationMapper, never()).update(any());
    }

    @Test
    void submitExam_unknownCourse_shouldThrow() {
        when(trainingCourseMapper.findPublishedById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.submitExam(100L, 999L, new ExamSubmitCmd()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("课程不存在");
    }

    @Test
    void getMyCertifications_shouldMarkExpiredWhenPastExpiry() {
        WorkerCertification wc = new WorkerCertification();
        wc.setWorkerId(100L);
        wc.setCertificationId(1L);
        wc.setStatus("ACTIVE");
        wc.setGrantedAt(LocalDateTime.now().minusDays(40));
        wc.setExpiresAt(LocalDateTime.now().minusDays(10));
        when(workerCertificationMapper.findByWorkerId(100L)).thenReturn(List.of(wc));
        when(trainingCertificationMapper.findByIds(List.of(1L))).thenReturn(List.of(certification()));

        List<WorkerCertificationVO> result = trainingService.getMyCertifications(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("EXPIRED");
        assertThat(result.get(0).getName()).isEqualTo("数据标注技能认证");
        assertThat(result.get(0).getTaskType()).isEqualTo("ANNOTATION");
    }
}
