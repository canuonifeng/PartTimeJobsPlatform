package com.parttime.enterprise.service;

import com.parttime.enterprise.enums.JobRateType;
import com.parttime.enterprise.enums.JobStatus;
import com.parttime.enterprise.mapper.CJobMapper;
import com.parttime.enterprise.mapper.EnterpriseMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.JobRateMapper;
import com.parttime.enterprise.mapper.JobScheduleMapper;
import com.parttime.enterprise.pojo.cmd.JobCreateCmd;
import com.parttime.enterprise.pojo.cmd.JobRateCmd;
import com.parttime.enterprise.pojo.cmd.JobScheduleCmd;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobRate;
import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.pojo.vo.JobRateVO;
import com.parttime.enterprise.pojo.vo.JobScheduleVO;
import com.parttime.enterprise.pojo.vo.JobVO;
import com.parttime.enterprise.service.impl.JobServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobMapper jobMapper;

    @Mock
    private JobRateMapper jobRateMapper;

    @Mock
    private JobScheduleMapper jobScheduleMapper;

    @Mock
    private EnterpriseMapper enterpriseMapper;

    @Mock
    private CJobMapper cJobMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<Job> jobCaptor;

    @Captor
    private ArgumentCaptor<JobRate> rateCaptor;

    @Captor
    private ArgumentCaptor<JobSchedule> scheduleCaptor;

    @InjectMocks
    private JobServiceImpl jobService;

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

        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        when(jobRateMapper.findByJobId(100L)).thenReturn(List.of(rate));
        when(jobScheduleMapper.findByJobId(100L)).thenReturn(List.of(schedule));

        JobVO response = jobService.getJobById(100L);

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
        JobRateCmd rateRequest = new JobRateCmd();
        rateRequest.setType(JobRateType.HOURLY);
        rateRequest.setAmount(new BigDecimal("25.00"));
        rateRequest.setCurrency("CNY");

        JobScheduleCmd scheduleRequest = new JobScheduleCmd();
        scheduleRequest.setScheduleDate(LocalDate.of(2026, 6, 1));
        scheduleRequest.setStartTime(LocalTime.of(9, 0));
        scheduleRequest.setEndTime(LocalTime.of(18, 0));
        scheduleRequest.setSlotsAvailable(5);

        JobCreateCmd request = new JobCreateCmd();
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
            return 1;
        }).when(jobMapper).insert(any(Job.class));

        JobVO response = jobService.createJob(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getCompanyId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Software Engineer");
        assertThat(response.getDescription()).isEqualTo("Build great software");
        assertThat(response.getLocation()).isEqualTo("Shanghai");
        assertThat(response.getCategoryId()).isEqualTo(10L);
        assertThat(response.getHeadcount()).isEqualTo(3);
        assertThat(response.getStatus()).isEqualTo(JobStatus.DRAFT);

        verify(jobMapper).insert(jobCaptor.capture());
        Job savedJob = jobCaptor.getValue();
        assertThat(savedJob.getTitle()).isEqualTo("Software Engineer");
        assertThat(savedJob.getCompanyId()).isEqualTo(1L);
        assertThat(savedJob.getStatus()).isEqualTo("DRAFT");

        verify(jobRateMapper).insert(rateCaptor.capture());
        assertThat(rateCaptor.getValue().getType()).isEqualTo("HOURLY");

        verify(jobScheduleMapper).insert(scheduleCaptor.capture());
        assertThat(scheduleCaptor.getValue().getSlotsAvailable()).isEqualTo(5);
    }

    @Test
    void publishJob_shouldTransitionFromDraftToPublished() throws Exception {
        Job job = new Job();
        job.setId(1L);
        job.setCompanyId(1L);
        job.setStatus("DRAFT");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));
        when(enterpriseMapper.findCompanyNameById(1L)).thenReturn("美味餐饮管理有限公司");
        when(enterpriseMapper.findCompanyLogoById(1L)).thenReturn("https://cdn.example.com/logos/meiwei.png");
        doReturn("[]").when(objectMapper).writeValueAsString(any());

        JobVO response = jobService.publishJob(1L);

        assertThat(response.getStatus()).isEqualTo(JobStatus.PUBLISHED);
        verify(jobMapper).updateStatus(1L, "PUBLISHED");
        verify(cJobMapper).upsert(
                eq(1L), eq(1L), eq("美味餐饮管理有限公司"), eq("https://cdn.example.com/logos/meiwei.png"),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), eq("PUBLISHED"), eq("[]"), isNull(), isNull()
        );
    }

    @Test
    void publishJob_shouldThrowWhenNotDraft() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("PUBLISHED");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.publishJob(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot publish");
    }

    @Test
    void closeJob_shouldTransitionFromPublishedToClosed() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("PUBLISHED");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));

        JobVO response = jobService.closeJob(1L);

        assertThat(response.getStatus()).isEqualTo(JobStatus.CLOSED);
        verify(jobMapper).updateStatus(1L, "CLOSED");
    }

    @Test
    void closeJob_shouldThrowWhenNotPublished() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("DRAFT");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.closeJob(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot close");
    }

    @Test
    void reopenJob_shouldTransitionFromClosedToPublished() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("CLOSED");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));

        JobVO response = jobService.reopenJob(1L);

        assertThat(response.getStatus()).isEqualTo(JobStatus.PUBLISHED);
        verify(jobMapper).updateStatus(1L, "PUBLISHED");
    }

    @Test
    void reopenJob_shouldThrowWhenNotClosed() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("EXPIRED");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.reopenJob(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot reopen");
    }

    @Test
    void expireJob_shouldTransitionFromPublishedToExpired() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("PUBLISHED");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));

        jobService.expireJob(1L);

        verify(jobMapper).updateStatus(1L, "EXPIRED");
    }

    @Test
    void expireJob_shouldThrowWhenNotPublished() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("DRAFT");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.expireJob(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot expire");
    }

    @Test
    void getJobRates_shouldReturnRatesForJob() {
        JobRate rate = new JobRate();
        rate.setId(1L);
        rate.setJobId(100L);
        rate.setType("HOURLY");
        rate.setAmount(new BigDecimal("25.00"));
        rate.setCurrency("CNY");

        when(jobRateMapper.findByJobId(100L)).thenReturn(List.of(rate));

        List<JobRateVO> rates = jobService.getJobRates(100L);

        assertThat(rates).hasSize(1);
        assertThat(rates.get(0).getType()).isEqualTo(JobRateType.HOURLY);
        assertThat(rates.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    void addJobRate_shouldCreateAndReturnRate() {
        JobRateCmd request = new JobRateCmd();
        request.setType(JobRateType.DAILY);
        request.setAmount(new BigDecimal("200.00"));
        request.setCurrency("CNY");

        doAnswer(invocation -> {
            JobRate rate = invocation.getArgument(0);
            rate.setId(99L);
            return 1;
        }).when(jobRateMapper).insert(any(JobRate.class));

        JobRateVO response = jobService.addJobRate(100L, request);

        assertThat(response.getJobId()).isEqualTo(100L);
        assertThat(response.getType()).isEqualTo(JobRateType.DAILY);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        verify(jobRateMapper).insert(any(JobRate.class));
    }

    @Test
    void updateJobRate_shouldModifyFields() {
        JobRate existing = new JobRate();
        existing.setId(1L);
        existing.setJobId(100L);
        existing.setType("HOURLY");
        existing.setAmount(new BigDecimal("25.00"));
        existing.setCurrency("CNY");

        JobRateCmd request = new JobRateCmd();
        request.setType(JobRateType.DAILY);
        request.setAmount(new BigDecimal("300.00"));
        request.setCurrency("CNY");

        when(jobRateMapper.findById(1L)).thenReturn(Optional.of(existing));

        JobRateVO response = jobService.updateJobRate(1L, request);

        assertThat(response.getType()).isEqualTo(JobRateType.DAILY);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        verify(jobRateMapper).update(existing);
    }

    @Test
    void removeJobRate_shouldDeleteRate() {
        jobService.removeJobRate(1L);
        verify(jobRateMapper).delete(1L);
    }

    @Test
    void getJobSchedules_shouldReturnSchedulesForJob() {
        JobSchedule schedule = new JobSchedule();
        schedule.setId(1L);
        schedule.setJobId(100L);
        schedule.setScheduleDate(LocalDate.of(2026, 6, 1));
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(18, 0));
        schedule.setSlotsAvailable(5);

        when(jobScheduleMapper.findByJobId(100L)).thenReturn(List.of(schedule));

        List<JobScheduleVO> schedules = jobService.getJobSchedules(100L);

        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).getSlotsAvailable()).isEqualTo(5);
    }

    @Test
    void addJobSchedule_shouldCreateAndReturnSchedule() {
        JobScheduleCmd request = new JobScheduleCmd();
        request.setScheduleDate(LocalDate.of(2026, 6, 1));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(18, 0));
        request.setSlotsAvailable(10);

        doAnswer(invocation -> {
            JobSchedule s = invocation.getArgument(0);
            s.setId(99L);
            return 1;
        }).when(jobScheduleMapper).insert(any(JobSchedule.class));

        Job syncJob = new Job();
        syncJob.setId(100L);
        syncJob.setCompanyId(1L);
        when(jobMapper.findById(100L)).thenReturn(Optional.of(syncJob));
        when(jobScheduleMapper.findByJobId(100L)).thenReturn(List.of());

        JobScheduleVO response = jobService.addJobSchedule(100L, request);

        assertThat(response.getJobId()).isEqualTo(100L);
        assertThat(response.getSlotsAvailable()).isEqualTo(10);
        verify(jobScheduleMapper).insert(any(JobSchedule.class));
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

        JobScheduleCmd request = new JobScheduleCmd();
        request.setScheduleDate(LocalDate.of(2026, 6, 2));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(17, 0));
        request.setSlotsAvailable(8);

        when(jobScheduleMapper.findById(1L)).thenReturn(Optional.of(existing));

        Job syncJob = new Job();
        syncJob.setId(100L);
        syncJob.setCompanyId(1L);
        when(jobMapper.findById(100L)).thenReturn(Optional.of(syncJob));
        when(jobScheduleMapper.findByJobId(100L)).thenReturn(List.of());

        JobScheduleVO response = jobService.updateJobSchedule(1L, request);

        assertThat(response.getSlotsAvailable()).isEqualTo(8);
        verify(jobScheduleMapper).update(existing);
    }

    @Test
    void removeJobSchedule_shouldDeleteSchedule() {
        JobSchedule existing = new JobSchedule();
        existing.setId(1L);
        existing.setJobId(100L);
        when(jobScheduleMapper.findById(1L)).thenReturn(Optional.of(existing));

        Job syncJob = new Job();
        syncJob.setId(100L);
        syncJob.setCompanyId(1L);
        when(jobMapper.findById(100L)).thenReturn(Optional.of(syncJob));
        when(jobScheduleMapper.findByJobId(100L)).thenReturn(List.of());

        jobService.removeJobSchedule(1L);
        verify(jobScheduleMapper).delete(1L);
    }
}
