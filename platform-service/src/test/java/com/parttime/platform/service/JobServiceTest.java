package com.parttime.platform.service;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.JobMapper;
import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.entity.Job;
import com.parttime.platform.pojo.vo.JobVO;
import com.parttime.platform.service.impl.JobServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    @Test
    void list_shouldReturnJobList() {
        Job job = new Job();
        job.setId(1L);
        job.setTitle("Test Job");
        job.setCompanyName("Test Company");
        job.setStatus("ACTIVE");

        when(jobMapper.findByFilters(any(), any(), any(), any())).thenReturn(List.of(job));

        JobQueryCmd cmd = new JobQueryCmd();
        cmd.setStatus("ACTIVE");
        List<JobVO> result = jobService.list(cmd);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Job");
        assertThat(result.get(0).getCompanyName()).isEqualTo("Test Company");
    }

    @Test
    void detail_shouldReturnJob_whenJobExists() {
        Job job = new Job();
        job.setId(1L);
        job.setTitle("Detail Job");
        job.setContactName("John");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));

        JobVO result = jobService.detail(1L);

        assertThat(result.getTitle()).isEqualTo("Detail Job");
        assertThat(result.getContactName()).isEqualTo("John");
    }

    @Test
    void detail_shouldThrowException_whenJobNotFound() {
        when(jobMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.detail(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("岗位不存在");
    }

    @Test
    void closeJob_shouldUpdateStatus() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("ACTIVE");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));

        jobService.closeJob(1L);

        verify(jobMapper).updateStatus(1L, "CLOSED");
    }

    @Test
    void reopenJob_shouldUpdateStatus() {
        Job job = new Job();
        job.setId(1L);
        job.setStatus("CLOSED");

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));

        jobService.reopenJob(1L);

        verify(jobMapper).updateStatus(1L, "PUBLISHED");
    }

    @Test
    void setTop_shouldUpdateTopStatus() {
        Job job = new Job();
        job.setId(1L);
        job.setIsTop(false);

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));

        jobService.setTop(1L, true);

        verify(jobMapper).updateTop(eq(1L), eq(true));
    }

    @Test
    void setRecommended_shouldUpdateRecommendedStatus() {
        Job job = new Job();
        job.setId(1L);
        job.setIsRecommended(false);

        when(jobMapper.findById(1L)).thenReturn(Optional.of(job));

        jobService.setRecommended(1L, true);

        verify(jobMapper).updateRecommended(eq(1L), eq(true));
    }

    @Test
    void setTop_shouldThrowException_whenJobNotFound() {
        when(jobMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.setTop(999L, true))
                .isInstanceOf(BusinessException.class);
    }
}
