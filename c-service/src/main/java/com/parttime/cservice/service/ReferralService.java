package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.ReferralLinkVO;
import com.parttime.cservice.pojo.vo.ReferralStatsVO;
import com.parttime.cservice.pojo.vo.RefereeVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.ReferralRewardVO;
import com.parttime.cservice.pojo.entity.ReferralConfig;

import java.util.List;

public interface ReferralService {

    ReferralLinkVO getReferralLink(Long workerId);

    String getReferralPoster(Long workerId);

    void bindReferral(Long refereeId, String code);

    void checkAndGrantReward(Long refereeId);

    ReferralStatsVO getReferralStats(Long workerId);

    PageVO<RefereeVO> getReferees(Long workerId, int page, int pageSize);

    PageVO<ReferralRewardVO> getReferralRewards(Long workerId, int page, int pageSize);

    List<ReferralConfig> getConfig();

    void updateConfig(List<ReferralConfig> configs);
}
