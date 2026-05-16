package com.parttime.cservice.service;

import com.parttime.cservice.service.impl.JobServiceImpl;
import com.parttime.cservice.mapper.CompanyWorkerInsertMapper;
import com.parttime.cservice.pojo.vo.ApplicationVO;
import com.parttime.cservice.pojo.vo.JobDetailVO;
import com.parttime.cservice.pojo.vo.JobSummaryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobServiceTest {

    @InjectMocks
    private JobServiceImpl jobService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(jobService, "jobMapper", InMemoryMappers.createJobMapper());
        ReflectionTestUtils.setField(jobService, "jobApplicationMapper", InMemoryMappers.createJobApplicationMapper());
        ReflectionTestUtils.setField(jobService, "companyWorkerInsertMapper", new CompanyWorkerInsertMapper() {
            @Override
            public int upsert(Long companyId, Long workerId) {
                return 1;
            }
        });
        TestDataFactory.addSampleJobs(jobService);
    }

    @Test
    void searchJobs_withoutFilters_returnsAllPublishedJobs() {
        List<JobSummaryVO> results = jobService.searchJobs(null, null, null, null, null, null, null);

        assertThat(results).hasSize(3);
    }

    @Test
    void searchJobs_withKeyword_filtersCorrectly() {
        List<JobSummaryVO> results = jobService.searchJobs("engineer", null, null, null, null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).contains("Engineer");
    }

    @Test
    void searchJobs_withCategoryId_filtersCorrectly() {
        List<JobSummaryVO> results = jobService.searchJobs(null, 2L, null, null, null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCategoryName()).isEqualTo("Marketing");
    }

    @Test
    void searchJobs_withLocation_filtersCorrectly() {
        List<JobSummaryVO> results = jobService.searchJobs(null, null, "Shanghai", null, null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLocation()).isEqualTo("Shanghai");
    }

    @Test
    void searchJobs_withMinRate_filtersCorrectly() {
        List<JobSummaryVO> results = jobService.searchJobs(null, null, null, new BigDecimal("500.00"), null, null, null);

        assertThat(results).hasSize(1);
    }

    @Test
    void searchJobs_withMaxRate_filtersCorrectly() {
        List<JobSummaryVO> results = jobService.searchJobs(null, null, null, null, new BigDecimal("100.00"), null, null);

        assertThat(results).hasSize(2);
    }

    @Test
    void searchJobs_withCoordinates_sortsByDistance() {
        List<JobSummaryVO> results = jobService.searchJobs(null, null, null, null, null,
                new BigDecimal("39.9"), new BigDecimal("116.4"));

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getId()).isEqualTo(1L);
        assertThat(results.get(0).getDistanceKm()).isNotNull();
    }

    @Test
    void getJobDetail_returnsFullJobWithRatesAndSchedules() {
        JobDetailVO detail = jobService.getJobDetail(1L);

        assertThat(detail).isNotNull();
        assertThat(detail.getId()).isEqualTo(1L);
        assertThat(detail.getTitle()).isEqualTo("Software Engineer");
        assertThat(detail.getDescription()).isEqualTo("负责后端系统开发与维护");
        assertThat(detail.getLocation()).isEqualTo("Beijing");
        assertThat(detail.getCategoryName()).isEqualTo("Technology");
        assertThat(detail.getStatus()).isEqualTo("PUBLISHED");
        assertThat(detail.getCompanyName()).isEqualTo("美味餐饮管理有限公司");
        assertThat(detail.getHeadcount()).isEqualTo(10);
        assertThat(detail.getDeadline()).isNotNull();
        assertThat(detail.getRates()).isNotEmpty();
        assertThat(detail.getRates().get(0).getType()).isEqualTo("HOURLY");
        assertThat(detail.getRates().get(0).getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void getJobDetail_withNonExistentId_throwsException() {
        assertThatThrownBy(() -> jobService.getJobDetail(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void applyForJob_createsApplicationWithPendingStatus() {
        boolean result = jobService.applyForJob(100L, 1L, List.of(1L, 2L));

        assertThat(result).isTrue();

        List<ApplicationVO> statuses = jobService.getApplicationStatus(100L, 1L);
        assertThat(statuses).hasSize(1);
        assertThat(statuses.get(0).getStatus()).isEqualTo("PENDING");
        assertThat(statuses.get(0).getJobId()).isEqualTo(1L);
        assertThat(statuses.get(0).getAppliedAt()).isNotNull();
    }

    @Test
    void applyForJob_failsIfAlreadyApplied() {
        jobService.applyForJob(100L, 1L, List.of(1L));

        boolean result = jobService.applyForJob(100L, 1L, List.of(2L));

        assertThat(result).isFalse();
    }

    @Test
    void getApplicationStatus_returnsEmptyListWhenNoApplication() {
        List<ApplicationVO> statuses = jobService.getApplicationStatus(999L, 1L);

        assertThat(statuses).isEmpty();
    }
}
