package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.TransactionQueryCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.TransactionOverviewVO;
import com.parttime.platform.pojo.vo.TransactionVO;
import com.parttime.platform.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/transactions")
public class TransactionController {

    @Resource
    private TransactionService transactionService;

    @Operation(summary = "获取交易流水列表")
    @PostMapping("/list")
    public ApiResponse<List<TransactionVO>> list(@RequestBody(required = false) TransactionQueryCmd body) {
        TransactionQueryCmd cmd = body != null ? body : new TransactionQueryCmd();
        return ApiResponse.success(transactionService.list(cmd));
    }

    @Operation(summary = "获取交易流水详情")
    @PostMapping("/detail")
    public ApiResponse<TransactionVO> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(transactionService.detail(body.getId()));
    }

    @Operation(summary = "获取交易概览统计")
    @PostMapping("/overview")
    public ApiResponse<TransactionOverviewVO> overview(@RequestBody(required = false) TransactionQueryCmd body) {
        return ApiResponse.success(transactionService.overview());
    }
}
