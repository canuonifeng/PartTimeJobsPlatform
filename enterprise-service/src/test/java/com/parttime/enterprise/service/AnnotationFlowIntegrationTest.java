package com.parttime.enterprise.service;

import com.parttime.enterprise.enums.JobRateType;
import com.parttime.enterprise.enums.PricingMode;
import com.parttime.enterprise.enums.TaskType;
import com.parttime.enterprise.mapper.AnnotationTaskOrderMapper;
import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.BalanceTransactionMapper;
import com.parttime.enterprise.mapper.CompanyWorkerMapper;
import com.parttime.enterprise.mapper.ExternalWorkerMappingMapper;
import com.parttime.enterprise.mapper.JobCategoryMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.JobRateMapper;
import com.parttime.enterprise.mapper.JobScheduleMapper;
import com.parttime.enterprise.mapper.JobTagMapper;
import com.parttime.enterprise.mapper.JobTagRelationMapper;
import com.parttime.enterprise.mapper.ScheduleApplicationMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerBalanceMapper;
import com.parttime.enterprise.mapper.WorkerNotificationMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.cmd.AnnotationSubmitCmd;
import com.parttime.enterprise.pojo.cmd.JobCreateCmd;
import com.parttime.enterprise.pojo.cmd.JobRateCmd;
import com.parttime.enterprise.pojo.cmd.QualityCheckCmd;
import com.parttime.enterprise.pojo.entity.AnnotationTaskOrder;
import com.parttime.enterprise.pojo.entity.ExternalWorkerMapping;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.pojo.entity.ScheduleApplication;
import com.parttime.enterprise.pojo.vo.JobVO;
import com.parttime.enterprise.service.impl.AnnotationSettlementServiceImpl;
import com.parttime.enterprise.service.impl.ApplicationServiceImpl;
import com.parttime.enterprise.service.impl.ExternalCallbackServiceImpl;
import com.parttime.enterprise.service.impl.JobServiceImpl;
import com.parttime.enterprise.service.impl.ScheduleServiceImpl;
import com.parttime.enterprise.service.impl.TaskOrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 标注任务全流程编排测试：
 * 发布标注任务包 -> 创建批次(带 totalItems/externalBatchId) -> 报名审核生成任务单(不生成排班)
 * -> 外部提交回调 -> 质检通过 -> 自动结算
 */
@ExtendWith(MockitoExtension.class)
class AnnotationFlowIntegrationTest {

    @Mock private JobMapper jobMapper;
    @Mock private JobRateMapper jobRateMapper;
    @Mock private JobScheduleMapper jobScheduleMapper;
    @Mock private ScheduleApplicationMapper applicationMapper;
    @Mock private ScheduleShiftMapper shiftMapper;
    @Mock private JobTagRelationMapper jobTagRelationMapper;
    @Mock private JobTagMapper jobTagMapper;
    @Mock private JobCategoryMapper jobCategoryMapper;
    @Mock private CompanyWorkerMapper companyWorkerMapper;
    @Mock private WorkerSyncMapper workerSyncMapper;
    @Mock private WorkerNotificationMapper workerNotificationMapper;
    @Mock private AttendanceRecordMapper attendanceRecordMapper;
    @Mock private AnnotationTaskOrderMapper taskOrderMapper;
    @Mock private ExternalWorkerMappingMapper externalWorkerMappingMapper;
    @Mock private WorkerBalanceMapper workerBalanceMapper;
    @Mock private BalanceTransactionMapper balanceTransactionMapper;

    @InjectMocks private JobServiceImpl jobService;
    @InjectMocks private ScheduleServiceImpl scheduleService;
    @InjectMocks private TaskOrderServiceImpl taskOrderService;
    @InjectMocks private AnnotationSettlementServiceImpl annotationSettlementService;
    @InjectMocks private ExternalCallbackServiceImpl externalCallbackService;
    @InjectMocks private ApplicationServiceImpl applicationService;

    @org.junit.jupiter.api.BeforeEach
    void wireServices() {
        ReflectionTestUtils.setField(applicationService, "taskOrderService", taskOrderService);
        ReflectionTestUtils.setField(externalCallbackService, "annotationSettlementService", annotationSettlementService);
    }

    private JobSchedule schedule(Long id) {
        JobSchedule s = new JobSchedule();
        s.setId(id);
        s.setJobId(1L);
        s.setSlotsAvailable(5);
        return s;
    }

    private ExternalWorkerMapping mapping() {
        ExternalWorkerMapping m = new ExternalWorkerMapping();
        m.setId(1L);
        m.setWorkerId(200L);
        return m;
    }

