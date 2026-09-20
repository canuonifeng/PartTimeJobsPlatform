package com.parttime.enterprise.service;

import com.parttime.enterprise.enums.AnnotationTaskOrderStatus;
import com.parttime.enterprise.mapper.AnnotationTaskOrderMapper;
import com.parttime.enterprise.mapper.ExternalWorkerMappingMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.JobScheduleMapper;
import com.parttime.enterprise.mapper.ScheduleApplicationMapper;
import com.parttime.enterprise.pojo.cmd.AnnotationSubmitCmd;
import com.parttime.enterprise.pojo.cmd.ProgressCmd;
import com.parttime.enterprise.pojo.cmd.QualityCheckCmd;
import com.parttime.enterprise.pojo.entity.AnnotationTaskOrder;
import com.parttime.enterprise.pojo.entity.ExternalWorkerMapping;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.pojo.entity.ScheduleApplication;
import com.parttime.enterprise.service.impl.ExternalCallbackServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalCallbackServiceTest {

    @Mock
    private JobMapper jobMapper;
    @Mock
    private JobScheduleMapper jobScheduleMapper;
    @Mock
    private ExternalWorkerMappingMapper externalWorkerMappingMapper;
    @Mock
    private AnnotationTaskOrderMapper taskOrderMapper;
    @Mock
    private ScheduleApplicationMapper applicationMapper;
    @Mock
    private AnnotationSettlementService annotationSettlementService;

    @InjectMocks
    private ExternalCallbackServiceImpl callbackService;

    private Job annotationJob() {
        Job job = new Job();
        job.setId(1L);
        job.setTitle("标注任务包");
        job.setTaskType("ANNOTATION");
        job.setPricingMode("PER_ITEM");
        job.setPricePerUnit(new BigDecimal("0.50"));
        job.setTotalItems(100);
        return job;
    }

    private JobSchedule schedule() {
        JobSchedule s = new JobSchedule();
        s.setId(10L);
        s.setJobId(1L);
        return s;
    }

    private ExternalWorkerMapping mapping() {
        ExternalWorkerMapping m = new ExternalWorkerMapping();
        m.setId(1L);
        m.setWorkerId(100L);
        m.setExternalWorkerId("EXT_WORKER_1");
        return m;
    }

    private AnnotationTaskOrder order() {
        AnnotationTaskOrder o = new AnnotationTaskOrder();
        o.setId(1L);
        o.setWorkerId(100L);
        o.setJobId(1L);
        o.setApplicationId(1L);
        o.setStatus("PENDING");
        o.setCompletedItems(0);
        return o;
    }

    @Test
    void handleAnnotationSubmit_newSubmission_shouldInsertSubmittedOrder() {
        when(jobMapper.findByExternalTaskId("TASK_1")).thenReturn(annotationJob());
        when(jobScheduleMapper.findByExternalBatchId("BATCH_1")).thenReturn(schedule());
        when(externalWorkerMappingMapper.selectByExternalId(null, "EXT_WORKER_1")).thenReturn(mapping());
        when(taskOrderMapper.selectByJobIdAndScheduleIdAndWorkerId(1L, 10L, 100L)).thenReturn(null);
        ScheduleApplication app = new ScheduleApplication();
        app.setId(1L);
        when(applicationMapper.findByScheduleIdAndWorkerId(10L, 100L)).thenReturn(app);
        when(taskOrderMapper.selectByApplicationId(1L)).thenReturn(null);

        AnnotationSubmitCmd cmd = new AnnotationSubmitCmd();
        cmd.setExternalTaskId("TASK_1");
        cmd.setExternalBatchId("BATCH_1");
        cmd.setExternalWorkerId("EXT_WORKER_1");
        cmd.setExternalSubmissionId("SUB_1");
        cmd.setItemsCompleted(80);
        callbackService.handleAnnotationSubmit(cmd, "key");

        verify(taskOrderMapper).insert(any(AnnotationTaskOrder.class));
    }

    @Test
    void handleAnnotationSubmit_duplicateSubmissionId_shouldBeIdempotent() {
        when(jobMapper.findByExternalTaskId("TASK_1")).thenReturn(annotationJob());
        when(jobScheduleMapper.findByExternalBatchId("BATCH_1")).thenReturn(schedule());
        when(externalWorkerMappingMapper.selectByExternalId(null, "EXT_WORKER_1")).thenReturn(mapping());
        AnnotationTaskOrder existing = order();
        existing.setExternalSubmissionId("SUB_1");
        existing.setStatus("SUBMITTED");
        when(taskOrderMapper.selectByJobIdAndScheduleIdAndWorkerId(1L, 10L, 100L)).thenReturn(existing);

        AnnotationSubmitCmd cmd = new AnnotationSubmitCmd();
        cmd.setExternalTaskId("TASK_1");
        cmd.setExternalBatchId("BATCH_1");
        cmd.setExternalWorkerId("EXT_WORKER_1");
        cmd.setExternalSubmissionId("SUB_1");
        cmd.setItemsCompleted(80);
        callbackService.handleAnnotationSubmit(cmd, "key");

        verify(taskOrderMapper, never()).update(any(AnnotationTaskOrder.class));
    }

    @Test
    void handleAnnotationSubmit_wrongApiKey_shouldReject() throws Exception {
        java.lang.reflect.Field field = ExternalCallbackServiceImpl.class.getDeclaredField("callbackApiKey");
        field.setAccessible(true);
        field.set(callbackService, "secret-key");

        AnnotationSubmitCmd cmd = new AnnotationSubmitCmd();
        cmd.setExternalTaskId("TASK_1");
        cmd.setExternalBatchId("BATCH_1");
        cmd.setExternalWorkerId("EXT_WORKER_1");

        assertThatThrownBy(() -> callbackService.handleAnnotationSubmit(cmd, "wrong-key"))
                .isInstanceOf(com.parttime.enterprise.exception.BusinessException.class)
                .hasMessageContaining("回调密钥");
        verify(taskOrderMapper, never()).insert(any(AnnotationTaskOrder.class));
    }

    @Test
    void handleQualityCheck_passed_shouldCompleteAndSettle() {
        when(jobMapper.findByExternalTaskId("TASK_1")).thenReturn(annotationJob());
        when(jobScheduleMapper.findByExternalBatchId("BATCH_1")).thenReturn(schedule());
        when(externalWorkerMappingMapper.selectByExternalId(null, "EXT_WORKER_1")).thenReturn(mapping());
        AnnotationTaskOrder existing = order();
        when(taskOrderMapper.selectByJobIdAndScheduleIdAndWorkerId(1L, 10L, 100L)).thenReturn(existing);

        QualityCheckCmd cmd = new QualityCheckCmd();
        cmd.setExternalTaskId("TASK_1");
        cmd.setExternalBatchId("BATCH_1");
        cmd.setExternalWorkerId("EXT_WORKER_1");
        cmd.setPassed(true);
        cmd.setItemsCompleted(80);
        callbackService.handleQualityCheck(cmd, "key");

        assertThat(existing.getStatus()).isEqualTo("COMPLETED");
        assertThat(existing.getCompletedItems()).isEqualTo(80);
        assertThat(existing.getCompletedAt()).isNotNull();
        verify(annotationSettlementService).settleAnnotationTask(existing, annotationJob());
        verify(taskOrderMapper).update(existing);
    }

    @Test
    void handleQualityCheck_failed_shouldRejectWithoutSettling() {
        when(jobMapper.findByExternalTaskId("TASK_1")).thenReturn(annotationJob());
        when(jobScheduleMapper.findByExternalBatchId("BATCH_1")).thenReturn(schedule());
        when(externalWorkerMappingMapper.selectByExternalId(null, "EXT_WORKER_1")).thenReturn(mapping());
        AnnotationTaskOrder existing = order();
        when(taskOrderMapper.selectByJobIdAndScheduleIdAndWorkerId(1L, 10L, 100L)).thenReturn(existing);

        QualityCheckCmd cmd = new QualityCheckCmd();
        cmd.setExternalTaskId("TASK_1");
        cmd.setExternalBatchId("BATCH_1");
        cmd.setExternalWorkerId("EXT_WORKER_1");
        cmd.setPassed(false);
        callbackService.handleQualityCheck(cmd, "key");

        assertThat(existing.getStatus()).isEqualTo("REJECTED");
        verify(annotationSettlementService, never()).settleAnnotationTask(any(), any());
        verify(taskOrderMapper).update(existing);
    }

    @Test
    void handleProgress_shouldUpdateItemsAndMoveToInProgress() {
        when(jobMapper.findByExternalTaskId("TASK_1")).thenReturn(annotationJob());
        when(jobScheduleMapper.findByExternalBatchId("BATCH_1")).thenReturn(schedule());
        when(externalWorkerMappingMapper.selectByExternalId(null, "EXT_WORKER_1")).thenReturn(mapping());
        AnnotationTaskOrder existing = order();
        when(taskOrderMapper.selectByJobIdAndScheduleIdAndWorkerId(1L, 10L, 100L)).thenReturn(existing);

        ProgressCmd cmd = new ProgressCmd();
        cmd.setExternalTaskId("TASK_1");
        cmd.setExternalBatchId("BATCH_1");
        cmd.setExternalWorkerId("EXT_WORKER_1");
        cmd.setItemsCompleted(30);
        callbackService.handleProgress(cmd, "key");

        assertThat(existing.getCompletedItems()).isEqualTo(30);
        assertThat(existing.getStatus()).isEqualTo("IN_PROGRESS");
        verify(taskOrderMapper).update(existing);
    }
}
