package com.parttime.platform.controller;

import com.parttime.platform.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/transactions")
public class TransactionController {

    @Operation(summary = "获取交易流水列表")
    @PostMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestBody(required = false) Map<String, String> body) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] types = {"ENTERPRISE_TOPUP", "WORKER_WITHDRAWAL", "SETTLEMENT", "SERVICE_FEE", "REFUND"};
        String[] typeNames = {"企业充值", "工人提现", "工资结算", "平台服务费", "退款"};
        String[] statuses = {"SUCCESS", "PENDING", "FAILED"};
        for (int i = 1; i <= 30; i++) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", (long) i);
            item.put("transactionNo", "TXN" + System.currentTimeMillis() / 1000 + i);
            item.put("type", types[i % 5]);
            item.put("typeName", typeNames[i % 5]);
            item.put("amount", new BigDecimal((i % 10 + 1) * 100));
            item.put("balanceAfter", new BigDecimal(5000 + (i % 10 + 1) * 50));
            item.put("relatedType", i % 2 == 0 ? "ENTERPRISE" : "WORKER");
            item.put("relatedName", (i % 2 == 0 ? "企业" : "工人") + (i % 10 + 1));
            item.put("relatedPhone", i % 2 == 0 ? "010-1234567" + i : "1380000" + String.format("%02d", i % 100));
            item.put("status", statuses[i % 3]);
            item.put("remark", "交易备注" + i);
            item.put("operator", i % 5 == 0 ? "admin" : "operator" + (i % 3 + 1));
            item.put("createdAt", LocalDateTime.now().minusHours(i).toString());
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "获取交易概览统计")
    @PostMapping("/overview")
    public ApiResponse<Map<String, Object>> overview(@RequestBody(required = false) Map<String, String> body) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("todayTopUpCount", 15);
        result.put("todayTopUpAmount", new BigDecimal("15000.00"));
        result.put("todayWithdrawalCount", 45);
        result.put("todayWithdrawalAmount", new BigDecimal("8500.00"));
        result.put("todaySettlementCount", 89);
        result.put("todaySettlementAmount", new BigDecimal("25600.00"));
        result.put("todayServiceFeeAmount", new BigDecimal("1280.00"));
        return ApiResponse.success(result);
    }
}
