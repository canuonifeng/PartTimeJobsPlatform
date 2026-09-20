package com.parttime.cservice.service;

import com.parttime.cservice.mapper.AnnotationTaskOrderMapper;
import com.parttime.cservice.mapper.CompanyWorkerInsertMapper;
import com.parttime.cservice.mapper.JobMapper;
import com.parttime.cservice.mapper.JobScheduleMapper;
import com.parttime.cservice.mapper.ScheduleApplicationMapper;
import com.parttime.cservice.pojo.cmd.GrabTaskOrderCmd;
import com.parttime.cservice.pojo.entity.Job;
import com.parttime.cservice.pojo.entity.JobSchedule;
import com.parttime.cservice.pojo.entity.ScheduleApplication;
import com.parttime.cservice.service.impl.TaskOrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskOrderServiceImplTest {

    @Mock
    private AnnotationTaskOrderMapper annotationTaskOrderMapper;

    @Mock
    private JobMapper jobMapper;

    @Mock
    private JobScheduleMapper jobScheduleMapper;

    @Mock
    private ScheduleApplicationMapper scheduleApplicationMapper;

    @Mock
    private CompanyWorkerInsertMapper companyWorkerInsertMapper;

    @Mock
    private CertificationGateService certificationGateService;

    @InjectMocks
    private TaskOrderServiceImpl taskOrderService;

    private Job annotationJob() {
        Job job = new Job();
        job.setId(1L);
        job.setCompanyId(88L);
        job.setTaskType("ANNOTATION");
        return job;
    }

    private JobSchedule activeFutureSchedule(Long id, Long jobId) {
        JobSchedule schedule = new JobSchedule();
        schedule.setId(id);
        schedule.setJobId(jobId);
        schedule.setScheduleDate(LocalDate.now().plusDays(1));
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));
        schedule.setSlotsAvailable(10);
        schedule.setStatus("ACTIVE");
        return schedule;
    }

    @Test
    void grabTaskOrder_withoutCertification_shouldReject() {
        when(jobMapper.findByJobId(1L)).thenReturn(Optional.of(annotationJob()));
        doThrow(new RuntimeException("需要先完成培训并通过技能认证，才能抢该类任务"))
                .when(certificationGateService).checkCertification(100L, "ANNOTATION");

        GrabTaskOrderCmd cmd = new GrabTaskOrderCmd();
        cmd.setJobId(1L);

        assertThatThrownBy(() -> taskOrderService.grabTaskOrder(100L, cmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("需要先完成培训并通过技能认证");
        verify(scheduleApplicationMapper, never()).batchInsert(anyList());
    }

    @Test
    void grabTaskOrder_missingJob_shouldReject() {
        when(jobMapper.findByJobId(999L)).thenReturn(Optional.empty());

        GrabTaskOrderCmd cmd = new GrabTaskOrderCmd();
        cmd.setJobId(999L);

        assertThatThrownBy(() -> taskOrderService.grabTaskOrder(100L, cmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("任务不存在");
    }

    @Test
    void grabTaskOrder_nonAnnotationJob_shouldReject() {
        Job job = new Job();
        job.setId(2L);
        job.setTaskType("WORK");
        when(jobMapper.findByJobId(2L)).thenReturn(Optional.of(job));

        GrabTaskOrderCmd cmd = new GrabTaskOrderCmd();
        cmd.setJobId(2L);

        assertThatThrownBy(() -> taskOrderService.grabTaskOrder(100L, cmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("不支持抢单");
    }

    @Test
    void grabTaskOrder_afterDeadline_shouldReject() {
        Job job = annotationJob();
        job.setDeadline(LocalDateTime.now().minusHours(1));
        when(jobMapper.findByJobId(1L)).thenReturn(Optional.of(job));

        GrabTaskOrderCmd cmd = new GrabTaskOrderCmd();
        cmd.setJobId(1L);

        assertThatThrownBy(() -> taskOrderService.grabTaskOrder(100L, cmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("报名已截止");
    }

    @Test
    void grabTaskOrder_shouldInsertPendingApplicationsAndLinkWorker() {
        when(jobMapper.findByJobId(1L)).thenReturn(Optional.of(annotationJob()));
        JobSchedule schedule = activeFutureSchedule(10L, 1L);
        when(jobScheduleMapper.findActiveByJobId(1L)).thenReturn(List.of(schedule));
        when(scheduleApplicationMapper.findScheduleIdsByWorkerIdAndJobId(100L, 1L)).thenReturn(List.of());
        when(scheduleApplicationMapper.countAcceptedByScheduleIds(List.of(10L))).thenReturn(Map.of(10L, 1));

        GrabTaskOrderCmd cmd = new GrabTaskOrderCmd();
        cmd.setJobId(1L);
        int count = taskOrderService.grabTaskOrder(100L, cmd);

        assertThat(count).isEqualTo(1);
        ArgumentCaptor<List<ScheduleApplication>> captor = ArgumentCaptor.forClass(List.class);
        verify(scheduleApplicationMapper).batchInsert(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getStatus()).isEqualTo("PENDING");
        assertThat(captor.getValue().get(0).getWorkerId()).isEqualTo(100L);
        verify(companyWorkerInsertMapper).upsert(88L, 100L);
    }

    @Test
    void grabTaskOrder_scheduleAlreadyApplied_shouldSkipIt() {
        when(jobMapper.findByJobId(1L)).thenReturn(Optional.of(annotationJob()));
        JobSchedule schedule = activeFutureSchedule(10L, 1L);
        when(jobScheduleMapper.findActiveByJobId(1L)).thenReturn(List.of(schedule));
        when(scheduleApplicationMapper.findScheduleIdsByWorkerIdAndJobId(100L, 1L)).thenReturn(List.of(10L));

        GrabTaskOrderCmd cmd = new GrabTaskOrderCmd();
        cmd.setJobId(1L);

        assertThatThrownBy(() -> taskOrderService.grabTaskOrder(100L, cmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("已全部抢过");
    }

    @Test
    void grabTaskOrder_scheduleFull_shouldReject() {
        when(jobMapper.findByJobId(1L)).thenReturn(Optional.of(annotationJob()));
        JobSchedule schedule = activeFutureSchedule(10L, 1L);
        schedule.setSlotsAvailable(1);
        when(jobScheduleMapper.findActiveByJobId(1L)).thenReturn(List.of(schedule));
        when(scheduleApplicationMapper.findScheduleIdsByWorkerIdAndJobId(100L, 1L)).thenReturn(List.of());
        when(scheduleApplicationMapper.countAcceptedByScheduleIds(List.of(10L))).thenReturn(Map.of(10L, 1));

        GrabTaskOrderCmd cmd = new GrabTaskOrderCmd();
        cmd.setJobId(1L);

        assertThatThrownBy(() -> taskOrderService.grabTaskOrder(100L, cmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("批次已满");
        verify(scheduleApplicationMapper, never()).batchInsert(anyList());
    }
}
