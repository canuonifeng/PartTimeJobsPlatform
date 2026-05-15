package com.parttime.platform.core.service;

import com.parttime.platform.api.dto.JobReportResponse;
import com.parttime.platform.api.dto.JobReportReviewRequest;
import com.parttime.platform.core.domain.JobReport;
import com.parttime.platform.core.exception.BusinessException;
import com.parttime.platform.core.repository.JobReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobReportServiceTest {

    @Mock
    private JobReportRepository jobReportRepository;

    @InjectMocks
    private JobReportService jobReportService;

    @Test
    void getJobReports_withoutStatus_shouldReturnAll() {
        JobReport report = createReport(1L, "PENDING");
        when(jobReportRepository.findAll()).thenReturn(List.of(report));

        List<JobReportResponse> result = jobReportService.getJobReports(null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getJobReports_withStatus_shouldReturnFiltered() {
        JobReport report = createReport(1L, "PENDING");
        when(jobReportRepository.findByStatus("PENDING")).thenReturn(List.of(report));

        List<JobReportResponse> result = jobReportService.getJobReports("PENDING");

        assertThat(result).hasSize(1);
    }

    @Test
    void getJobReport_shouldReturnReport() {
        JobReport report = createReport(1L, "PENDING");
        when(jobReportRepository.findById(1L)).thenReturn(Optional.of(report));

        JobReportResponse response = jobReportService.getJobReport(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getJobId()).isEqualTo(100L);
    }

    @Test
    void getJobReport_notFound_shouldThrow() {
        when(jobReportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobReportService.getJobReport(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void dismissReport_shouldDismiss() {
        JobReport report = createReport(1L, "PENDING");
        when(jobReportRepository.findById(1L)).thenReturn(Optional.of(report));

        JobReportReviewRequest request = new JobReportReviewRequest();
        request.setRemark("No violation");

        JobReportResponse response = jobReportService.dismissReport(1L, "admin", request);

        assertThat(response.getStatus()).isEqualTo("DISMISSED");
        assertThat(response.getReviewRemark()).isEqualTo("No violation");
        verify(jobReportRepository).update(any());
    }

    @Test
    void dismissReport_nonPending_shouldThrow() {
        JobReport report = createReport(1L, "DISMISSED");
        when(jobReportRepository.findById(1L)).thenReturn(Optional.of(report));

        JobReportReviewRequest request = new JobReportReviewRequest();

        assertThatThrownBy(() -> jobReportService.dismissReport(1L, "admin", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not in PENDING");
    }

    @Test
    void banJobReport_shouldBan() {
        JobReport report = createReport(1L, "PENDING");
        when(jobReportRepository.findById(1L)).thenReturn(Optional.of(report));

        JobReportReviewRequest request = new JobReportReviewRequest();
        request.setRemark("Violates terms");

        JobReportResponse response = jobReportService.banJobReport(1L, "admin", request);

        assertThat(response.getStatus()).isEqualTo("BANNED");
        assertThat(response.getReviewRemark()).isEqualTo("Violates terms");
        verify(jobReportRepository).update(any());
    }

    private JobReport createReport(Long id, String status) {
        JobReport report = new JobReport();
        report.setId(id);
        report.setJobId(100L);
        report.setReporterId(200L);
        report.setReason("Spam");
        report.setDescription("This job is spam");
        report.setStatus(status);
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        return report;
    }
}
