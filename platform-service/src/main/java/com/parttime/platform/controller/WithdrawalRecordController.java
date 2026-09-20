package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.WithdrawalQueryCmd;
import com.parttime.platform.pojo.cmd.WithdrawalRejectCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.WithdrawalRecordVO;
import com.parttime.platform.service.WithdrawalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/withdrawals")
public class WithdrawalRecordController {

    @Resource
    private WithdrawalRecordService withdrawalRecordService;

    @Operation(summary = "提现记录列表")
    @PostMapping("/list")
    public ApiResponse<List<WithdrawalRecordVO>> list(@RequestBody(required = false) WithdrawalQueryCmd body) {
        String status = body != null ? body.getStatus() : null;
        String keyword = body != null ? body.getKeyword() : null;
        return ApiResponse.success(withdrawalRecordService.list(status, keyword));
    }

    @Operation(summary = "提现详情")
    @PostMapping("/detail")
    public ApiResponse<WithdrawalRecordVO> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(withdrawalRecordService.detail(body.getId()));
    }

    @Operation(summary = "审核通过")
    @PostMapping("/approve")
    public ApiResponse<Void> approve(@RequestBody IdCmd body) {
        withdrawalRecordService.approve(body.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/reject")
    public ApiResponse<Void> reject(@RequestBody WithdrawalRejectCmd body) {
        withdrawalRecordService.reject(body.getId(), body.getReason());
        return ApiResponse.success();
    }
}
