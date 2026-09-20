package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.SettlementCancelCmd;
import com.parttime.platform.pojo.cmd.SettlementQueryCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.SettlementVO;
import com.parttime.platform.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/settlements")
public class SettlementController {

    @Resource
    private SettlementService settlementService;

    @Operation(summary = "获取结算列表")
    @PostMapping("/list")
    public ApiResponse<List<SettlementVO>> list(@RequestBody(required = false) SettlementQueryCmd body) {
        SettlementQueryCmd cmd = body != null ? body : new SettlementQueryCmd();
        return ApiResponse.success(settlementService.list(cmd));
    }

    @Operation(summary = "获取结算详情")
    @PostMapping("/detail")
    public ApiResponse<SettlementVO> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(settlementService.detail(body.getId()));
    }

    @Operation(summary = "确认结算")
    @PostMapping("/confirm")
    public ApiResponse<Void> confirm(@RequestBody IdCmd body) {
        settlementService.confirm(body.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "撤销结算")
    @PostMapping("/cancel")
    public ApiResponse<Void> cancel(@RequestBody SettlementCancelCmd body) {
        settlementService.cancel(body.getId(), body.getReason());
        return ApiResponse.success();
    }
}
