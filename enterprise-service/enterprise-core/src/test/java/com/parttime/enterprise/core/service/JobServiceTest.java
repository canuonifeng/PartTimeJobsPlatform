package com.parttime.enterprise.core.service;

import com.parttime.enterprise.api.dto.*;
import com.parttime.enterprise.core.domain.Job;
import com.parttime.enterprise.core.domain.JobRate;
import com.parttime.enterprise.core.domain.JobSchedule;
import com.parttime.enterprise.core.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Captor
    private ArgumentCaptor<Job> jobCaptor;

    @Captor
    private ArgumentCaptor<JobRate> rateCaptor;

    @Captor
    private ArgumentCaptor<JobSchedule> scheduleCaptor;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobRepository);
    }

    @Test
    void getJobById_shouldReturnJobWithRatesAndSchedules() {
        Job job = new Job();
        job.setId(100L);
        job.setCompanyId(1L);
        job.setTitle("Software Engineer");
        job.setDescription("Build great software");
        job.setLocation("Shanghai");
        job.setCategoryId(10L);
        job.setHeadcount(3);
        job.setStatus("PUBLISHED");
        job.setDeadline(LocalDateTime.of(2026, 7, 1, 0, 0));

        JobRate rate = new JobRate();
        rate.setId(1L);
        rate.setJobId(100L);
        rate.setType("HOURLY");
        rate.setAmount(new BigDecimal("25.00"));
        rate.setCurrency("CNY");

        JobSchedule schedule = new JobSchedule();
        schedule.setId(1L);
        schedule.setJobId(100L);
        schedule.setScheduleDate(LocalDate.of(2026, 6, 1));
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));
        schedule.setSlotsAvailable(5);

        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));
        when(jobRepository.findRatesByJobId(100L)).thenReturn(List.of(rate));
        when(jobRepository.findSchedulesByJobId(100L)).thenReturn(List.of(schedule));

        JobResponse response = jobService.getJobById(100L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTitle()).isEqualTo("Software Engineer");
        assertThat(response.getStatus()).isEqualTo(JobStatus.PUBLISHED);

        assertThat(response.getRates()).hasSize(1);
        assertThat(response.getRates().get(0).getType()).isEqualTo(JobRateType.HOURLY);
        assertThat(response.getRates().get(0).getAmount()).isEqualByComparingTo(new BigDecimal("25.00"));

        assertThat(response.getSchedules()).hasSize(1);
        assertThat(response.getSchedules().get(0).getSlotsAvailable()).isEqualTo(5);
    }

    @Test
    void createJob_shouldCreateJobAndReturnResponse() {
        JobRateRequest rateRequest = new JobRateRequest();
        rateRequest.setType(JobRateType.HOURLY);
        rateRequest.setAmount(new BigDecimal("25.00"));
        rateRequest.setCurrency("CNY");

        JobScheduleRequest scheduleRequest = new JobScheduleRequest();
        scheduleRequest.setScheduleDate(LocalDate.of(2026, 6, 1));
        scheduleRequest.setStartTime(LocalTime.of(9, 0));
        scheduleRequest.setEndTime(LocalTime.of(18, 0));
        scheduleRequest.setSlotsAvailable(5);

        JobCreateRequest request = new JobCreateRequest();
        request.setCompanyId(1L);
        request.setTitle("Software Engineer");
        request.setDescription("Build great software");
        request.setLocation("Shanghai");
        request.setCategoryId(10L);
        request.setHeadcount(3);
        request.setDeadline(LocalDateTime.of(2026, 7, 1, 0, 0));
        request.setRates(List.of(rateRequest));
        request.setSchedules(List.of(scheduleRequest));

        doAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            job.setId(100L);
            return null;
        }).when(jobRepository).save(any(Job.class));

        JobResponse response = jobService.createJob(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getCompanyId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Software Engineer");
        assertThat(response.getDescription()).isEqualTo("Build great software");
        assertThat(response.getLocation()).isEqualTo("Shanghai");
        assertThat(response.getCategoryId()).isEqualTo(10L);
        assertThat(response.getHeadcount()).isEqualTo(3);
        assertThat(response.getStatus()).isEqualTo(JobStatus.DRAFT);

        verify(jobRepository).save(jobCaptor.capture());
        Job savedJob = jobCaptor.getValue();
        assertThat(savedJob.getTitle()).isEqualTo("Software Engineer");
        assertThat(savedJob.getCompanyId()).isEqualTo(1L);
        assertThat(savedJob.getStatus()).isEqualTo("DRAFT");

        verify(jobRepository).saveRate(rateCaptor.capture());
        assertThat(rateCaptor.getValue().getType()).isEqualTo("HOURLY");

        verify(jobRepository).saveSchedule(scheduleCaptor.capture());
        assertThat(scheduleCaptor.getValue().getSlotsAvailable()).isEqualTo(5);
    }

    // === 3.3 Status Management Tests ===

    @Test
    void publishJob_shouldTransitionFromDraftToPublished() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("DRAFT");

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        JobResponse response = jobService.publishJob(1L);

        assertThat(response.getStatus()).isEqualTo(JobStatus.PUBLISHED);
        verify(jobRepository).updateStatus(1L, "PUBLISHED");
    }

    @Test
    void publishJob_shouldThrowWhenNotDraft() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("PUBLISHED");

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.publishJob(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot publish");
    }

    @Test
    void closeJob_shouldTransitionFromPublishedToClosed() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("PUBLISHED");

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        JobResponse response = jobService.closeJob(1L);

        assertThat(response.getStatus()).isEqualTo(JobStatus.CLOSED);
        verify(jobRepository).updateStatus(1L, "CLOSED");
    }

    @Test
    void closeJob_shouldThrowWhenNotPublished() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("DRAFT");

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.closeJob(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot close");
    }

    @Test
    void reopenJob_shouldTransitionFromClosedToPublished() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("CLOSED");

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        JobResponse response = jobService.reopenJob(1L);

        assertThat(response.getStatus()).isEqualTo(JobStatus.PUBLISHED);
        verify(jobRepository).updateStatus(1L, "PUBLISHED");
    }

    @Test
    void reopenJob_shouldThrowWhenNotClosed() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("EXPIRED");

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.reopenJob(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot reopen");
    }

    @Test
    void expireJob_shouldTransitionFromPublishedToExpired() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("PUBLISHED");

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        jobService.expireJob(1L);

        verify(jobRepository).updateStatus(1L, "EXPIRED");
    }

    @Test
    void expireJob_shouldThrowWhenNotPublished() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("DRAFT");

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.expireJob(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot expire");
    }

    // === 3.4 Mixed Salary Rate Tests ===

    @Test
    void getJobRates_shouldReturnRatesForJob() {
        JobRate rate = new JobRate();
        rate.setId(1L);
        rate.setJobId(100L);
        rate.setType("HOURLY");
        rate.setAmount(new BigDecimal("25.00"));
        rate.setCurrency("CNY");

        when(jobRepository.findRatesByJobId(100L)).thenReturn(List.of(rate));

        List<JobRateResponse> rates = jobService.getJobRates(100L);

        assertThat(rates).hasSize(1);
        assertThat(rates.get(0).getType()).isEqualTo(JobRateType.HOURLY);
        assertThat(rates.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    void addJobRate_shouldCreateAndReturnRate() {
        JobRateRequest request = new JobRateRequest();
        request.setType(JobRateType.DAILY);
        request.setAmount(new BigDecimal("200.00"));
        request.setCurrency("CNY");

        doAnswer(invocation -> {
            JobRate rate = invocation.getArgument(0);
            rate.setId(99L);
            return null;
        }).when(jobRepository).saveRate(any(JobRate.class));

        JobRateResponse response = jobService.addJobRate(100L, request);

        assertThat(response.getJobId()).isEqualTo(100L);
        assertThat(response.getType()).isEqualTo(JobRateType.DAILY);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        verify(jobRepository).saveRate(any(JobRate.class));
    }

    @Test
    void updateJobRate_shouldModifyFields() {
        JobRate existing = new JobRate();
        existing.setId(1L);
        existing.setJobId(100L);
        existing.setType("HOURLY");
        existing.setAmount(new BigDecimal("25.00"));
        existing.setCurrency("CNY");

        JobRateRequest request = new JobRateRequest();
        request.setType(JobRateType.DAILY);
        request.setAmount(new BigDecimal("300.00"));
        request.setCurrency("CNY");

        when(jobRepository.findRateById(1L)).thenReturn(Optional.of(existing));

        JobRateResponse response = jobService.updateJobRate(1L, request);

        assertThat(response.getType()).isEqualTo(JobRateType.DAILY);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        verify(jobRepository).updateRate(existing);
    }

    @Test
    void removeJobRate_shouldDeleteRate() {
        jobService.removeJobRate(1L);
        verify(jobRepository).deleteRate(1L);
    }

    // === 3.5 Optional Schedule Tests ===

    @Test
    void getJobSchedules_shouldReturnSchedulesForJob() {
        JobSchedule schedule = new JobSchedule();
        schedule.setId(1L);
        schedule.setJobId(100L);
        schedule.setScheduleDate(LocalDate.of(2026, 6, 1));
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));
        schedule.setSlotsAvailable(5);

        when(jobRepository.findSchedulesByJobId(100L)).thenReturn(List.of(schedule));

        List<JobScheduleResponse> schedules = jobService.getJobSchedules(100L);

        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).getSlotsAvailable()).isEqualTo(5);
    }

    @Test
    void addJobSchedule_shouldCreateAndReturnSchedule() {
        JobScheduleRequest request = new JobScheduleRequest();
        request.setScheduleDate(LocalDate.of(2026, 6, 1));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(18, 0));
        request.setSlotsAvailable(10);

        doAnswer(invocation -> {
            JobSchedule s = invocation.getArgument(0);
            s.setId(99L);
            return null;
        }).when(jobRepository).saveSchedule(any(JobSchedule.class));

        JobScheduleResponse response = jobService.addJobSchedule(100L, request);

        assertThat(response.getJobId()).isEqualTo(100L);
        assertThat(response.getSlotsAvailable()).isEqualTo(10);
        verify(jobRepository).saveSchedule(any(JobSchedule.class));
    }

    @Test
    void updateJobSchedule_shouldModifyFields() {
        JobSchedule existing = new JobSchedule();
        existing.setId(1L);
        existing.setJobId(100L);
        existing.setScheduleDate(LocalDate.of(2026, 6, 1));
        existing.setStartTime(LocalTime.of(9, 0));
        existing.setEndTime(LocalTime.of(18, 0));
        existing.setSlotsAvailable(5);

        JobScheduleRequest request = new JobScheduleRequest();
        request.setScheduleDate(LocalDate.of(2026, 6, 2));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(17, 0));
        request.setSlotsAvailable(8);

        when(jobRepository.findScheduleById(1L)).thenReturn(Optional.of(existing));

        JobScheduleResponse response = jobService.updateJobSchedule(1L, request);

        assertThat(response.getSlotsAvailable()).isEqualTo(8);
        verify(jobRepository).updateSchedule(existing);
    }

    @Test
    void removeJobSchedule_shouldDeleteSchedule() {
        jobService.removeJobSchedule(1L);
        verify(jobRepository).deleteSchedule(1L);
    }
}
