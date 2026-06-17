package com.parttime.platform.service;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.LeadMapper;
import com.parttime.platform.pojo.cmd.LeadCreateCmd;
import com.parttime.platform.pojo.entity.Lead;
import com.parttime.platform.pojo.vo.LeadVO;
import com.parttime.platform.service.notification.LeadNotifier;
import com.parttime.platform.service.impl.LeadServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private LeadMapper leadMapper;

    @Mock
    private LeadNotifier leadNotifier;

    @InjectMocks
    private LeadServiceImpl leadService;

    @Test
    void createLead_shouldNormalizeAndPersistLead() {
        LeadCreateCmd cmd = new LeadCreateCmd();
        cmd.setContactName(" 张三 ");
        cmd.setCompanyName(" 上海零售有限公司 ");
        cmd.setPhone(" 13800138000 ");
        cmd.setDemand(" 需要演示排班考勤 ");
        cmd.setSourcePage(" contact ");

        doAnswer(invocation -> {
            Lead lead = invocation.getArgument(0);
            lead.setId(88L);
            return 1;
        }).when(leadMapper).insert(argThat(lead ->
                "张三".equals(lead.getContactName())
                        && "上海零售有限公司".equals(lead.getCompanyName())
                        && "13800138000".equals(lead.getPhone())
                        && "需要演示排班考勤".equals(lead.getDemand())
                        && "contact".equals(lead.getSourcePage())
                        && "NEW".equals(lead.getStatus())
        ));

        LeadVO result = leadService.createLead(cmd);

        assertThat(result.getId()).isEqualTo(88L);
        assertThat(result.getContactName()).isEqualTo("张三");
        assertThat(result.getCompanyName()).isEqualTo("上海零售有限公司");
        assertThat(result.getPhone()).isEqualTo("13800138000");
        assertThat(result.getStatus()).isEqualTo("NEW");

        verify(leadNotifier).notifyLeadCreated(argThat(lead ->
                Long.valueOf(88L).equals(lead.getId())
                        && "张三".equals(lead.getContactName())
                        && "上海零售有限公司".equals(lead.getCompanyName())
                        && "13800138000".equals(lead.getPhone())
        ));
    }

    @Test
    void createLead_notifierFailure_shouldStillReturnSavedLead() {
        LeadCreateCmd cmd = new LeadCreateCmd();
        cmd.setContactName("张三");
        cmd.setCompanyName("上海零售有限公司");
        cmd.setPhone("13800138000");

        doAnswer(invocation -> {
            Lead lead = invocation.getArgument(0);
            lead.setId(99L);
            return 1;
        }).when(leadMapper).insert(org.mockito.ArgumentMatchers.any(Lead.class));
        doThrow(new RuntimeException("feishu unavailable"))
                .when(leadNotifier).notifyLeadCreated(org.mockito.ArgumentMatchers.any(Lead.class));

        LeadVO result = leadService.createLead(cmd);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getStatus()).isEqualTo("NEW");
        verify(leadNotifier).notifyLeadCreated(org.mockito.ArgumentMatchers.any(Lead.class));
    }

    @Test
    void createLead_missingPhone_shouldThrow() {
        LeadCreateCmd cmd = new LeadCreateCmd();
        cmd.setContactName("张三");
        cmd.setCompanyName("上海零售有限公司");
        cmd.setPhone(" ");

        assertThatThrownBy(() -> leadService.createLead(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("手机号不能为空");

        verify(leadMapper, never()).insert(org.mockito.ArgumentMatchers.any(Lead.class));
    }
}
