package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.BlacklistQueryCmd;
import com.parttime.platform.pojo.cmd.BlacklistRemoveCmd;
import com.parttime.platform.pojo.cmd.BlacklistSaveCmd;
import com.parttime.platform.pojo.cmd.RuleSaveCmd;
import com.parttime.platform.pojo.cmd.RuleToggleCmd;
import com.parttime.platform.pojo.cmd.WhitelistQueryCmd;
import com.parttime.platform.pojo.cmd.WhitelistRemoveCmd;
import com.parttime.platform.pojo.cmd.WhitelistSaveCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.RiskBlacklistVO;
import com.parttime.platform.pojo.vo.RiskMonitorVO;
import com.parttime.platform.pojo.vo.RiskRuleVO;
import com.parttime.platform.pojo.vo.RiskWhitelistVO;
import com.parttime.platform.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/risk")
public class RiskController {

    @Resource
    private RiskService riskService;

    @Operation(summary = "异常监控列表")
    @PostMapping("/monitor")
    public ApiResponse<List<RiskMonitorVO>> monitor() {
        return ApiResponse.success(riskService.monitor());
    }

    @Operation(summary = "获取黑名单列表")
    @PostMapping("/blacklist")
    public ApiResponse<List<RiskBlacklistVO>> blacklist(@RequestBody(required = false) BlacklistQueryCmd body) {
        return ApiResponse.success(riskService.blacklistList(body));
    }

    @Operation(summary = "加入黑名单")
    @PostMapping("/blacklist/add")
    public ApiResponse<Void> blacklistAdd(@RequestBody BlacklistSaveCmd body) {
        riskService.blacklistAdd(body);
        return ApiResponse.success();
    }

    @Operation(summary = "移出黑名单")
    @PostMapping("/blacklist/remove")
    public ApiResponse<Void> blacklistRemove(@RequestBody BlacklistRemoveCmd body) {
        riskService.blacklistRemove(body);
        return ApiResponse.success();
    }

    @Operation(summary = "获取白名单列表")
    @PostMapping("/whitelist")
    public ApiResponse<List<RiskWhitelistVO>> whitelist(@RequestBody(required = false) WhitelistQueryCmd body) {
        return ApiResponse.success(riskService.whitelistList(body));
    }

    @Operation(summary = "加入白名单")
    @PostMapping("/whitelist/add")
    public ApiResponse<Void> whitelistAdd(@RequestBody WhitelistSaveCmd body) {
        riskService.whitelistAdd(body);
        return ApiResponse.success();
    }

    @Operation(summary = "移出白名单")
    @PostMapping("/whitelist/remove")
    public ApiResponse<Void> whitelistRemove(@RequestBody WhitelistRemoveCmd body) {
        riskService.whitelistRemove(body);
        return ApiResponse.success();
    }

    @Operation(summary = "获取风控规则列表")
    @PostMapping("/rules")
    public ApiResponse<List<RiskRuleVO>> rules() {
        return ApiResponse.success(riskService.ruleList());
    }

    @Operation(summary = "切换风控规则状态")
    @PostMapping("/rules/toggle")
    public ApiResponse<Void> toggleRule(@RequestBody RuleToggleCmd body) {
        riskService.ruleToggle(body);
        return ApiResponse.success();
    }

    @Operation(summary = "保存风控规则（新增/编辑）")
    @PostMapping("/rules/save")
    public ApiResponse<Void> saveRule(@RequestBody RuleSaveCmd body) {
        riskService.ruleSave(body);
        return ApiResponse.success();
    }
}
