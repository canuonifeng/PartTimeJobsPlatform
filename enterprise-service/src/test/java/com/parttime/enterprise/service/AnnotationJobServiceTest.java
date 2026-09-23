package com.parttime.enterprise.service;

import com.parttime.enterprise.enums.JobRateType;
import com.parttime.enterprise.enums.JobStatus;
import com.parttime.enterprise.enums.PricingMode;
import com.parttime.enterprise.enums.TaskType;
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
import com.parttime.enterprise.pojo.cmd.UpdateJobCmd;
import com.parttime.enterprise.pojo.entity.Job;
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
class AnnotationJobServiceTest {

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

    @InjectMocks
    private JobServiceImpl jobService;

    @Captor
    private ArgumentCaptor<Job> jobCaptor;

    private JobCreateCmd annotationRequest() {
        JobCreateCmd request = new JobCreateCmd();
        request.setCompanyId(1L);
        request.setTitle("图片分类标注");
        request.setDescription("标注任务包");
        request.setCategoryId(10L);
        request.setTaskType(TaskType.ANNOTATION);
        request.setPricingMode(PricingMode.PER_ITEM);
        request.setPricePerUnit(new BigDecimal("0.50"));
        request.setTotalItems(1000);
        request.setExternalTaskId("EXT_TASK_001");
        request.setExternalSystemType("EXT_SYS");
        JobRateCmd rate = new JobRateCmd();
        rate.setType(JobRateType.HOURLY);
        rate.setAmount(new BigDecimal("30.00"));
        request.setRates(List.of(rate));
        return request;
    }

