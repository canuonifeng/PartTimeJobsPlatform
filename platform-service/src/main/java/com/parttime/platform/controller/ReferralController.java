package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.ReferralAuditCmd;
import com.parttime.platform.pojo.cmd.ReferralConfigUpdateCmd;
import com.parttime.platform.pojo.entity.ReferralConfig;
import com.parttime.platform.service.ReferralService;
import com.parttime.platform.pojo.vo.ReferralAuditVO;
import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/referral")
public class ReferralController {

    @Resource
    private ReferralService referralService;

    @Operation(summary = "获取奖励规则配置")
    @GetMapping("/config")
    public ApiResponse<List<ReferralConfig>> getConfig() {
        return ApiResponse.success(referralService.getConfig());
    }

    @Operation(summary = "修改奖励规则配置")
    @PostMapping("/config")
    public ApiResponse<Void> updateConfig(@RequestBody ReferralConfigUpdateCmd cmd) {
        referralService.updateConfig(cmd.getConfigs());
        return ApiResponse.success();
    }

    @Operation(summary = "待审核奖励列表")
    @GetMapping("/audit/list")
    public ApiResponse<PageVO<ReferralAuditVO>> getAuditList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(referralService.getAuditList(page, pageSize));
    }

    @Operation(summary = "审核通过")
    @PostMapping("/audit/approve")
    public ApiResponse<Void> approveReward(@RequestBody ReferralAuditCmd cmd) {
        referralService.approveReward(cmd.getId(), cmd.getRemark());
        return ApiResponse.success();
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/audit/reject")
    public ApiResponse<Void> rejectReward(@RequestBody ReferralAuditCmd cmd) {
        referralService.rejectReward(cmd.getId(), cmd.getRemark());
        return ApiResponse.success();
    }
}
