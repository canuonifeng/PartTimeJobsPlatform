package com.parttime.cservice.service;

import com.parttime.cservice.service.impl.JobServiceImpl;
import com.parttime.cservice.mapper.CompanyWorkerInsertMapper;
import com.parttime.cservice.mapper.EnterpriseMapper;
import com.parttime.cservice.mapper.JobCategoryMapper;
import com.parttime.cservice.mapper.JobMapper;
import com.parttime.cservice.mapper.JobRateMapper;
import com.parttime.cservice.mapper.JobTagRelationMapper;
import com.parttime.cservice.mapper.NotificationMapper;
import com.parttime.cservice.mapper.ScheduleApplicationMapper;
import com.parttime.cservice.mapper.ShiftMapper;
import com.parttime.cservice.mapper.SystemConfigMapper;
import com.parttime.cservice.pojo.entity.Enterprise;
import com.parttime.cservice.pojo.entity.Job;
import com.parttime.cservice.pojo.entity.JobCategory;
import com.parttime.cservice.pojo.entity.JobRate;
import com.parttime.cservice.pojo.entity.ScheduleApplication;
import com.parttime.cservice.pojo.vo.JobDetailVO;
import com.parttime.cservice.pojo.vo.JobSummaryVO;
import com.parttime.cservice.pojo.vo.JobTagVO;
import com.parttime.cservice.pojo.vo.PageVO;
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
        ReflectionTestUtils.setField(jobService, "systemConfigMapper", InMemoryMappers.createSystemConfigMapper());
        ReflectionTestUtils.setField(jobService, "shiftMapper", InMemoryMappers.createShiftMapper());
        ReflectionTestUtils.setField(jobService, "notificationMapper", InMemoryMappers.createNotificationMapper());
        ReflectionTestUtils.setField(jobService, "jobTagGroupMapper", InMemoryMappers.createJobTagGroupMapper());
        EnterpriseMapper enterpriseMapper = InMemoryMappers.createEnterpriseMapper();
        JobRateMapper jobRateMapper = InMemoryMappers.createJobRateMapper();
        JobCategoryMapper jobCategoryMapper = InMemoryMappers.createJobCategoryMapper();
        ReflectionTestUtils.setField(jobService, "enterpriseMapper", enterpriseMapper);
        ReflectionTestUtils.setField(jobService, "jobRateMapper", jobRateMapper);
        ReflectionTestUtils.setField(jobService, "jobCategoryMapper", jobCategoryMapper);

        // Pre-populate enterprises for demo jobs (companyId 1, 2, 3)
        for (long i = 1; i <= 3; i++) {
            Enterprise e = new Enterprise();
            e.setId(i);
            e.setCompanyName("Company " + i);
            enterpriseMapper.insert(e);
        }

        // Pre-populate categories for demo jobs (categoryId 1, 2)
        JobCategory cat1 = new JobCategory();
        cat1.setId(1L);
        cat1.setName("Technology");
        jobCategoryMapper.insert(cat1);
        JobCategory cat2 = new JobCategory();
        cat2.setId(2L);
        cat2.setName("Marketing");
        jobCategoryMapper.insert(cat2);

        TestDataFactory.addSampleJobs(jobService);

        // Pre-populate job rates for the 3 demo jobs
        insertJobRate(jobRateMapper, 1L, "HOURLY", new BigDecimal("50.00"));
        insertJobRate(jobRateMapper, 1L, "DAILY", new BigDecimal("400.00"));
        insertJobRate(jobRateMapper, 2L, "HOURLY", new BigDecimal("80.00"));
        insertJobRate(jobRateMapper, 3L, "DAILY", new BigDecimal("600.00"));
    }

    private void insertJobRate(JobRateMapper mapper, Long jobId, String type, BigDecimal amount) {
        JobRate rate = new JobRate();
        rate.setJobId(jobId);
        rate.setType(type);
        rate.setAmount(amount);
        rate.setCurrency("CNY");
        mapper.insert(rate);
    }

    @Test
    void searchJobs_withoutFilters_returnsAllPublishedJobs() {
        PageVO<JobSummaryVO> page = jobService.searchJobs(null, null, null, null, null, null, null, null, null, 1, 10);

        assertThat(page.getRecords()).hasSize(3);
        assertThat(page.getTotal()).isEqualTo(3);
    }

    @Test
    void searchJobs_withKeyword_filtersCorrectly() {
        PageVO<JobSummaryVO> page = jobService.searchJobs("engineer", null, null, null, null, null, null, null, null, 1, 10);

        assertThat(page.getRecords()).hasSize(1);
        assertThat(page.getRecords().get(0).getTitle()).contains("Engineer");
    }

    @Test
    void searchJobs_withCategoryId_filtersCorrectly() {
        PageVO<JobSummaryVO> page = jobService.searchJobs(null, 2L, null, null, null, null, null, null, null, 1, 10);

        assertThat(page.getRecords()).hasSize(1);
        assertThat(page.getRecords().get(0).getCategoryName()).isEqualTo("Marketing");
    }

    @Test
    void searchJobs_withLocation_filtersCorrectly() {
        PageVO<JobSummaryVO> page = jobService.searchJobs(null, null, "Shanghai", null, null, null, null, null, null, 1, 10);

        assertThat(page.getRecords()).hasSize(1);
        assertThat(page.getRecords().get(0).getLocation()).isEqualTo("Shanghai");
    }

    @Test
    void searchJobs_withMinRate_filtersCorrectly() {
        PageVO<JobSummaryVO> page = jobService.searchJobs(null, null, null, new BigDecimal("500.00"), null, null, null, null, null, 1, 10);

        assertThat(page.getRecords()).hasSize(1);
    }

    @Test
    void searchJobs_withMaxRate_filtersCorrectly() {
        PageVO<JobSummaryVO> page = jobService.searchJobs(null, null, null, null, new BigDecimal("100.00"), null, null, null, null, 1, 10);

        assertThat(page.getRecords()).hasSize(2);
    }

    @Test
    void searchJobs_withCoordinates_sortsByDistance() {
        PageVO<JobSummaryVO> page = jobService.searchJobs(null, null, null, null, null,
                new BigDecimal("39.9"), new BigDecimal("116.4"), null, null, 1, 10);

        assertThat(page.getRecords()).hasSize(3);
        assertThat(page.getRecords().get(0).getId()).isEqualTo(1L);
        assertThat(page.getRecords().get(0).getDistanceKm()).isNotNull();
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
        taggedJob.setCompanyId(1L);
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

        PageVO<JobSummaryVO> page = jobService.searchJobs(null, null, null, null, null, null, null, null, null, 1, 10);

        assertThat(page.getRecords()).hasSize(3);
        assertThat(page.getRecords().get(0).getTags()).extracting(JobTagVO::getName).containsExactly("日结");
        assertThat(page.getRecords().get(1).getTags()).extracting(JobTagVO::getName).containsExactly("按时");
        assertThat(jobTagRelationMapper.batchFetchCount).isEqualTo(1);
        assertThat(jobTagRelationMapper.singleFetchCount).isZero();
        assertThat(jobTagRelationMapper.lastBatchJobIds).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void searchJobs_doesNotFetchTagsWhenNoJobsMatch() {
        PageVO<JobSummaryVO> page = jobService.searchJobs("missing", null, null, null, null, null, null, null, null, 1, 10);

        assertThat(page.getRecords()).isEmpty();
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
