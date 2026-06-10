package com.parttime.platform.service;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.JobReportMapper;
import com.parttime.platform.pojo.cmd.ReviewJobReportCmd;
import com.parttime.platform.pojo.entity.JobReport;
import com.parttime.platform.pojo.vo.JobReportVO;
import com.parttime.platform.service.impl.JobReportServiceImpl;
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
    private JobReportMapper jobReportMapper;

    @InjectMocks
    private JobReportServiceImpl jobReportService;

    @Test
    void getJobReports_withoutStatus_shouldReturnAll() {
        JobReport report = createReport(1L, "PENDING");
        when(jobReportMapper.findAll()).thenReturn(List.of(report));

        List<JobReportVO> result = jobReportService.getJobReports(null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getJobReports_withStatus_shouldReturnFiltered() {
        JobReport report = createReport(1L, "PENDING");
        when(jobReportMapper.findByStatus("PENDING")).thenReturn(List.of(report));

        List<JobReportVO> result = jobReportService.getJobReports("PENDING");

        assertThat(result).hasSize(1);
    }

    @Test
    void getJobReport_shouldReturnReport() {
        JobReport report = createReport(1L, "PENDING");
        when(jobReportMapper.findById(1L)).thenReturn(Optional.of(report));

        JobReportVO response = jobReportService.getJobReport(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getJobId()).isEqualTo(100L);
    }

    @Test
    void getJobReport_notFound_shouldThrow() {
        when(jobReportMapper.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobReportService.getJobReport(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void dismissReport_shouldDismiss() {
        JobReport report = createReport(1L, "PENDING");
        when(jobReportMapper.findById(1L)).thenReturn(Optional.of(report));

        ReviewJobReportCmd cmd = new ReviewJobReportCmd();
        cmd.setRemark("No violation");

        JobReportVO response = jobReportService.dismissReport(1L, 1L, cmd);

        assertThat(response.getStatus()).isEqualTo("DISMISSED");
        assertThat(response.getReviewRemark()).isEqualTo("No violation");
        verify(jobReportMapper).update(any());
    }

    @Test
    void dismissReport_nonPending_shouldThrow() {
        JobReport report = createReport(1L, "DISMISSED");
        when(jobReportMapper.findById(1L)).thenReturn(Optional.of(report));

        ReviewJobReportCmd cmd = new ReviewJobReportCmd();

        assertThatThrownBy(() -> jobReportService.dismissReport(1L, 1L, cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not in PENDING");
    }

    @Test
    void banJobReport_shouldBan() {
        JobReport report = createReport(1L, "PENDING");
        when(jobReportMapper.findById(1L)).thenReturn(Optional.of(report));

        ReviewJobReportCmd cmd = new ReviewJobReportCmd();
        cmd.setRemark("Violates terms");

        JobReportVO response = jobReportService.banJobReport(1L, 1L, cmd);

        assertThat(response.getStatus()).isEqualTo("BANNED");
        assertThat(response.getReviewRemark()).isEqualTo("Violates terms");
        verify(jobReportMapper).update(any());
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
