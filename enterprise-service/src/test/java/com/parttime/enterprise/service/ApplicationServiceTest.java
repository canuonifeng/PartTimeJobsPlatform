package com.parttime.enterprise.service;

import com.parttime.enterprise.enums.ApplicationStatus;
import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.CompanyWorkerMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.JobRateMapper;
import com.parttime.enterprise.mapper.JobScheduleMapper;
import com.parttime.enterprise.mapper.ScheduleApplicationMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerNotificationMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobRate;
import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.pojo.entity.ScheduleApplication;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.pojo.vo.ScheduleApplicationVO;
import com.parttime.enterprise.service.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ScheduleApplicationMapper applicationMapper;

    @Mock
    private JobMapper jobMapper;

    @Mock
    private WorkerSyncMapper workerSyncMapper;

    @Mock
    private CompanyWorkerMapper companyWorkerMapper;

    @Mock
    private JobScheduleMapper jobScheduleMapper;

    @Mock
    private JobRateMapper jobRateMapper;

    @Mock
    private ScheduleShiftMapper shiftMapper;

    @Mock
    private WorkerNotificationMapper workerNotificationMapper;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(applicationService, "scheduleApplicationMapper", applicationMapper);
    }

    @Test
    void getApplicationsByJob_shouldReturnPagedVOs() {
        ScheduleApplicationVO app = new ScheduleApplicationVO();
        app.setId(1L);
        app.setJobId(100L);
        app.setScheduleId(11L);
        app.setWorkerId(10L);
        app.setWorkerPhone("13800000000");
        app.setStatus(ApplicationStatus.PENDING);
        app.setAppliedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        when(applicationMapper.findVOByJobId(100L)).thenReturn(List.of(app));

        PageVO<ScheduleApplicationVO> result = applicationService.getApplicationsByJob(1L, 100L, null, null, 1, 20);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getId()).isEqualTo(1L);
        assertThat(result.getRecords().get(0).getJobId()).isEqualTo(100L);
        assertThat(result.getRecords().get(0).getWorkerId()).isEqualTo(10L);
        assertThat(result.getRecords().get(0).getWorkerPhone()).isEqualTo("13800000000");
        assertThat(result.getRecords().get(0).getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    void getApplicationsByJob_shouldApplyPagination() {
        ScheduleApplicationVO first = new ScheduleApplicationVO();
        first.setId(1L);
        first.setJobId(100L);
        first.setScheduleId(11L);
        first.setWorkerId(10L);
        first.setStatus(ApplicationStatus.PENDING);
        first.setAppliedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        ScheduleApplicationVO second = new ScheduleApplicationVO();
        second.setId(2L);
        second.setJobId(100L);
        second.setScheduleId(12L);
        second.setWorkerId(11L);
        second.setStatus(ApplicationStatus.PENDING);
        second.setAppliedAt(LocalDateTime.of(2026, 5, 1, 9, 0));

        when(applicationMapper.findVOByJobId(100L)).thenReturn(List.of(first, second));

        PageVO<ScheduleApplicationVO> result = applicationService.getApplicationsByJob(1L, 100L, null, null, 1, 1);

        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void acceptApplication_shouldChangeStatusToAccepted() {
        ScheduleApplication app = pendingApplication();
        Job job = job();
        JobSchedule schedule = schedule();
        JobRate rate = hourlyRate();

        when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
        when(jobScheduleMapper.findById(11L)).thenReturn(Optional.of(schedule));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        when(applicationMapper.countByJobIdAndStatus(100L, "ACCEPTED")).thenReturn(2);
        when(jobRateMapper.findByJobId(100L)).thenReturn(List.of(rate));
        when(shiftMapper.findByApplicationId(1L)).thenReturn(List.of());
        when(workerSyncMapper.findWorkerNameById(10L)).thenReturn("张三");
        when(workerSyncMapper.findWorkerPhoneById(10L)).thenReturn("13800000000");

        ScheduleApplicationVO result = applicationService.acceptApplication(1L);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        assertThat(result.getScheduleId()).isEqualTo(11L);
        assertThat(result.getJobId()).isEqualTo(100L);
        verify(applicationMapper).updateStatus(1L, "ACCEPTED");
        verify(workerNotificationMapper).insertWorkerNotification(
                10L,
                "APPLICATION_ACCEPTED",
                "application",
                "报名已通过",
                "您报名的测试岗位已通过审核",
                "APPLICATION",
                1L);
    }

    @Test
    void acceptApplication_shouldGenerateShiftForApplicationSchedule() {
        ScheduleApplication app = pendingApplication();
        Job job = job();
        JobSchedule schedule = schedule();
        JobRate rate = hourlyRate();

        when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
        when(jobScheduleMapper.findById(11L)).thenReturn(Optional.of(schedule));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        when(applicationMapper.countByJobIdAndStatus(100L, "ACCEPTED")).thenReturn(0);
        when(jobRateMapper.findByJobId(100L)).thenReturn(List.of(rate));
        when(shiftMapper.findByApplicationId(1L)).thenReturn(List.of());

        applicationService.acceptApplication(1L);

        ArgumentCaptor<ScheduleShift> captor = ArgumentCaptor.forClass(ScheduleShift.class);
        verify(shiftMapper).insert(captor.capture());
        ScheduleShift shift = captor.getValue();

        assertThat(shift.getApplicationId()).isEqualTo(1L);
        assertThat(shift.getJobId()).isEqualTo(100L);
        assertThat(shift.getCompanyId()).isEqualTo(300L);
        assertThat(shift.getWorkerId()).isEqualTo(10L);
        assertThat(shift.getShiftDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(shift.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(shift.getEndTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(shift.getSalaryType()).isEqualTo("HOURLY");
        assertThat(shift.getSalaryAmount()).isEqualByComparingTo("25.00");
        assertThat(shift.getSalaryCurrency()).isEqualTo("CNY");
    }

    @Test
    void acceptApplication_shouldUseFirstRateWhenMultipleRatesExist() {
        ScheduleApplication app = pendingApplication();
        Job job = job();
        JobSchedule schedule = schedule();
        JobRate firstRate = hourlyRate();
        JobRate secondRate = new JobRate();
        secondRate.setType("DAILY");
        secondRate.setAmount(new BigDecimal("200.00"));
        secondRate.setCurrency("CNY");

        when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
        when(jobScheduleMapper.findById(11L)).thenReturn(Optional.of(schedule));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        when(applicationMapper.countByJobIdAndStatus(100L, "ACCEPTED")).thenReturn(0);
        when(jobRateMapper.findByJobId(100L)).thenReturn(List.of(firstRate, secondRate));
        when(shiftMapper.findByApplicationId(1L)).thenReturn(List.of());

        applicationService.acceptApplication(1L);

        ArgumentCaptor<ScheduleShift> captor = ArgumentCaptor.forClass(ScheduleShift.class);
        verify(shiftMapper).insert(captor.capture());
        ScheduleShift shift = captor.getValue();

        assertThat(shift.getSalaryType()).isEqualTo("HOURLY");
        assertThat(shift.getSalaryAmount()).isEqualByComparingTo("25.00");
        assertThat(shift.getSalaryCurrency()).isEqualTo("CNY");
    }

    @Test
    void acceptApplication_shouldGenerateShiftWithEmptySalarySnapshotWhenNoRateExists() {
        ScheduleApplication app = pendingApplication();
        Job job = job();
        JobSchedule schedule = schedule();

        when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
        when(jobScheduleMapper.findById(11L)).thenReturn(Optional.of(schedule));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        when(applicationMapper.countByJobIdAndStatus(100L, "ACCEPTED")).thenReturn(0);
        when(jobRateMapper.findByJobId(100L)).thenReturn(List.of());
        when(shiftMapper.findByApplicationId(1L)).thenReturn(List.of());

        applicationService.acceptApplication(1L);

        ArgumentCaptor<ScheduleShift> captor = ArgumentCaptor.forClass(ScheduleShift.class);
        verify(shiftMapper).insert(captor.capture());
        ScheduleShift shift = captor.getValue();

        assertThat(shift.getSalaryType()).isNull();
        assertThat(shift.getSalaryAmount()).isNull();
        assertThat(shift.getSalaryCurrency()).isNull();
    }

    @Test
    void acceptApplication_shouldNotDuplicateExistingShift() {
        ScheduleApplication app = pendingApplication();
        app.setStatus("ACCEPTED");
        Job job = job();
        JobSchedule schedule = schedule();
        ScheduleShift existingShift = new ScheduleShift();
        existingShift.setApplicationId(1L);
        existingShift.setShiftDate(LocalDate.of(2026, 6, 1));
        existingShift.setStartTime(LocalTime.of(9, 0));
        existingShift.setEndTime(LocalTime.of(18, 0));

        when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
        when(jobScheduleMapper.findById(11L)).thenReturn(Optional.of(schedule));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        when(jobRateMapper.findByJobId(100L)).thenReturn(List.of(hourlyRate()));
        when(shiftMapper.findByApplicationId(1L)).thenReturn(List.of(existingShift));

        applicationService.acceptApplication(1L);

        verify(shiftMapper, never()).insert(org.mockito.ArgumentMatchers.any());
        verify(applicationMapper, never()).updateStatus(1L, "ACCEPTED");
    }

    @Test
    void rejectApplication_shouldChangeStatusToRejected() {
        ScheduleApplication app = pendingApplication();
        app.setAppliedAt(LocalDateTime.of(2026, 5, 1, 10, 0));
        JobSchedule schedule = schedule();
        Job job = job();

        when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
        when(jobScheduleMapper.findById(11L)).thenReturn(Optional.of(schedule));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        when(workerSyncMapper.findWorkerNameById(10L)).thenReturn("张三");
        when(workerSyncMapper.findWorkerPhoneById(10L)).thenReturn("13800000000");

        ScheduleApplicationVO result = applicationService.rejectApplication(1L);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        verify(applicationMapper).updateStatus(1L, "REJECTED");
        verify(workerNotificationMapper).insertWorkerNotification(
                10L,
                "APPLICATION_REJECTED",
                "application",
                "报名未通过",
                "您报名的测试岗位未通过审核",
                "APPLICATION",
                1L);
    }

    @Test
    void acceptApplication_shouldThrowWhenJobIsFull() {
        ScheduleApplication app = pendingApplication();
        Job job = job();
        job.setHeadcount(5);
        JobSchedule schedule = schedule();

        when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
        when(jobScheduleMapper.findById(11L)).thenReturn(Optional.of(schedule));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        when(applicationMapper.countByJobIdAndStatus(100L, "ACCEPTED")).thenReturn(5);

        assertThatThrownBy(() -> applicationService.acceptApplication(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("岗位已录满");
    }

    private ScheduleApplication pendingApplication() {
        ScheduleApplication app = new ScheduleApplication();
        app.setId(1L);
        app.setScheduleId(11L);
        app.setWorkerId(10L);
        app.setStatus("PENDING");
        return app;
    }

    private Job job() {
        Job job = new Job();
        job.setId(100L);
        job.setCompanyId(300L);
        job.setHeadcount(5);
        job.setTitle("测试岗位");
        return job;
    }

    private JobSchedule schedule() {
        JobSchedule schedule = new JobSchedule();
        schedule.setId(11L);
        schedule.setJobId(100L);
        schedule.setScheduleDate(LocalDate.of(2026, 6, 1));
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));
        return schedule;
    }

    private JobRate hourlyRate() {
        JobRate rate = new JobRate();
        rate.setType("HOURLY");
        rate.setAmount(new BigDecimal("25.00"));
        rate.setCurrency("CNY");
        return rate;
    }
}
