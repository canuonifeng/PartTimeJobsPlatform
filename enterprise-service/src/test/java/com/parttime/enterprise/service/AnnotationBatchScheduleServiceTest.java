package com.parttime.enterprise.service;

import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.CompanyWorkerMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.JobRateMapper;
import com.parttime.enterprise.mapper.JobScheduleMapper;
import com.parttime.enterprise.mapper.ScheduleApplicationMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerNotificationMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.cmd.ScheduleBatchCreateCmd;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.service.impl.ScheduleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnotationBatchScheduleServiceTest {

    @Mock
    private ScheduleShiftMapper shiftMapper;
    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;
    @Mock
    private JobMapper jobMapper;
    @Mock
    private JobRateMapper jobRateMapper;
    @Mock
    private JobScheduleMapper jobScheduleMapper;
    @Mock
    private ScheduleApplicationMapper scheduleApplicationMapper;
    @Mock
    private CompanyWorkerMapper companyWorkerMapper;
    @Mock
    private WorkerSyncMapper workerSyncMapper;
    @Mock
    private WorkerNotificationMapper workerNotificationMapper;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    @Captor
    private ArgumentCaptor<JobSchedule> scheduleCaptor;

    @Test
    void batchCreateAnnotationSchedules_shouldPersistTotalItemsAndExternalBatchId() {
        Job job = new Job();
        job.setId(10L);
        job.setTitle("图片分类标注");
        job.setHeadcount(5);
        job.setContactName("王经理");
        job.setContactPhone("13800000000");
        job.setCompanyId(1L);
        when(jobMapper.findById(10L)).thenReturn(Optional.of(job));
        when(jobScheduleMapper.findById(anyLong())).thenReturn(Optional.of(scheduleOf(10L)));
        com.parttime.enterprise.pojo.vo.ScheduleManagementVO managed = new com.parttime.enterprise.pojo.vo.ScheduleManagementVO();
        managed.setId(100L);
        managed.setJobId(10L);
        when(jobScheduleMapper.findManagementPage(anyLong(), anyLong(), isNull(), isNull(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(managed));

        ScheduleBatchCreateCmd request = new ScheduleBatchCreateCmd();
        request.setJobId(10L);
        request.setStartDate(LocalDate.of(2026, 9, 21));
        request.setEndDate(LocalDate.of(2026, 9, 23));
        request.setWeekdays(List.of(1, 2, 3));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(18, 0));
        request.setTotalItems(300);
        request.setExternalBatchId("BATCH_EXT_001");

        doAnswer(inv -> {
            JobSchedule s = inv.getArgument(0);
            s.setId(100L);
            return 1;
        }).when(jobScheduleMapper).insert(any(JobSchedule.class));
        scheduleService.batchCreateManagedSchedules(request);

        verify(jobScheduleMapper, org.mockito.Mockito.times(3)).insert(scheduleCaptor.capture());
        List<JobSchedule> saved = scheduleCaptor.getAllValues();
        assertThat(saved).hasSize(3);
        for (JobSchedule s : saved) {
            assertThat(s.getTotalItems()).isEqualTo(300);
            assertThat(s.getExternalBatchId()).isEqualTo("BATCH_EXT_001");
            assertThat(s.getStatus()).isEqualTo("ACTIVE");
            assertThat(s.getScheduleName()).isEqualTo("图片分类标注");
        }
        assertThat(saved.get(0).getScheduleDate()).isEqualTo(LocalDate.of(2026, 9, 21));
        assertThat(saved.get(2).getScheduleDate()).isEqualTo(LocalDate.of(2026, 9, 23));
    }

    private JobSchedule scheduleOf(Long jobId) {
        JobSchedule s = new JobSchedule();
        s.setId(1L);
        s.setJobId(jobId);
        return s;
    }

    @Test
    void batchCreateAnnotationSchedules_weekdayFilter_shouldSkipNonMatchingDays() {
        Job job = new Job();
        job.setId(10L);
        job.setTitle("图片分类标注");
        job.setHeadcount(5);
        job.setContactName("王经理");
        job.setContactPhone("13800000000");
        job.setCompanyId(1L);
        when(jobMapper.findById(10L)).thenReturn(Optional.of(job));
        when(jobScheduleMapper.findById(anyLong())).thenReturn(Optional.of(scheduleOf(10L)));
        com.parttime.enterprise.pojo.vo.ScheduleManagementVO managed = new com.parttime.enterprise.pojo.vo.ScheduleManagementVO();
        managed.setId(100L);
        managed.setJobId(10L);
        when(jobScheduleMapper.findManagementPage(anyLong(), anyLong(), isNull(), isNull(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(managed));

        ScheduleBatchCreateCmd request = new ScheduleBatchCreateCmd();
        request.setJobId(10L);
        request.setStartDate(LocalDate.of(2026, 9, 20));
        request.setEndDate(LocalDate.of(2026, 9, 26));
        request.setWeekdays(List.of(7));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(18, 0));
        request.setTotalItems(100);
        request.setExternalBatchId("BATCH_EXT_002");

        doAnswer(inv -> {
            JobSchedule s = inv.getArgument(0);
            s.setId(100L);
            return 1;
        }).when(jobScheduleMapper).insert(any(JobSchedule.class));
        scheduleService.batchCreateManagedSchedules(request);

        verify(jobScheduleMapper, org.mockito.Mockito.times(1)).insert(any(JobSchedule.class));
    }
}
