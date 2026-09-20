package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.BlacklistQueryCmd;
import com.parttime.platform.pojo.cmd.BlacklistRemoveCmd;
import com.parttime.platform.pojo.cmd.BlacklistSaveCmd;
import com.parttime.platform.pojo.cmd.RuleSaveCmd;
import com.parttime.platform.pojo.cmd.RuleToggleCmd;
import com.parttime.platform.pojo.cmd.WhitelistQueryCmd;
import com.parttime.platform.pojo.cmd.WhitelistRemoveCmd;
import com.parttime.platform.pojo.cmd.WhitelistSaveCmd;
import com.parttime.platform.pojo.vo.RiskBlacklistVO;
import com.parttime.platform.pojo.vo.RiskMonitorVO;
import com.parttime.platform.pojo.vo.RiskRuleVO;
import com.parttime.platform.pojo.vo.RiskWhitelistVO;

import java.util.List;

public interface RiskService {
    List<RiskMonitorVO> monitor();
    List<RiskBlacklistVO> blacklistList(BlacklistQueryCmd cmd);
    void blacklistAdd(BlacklistSaveCmd cmd);
    void blacklistRemove(BlacklistRemoveCmd cmd);
    List<RiskWhitelistVO> whitelistList(WhitelistQueryCmd cmd);
    void whitelistAdd(WhitelistSaveCmd cmd);
    void whitelistRemove(WhitelistRemoveCmd cmd);
    List<RiskRuleVO> ruleList();
    void ruleToggle(RuleToggleCmd cmd);
    void ruleSave(RuleSaveCmd cmd);
}
