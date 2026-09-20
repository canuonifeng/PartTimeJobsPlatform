package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.ComplaintMapper;
import com.parttime.platform.mapper.RiskBlacklistMapper;
import com.parttime.platform.mapper.RiskRuleMapper;
import com.parttime.platform.mapper.RiskWhitelistMapper;
import com.parttime.platform.pojo.cmd.BlacklistQueryCmd;
import com.parttime.platform.pojo.cmd.BlacklistRemoveCmd;
import com.parttime.platform.pojo.cmd.BlacklistSaveCmd;
import com.parttime.platform.pojo.cmd.RuleSaveCmd;
import com.parttime.platform.pojo.cmd.RuleToggleCmd;
import com.parttime.platform.pojo.cmd.WhitelistQueryCmd;
import com.parttime.platform.pojo.cmd.WhitelistRemoveCmd;
import com.parttime.platform.pojo.cmd.WhitelistSaveCmd;
import com.parttime.platform.pojo.entity.Complaint;
import com.parttime.platform.pojo.entity.RiskBlacklist;
import com.parttime.platform.pojo.entity.RiskRule;
import com.parttime.platform.pojo.entity.RiskWhitelist;
import com.parttime.platform.pojo.vo.RiskBlacklistVO;
import com.parttime.platform.pojo.vo.RiskMonitorVO;
import com.parttime.platform.pojo.vo.RiskRuleVO;
import com.parttime.platform.pojo.vo.RiskWhitelistVO;
import com.parttime.platform.service.RiskService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RiskServiceImpl implements RiskService {

    @Resource
    private RiskBlacklistMapper riskBlacklistMapper;
    @Resource
    private RiskWhitelistMapper riskWhitelistMapper;
    @Resource
    private RiskRuleMapper riskRuleMapper;
    @Resource
    private ComplaintMapper complaintMapper;

    @Override
    public List<RiskMonitorVO> monitor() {
        List<Complaint> active = complaintMapper.findActiveForMonitor();
        return active.stream().map(c -> {
            RiskMonitorVO vo = new RiskMonitorVO();
            vo.setId(c.getId());
            vo.setSourceType("COMPLAINT");
            vo.setTitle(c.getTitle());
            vo.setLevel(c.getPriority());
            vo.setTargetName(c.getAccusedName());
            vo.setStatus(c.getStatus());
            vo.setCreatedAt(c.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<RiskBlacklistVO> blacklistList(BlacklistQueryCmd cmd) {
        BlacklistQueryCmd q = cmd != null ? cmd : new BlacklistQueryCmd();
        return riskBlacklistMapper.findByFilters(q.getTargetType(), q.getStatus(), q.getKeyword())
                .stream().map(this::toBlacklistVO).collect(Collectors.toList());
    }

    @Override
    public void blacklistAdd(BlacklistSaveCmd cmd) {
        RiskBlacklist record = new RiskBlacklist();
        record.setTargetType(cmd.getTargetType());
        record.setTargetId(cmd.getTargetId());
        record.setTargetName(cmd.getTargetName());
        record.setTargetValue(cmd.getTargetValue());
        record.setReason(cmd.getReason());
        record.setRiskLevel(cmd.getRiskLevel() != null ? cmd.getRiskLevel() : "MEDIUM");
        record.setBanType(cmd.getBanType() != null ? cmd.getBanType() : "PERMANENT");
        record.setBanStartTime(LocalDateTime.now());
        record.setBanEndTime(cmd.getBanEndTime());
        record.setOperatorName(cmd.getOperatorName());
        riskBlacklistMapper.insert(record);
    }

    @Override
    public void blacklistRemove(BlacklistRemoveCmd cmd) {
        riskBlacklistMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("黑名单记录不存在: " + cmd.getId()));
        riskBlacklistMapper.softRemove(cmd.getId(), cmd.getReason(), cmd.getOperatorName());
    }

    @Override
    public List<RiskWhitelistVO> whitelistList(WhitelistQueryCmd cmd) {
        WhitelistQueryCmd q = cmd != null ? cmd : new WhitelistQueryCmd();
        return riskWhitelistMapper.findByFilters(q.getTargetType(), q.getStatus(), q.getKeyword())
                .stream().map(this::toWhitelistVO).collect(Collectors.toList());
    }

    @Override
    public void whitelistAdd(WhitelistSaveCmd cmd) {
        RiskWhitelist record = new RiskWhitelist();
        record.setTargetType(cmd.getTargetType());
        record.setTargetId(cmd.getTargetId());
        record.setTargetName(cmd.getTargetName());
        record.setTargetValue(cmd.getTargetValue());
        record.setReason(cmd.getReason());
        record.setEffectiveStartTime(LocalDateTime.now());
        record.setEffectiveEndTime(cmd.getEffectiveEndTime());
        record.setOperatorName(cmd.getOperatorName());
        riskWhitelistMapper.insert(record);
    }

    @Override
    public void whitelistRemove(WhitelistRemoveCmd cmd) {
        riskWhitelistMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("白名单记录不存在: " + cmd.getId()));
        riskWhitelistMapper.softRemove(cmd.getId(), cmd.getOperatorName());
    }

    @Override
    public List<RiskRuleVO> ruleList() {
        return riskRuleMapper.findAll().stream().map(this::toRuleVO).collect(Collectors.toList());
    }

    @Override
    public void ruleToggle(RuleToggleCmd cmd) {
        riskRuleMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("风控规则不存在: " + cmd.getId()));
        riskRuleMapper.updateStatus(cmd.getId(), Boolean.TRUE.equals(cmd.getEnabled()) ? "ACTIVE" : "INACTIVE");
    }

    @Override
    public void ruleSave(RuleSaveCmd cmd) {
        if (cmd.getId() != null) {
            RiskRule rule = riskRuleMapper.findById(cmd.getId())
                    .orElseThrow(() -> new BusinessException("风控规则不存在: " + cmd.getId()));
            rule.setRuleName(cmd.getRuleName());
            rule.setRuleType(cmd.getRuleType());
            rule.setDescription(cmd.getDescription());
            rule.setTriggerCondition(cmd.getTriggerCondition());
            rule.setActionType(cmd.getActionType());
            rule.setActionConfig(cmd.getActionConfig());
            rule.setRiskLevel(cmd.getRiskLevel());
            rule.setStatus(cmd.getStatus() != null ? cmd.getStatus() : "ACTIVE");
            riskRuleMapper.update(rule);
        } else {
            riskRuleMapper.findByCode(cmd.getRuleCode()).ifPresent(r -> {
                throw new BusinessException("规则代码已存在: " + cmd.getRuleCode());
            });
            RiskRule rule = new RiskRule();
            rule.setRuleName(cmd.getRuleName());
            rule.setRuleCode(cmd.getRuleCode());
            rule.setRuleType(cmd.getRuleType());
            rule.setDescription(cmd.getDescription());
            rule.setTriggerCondition(cmd.getTriggerCondition());
            rule.setActionType(cmd.getActionType());
            rule.setActionConfig(cmd.getActionConfig());
            rule.setRiskLevel(cmd.getRiskLevel() != null ? cmd.getRiskLevel() : "MEDIUM");
            rule.setStatus(cmd.getStatus() != null ? cmd.getStatus() : "ACTIVE");
            rule.setOperatorName(cmd.getOperatorName());
            riskRuleMapper.insert(rule);
        }
    }

    private RiskBlacklistVO toBlacklistVO(RiskBlacklist e) {
        RiskBlacklistVO vo = new RiskBlacklistVO();
        vo.setId(e.getId());
        vo.setTargetType(e.getTargetType());
        vo.setTargetId(e.getTargetId());
        vo.setTargetName(e.getTargetName());
        vo.setTargetValue(e.getTargetValue());
        vo.setReason(e.getReason());
        vo.setRiskLevel(e.getRiskLevel());
        vo.setBanType(e.getBanType());
        vo.setBanStartTime(e.getBanStartTime());
        vo.setBanEndTime(e.getBanEndTime());
        vo.setOperatorName(e.getOperatorName());
        vo.setStatus(e.getStatus());
        vo.setCreatedAt(e.getCreatedAt());
        return vo;
    }

    private RiskWhitelistVO toWhitelistVO(RiskWhitelist e) {
        RiskWhitelistVO vo = new RiskWhitelistVO();
        vo.setId(e.getId());
        vo.setTargetType(e.getTargetType());
        vo.setTargetId(e.getTargetId());
        vo.setTargetName(e.getTargetName());
        vo.setTargetValue(e.getTargetValue());
        vo.setReason(e.getReason());
        vo.setEffectiveStartTime(e.getEffectiveStartTime());
        vo.setEffectiveEndTime(e.getEffectiveEndTime());
        vo.setOperatorName(e.getOperatorName());
        vo.setStatus(e.getStatus());
        vo.setCreatedAt(e.getCreatedAt());
        return vo;
    }

    private RiskRuleVO toRuleVO(RiskRule e) {
        RiskRuleVO vo = new RiskRuleVO();
        vo.setId(e.getId());
        vo.setRuleName(e.getRuleName());
        vo.setRuleCode(e.getRuleCode());
        vo.setRuleType(e.getRuleType());
        vo.setDescription(e.getDescription());
        vo.setTriggerCondition(e.getTriggerCondition());
        vo.setActionType(e.getActionType());
        vo.setActionConfig(e.getActionConfig());
        vo.setRiskLevel(e.getRiskLevel());
        vo.setStatus(e.getStatus());
        vo.setTriggerCount(e.getTriggerCount());
        vo.setLastTriggerAt(e.getLastTriggerAt());
        vo.setOperatorName(e.getOperatorName());
        vo.setCreatedAt(e.getCreatedAt());
        return vo;
    }
}
