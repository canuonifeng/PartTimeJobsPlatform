package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.TopUpQueryCmd;
import com.parttime.platform.pojo.cmd.TopUpReviewCmd;
import com.parttime.platform.pojo.entity.EnterpriseTopUp;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.service.EnterpriseTopUpService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/top-up")
public class EnterpriseTopUpController {

    @Resource
    private EnterpriseTopUpService topUpService;

    @Operation(summary = "获取企业充值列表")
    @PostMapping("/list")
    public ApiResponse<List<EnterpriseTopUp>> list(@RequestBody(required = false) TopUpQueryCmd body) {
        TopUpQueryCmd cmd = body != null ? body : new TopUpQueryCmd();
        return ApiResponse.success(topUpService.list(cmd.getStatus(), cmd.getKeyword()));
    }

    @Operation(summary = "获取充值详情")
    @PostMapping("/detail")
    public ApiResponse<EnterpriseTopUp> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(topUpService.detail(body.getId()));
    }

    @Operation(summary = "审核通过充值")
    @PostMapping("/approve")
    public ApiResponse<Void> approve(@RequestBody TopUpReviewCmd body) {
        String remark = body.getRemark() != null ? body.getRemark() : "";
        topUpService.approve(body.getId(), "admin", remark);
        return ApiResponse.success();
    }

    @Operation(summary = "审核拒绝充值")
    @PostMapping("/reject")
    public ApiResponse<Void> reject(@RequestBody TopUpReviewCmd body) {
        String remark = body.getRemark() != null ? body.getRemark() : "";
        topUpService.reject(body.getId(), "admin", remark);
        return ApiResponse.success();
    }
}