    @Test
    void createAnnotationJob_shouldPersistAnnotationFields() {
        doAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            job.setId(200L);
            return 1;
        }).when(jobMapper).insert(any(Job.class));

        JobVO response = jobService.createJob(annotationRequest());

        assertThat(response.getId()).isEqualTo(200L);
        assertThat(response.getTaskType()).isEqualTo(TaskType.ANNOTATION);
        assertThat(response.getPricingMode()).isEqualTo(PricingMode.PER_ITEM);
        assertThat(response.getPricePerUnit()).isEqualByComparingTo(new BigDecimal("0.50"));
        assertThat(response.getTotalItems()).isEqualTo(1000);
        assertThat(response.getExternalTaskId()).isEqualTo("EXT_TASK_001");
        assertThat(response.getExternalSystemType()).isEqualTo("EXT_SYS");

        verify(jobMapper).insert(jobCaptor.capture());
        Job saved = jobCaptor.getValue();
        assertThat(saved.getTaskType()).isEqualTo("ANNOTATION");
        assertThat(saved.getPricingMode()).isEqualTo("PER_ITEM");
        assertThat(saved.getTotalItems()).isEqualTo(1000);
        assertThat(saved.getExternalTaskId()).isEqualTo("EXT_TASK_001");

        verify(jobRateMapper).insert(any());
    }

    @Test
    void createAnnotationJob_missingTotalItems_shouldReject() {
        JobCreateCmd request = annotationRequest();
        request.setTotalItems(null);

        assertThatThrownBy(() -> jobService.createJob(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("总数量必须大于0");
    }

    @Test
    void createAnnotationJob_missingRates_shouldReject() {
        JobCreateCmd request = annotationRequest();
        request.setRates(null);

        assertThatThrownBy(() -> jobService.createJob(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("薪资标准");
    }

    @Test
    void createAnnotationJob_withoutPricingMode_shouldSucceed() {
        doAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            job.setId(200L);
            return 1;
        }).when(jobMapper).insert(any(Job.class));

        JobCreateCmd request = annotationRequest();
        request.setPricingMode(null);
        request.setPricePerUnit(null);

        JobVO response = jobService.createJob(request);

        assertThat(response.getId()).isEqualTo(200L);
        assertThat(response.getTaskType()).isEqualTo(TaskType.ANNOTATION);
        verify(jobMapper).insert(any(Job.class));
    }

    @Test
    void createAnnotationJob_zeroTotalItems_shouldReject() {
        JobCreateCmd request = annotationRequest();
        request.setTotalItems(0);

        assertThatThrownBy(() -> jobService.createJob(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("总数量必须大于0");
    }

    @Test
    void updateAnnotationJob_missingRates_shouldReject() {
        Job job = new Job();
        job.setId(200L);
        job.setCompanyId(1L);
        job.setTitle("图片分类标注");
        job.setStatus("DRAFT");
        job.setTaskType("ANNOTATION");
        when(jobMapper.findById(200L)).thenReturn(Optional.of(job));

        UpdateJobCmd request = new UpdateJobCmd();
        request.setRates(List.of());

        assertThatThrownBy(() -> jobService.updateJob(200L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("薪资标准");
    }

    @Test
    void updateAnnotationJob_withoutRates_shouldSucceed() {
        Job job = new Job();
        job.setId(200L);
        job.setCompanyId(1L);
        job.setTitle("图片分类标注");
        job.setStatus("DRAFT");
        job.setTaskType("ANNOTATION");
        when(jobMapper.findById(200L)).thenReturn(Optional.of(job));

        UpdateJobCmd request = new UpdateJobCmd();
        request.setTitle("更新后的标题");

        jobService.updateJob(200L, request);

        verify(jobMapper).update(any(Job.class));
    }

    @Test
    void updateAnnotationJob_zeroTotalItems_shouldReject() {
        Job job = new Job();
        job.setId(200L);
        job.setCompanyId(1L);
        job.setTitle("图片分类标注");
        job.setStatus("DRAFT");
        job.setTaskType("ANNOTATION");
        when(jobMapper.findById(200L)).thenReturn(Optional.of(job));

        UpdateJobCmd request = new UpdateJobCmd();
        request.setTotalItems(0);

        assertThatThrownBy(() -> jobService.updateJob(200L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("总数量必须大于0");
    }

    @Test
    void updateAnnotationJob_totalItems_shouldPersist() {
        Job job = new Job();
        job.setId(200L);
        job.setCompanyId(1L);
        job.setTitle("图片分类标注");
        job.setStatus("DRAFT");
        job.setTaskType("ANNOTATION");
        when(jobMapper.findById(200L)).thenReturn(Optional.of(job));

        UpdateJobCmd request = new UpdateJobCmd();
        request.setTotalItems(2000);

        jobService.updateJob(200L, request);

        verify(jobMapper).update(jobCaptor.capture());
        assertThat(jobCaptor.getValue().getTotalItems()).isEqualTo(2000);
    }

    @Test
    void updateAnnotationJob_withValidRates_shouldReplaceRates() {
        Job job = new Job();
        job.setId(200L);
        job.setCompanyId(1L);
        job.setTitle("图片分类标注");
        job.setStatus("DRAFT");
        job.setTaskType("ANNOTATION");
        when(jobMapper.findById(200L)).thenReturn(Optional.of(job));

        JobRateCmd rate = new JobRateCmd();
        rate.setType(JobRateType.HOURLY);
        rate.setAmount(new BigDecimal("30.00"));
        UpdateJobCmd request = new UpdateJobCmd();
        request.setRates(List.of(rate));

        jobService.updateJob(200L, request);

        verify(jobRateMapper).deleteByJobId(200L);
        verify(jobRateMapper).insert(any());
    }

    @Test
    void publishAnnotationJob_shouldChangeStatusToPublished() {
        Job job = new Job();
        job.setId(200L);
        job.setCompanyId(1L);
        job.setTitle("图片分类标注");
        job.setStatus("DRAFT");
        job.setTaskType("ANNOTATION");
        when(jobMapper.findById(200L)).thenReturn(Optional.of(job));

        jobService.publishJob(200L);

        verify(jobMapper).updateStatus(200L, "PUBLISHED");
    }

    @Test
    void closeAnnotationJob_shouldChangeStatusToClosed() {
        Job job = new Job();
        job.setId(200L);
        job.setCompanyId(1L);
        job.setTitle("图片分类标注");
        job.setStatus("PUBLISHED");
        job.setTaskType("ANNOTATION");
        when(jobMapper.findById(200L)).thenReturn(Optional.of(job));

        jobService.closeJob(200L);

        verify(jobMapper).updateStatus(200L, "CLOSED");
    }

    @Test
    void listJobs_withStatusAndTaskType_shouldUseTaskTypeQuery() {
        Job job = new Job();
        job.setId(200L);
        job.setTitle("图片分类标注");
        job.setTaskType("ANNOTATION");
        job.setStatus("PUBLISHED");
        when(jobMapper.findByCompanyIdAndStatusAndTaskType(1L, "PUBLISHED", "ANNOTATION"))
                .thenReturn(List.of(job));

        List<JobVO> jobs = jobService.getJobsByCompany(1L, "PUBLISHED", "ANNOTATION", null, null);

        assertThat(jobs).hasSize(1);
        assertThat(jobs.get(0).getTaskType()).isEqualTo(TaskType.ANNOTATION);
        verify(jobMapper, never()).findByCompanyIdAndStatus(any(), any());
        verify(jobMapper, never()).findByCompanyId(any());
    }

    @Test
    void listJobs_withoutTaskType_shouldFallBackToStatusQuery() {
        Job job = new Job();
        job.setId(1L);
        job.setTitle("普通零工");
        job.setTaskType("WORK");
        job.setStatus("PUBLISHED");
        when(jobMapper.findByCompanyIdAndStatus(1L, "PUBLISHED")).thenReturn(List.of(job));

        List<JobVO> jobs = jobService.getJobsByCompany(1L, "PUBLISHED", null, null, null);

        assertThat(jobs).hasSize(1);
        assertThat(jobs.get(0).getTaskType()).isEqualTo(TaskType.WORK);
    }
}
