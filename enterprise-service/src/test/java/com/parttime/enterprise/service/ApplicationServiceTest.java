package com.parttime.enterprise.service;

import com.parttime.enterprise.enums.ApplicationStatus;
import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.CompanyWorkerMapper;
import com.parttime.enterprise.mapper.JobApplicationMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.JobRateMapper;
import com.parttime.enterprise.mapper.JobScheduleMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.entity.JobRate;
import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobApplication;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.vo.JobApplicationVO;
import com.parttime.enterprise.service.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private JobApplicationMapper applicationMapper;

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

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @Test
    void getApplicationsByJob_shouldReturnList() {
        JobApplication app = new JobApplication();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus("PENDING");
        app.setAppliedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        when(applicationMapper.findByJobId(100L)).thenReturn(List.of(app));
        when(workerSyncMapper.findWorkerNameById(10L)).thenReturn("张三");
        when(workerSyncMapper.findWorkerPhoneById(10L)).thenReturn("13800000000");
        when(jobMapper.findById(100L)).thenReturn(Optional.of(new Job()));

        List<JobApplicationVO> result = applicationService.getApplicationsByJob(100L, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getJobId()).isEqualTo(100L);
        assertThat(result.get(0).getWorkerId()).isEqualTo(10L);
        assertThat(result.get(0).getWorkerPhone()).isEqualTo("13800000000");
        assertThat(result.get(0).getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    void getApplicationsByJob_shouldApplyPagination() {
        JobApplication first = new JobApplication();
        first.setId(1L);
        first.setJobId(100L);
        first.setWorkerId(10L);
        first.setStatus("PENDING");
        first.setAppliedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        JobApplication second = new JobApplication();
        second.setId(2L);
        second.setJobId(100L);
        second.setWorkerId(11L);
        second.setStatus("PENDING");
        second.setAppliedAt(LocalDateTime.of(2026, 5, 1, 9, 0));

        when(applicationMapper.findByJobId(100L)).thenReturn(List.of(first, second));
        when(workerSyncMapper.findWorkerNameById(10L)).thenReturn("张三");
        when(workerSyncMapper.findWorkerPhoneById(10L)).thenReturn("13800000000");
        when(jobMapper.findById(100L)).thenReturn(Optional.of(new Job()));

        List<JobApplicationVO> result = applicationService.getApplicationsByJob(100L, null, null, 1, 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void acceptApplication_shouldChangeStatusToAccepted() {
        JobApplication app = new JobApplication();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus("PENDING");

        Job job = new Job();
        job.setId(100L);
        job.setHeadcount(5);

        JobRate rate = new JobRate();
        rate.setType("HOURLY");
        rate.setAmount(new BigDecimal("25.00"));
        rate.setCurrency("CNY");

        when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        when(applicationMapper.countByJobIdAndStatus(100L, "ACCEPTED")).thenReturn(2);
        when(jobScheduleMapper.findByJobId(100L)).thenReturn(List.of());
        when(jobRateMapper.findByJobId(100L)).thenReturn(List.of(rate));

        JobApplicationVO result = applicationService.acceptApplication(1L);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        verify(applicationMapper).updateStatus(1L, "ACCEPTED");
    }

    @Test
    void acceptApplication_shouldGenerateOneShiftPerJobSchedule() {
        JobApplication app = new JobApplication();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus("PENDING");

        Job job = new Job();
        job.setId(100L);
        job.setCompanyId(300L);
        job.setHeadcount(5);

        JobSchedule firstSchedule = new JobSchedule();
        firstSchedule.setId(11L);
        firstSchedule.setJobId(100L);
        firstSchedule.setScheduleDate(LocalDate.of(2026, 6, 1));
        firstSchedule.setStartTime(LocalTime.of(9, 0));
        firstSchedule.setEndTime(LocalTime.of(18, 0));

        JobSchedule secondSchedule = new JobSchedule();
        secondSchedule.setId(12L);
        secondSchedule.setJobId(100L);
        secondSchedule.setScheduleDate(LocalDate.of(2026, 6, 2));
        secondSchedule.setStartTime(LocalTime.of(10, 0));
        secondSchedule.setEndTime(LocalTime.of(19, 0));

        JobRate rate = new JobRate();
        rate.setType("HOURLY");
        rate.setAmount(new BigDecimal("25.00"));
        rate.setCurrency("CNY");

        when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        when(applicationMapper.countByJobIdAndStatus(100L, "ACCEPTED")).thenReturn(0);
        when(jobScheduleMapper.findByJobId(100L)).thenReturn(List.of(firstSchedule, secondSchedule));
        when(jobRateMapper.findByJobId(100L)).thenReturn(List.of(rate));

        applicationService.acceptApplication(1L);

        ArgumentCaptor<ScheduleShift> captor = ArgumentCaptor.forClass(ScheduleShift.class);
        verify(shiftMapper, times(2)).insert(captor.capture());
        List<ScheduleShift> shifts = captor.getAllValues();

        assertThat(shifts).hasSize(2);
        assertThat(shifts.get(0).getApplicationId()).isEqualTo(1L);
        assertThat(shifts.get(0).getJobId()).isEqualTo(100L);
        assertThat(shifts.get(0).getWorkerId()).isEqualTo(10L);
        assertThat(shifts.get(0).getShiftDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(shifts.get(0).getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(shifts.get(0).getEndTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(shifts.get(0).getSalaryType()).isEqualTo("HOURLY");
        assertThat(shifts.get(0).getSalaryAmount()).isEqualByComparingTo("25.00");
        assertThat(shifts.get(0).getSalaryCurrency()).isEqualTo("CNY");

        assertThat(shifts.get(1).getApplicationId()).isEqualTo(1L);
        assertThat(shifts.get(1).getJobId()).isEqualTo(100L);
        assertThat(shifts.get(1).getWorkerId()).isEqualTo(10L);
        assertThat(shifts.get(1).getShiftDate()).isEqualTo(LocalDate.of(2026, 6, 2));
        assertThat(shifts.get(1).getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(shifts.get(1).getEndTime()).isEqualTo(LocalTime.of(19, 0));
        assertThat(shifts.get(1).getSalaryType()).isEqualTo("HOURLY");
        assertThat(shifts.get(1).getSalaryAmount()).isEqualByComparingTo("25.00");
        assertThat(shifts.get(1).getSalaryCurrency()).isEqualTo("CNY");
    }

    @Test
    void acceptApplication_shouldUseFirstRateWhenMultipleRatesExist() {
        JobApplication app = new JobApplication();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus("PENDING");

        Job job = new Job();
        job.setId(100L);
        job.setCompanyId(300L);
        job.setHeadcount(5);

        JobSchedule schedule = new JobSchedule();
        schedule.setId(11L);
        schedule.setJobId(100L);
        schedule.setScheduleDate(LocalDate.of(2026, 6, 1));
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));

        JobRate firstRate = new JobRate();
        firstRate.setType("HOURLY");
        firstRate.setAmount(new BigDecimal("25.00"));
        firstRate.setCurrency("CNY");

        JobRate secondRate = new JobRate();
        secondRate.setType("DAILY");
        secondRate.setAmount(new BigDecimal("200.00"));
        secondRate.setCurrency("CNY");

        when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        when(applicationMapper.countByJobIdAndStatus(100L, "ACCEPTED")).thenReturn(0);
        when(jobScheduleMapper.findByJobId(100L)).thenReturn(List.of(schedule));
        when(jobRateMapper.findByJobId(100L)).thenReturn(List.of(firstRate, secondRate));

        applicationService.acceptApplication(1L);

        ArgumentCaptor<ScheduleShift> captor = ArgumentCaptor.forClass(ScheduleShift.class);
        verify(shiftMapper).insert(captor.capture());
        ScheduleShift shift = captor.getValue();

        assertThat(shift.getSalaryType()).isEqualTo("HOURLY");
        assertThat(shift.getSalaryAmount()).isEqualByComparingTo("25.00");
        assertThat(shift.getSalaryCurrency()).isEqualTo("CNY");
    }

    @Test
    void acceptApplication_shouldGenerateShiftsWithEmptySalarySnapshotWhenNoRateExists() {
        JobApplication app = new JobApplication();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus("PENDING");

        Job job = new Job();
        job.setId(100L);
        job.setCompanyId(300L);
        job.setHeadcount(5);

        JobSchedule schedule = new JobSchedule();
        schedule.setId(11L);
        schedule.setJobId(100L);
        schedule.setScheduleDate(LocalDate.of(2026, 6, 1));
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));

        when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        when(applicationMapper.countByJobIdAndStatus(100L, "ACCEPTED")).thenReturn(0);
        when(jobScheduleMapper.findByJobId(100L)).thenReturn(List.of(schedule));
        when(jobRateMapper.findByJobId(100L)).thenReturn(List.of());

        applicationService.acceptApplication(1L);

        ArgumentCaptor<ScheduleShift> captor = ArgumentCaptor.forClass(ScheduleShift.class);
        verify(shiftMapper).insert(captor.capture());
        ScheduleShift shift = captor.getValue();

        assertThat(shift.getSalaryType()).isNull();
        assertThat(shift.getSalaryAmount()).isNull();
        assertThat(shift.getSalaryCurrency()).isNull();
    }

    @Test
    void acceptApplication_shouldFillMissingShiftsWhenSomeAlreadyExist() {
        JobApplication app = new JobApplication();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus("ACCEPTED");

        Job job = new Job();
        job.setId(100L);
        job.setCompanyId(300L);
        job.setHeadcount(5);

        JobSchedule firstSchedule = new JobSchedule();
        firstSchedule.setId(11L);
        firstSchedule.setJobId(100L);
        firstSchedule.setScheduleDate(LocalDate.of(2026, 6, 1));
        firstSchedule.setStartTime(LocalTime.of(9, 0));
        firstSchedule.setEndTime(LocalTime.of(18, 0));

        JobSchedule secondSchedule = new JobSchedule();
        secondSchedule.setId(12L);
        secondSchedule.setJobId(100L);
        secondSchedule.setScheduleDate(LocalDate.of(2026, 6, 2));
        secondSchedule.setStartTime(LocalTime.of(10, 0));
        secondSchedule.setEndTime(LocalTime.of(19, 0));

        ScheduleShift existingShift = new ScheduleShift();
        existingShift.setApplicationId(1L);
        existingShift.setShiftDate(LocalDate.of(2026, 6, 1));
        existingShift.setStartTime(LocalTime.of(9, 0));
        existingShift.setEndTime(LocalTime.of(18, 0));

        JobRate rate = new JobRate();
        rate.setType("HOURLY");
        rate.setAmount(new BigDecimal("25.00"));
        rate.setCurrency("CNY");

        when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        when(jobScheduleMapper.findByJobId(100L)).thenReturn(List.of(firstSchedule, secondSchedule));
        when(jobRateMapper.findByJobId(100L)).thenReturn(List.of(rate));
        when(shiftMapper.findByApplicationId(1L)).thenReturn(List.of(existingShift));

        applicationService.acceptApplication(1L);

        ArgumentCaptor<ScheduleShift> captor = ArgumentCaptor.forClass(ScheduleShift.class);
        verify(shiftMapper).insert(captor.capture());
        ScheduleShift inserted = captor.getValue();

        assertThat(inserted.getShiftDate()).isEqualTo(LocalDate.of(2026, 6, 2));
        assertThat(inserted.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(inserted.getEndTime()).isEqualTo(LocalTime.of(19, 0));
        verify(applicationMapper, times(0)).updateStatus(1L, "ACCEPTED");
    }

    @Test
    void rejectApplication_shouldChangeStatusToRejected() {
        JobApplication app = new JobApplication();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus("PENDING");
        app.setAppliedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(new Job()));
        when(workerSyncMapper.findWorkerNameById(10L)).thenReturn("张三");
        when(workerSyncMapper.findWorkerPhoneById(10L)).thenReturn("13800000000");

        JobApplicationVO result = applicationService.rejectApplication(1L);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        verify(applicationMapper).updateStatus(1L, "REJECTED");
    }

    @Test
    void acceptApplication_shouldThrowWhenJobIsFull() {
        JobApplication app = new JobApplication();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus("PENDING");

        Job job = new Job();
        job.setId(100L);
        job.setHeadcount(5);

        when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        when(applicationMapper.countByJobIdAndStatus(100L, "ACCEPTED")).thenReturn(5);

        assertThatThrownBy(() -> applicationService.acceptApplication(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("岗位已录满");
    }
}
