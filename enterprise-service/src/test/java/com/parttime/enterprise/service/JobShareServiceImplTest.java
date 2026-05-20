package com.parttime.enterprise.service;

import com.parttime.enterprise.config.WeChatMiniProgramConfig;
import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.vo.JobShareLinkVO;
import com.parttime.enterprise.service.impl.JobShareServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobShareServiceImplTest {

    @Mock
    private JobMapper jobMapper;

    @Mock
    private WeChatMiniProgramConfig weChatMiniProgramConfig;

    @InjectMocks
    private JobShareServiceImpl jobShareService;

    @Test
    void getShareLink_shouldReturnSchemeLinkForPublishedJob() {
        Job job = new Job();
        job.setId(42L);
        job.setCompanyId(1L);
        job.setStatus("PUBLISHED");

        when(jobMapper.findById(42L)).thenReturn(Optional.of(job));
        when(weChatMiniProgramConfig.getCustomerAppId()).thenReturn("test_appid");

        JobShareLinkVO response = jobShareService.getShareLink(42L);

        assertThat(response.getJobId()).isEqualTo(42L);
        assertThat(response.getLink()).isEqualTo("weixin://dl/business/?appid=test_appid&path=/pages/jobs/jobDetail&query=id%3D42&env_version=release");
    }

    @Test
    void getShareLink_shouldRejectDraftJob() {
        Job job = new Job();
        job.setId(42L);
        job.setCompanyId(1L);
        job.setStatus("DRAFT");

        when(jobMapper.findById(42L)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobShareService.getShareLink(42L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅已发布职位可生成链接");
    }
}
