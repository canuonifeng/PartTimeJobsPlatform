package com.parttime.cservice.service;

import com.parttime.cservice.service.impl.JobServiceImpl;
import com.parttime.cservice.mapper.CompanyWorkerInsertMapper;
import com.parttime.cservice.mapper.JobMapper;
import com.parttime.cservice.mapper.JobTagRelationMapper;
import com.parttime.cservice.mapper.ScheduleApplicationMapper;
import com.parttime.cservice.pojo.entity.Job;
import com.parttime.cservice.pojo.entity.ScheduleApplication;
import com.parttime.cservice.pojo.vo.JobDetailVO;
import com.parttime.cservice.pojo.vo.JobSummaryVO;
import com.parttime.cservice.pojo.vo.JobTagVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobServiceTest {

    @InjectMocks
    private JobServiceImpl jobService;
    private CountingJobTagRelationMapper jobTagRelationMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jobTagRelationMapper = new CountingJobTagRelationMapper();
        ReflectionTestUtils.setField(jobService, "jobMapper", InMemoryMappers.createJobMapper());
        ReflectionTestUtils.setField(jobService, "jobTagRelationMapper", jobTagRelationMapper);
        ReflectionTestUtils.setField(jobService, "scheduleApplicationMapper", InMemoryMappers.createScheduleApplicationMapper());
        ReflectionTestUtils.setField(jobService, "jobScheduleMapper", InMemoryMappers.createJobScheduleMapper());
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
        assertThat(detail.getHeadcount()).isEqualTo(10);
        assertThat(detail.getDeadline()).isNotNull();
        assertThat(detail.getRates()).isNotEmpty();
        assertThat(detail.getRates().get(0).getType()).isEqualTo("HOURLY");
        assertThat(detail.getRates().get(0).getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void getJobDetail_returnsRequirementsContactPhoneAndTags() {
        Job taggedJob = new Job();
        taggedJob.setId(20L);
        taggedJob.setJobId(20L);
        taggedJob.setTitle("Tagged Job");
        taggedJob.setDescription("负责门店运营");
        taggedJob.setRequirements("需要健康证");
        taggedJob.setContactPhone("13800138000");
        taggedJob.setStatus("PUBLISHED");
        jobTagRelationMapper.addTag(20L, "日结");
        jobTagRelationMapper.addTag(20L, "按时");
        JobMapper jobMapper = (JobMapper) ReflectionTestUtils.getField(jobService, "jobMapper");
        jobMapper.insert(taggedJob);

        JobDetailVO detail = jobService.getJobDetail(20L);

        assertThat(detail.getRequirements()).isEqualTo("需要健康证");
        assertThat(detail.getContactPhone()).isEqualTo("13800138000");
        assertThat(detail.getTags()).extracting(JobTagVO::getName).containsExactly("日结", "按时");
        assertThat(jobTagRelationMapper.singleFetchCount).isEqualTo(1);
    }

    @Test
    void searchJobs_fetchesTagsInOneBatch() {
        jobTagRelationMapper.addTag(1L, "日结");
        jobTagRelationMapper.addTag(2L, "按时");

        List<JobSummaryVO> results = jobService.searchJobs(null, null, null, null, null, null, null);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getTags()).extracting(JobTagVO::getName).containsExactly("日结");
        assertThat(results.get(1).getTags()).extracting(JobTagVO::getName).containsExactly("按时");
        assertThat(jobTagRelationMapper.batchFetchCount).isEqualTo(1);
        assertThat(jobTagRelationMapper.singleFetchCount).isZero();
        assertThat(jobTagRelationMapper.lastBatchJobIds).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void searchJobs_doesNotFetchTagsWhenNoJobsMatch() {
        List<JobSummaryVO> results = jobService.searchJobs("missing", null, null, null, null, null, null);

        assertThat(results).isEmpty();
        assertThat(jobTagRelationMapper.batchFetchCount).isZero();
        assertThat(jobTagRelationMapper.singleFetchCount).isZero();
    }

    @Test
    void getJobDetail_withWorkerId_setsAppliedScheduleIdsWhenApplicationExists() {
        jobService.applyForJob(100L, 1L, List.of(1L));

        JobDetailVO detail = jobService.getJobDetail(1L, 100L);

        assertThat(detail.getAppliedScheduleIds()).containsExactly(1L);
    }

    @Test
    void getJobDetail_returnsRemainingSlotsForSchedules() {
        ScheduleApplication application = new ScheduleApplication();
        application.setScheduleId(4L);
        application.setWorkerId(100L);
        application.setStatus("PENDING");
        ScheduleApplicationMapper mapper = (ScheduleApplicationMapper) ReflectionTestUtils.getField(jobService, "scheduleApplicationMapper");
        mapper.insert(application);

        JobDetailVO detail = jobService.getJobDetail(3L, 200L);

        assertThat(detail.getSchedules())
                .filteredOn(schedule -> schedule.getId().equals(4L))
                .singleElement()
                .satisfies(schedule -> assertThat(schedule.getRemainingSlots()).isEqualTo(schedule.getSlotsAvailable() - 1));
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

        List<ScheduleApplication> statuses = jobService.getApplicationStatus(100L, 1L);
        assertThat(statuses).hasSize(2);
        assertThat(statuses.get(0).getStatus()).isEqualTo("PENDING");
        assertThat(statuses.get(0).getScheduleId()).isEqualTo(1L);
    }

    @Test
    void applyForJob_allowsDifferentScheduleAfterAlreadyApplied() {
        jobService.applyForJob(100L, 1L, List.of(1L));

        boolean result = jobService.applyForJob(100L, 1L, List.of(2L));

        assertThat(result).isTrue();
        assertThat(jobService.getApplicationStatus(100L, 1L)).hasSize(2);
    }

    @Test
    void getApplicationStatus_returnsEmptyListWhenNoApplication() {
        List<ScheduleApplication> statuses = jobService.getApplicationStatus(999L, 1L);

        assertThat(statuses).isEmpty();
    }

    private static class CountingJobTagRelationMapper implements JobTagRelationMapper {
        private final List<JobTagVO> tags = new ArrayList<>();
        private int singleFetchCount;
        private int batchFetchCount;
        private List<Long> lastBatchJobIds = List.of();

        void addTag(Long jobId, String name) {
            JobTagVO tag = new JobTagVO();
            tag.setId((long) tags.size() + 1);
            tag.setJobId(jobId);
            tag.setName(name);
            tag.setStatus("ACTIVE");
            tags.add(tag);
        }

        @Override
        public List<JobTagVO> findTagsByJobId(Long jobId) {
            singleFetchCount++;
            return tags.stream()
                    .filter(tag -> jobId.equals(tag.getJobId()))
                    .toList();
        }

        @Override
        public List<JobTagVO> findTagsByJobIds(List<Long> jobIds) {
            batchFetchCount++;
            lastBatchJobIds = jobIds;
            return tags.stream()
                    .filter(tag -> jobIds.contains(tag.getJobId()))
                    .toList();
        }
    }
}
