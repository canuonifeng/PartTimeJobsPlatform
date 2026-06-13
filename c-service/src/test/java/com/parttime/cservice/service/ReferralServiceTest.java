package com.parttime.cservice.service;

import com.parttime.cservice.mapper.ReferralRecordMapper;
import com.parttime.cservice.mapper.ReferralRewardMapper;
import com.parttime.cservice.service.impl.ReferralServiceImpl;
import com.parttime.cservice.pojo.entity.ReferralCode;
import com.parttime.cservice.pojo.entity.ReferralRecord;
import com.parttime.cservice.pojo.entity.ReferralReward;
import com.parttime.cservice.pojo.entity.ReferralConfig;
import com.parttime.cservice.pojo.vo.ReferralLinkVO;
import com.parttime.cservice.pojo.vo.ReferralStatsVO;
import com.parttime.cservice.pojo.vo.RefereeVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.ReferralRewardVO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReferralServiceTest {

    @InjectMocks
    private ReferralServiceImpl referralService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(referralService, "referralCodeMapper", InMemoryMappers.createReferralCodeMapper());
        ReflectionTestUtils.setField(referralService, "referralRecordMapper", InMemoryMappers.createReferralRecordMapper());
        ReflectionTestUtils.setField(referralService, "referralRewardMapper", InMemoryMappers.createReferralRewardMapper());
        ReflectionTestUtils.setField(referralService, "referralConfigMapper", InMemoryMappers.createReferralConfigMapper());
    }

    @Test
    void getReferralLink_shouldGenerateCodeForNewUser() {
        ReferralLinkVO link = referralService.getReferralLink(100L);

        assertThat(link).isNotNull();
        assertThat(link.getCode()).isNotBlank();
        assertThat(link.getCode()).hasSize(8);
        assertThat(link.getLink()).contains("code=");
    }

    @Test
    void getReferralLink_shouldReturnSameCodeForExistingUser() {
        ReferralLinkVO first = referralService.getReferralLink(100L);
        ReferralLinkVO second = referralService.getReferralLink(100L);

        assertThat(second.getCode()).isEqualTo(first.getCode());
    }

    @Test
    void getReferralPoster_shouldReturnPosterUrl() {
        String posterUrl = referralService.getReferralPoster(100L);

        assertThat(posterUrl).isNotBlank();
        assertThat(posterUrl).contains("cdn.example.com");
    }

    @Test
    void bindReferral_shouldCreateRecord() {
        referralService.getReferralLink(100L);
        String code = referralService.getReferralLink(100L).getCode();

        referralService.bindReferral(200L, code);

        com.parttime.cservice.mapper.ReferralRecordMapper recordMapper =
                (com.parttime.cservice.mapper.ReferralRecordMapper) ReflectionTestUtils.getField(referralService, "referralRecordMapper");
        ReferralRecord record = recordMapper.findByRefereeId(200L);
        assertThat(record).isNotNull();
        assertThat(record.getReferrerId()).isEqualTo(100L);
        assertThat(record.getRefereeId()).isEqualTo(200L);
    }

    @Test
    void bindReferral_withEmptyCode_shouldNotThrow() {
        referralService.bindReferral(200L, "");
        referralService.bindReferral(200L, null);
    }

    @Test
    void bindReferral_withInvalidCode_shouldThrow() {
        assertThatThrownBy(() -> referralService.bindReferral(200L, "INVALID"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("邀请码无效");
    }

    @Test
    void bindReferral_withSelfReferral_shouldThrow() {
        referralService.getReferralLink(100L);
        String code = referralService.getReferralLink(100L).getCode();

        assertThatThrownBy(() -> referralService.bindReferral(100L, code))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("不能邀请自己");
    }

    @Test
    void bindReferral_withDuplicateReferee_shouldThrow() {
        referralService.getReferralLink(100L);
        String code = referralService.getReferralLink(100L).getCode();

        referralService.bindReferral(200L, code);

        assertThatThrownBy(() -> referralService.bindReferral(200L, code))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("已绑定邀请人");
    }

    @Test
    void getReferralStats_shouldReturnCorrectCounts() {
        referralService.getReferralLink(100L);
        String code = referralService.getReferralLink(100L).getCode();
        referralService.bindReferral(200L, code);
        referralService.bindReferral(300L, code);

        ReferralStatsVO stats = referralService.getReferralStats(100L);

        assertThat(stats).isNotNull();
        assertThat(stats.getTotalReferees()).isEqualTo(2);
    }

    @Test
    void getReferralStats_shouldReturnZeroForNewUser() {
        ReferralStatsVO stats = referralService.getReferralStats(999L);

        assertThat(stats.getTotalReferees()).isZero();
        assertThat(stats.getTotalRewardAmount()).isZero();
    }

    @Test
    void getReferees_shouldReturnPaginatedList() {
        referralService.getReferralLink(100L);
        String code = referralService.getReferralLink(100L).getCode();
        referralService.bindReferral(200L, code);
        referralService.bindReferral(300L, code);

        PageVO<RefereeVO> page = referralService.getReferees(100L, 1, 10);

        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getRecords()).hasSize(2);
        assertThat(page.getRecords().get(0).getRewardStatus()).isEqualTo("NOT_QUALIFIED");
    }

    @Test
    void getReferees_shouldUseMapperPagination() {
        ReferralRecordMapper recordMapper = org.mockito.Mockito.mock(ReferralRecordMapper.class);
        ReferralRewardMapper rewardMapper = org.mockito.Mockito.mock(ReferralRewardMapper.class);
        ReferralServiceImpl service = new ReferralServiceImpl();
        ReflectionTestUtils.setField(service, "referralRecordMapper", recordMapper);
        ReflectionTestUtils.setField(service, "referralRewardMapper", rewardMapper);
        when(recordMapper.findByReferrerIdPage(100L, 10, 10)).thenReturn(List.of());
        when(recordMapper.countByReferrerId(100L)).thenReturn(15);

        PageVO<RefereeVO> page = service.getReferees(100L, 2, 10);

        assertThat(page.getTotal()).isEqualTo(15);
        verify(recordMapper).findByReferrerIdPage(100L, 10, 10);
        verify(recordMapper, never()).findByReferrerId(100L);
    }

    @Test
    void getReferees_shouldReturnEmptyForNoReferrals() {
        PageVO<RefereeVO> page = referralService.getReferees(999L, 1, 10);

        assertThat(page.getTotal()).isZero();
        assertThat(page.getRecords()).isEmpty();
    }

    @Test
    void getConfig_shouldReturnDefaultConfig() {
        List<ReferralConfig> configs = referralService.getConfig();

        assertThat(configs).isNotNull();
    }

    @Test
    void updateConfig_shouldStoreConfigValues() {
        ReferralConfig config = new ReferralConfig();
        config.setConfigKey("reward_amount");
        config.setConfigValue("50");
        config.setDescription("奖励金额");

        referralService.updateConfig(List.of(config));

        List<ReferralConfig> configs = referralService.getConfig();
        assertThat(configs).hasSize(1);
        assertThat(configs.get(0).getConfigKey()).isEqualTo("reward_amount");
        assertThat(configs.get(0).getConfigValue()).isEqualTo("50");
    }

    @Test
    void checkAndGrantReward_withNoReferral_shouldDoNothing() {
        referralService.checkAndGrantReward(999L);
    }

    @Test
    void checkAndGrantReward_withValidReferral_shouldCreateReward() {
        ReferralConfig minWorkConfig = new ReferralConfig();
        minWorkConfig.setConfigKey("min_work_count");
        minWorkConfig.setConfigValue("0");
        referralService.updateConfig(List.of(minWorkConfig));

        referralService.getReferralLink(100L);
        String code = referralService.getReferralLink(100L).getCode();
        referralService.bindReferral(200L, code);

        referralService.checkAndGrantReward(200L);

        com.parttime.cservice.mapper.ReferralRewardMapper rewardMapper =
                (com.parttime.cservice.mapper.ReferralRewardMapper) ReflectionTestUtils.getField(referralService, "referralRewardMapper");
        long count = rewardMapper.countByReferrerId(100L);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void checkAndGrantReward_withDuplicate_shouldNotCreateSecondReward() {
        ReferralConfig minWorkConfig = new ReferralConfig();
        minWorkConfig.setConfigKey("min_work_count");
        minWorkConfig.setConfigValue("0");
        referralService.updateConfig(List.of(minWorkConfig));

        referralService.getReferralLink(100L);
        String code = referralService.getReferralLink(100L).getCode();
        referralService.bindReferral(200L, code);

        referralService.checkAndGrantReward(200L);
        referralService.checkAndGrantReward(200L);

        com.parttime.cservice.mapper.ReferralRewardMapper rewardMapper =
                (com.parttime.cservice.mapper.ReferralRewardMapper) ReflectionTestUtils.getField(referralService, "referralRewardMapper");
        long count = rewardMapper.countByReferrerId(100L);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void getReferralRewards_shouldReturnEmptyForNewUser() {
        PageVO<ReferralRewardVO> page = referralService.getReferralRewards(999L, 1, 10);

        assertThat(page.getTotal()).isZero();
        assertThat(page.getRecords()).isEmpty();
    }

    @Test
    void getReferralRewards_shouldReturnRewards() {
        referralService.getReferralLink(100L);
        String code = referralService.getReferralLink(100L).getCode();
        referralService.bindReferral(200L, code);

        PageVO<ReferralRewardVO> page = referralService.getReferralRewards(100L, 1, 10);

        assertThat(page).isNotNull();
    }
}
