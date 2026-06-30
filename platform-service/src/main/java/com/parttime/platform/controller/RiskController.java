package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.service.RiskService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/risk")
public class RiskController {

    @Autowired
    private RiskService riskService;

    @Operation(summary = "获取黑名单列表")
    @PostMapping("/blacklist")
    public ApiResponse<List<Map<String, Object>>> blacklist(@RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.success(riskService.getBlacklist());
    }

    @Operation(summary = "加入黑名单")
    @PostMapping("/blacklist/add")
    public ApiResponse<Void> addToBlacklist(@RequestBody Map<String, Object> body) {
        riskService.addToBlacklist(body);
        return ApiResponse.success();
    }

    @Operation(summary = "移出黑名单")
    @PostMapping("/blacklist/remove")
    public ApiResponse<Void> removeFromBlacklist(@RequestBody IdCmd body) {
        riskService.removeFromBlacklist(body.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "获取白名单列表")
    @PostMapping("/whitelist")
    public ApiResponse<List<Map<String, Object>>> whitelist(@RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.success(riskService.getWhitelist());
    }

    @Operation(summary = "获取风控规则列表")
    @PostMapping("/rules")
    public ApiResponse<List<Map<String, Object>>> rules() {
        return ApiResponse.success(riskService.getRules());
    }

    @Operation(summary = "切换风控规则状态")
    @PostMapping("/rules/toggle")
    public ApiResponse<Void> toggleRule(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        boolean enabled = Boolean.parseBoolean(body.get("enabled").toString());
        riskService.toggleRule(id, enabled);
        return ApiResponse.success();
    }
}
