package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settlements")
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    @Operation(summary = "获取结算列表")
    @PostMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestBody(required = false) Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        String keyword = body != null ? body.get("keyword") : null;
        return ApiResponse.success(settlementService.list(status, keyword));
    }

    @Operation(summary = "获取结算详情")
    @PostMapping("/detail")
    public ApiResponse<Map<String, Object>> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(settlementService.detail(body.getId()));
    }

    @Operation(summary = "确认结算")
    @PostMapping("/confirm")
    public ApiResponse<Void> confirm(@RequestBody IdCmd body) {
        settlementService.confirm(body.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "取消结算")
    @PostMapping("/cancel")
    public ApiResponse<Void> cancel(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String reason = (String) body.get("reason");
        settlementService.cancel(id, reason);
        return ApiResponse.success();
    }
}
