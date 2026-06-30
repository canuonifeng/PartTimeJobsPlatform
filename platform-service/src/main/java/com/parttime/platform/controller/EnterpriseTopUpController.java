package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.service.EnterpriseTopUpService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/top-up")
public class EnterpriseTopUpController {

    @Resource
    private EnterpriseTopUpService topUpService;

    @Operation(summary = "获取企业充值列表")
    @PostMapping("/list")
    public ApiResponse<List<?>> list(@RequestBody(required = false) Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        String keyword = body != null ? body.get("keyword") : null;
        return ApiResponse.success(topUpService.list(status, keyword));
    }

    @Operation(summary = "获取充值详情")
    @PostMapping("/detail")
    public ApiResponse<?> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(topUpService.detail(body.getId()));
    }

    @Operation(summary = "审核通过充值")
    @PostMapping("/approve")
    public ApiResponse<Void> approve(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String remark = body.get("remark") != null ? body.get("remark").toString() : "";
        topUpService.approve(id, "admin", remark);
        return ApiResponse.success();
    }

    @Operation(summary = "审核拒绝充值")
    @PostMapping("/reject")
    public ApiResponse<Void> reject(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String remark = body.get("remark") != null ? body.get("remark").toString() : "";
        topUpService.reject(id, "admin", remark);
        return ApiResponse.success();
    }
}
