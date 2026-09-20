package com.parttime.platform.service;

import com.parttime.platform.pojo.vo.JobReportVO;
import com.parttime.platform.service.impl.JobReportServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JobReportServiceTest {

    private final JobReportServiceImpl jobReportService = new JobReportServiceImpl();

    @Test
    void list_shouldReturnReports() {
        List<JobReportVO> all = jobReportService.list(null);
        assertThat(all).isNotEmpty();

        List<JobReportVO> filtered = jobReportService.list("PENDING");
        assertThat(filtered).isNotNull();
    }

    @Test
    void detail_shouldReturnReportWithGivenId() {
        JobReportVO vo = jobReportService.detail(1L);
        assertThat(vo.getId()).isEqualTo(1L);
    }

    @Test
    void dismiss_and_ban_shouldNotThrow() {
        assertDoesNotThrow(() -> jobReportService.dismiss(1L));
        assertDoesNotThrow(() -> jobReportService.ban(1L));
    }
}
