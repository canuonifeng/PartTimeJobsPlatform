package com.parttime.platform.controller;

import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Operation(summary = "获取交易流水列表")
    @PostMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestBody(required = false) Map<String, String> body) {
        String type = body != null ? body.get("type") : null;
        String status = body != null ? body.get("status") : null;
        String keyword = body != null ? body.get("keyword") : null;
        return ApiResponse.success(transactionService.list(type, status, keyword));
    }

    @Operation(summary = "获取交易概览统计")
    @PostMapping("/overview")
    public ApiResponse<Map<String, Object>> overview(@RequestBody(required = false) Map<String, String> body) {
        return ApiResponse.success(transactionService.overview());
    }
}
