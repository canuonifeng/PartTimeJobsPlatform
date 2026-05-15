package com.parttime.enterprise.core.service;

import com.parttime.enterprise.api.dto.ApplicationStatus;
import com.parttime.enterprise.api.dto.JobApplicationResponse;
import com.parttime.enterprise.core.domain.Job;
import com.parttime.enterprise.core.domain.JobApplication;
import com.parttime.enterprise.core.exception.BusinessException;
import com.parttime.enterprise.core.repository.JobApplicationRepository;
import com.parttime.enterprise.core.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationService(applicationRepository, jobRepository);
    }

    @Test
    void getApplicationsByJob_shouldReturnList() {
        JobApplication app = new JobApplication();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus("PENDING");
        app.setAppliedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        when(applicationRepository.findByJobId(100L)).thenReturn(List.of(app));

        List<JobApplicationResponse> result = applicationService.getApplicationsByJob(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getJobId()).isEqualTo(100L);
        assertThat(result.get(0).getWorkerId()).isEqualTo(10L);
        assertThat(result.get(0).getStatus()).isEqualTo(ApplicationStatus.PENDING);
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

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));
        when(applicationRepository.countByJobIdAndStatus(100L, "ACCEPTED")).thenReturn(2);

        JobApplicationResponse result = applicationService.acceptApplication(1L);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        verify(applicationRepository).updateStatus(1L, "ACCEPTED");
    }

    @Test
    void rejectApplication_shouldChangeStatusToRejected() {
        JobApplication app = new JobApplication();
        app.setId(1L);
        app.setJobId(100L);
        app.setWorkerId(10L);
        app.setStatus("PENDING");

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));

        JobApplicationResponse result = applicationService.rejectApplication(1L);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        verify(applicationRepository).updateStatus(1L, "REJECTED");
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

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));
        when(applicationRepository.countByJobIdAndStatus(100L, "ACCEPTED")).thenReturn(5);

        assertThatThrownBy(() -> applicationService.acceptApplication(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("岗位已录满");
    }
}
