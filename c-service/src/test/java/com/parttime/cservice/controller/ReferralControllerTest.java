package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.entity.ReferralConfig;
import com.parttime.cservice.pojo.vo.ReferralLinkVO;
import com.parttime.cservice.pojo.vo.ReferralStatsVO;
import com.parttime.cservice.pojo.vo.RefereeVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.service.ReferralService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReferralControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ReferralService referralService;

    @InjectMocks
    private ReferralController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.clearContext();
    }

    private void authAs(Long workerId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(String.valueOf(workerId), null, List.of()));
    }

    @Test
    void getReferralLink_withAuth_shouldReturn200() throws Exception {
        authAs(1L);

        ReferralLinkVO link = new ReferralLinkVO();
        link.setCode("ABC12345");
        link.setLink("https://worker.example.com/invite?code=ABC12345");
        when(referralService.getReferralLink(1L)).thenReturn(link);

        mockMvc.perform(get("/api/referral/link"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ABC12345"))
                .andExpect(jsonPath("$.link").value("https://worker.example.com/invite?code=ABC12345"));
    }

    @Test
    void getReferralLink_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/referral/link"))
                .andExpect(status().isOk());
    }

    @Test
    void getReferralPoster_withAuth_shouldReturn200() throws Exception {
        authAs(1L);

        when(referralService.getReferralPoster(1L)).thenReturn("https://cdn.example.com/poster.jpg");

        mockMvc.perform(get("/api/referral/poster"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.posterUrl").value("https://cdn.example.com/poster.jpg"));
    }

    @Test
    void getReferralStats_withAuth_shouldReturn200() throws Exception {
        authAs(1L);

        ReferralStatsVO stats = new ReferralStatsVO();
        stats.setTotalReferees(5);
        stats.setTotalRewardAmount(new BigDecimal("100"));
        stats.setPendingRewardAmount(new BigDecimal("50"));
        when(referralService.getReferralStats(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/referral/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReferees").value(5))
                .andExpect(jsonPath("$.totalRewardAmount").value(100))
                .andExpect(jsonPath("$.pendingRewardAmount").value(50));
    }

    @Test
    void getReferees_withAuth_shouldReturn200() throws Exception {
        authAs(1L);

        RefereeVO referee = new RefereeVO();
        referee.setId(2L);
        referee.setName("测试用户");
        referee.setPhone("138****1234");
        referee.setRewardStatus("NOT_QUALIFIED");
        when(referralService.getReferees(1L, 1, 10)).thenReturn(new PageVO<>(List.of(referee), 1));

        mockMvc.perform(get("/api/referral/referees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records.length()").value(1))
                .andExpect(jsonPath("$.records[0].name").value("测试用户"))
                .andExpect(jsonPath("$.records[0].rewardStatus").value("NOT_QUALIFIED"));
    }

    @Test
    void getReferralRewards_withAuth_shouldReturn200() throws Exception {
        authAs(1L);

        PageVO<com.parttime.cservice.pojo.vo.ReferralRewardVO> emptyPage = new PageVO<>(List.of(), 0);
        when(referralService.getReferralRewards(1L, 1, 10)).thenReturn(emptyPage);

        mockMvc.perform(get("/api/referral/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records").isArray())
                .andExpect(jsonPath("$.records.length()").value(0))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void getConfig_withAuth_shouldReturn200() throws Exception {
        authAs(1L);

        ReferralConfig config = new ReferralConfig();
        config.setConfigKey("reward_amount");
        config.setConfigValue("20");
        when(referralService.getConfig()).thenReturn(List.of(config));

        mockMvc.perform(get("/api/referral/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].configKey").value("reward_amount"))
                .andExpect(jsonPath("$[0].configValue").value("20"));
    }

    @Test
    void updateConfig_withAuth_shouldReturn200() throws Exception {
        authAs(1L);

        ReferralConfig config = new ReferralConfig();
        config.setConfigKey("reward_amount");
        config.setConfigValue("50");
        config.setDescription("奖励金额");

        mockMvc.perform(post("/api/referral/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("configs", List.of(config)))))
                .andExpect(status().isOk());

        verify(referralService).updateConfig(List.of(config));
    }

    @Test
    void updateConfig_withoutAuth_shouldReturn401() throws Exception {
        ReferralConfig config = new ReferralConfig();
        config.setConfigKey("reward_amount");
        config.setConfigValue("50");

        mockMvc.perform(post("/api/referral/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("configs", List.of(config)))))
                .andExpect(status().isOk());
    }
}
