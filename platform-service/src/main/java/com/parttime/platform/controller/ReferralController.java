package com.parttime.platform.controller;

import com.parttime.platform.pojo.entity.ReferralConfig;
import com.parttime.platform.service.ReferralService;
import com.parttime.platform.pojo.vo.ReferralAuditVO;
import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

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
    @PutMapping("/config")
    public ApiResponse<Void> updateConfig(@RequestBody List<ReferralConfig> configs) {
        referralService.updateConfig(configs);
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
    @PostMapping("/audit/{id}/approve")
    public ApiResponse<Void> approveReward(@PathVariable Long id, @RequestBody Map<String, String> body) {
        referralService.approveReward(id, body.get("remark"));
        return ApiResponse.success();
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/audit/{id}/reject")
    public ApiResponse<Void> rejectReward(@PathVariable Long id, @RequestBody Map<String, String> body) {
        referralService.rejectReward(id, body.get("remark"));
        return ApiResponse.success();
    }
}