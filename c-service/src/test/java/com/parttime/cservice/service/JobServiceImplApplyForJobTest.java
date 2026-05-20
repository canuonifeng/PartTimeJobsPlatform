package com.parttime.cservice.service;

import com.parttime.cservice.mapper.CompanyWorkerInsertMapper;
import com.parttime.cservice.mapper.JobApplicationMapper;
import com.parttime.cservice.mapper.JobMapper;
import com.parttime.cservice.pojo.entity.Job;
import com.parttime.cservice.pojo.entity.JobApplication;
import com.parttime.cservice.service.impl.JobServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceImplApplyForJobTest {

    @Mock
    private JobMapper jobMapper;

    @Mock
    private JobApplicationMapper jobApplicationMapper;

    @Mock
    private CompanyWorkerInsertMapper companyWorkerInsertMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    @Test
    void applyForJob_shouldSkipCompanyWorkerLinkWhenCompanyIdMissing() {
        Job job = new Job();
        job.setId(1L);
        job.setJobId(1L);
        job.setCompanyId(null);

        when(jobApplicationMapper.findByWorkerIdAndJobId(100L, 1L)).thenReturn(List.of());
        when(jobMapper.findByJobId(1L)).thenReturn(Optional.of(job));

        boolean result = jobService.applyForJob(100L, 1L, List.of());

        assertThat(result).isTrue();
        verify(companyWorkerInsertMapper, never()).upsert(anyLong(), anyLong());
    }

    @Test
    void applyForJob_shouldPersistApplicationWithoutCompanyId() {
        Job job = new Job();
        job.setId(1L);
        job.setJobId(1L);
        job.setCompanyId(88L);

        when(jobApplicationMapper.findByWorkerIdAndJobId(100L, 1L)).thenReturn(List.of());
        when(jobMapper.findByJobId(1L)).thenReturn(Optional.of(job));
        doAnswer(invocation -> 1).when(jobApplicationMapper).insert(any(JobApplication.class));

        boolean result = jobService.applyForJob(100L, 1L, List.of());

        assertThat(result).isTrue();
    }
}