    @Test
    void annotationFullFlow_publishBatchReviewCallbackQualityCheckSettle() {
        // ---- 1. 发布标注任务包 ----
        JobCreateCmd createCmd = new JobCreateCmd();
        createCmd.setCompanyId(1L);
        createCmd.setTitle("图片分类标注");
        createCmd.setCategoryId(10L);
        createCmd.setTaskType(TaskType.ANNOTATION);
        createCmd.setPricingMode(PricingMode.PER_ITEM);
        createCmd.setPricePerUnit(new BigDecimal("0.50"));
        createCmd.setTotalItems(1000);
        createCmd.setExternalTaskId("EXT_TASK_001");
        createCmd.setExternalSystemType("EXT_SYS");
        JobRateCmd rate = new JobRateCmd();
        rate.setType(JobRateType.HOURLY);
        rate.setAmount(new BigDecimal("30.00"));
        createCmd.setRates(List.of(rate));

        doAnswer(inv -> {
            Job j = inv.getArgument(0);
            j.setId(1L);
            j.setStatus("DRAFT");
            return 1;
        }).when(jobMapper).insert(any(Job.class));

        JobVO jobVO = jobService.createJob(createCmd);
        assertThat(jobVO.getTaskType()).isEqualTo(TaskType.ANNOTATION);
        assertThat(jobVO.getExternalTaskId()).isEqualTo("EXT_TASK_001");

        // 发布
        Job draft = new Job();
        draft.setId(1L);
        draft.setCompanyId(1L);
        draft.setTitle("图片分类标注");
        draft.setStatus("DRAFT");
        draft.setTaskType("ANNOTATION");
        draft.setPricingMode("PER_ITEM");
        draft.setPricePerUnit(new BigDecimal("0.50"));
        draft.setTotalItems(1000);
        when(jobMapper.findById(1L)).thenReturn(Optional.of(draft));
        jobService.publishJob(1L);
        verify(jobMapper).updateStatus(1L, "PUBLISHED");

        // ---- 2. 创建批次（标注批次字段落库） ----
        com.parttime.enterprise.pojo.cmd.ScheduleBatchCreateCmd batchCmd = new com.parttime.enterprise.pojo.cmd.ScheduleBatchCreateCmd();
        batchCmd.setJobId(1L);
        batchCmd.setStartDate(LocalDate.of(2026, 9, 21));
        batchCmd.setEndDate(LocalDate.of(2026, 9, 21));
        batchCmd.setWeekdays(List.of(1));
        batchCmd.setStartTime(LocalTime.of(9, 0));
        batchCmd.setEndTime(LocalTime.of(18, 0));
        batchCmd.setTotalItems(300);
        batchCmd.setExternalBatchId("BATCH_EXT_001");

        Job published = new Job();
        published.setId(1L);
        published.setCompanyId(1L);
        published.setTitle("图片分类标注");
        published.setTaskType("ANNOTATION");
        published.setPricingMode("PER_ITEM");
        published.setPricePerUnit(new BigDecimal("0.50"));
        published.setHeadcount(5);
        published.setContactName("王经理");
        published.setContactPhone("13800000000");
        when(jobMapper.findById(1L)).thenReturn(Optional.of(published));
        when(jobScheduleMapper.findById(anyLong())).thenReturn(Optional.of(schedule(100L)));
        com.parttime.enterprise.pojo.vo.ScheduleManagementVO managed = new com.parttime.enterprise.pojo.vo.ScheduleManagementVO();
        managed.setId(100L);
        managed.setJobId(1L);
        when(jobScheduleMapper.findManagementPage(anyLong(), anyLong(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(managed));
        doAnswer(inv -> {
            JobSchedule s = inv.getArgument(0);
            s.setId(100L);
            return 1;
        }).when(jobScheduleMapper).insert(any(JobSchedule.class));
        scheduleService.batchCreateManagedSchedules(batchCmd);
        ArgumentCaptor<JobSchedule> scheduleCaptor = ArgumentCaptor.forClass(JobSchedule.class);
        verify(jobScheduleMapper).insert(scheduleCaptor.capture());
        assertThat(scheduleCaptor.getValue().getTotalItems()).isEqualTo(300);
        assertThat(scheduleCaptor.getValue().getExternalBatchId()).isEqualTo("BATCH_EXT_001");

        // ---- 3. 报名审核生成任务单（标注不生成排班） ----
        ScheduleApplication app = new ScheduleApplication();
        app.setId(50L);
        app.setWorkerId(200L);
        app.setScheduleId(100L);
        app.setStatus("PENDING");
        when(applicationMapper.findById(50L)).thenReturn(Optional.of(app));
        when(jobScheduleMapper.findById(100L)).thenReturn(Optional.of(schedule(100L)));
        when(jobMapper.findById(1L)).thenReturn(Optional.of(published));
        when(applicationMapper.countByScheduleIdAndStatus(100L, "ACCEPTED")).thenReturn(0);
        when(applicationMapper.findById(50L)).thenReturn(Optional.of(app));

        // TaskOrderService.createByApplication 内部
        when(taskOrderMapper.selectByApplicationId(50L)).thenReturn(null);
        doAnswer(inv -> {
            AnnotationTaskOrder o = inv.getArgument(0);
            o.setId(300L);
            return 1;
        }).when(taskOrderMapper).insert(any(AnnotationTaskOrder.class));
        AnnotationTaskOrder created = new AnnotationTaskOrder();
        created.setId(300L);
        when(taskOrderMapper.selectById(anyLong())).thenReturn(created);

        applicationService.acceptApplication(50L);

        verify(shiftMapper, never()).insert(any());
        ArgumentCaptor<AnnotationTaskOrder> orderCaptor = ArgumentCaptor.forClass(AnnotationTaskOrder.class);
        verify(taskOrderMapper).insert(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getJobId()).isEqualTo(1L);
        assertThat(orderCaptor.getValue().getWorkerId()).isEqualTo(200L);

        // ---- 4. 外部提交回调 ----
        AnnotationSubmitCmd submitCmd = new AnnotationSubmitCmd();
        submitCmd.setExternalTaskId("EXT_TASK_001");
        submitCmd.setExternalBatchId("BATCH_EXT_001");
        submitCmd.setExternalWorkerId("EXT_WORKER_1");
        submitCmd.setExternalSubmissionId("SUB_001");
        submitCmd.setItemsCompleted(80);
        when(jobMapper.findByExternalTaskId("EXT_TASK_001")).thenReturn(published);
        when(jobScheduleMapper.findByExternalBatchId("BATCH_EXT_001")).thenReturn(schedule(100L));
        when(externalWorkerMappingMapper.selectByExternalId(null, "EXT_WORKER_1"))
                .thenReturn(mapping());
        when(taskOrderMapper.selectByJobIdAndScheduleIdAndWorkerId(1L, 100L, 200L)).thenReturn(null);
        when(applicationMapper.findByScheduleIdAndWorkerId(100L, 200L)).thenReturn(app);
        when(taskOrderMapper.selectByApplicationId(50L)).thenReturn(null);
        externalCallbackService.handleAnnotationSubmit(submitCmd, "key");

        // ---- 5. 质检通过 -> 自动结算 ----
        AnnotationTaskOrder order = new AnnotationTaskOrder();
        order.setId(300L);
        order.setJobId(1L);
        order.setJobScheduleId(100L);
        order.setWorkerId(200L);
        order.setCompletedItems(80);
        order.setStatus("SUBMITTED");
        when(jobMapper.findByExternalTaskId("EXT_TASK_001")).thenReturn(published);
        when(jobScheduleMapper.findByExternalBatchId("BATCH_EXT_001")).thenReturn(schedule(100L));
        when(externalWorkerMappingMapper.selectByExternalId(null, "EXT_WORKER_1"))
                .thenReturn(mapping());
        when(taskOrderMapper.selectByJobIdAndScheduleIdAndWorkerId(1L, 100L, 200L)).thenReturn(order);
        when(workerBalanceMapper.findByWorkerId(200L)).thenReturn(null);

        QualityCheckCmd checkCmd = new QualityCheckCmd();
        checkCmd.setExternalTaskId("EXT_TASK_001");
        checkCmd.setExternalBatchId("BATCH_EXT_001");
        checkCmd.setExternalWorkerId("EXT_WORKER_1");
        checkCmd.setPassed(true);
        checkCmd.setItemsCompleted(80);
        externalCallbackService.handleQualityCheck(checkCmd, "key");

        // 完成量先更新再结算：80 件 × 0.50 = 40.00
        assertThat(order.getStatus()).isEqualTo("COMPLETED");
        assertThat(order.getCompletedItems()).isEqualTo(80);
        verify(workerBalanceMapper).upsert(200L, new BigDecimal("40.00"), new BigDecimal("40.00"), BigDecimal.ZERO);
    }
}
