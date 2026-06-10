package com.parttime.enterprise.service;

import com.parttime.enterprise.enums.JobRateType;
import com.parttime.enterprise.enums.JobStatus;
import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.JobCategoryMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.JobRateMapper;
import com.parttime.enterprise.mapper.JobScheduleMapper;
import com.parttime.enterprise.mapper.JobTagMapper;
import com.parttime.enterprise.mapper.JobTagRelationMapper;
import com.parttime.enterprise.mapper.ScheduleApplicationMapper;
import com.parttime.enterprise.pojo.cmd.JobCreateCmd;
import com.parttime.enterprise.pojo.cmd.JobRateCmd;
import com.parttime.enterprise.pojo.cmd.JobScheduleCmd;
import com.parttime.enterprise.pojo.cmd.UpdateJobCmd;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobRate;
import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.pojo.entity.JobTag;
import com.parttime.enterprise.pojo.vo.JobRateVO;
import com.parttime.enterprise.pojo.vo.JobScheduleVO;
import com.parttime.enterprise.pojo.vo.JobVO;
import com.parttime.enterprise.service.impl.JobServiceImpl;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
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
    private ScheduleApplicationMapper scheduleApplicationMapper;

    @Mock
    private JobTagRelationMapper jobTagRelationMapper;

    @Mock
    private JobTagMapper jobTagMapper;

    @Mock
    private JobCategoryMapper jobCategoryMapper;

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
        when(jobScheduleMapper.findActiveByJobId(100L)).thenReturn(List.of(schedule));
        when(jobTagRelationMapper.findTagsByJobId(100L)).thenReturn(List.of());

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
    void getJobsByCompany_shouldIncludeApplicationCounts() {
        Job first = new Job();
        first.setId(1L);
        first.setCompanyId(1L);
        first.setTitle("First Job");
        first.setStatus("PUBLISHED");

        Job second = new Job();
        second.setId(2L);
        second.setCompanyId(1L);
        second.setTitle("Second Job");
        second.setStatus("PUBLISHED");

        when(jobMapper.findByCompanyId(1L)).thenReturn(List.of(first, second));
        when(jobRateMapper.findByJobId(1L)).thenReturn(List.of());
        when(jobRateMapper.findByJobId(2L)).thenReturn(List.of());
        when(jobScheduleMapper.findActiveByJobId(1L)).thenReturn(List.of());
        when(jobScheduleMapper.findActiveByJobId(2L)).thenReturn(List.of());
        when(jobTagRelationMapper.findTagsByJobId(1L)).thenReturn(List.of());
        when(jobTagRelationMapper.findTagsByJobId(2L)).thenReturn(List.of());
        when(scheduleApplicationMapper.countByJobId(1L)).thenReturn(8);
        when(scheduleApplicationMapper.countByJobId(2L)).thenReturn(3);
        when(scheduleApplicationMapper.countByJobIdAndStatus(1L, "PENDING")).thenReturn(2);
        when(scheduleApplicationMapper.countByJobIdAndStatus(2L, "PENDING")).thenReturn(1);

        List<JobVO> jobs = jobService.getJobsByCompany(1L, null, null, null);

        assertThat(jobs).hasSize(2);
        assertThat(jobs.get(0).getApplicationCount()).isEqualTo(8);
        assertThat(jobs.get(0).getPendingApplicationCount()).isEqualTo(2);
        assertThat(jobs.get(1).getApplicationCount()).isEqualTo(3);
        assertThat(jobs.get(1).getPendingApplicationCount()).isEqualTo(1);
    }

    @Test
    void deleteJob_shouldDeleteWhenClosedAndNoApplications() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("CLOSED");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));
        when(scheduleApplicationMapper.countByJobId(1L)).thenReturn(0);

        jobService.deleteJob(1L);

        verify(jobMapper).delete(1L);
    }

    @Test
    void deleteJob_shouldThrowWhenJobIsNotClosed() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("PUBLISHED");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.deleteJob(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("只有关闭后的职位才能删除");
    }

    @Test
    void deleteJob_shouldThrowWhenJobHasApplications() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("CLOSED");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));
        when(scheduleApplicationMapper.countByJobId(1L)).thenReturn(2);

        assertThatThrownBy(() -> jobService.deleteJob(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("已有报名记录的职位不能删除");
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
        assertThat(scheduleCaptor.getValue().getSlotsAvailable()).isEqualTo(3);
    }

    @Test
    void createJob_shouldPersistRequirementsContactPhoneAndDeduplicatedTagIds() {
        JobCreateCmd request = new JobCreateCmd();
        request.setCompanyId(1L);
        request.setTitle("Software Engineer");
        request.setDescription("<p>岗位职责</p>");
        request.setRequirements("<p>任职要求</p>");
        request.setContactPhone("13800138000");
        request.setCategoryId(10L);
        request.setHeadcount(3);
        request.setTagIds(List.of(2L, 2L, 1L));

        doAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            job.setId(100L);
            return 1;
        }).when(jobMapper).insert(any(Job.class));
        when(jobTagMapper.findActiveExistingIds(List.of(2L, 1L))).thenReturn(List.of(2L, 1L));
        JobTag tag1 = new JobTag();
        tag1.setId(2L);
        JobTag tag2 = new JobTag();
        tag2.setId(1L);
        when(jobTagRelationMapper.findTagsByJobId(100L)).thenReturn(List.of(tag1, tag2));

        JobVO response = jobService.createJob(request);

        verify(jobMapper).insert(jobCaptor.capture());
        Job savedJob = jobCaptor.getValue();
        assertThat(savedJob.getDescription()).isEqualTo("<p>岗位职责</p>");
        assertThat(savedJob.getRequirements()).isEqualTo("<p>任职要求</p>");
        assertThat(savedJob.getContactPhone()).isEqualTo("13800138000");
        verify(jobTagRelationMapper).deleteByJobId(100L);
        verify(jobTagRelationMapper).batchInsert(100L, List.of(2L, 1L));
        assertThat(response.getRequirements()).isEqualTo("<p>任职要求</p>");
        assertThat(response.getContactPhone()).isEqualTo("13800138000");
        assertThat(response.getTagIds()).containsExactly(2L, 1L);
    }

    @Test
    void updateJob_shouldUpdateRequirementsContactPhoneAndReplaceDeduplicatedTagIds() {
        Job existing = new Job();
        existing.setId(1L);
        existing.setCompanyId(1L);
        existing.setTitle("Old Title");
        existing.setDescription("Old Description");
        existing.setStatus("DRAFT");

        UpdateJobCmd request = new UpdateJobCmd();
        request.setRequirements("<p>新要求</p>");
        request.setContactPhone("13900139000");
        request.setTagIds(List.of(3L, 3L, 4L));

        when(jobMapper.findById(1L)).thenReturn(Optional.of(existing));
        when(jobTagMapper.findActiveExistingIds(List.of(3L, 4L))).thenReturn(List.of(3L, 4L));
        JobTag tag3 = new JobTag();
        tag3.setId(3L);
        JobTag tag4 = new JobTag();
        tag4.setId(4L);
        when(jobTagRelationMapper.findTagsByJobId(1L)).thenReturn(List.of(tag3, tag4));

        JobVO response = jobService.updateJob(1L, request);

        verify(jobMapper).update(existing);
        assertThat(existing.getRequirements()).isEqualTo("<p>新要求</p>");
        assertThat(existing.getContactPhone()).isEqualTo("13900139000");
        verify(jobTagRelationMapper).deleteByJobId(1L);
        verify(jobTagRelationMapper).batchInsert(1L, List.of(3L, 4L));
        assertThat(response.getRequirements()).isEqualTo("<p>新要求</p>");
        assertThat(response.getContactPhone()).isEqualTo("13900139000");
        assertThat(response.getTagIds()).containsExactly(3L, 4L);
    }

    @Test
    void createJob_shouldRejectInvalidOrDisabledTagIds() {
        JobCreateCmd request = new JobCreateCmd();
        request.setCompanyId(1L);
        request.setTitle("Software Engineer");
        request.setHeadcount(3);
        request.setTagIds(List.of(2L, 2L, 99L));

        doAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            job.setId(100L);
            return 1;
        }).when(jobMapper).insert(any(Job.class));
        when(jobTagMapper.findActiveExistingIds(List.of(2L, 99L))).thenReturn(List.of(2L));

        assertThatThrownBy(() -> jobService.createJob(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("存在无效或已停用的岗位标签");
    }

    @Test
    void updateJob_shouldUpdateJobReplaceChildren() {
        Job existing = new Job();
        existing.setId(1L);
        existing.setCompanyId(1L);
        existing.setTitle("Old Title");
        existing.setDescription("Old Description");
        existing.setLocation("Old Location");
        existing.setStatus("DRAFT");

        JobRateCmd rateRequest = new JobRateCmd();
        rateRequest.setType(JobRateType.DAILY);
        rateRequest.setAmount(new BigDecimal("300.00"));
        rateRequest.setCurrency("CNY");

        JobSchedule existingSchedule = new JobSchedule();
        existingSchedule.setId(10L);
        existingSchedule.setJobId(1L);
        existingSchedule.setScheduleDate(LocalDate.of(2026, 6, 1));
        existingSchedule.setStartTime(LocalTime.of(9, 0));
        existingSchedule.setEndTime(LocalTime.of(18, 0));
        existingSchedule.setSlotsAvailable(8);

        JobScheduleCmd scheduleRequest = new JobScheduleCmd();
        scheduleRequest.setId(10L);
        scheduleRequest.setScheduleDate(LocalDate.of(2026, 6, 2));
        scheduleRequest.setStartTime(LocalTime.of(10, 0));
        scheduleRequest.setEndTime(LocalTime.of(19, 0));
        scheduleRequest.setSlotsAvailable(8);

        UpdateJobCmd request = new UpdateJobCmd();
        request.setTitle("New Title");
        request.setDescription("New Description");
        request.setLocation("New Location");
        request.setCategoryId(10L);
        request.setHeadcount(5);
        request.setDeadline(LocalDateTime.of(2026, 7, 1, 23, 59, 59));
        request.setRates(List.of(rateRequest));
        request.setSchedules(List.of(scheduleRequest));

        when(jobMapper.findById(1L)).thenReturn(Optional.of(existing));
        when(jobScheduleMapper.findByJobId(1L)).thenReturn(List.of(existingSchedule));

        JobVO response = jobService.updateJob(1L, request);

        assertThat(response.getTitle()).isEqualTo("New Title");
        verify(jobMapper).update(existing);
        verify(jobRateMapper).deleteByJobId(1L);
        verify(jobRateMapper).insert(rateCaptor.capture());
        assertThat(rateCaptor.getValue().getType()).isEqualTo("DAILY");
        verify(jobScheduleMapper, never()).deleteByJobId(1L);
        verify(jobScheduleMapper, never()).insert(any(JobSchedule.class));
        verify(jobScheduleMapper).update(scheduleCaptor.capture());
        assertThat(scheduleCaptor.getValue().getId()).isEqualTo(10L);
        assertThat(scheduleCaptor.getValue().getScheduleDate()).isEqualTo(LocalDate.of(2026, 6, 2));
    }

    @Test
    void publishJob_shouldTransitionFromDraftToPublished() {
        Job job = new Job();
        job.setId(1L);
        job.setCompanyId(1L);
        job.setStatus("DRAFT");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));

        JobVO response = jobService.publishJob(1L);

        assertThat(response.getStatus()).isEqualTo(JobStatus.PUBLISHED);
        verify(jobMapper).updateStatus(1L, "PUBLISHED");
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
        schedule.setStatus("ACTIVE");

        when(jobScheduleMapper.findActiveByJobId(100L)).thenReturn(List.of(schedule));

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

        Job job = new Job();
        job.setId(100L);
        job.setHeadcount(10);
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
        doAnswer(invocation -> {
            JobSchedule s = invocation.getArgument(0);
            s.setId(99L);
            return 1;
        }).when(jobScheduleMapper).insert(any(JobSchedule.class));

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

        Job job = new Job();
        job.setId(100L);
        job.setHeadcount(8);
        when(jobScheduleMapper.findById(1L)).thenReturn(Optional.of(existing));
        when(jobMapper.findById(100L)).thenReturn(Optional.of(job));

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

        jobService.removeJobSchedule(1L);
        verify(jobScheduleMapper).delete(1L);
    }
}
