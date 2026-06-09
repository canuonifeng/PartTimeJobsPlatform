package com.parttime.platform.service;

import com.parttime.platform.pojo.entity.ReferralConfig;
import com.parttime.platform.pojo.vo.ReferralAuditVO;
import com.parttime.platform.pojo.vo.PageVO;

import java.util.List;

public interface ReferralService {
    List<ReferralConfig> getConfig();
    void updateConfig(List<ReferralConfig> configs);
    PageVO<ReferralAuditVO> getAuditList(int page, int pageSize);
    void approveReward(Long rewardId, String remark);
    void rejectReward(Long rewardId, String remark);
}