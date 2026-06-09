package com.parttime.cservice.service;

import com.parttime.cservice.mapper.CompanyWorkerInsertMapper;
import com.parttime.cservice.mapper.JobMapper;
import com.parttime.cservice.mapper.JobScheduleMapper;
import com.parttime.cservice.mapper.ScheduleApplicationMapper;
import com.parttime.cservice.pojo.entity.Job;
import com.parttime.cservice.pojo.entity.JobSchedule;
import com.parttime.cservice.pojo.entity.ScheduleApplication;
import com.parttime.cservice.service.impl.JobServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceImplApplyForJobTest {

    @Mock
    private JobMapper jobMapper;

    @Mock
    private ScheduleApplicationMapper scheduleApplicationMapper;

    @Mock
    private JobScheduleMapper jobScheduleMapper;

    @Mock
    private CompanyWorkerInsertMapper companyWorkerInsertMapper;

    @Mock
    private com.parttime.cservice.mapper.SystemConfigMapper systemConfigMapper;

    @Mock
    private com.parttime.cservice.mapper.ShiftMapper shiftMapper;

    @Mock
    private com.parttime.cservice.mapper.NotificationMapper notificationMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    @Test
    void applyForJob_shouldSkipCompanyWorkerLinkWhenCompanyIdMissing() {
        Job job = new Job();
        job.setId(1L);
        job.setJobId(1L);
        job.setCompanyId(null);

        JobSchedule schedule = activeFutureSchedule(10L, 1L);
        when(scheduleApplicationMapper.findScheduleIdsByWorkerIdAndJobId(100L, 1L)).thenReturn(List.of());
        when(jobMapper.findByJobId(1L)).thenReturn(Optional.of(job));
        when(jobScheduleMapper.findByIds(List.of(10L))).thenReturn(List.of(schedule));

        boolean result = jobService.applyForJob(100L, 1L, List.of(10L));

        assertThat(result).isTrue();
        verify(scheduleApplicationMapper).insert(any(ScheduleApplication.class));
        verify(companyWorkerInsertMapper, never()).upsert(anyLong(), anyLong());
    }

    @Test
    void applyForJob_shouldPersistApplicationWithoutCompanyId() {
        Job job = new Job();
        job.setId(1L);
        job.setJobId(1L);
        job.setCompanyId(88L);

        JobSchedule schedule = activeFutureSchedule(10L, 1L);
        when(scheduleApplicationMapper.findScheduleIdsByWorkerIdAndJobId(100L, 1L)).thenReturn(List.of());
        when(jobMapper.findByJobId(1L)).thenReturn(Optional.of(job));
        when(jobScheduleMapper.findByIds(List.of(10L))).thenReturn(List.of(schedule));
        doAnswer(invocation -> 1).when(scheduleApplicationMapper).insert(any(ScheduleApplication.class));

        boolean result = jobService.applyForJob(100L, 1L, List.of(10L));

        assertThat(result).isTrue();
        verify(companyWorkerInsertMapper).upsert(88L, 100L);
    }

    @Test
    void applyForJob_autoApproveEnabled_shouldCreateAcceptedApplicationAndShift() {
        Job job = new Job();
        job.setId(1L);
        job.setJobId(1L);
        job.setCompanyId(88L);
        job.setAutoApprove(true);

        JobSchedule schedule = activeFutureSchedule(10L, 1L);
        when(scheduleApplicationMapper.findScheduleIdsByWorkerIdAndJobId(100L, 1L)).thenReturn(List.of());
        when(jobMapper.findByJobId(1L)).thenReturn(Optional.of(job));
        when(jobScheduleMapper.findByIds(List.of(10L))).thenReturn(List.of(schedule));
        doAnswer(invocation -> {
            com.parttime.cservice.pojo.entity.ScheduleApplication sa = invocation.getArgument(0);
            sa.setId(1L);
            return 1;
        }).when(scheduleApplicationMapper).insert(any(com.parttime.cservice.pojo.entity.ScheduleApplication.class));

        boolean result = jobService.applyForJob(100L, 1L, List.of(10L));

        assertThat(result).isTrue();
        verify(scheduleApplicationMapper).insert(argThat(sa -> "ACCEPTED".equals(sa.getStatus())));
        verify(shiftMapper).insert(any(com.parttime.cservice.pojo.entity.ShiftEntity.class));
    }

    @Test
    void applyForJob_autoApproveDisabled_shouldCreatePendingApplication() {
        Job job = new Job();
        job.setId(1L);
        job.setJobId(1L);
        job.setCompanyId(88L);
        job.setAutoApprove(null);

        JobSchedule schedule = activeFutureSchedule(10L, 1L);
        when(scheduleApplicationMapper.findScheduleIdsByWorkerIdAndJobId(100L, 1L)).thenReturn(List.of());
        when(jobMapper.findByJobId(1L)).thenReturn(Optional.of(job));
        when(jobScheduleMapper.findByIds(List.of(10L))).thenReturn(List.of(schedule));
        when(systemConfigMapper.findByKey("auto_approve_applications")).thenReturn(
                Optional.of(new com.parttime.cservice.pojo.entity.SystemConfig()));
        doAnswer(invocation -> 1).when(scheduleApplicationMapper).insert(any(com.parttime.cservice.pojo.entity.ScheduleApplication.class));

        boolean result = jobService.applyForJob(100L, 1L, List.of(10L));

        assertThat(result).isTrue();
        verify(scheduleApplicationMapper).insert(argThat(sa -> "PENDING".equals(sa.getStatus())));
        verify(shiftMapper, never()).insert(any());
    }

    private JobSchedule activeFutureSchedule(Long id, Long jobId) {
        JobSchedule schedule = new JobSchedule();
        schedule.setId(id);
        schedule.setJobId(jobId);
        schedule.setScheduleDate(LocalDate.now().plusDays(1));
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));
        schedule.setSlotsAvailable(1);
        schedule.setStatus("ACTIVE");
        return schedule;
    }
}
