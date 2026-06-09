package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.ReferralConfigMapper;
import com.parttime.platform.mapper.ReferralRewardMapper;
import com.parttime.platform.pojo.entity.ReferralConfig;
import com.parttime.platform.pojo.entity.ReferralReward;
import com.parttime.platform.service.ReferralService;
import com.parttime.platform.pojo.vo.ReferralAuditVO;
import com.parttime.platform.pojo.vo.PageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReferralServiceImpl implements ReferralService {

    @Resource
    private ReferralConfigMapper referralConfigMapper;

    @Resource
    private ReferralRewardMapper referralRewardMapper;

    private static final DateTimeFormatter BEIJING_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<ReferralConfig> getConfig() {
        return referralConfigMapper.findAll();
    }

    @Override
    @Transactional
    public void updateConfig(List<ReferralConfig> configs) {
        referralConfigMapper.batchUpsert(configs);
    }

    @Override
    public PageVO<ReferralAuditVO> getAuditList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ReferralReward> rewards = referralRewardMapper.findByStatusPage("PENDING", offset, pageSize);
        long total = referralRewardMapper.countByStatus("PENDING");

        List<ReferralAuditVO> list = rewards.stream().map(reward -> {
            ReferralAuditVO vo = new ReferralAuditVO();
            vo.setId(reward.getId());
            vo.setAmount(reward.getAmount());
            vo.setStatus(reward.getStatus());
            vo.setCreatedAt(reward.getCreatedAt() != null ? reward.getCreatedAt().format(BEIJING_FMT) : "");
            return vo;
        }).collect(Collectors.toList());

        return new PageVO<>(list, total);
    }

    @Override
    @Transactional
    public void approveReward(Long rewardId, String remark) {
        ReferralReward reward = referralRewardMapper.findById(rewardId);
        if (reward == null) {
            throw new RuntimeException("奖励记录不存在");
        }
        if (!"PENDING".equals(reward.getStatus()) && !"AUDITING".equals(reward.getStatus())) {
            throw new RuntimeException("当前状态无法审核");
        }
        referralRewardMapper.updateStatus(rewardId, "GRANTED", remark);
        referralRewardMapper.updateGrantedAt(rewardId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void rejectReward(Long rewardId, String remark) {
        ReferralReward reward = referralRewardMapper.findById(rewardId);
        if (reward == null) {
            throw new RuntimeException("奖励记录不存在");
        }
        if (!"PENDING".equals(reward.getStatus()) && !"AUDITING".equals(reward.getStatus())) {
            throw new RuntimeException("当前状态无法审核");
        }
        referralRewardMapper.updateStatus(rewardId, "REJECTED", remark);
    }
}