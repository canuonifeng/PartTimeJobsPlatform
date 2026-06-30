package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
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
@RequestMapping("/api/admin/settlements")
public class SettlementController {

    @Operation(summary = "获取结算列表")
    @PostMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestBody(required = false) Map<String, String> body) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] statuses = {"PENDING", "PAID", "CANCELLED"};
        for (int i = 1; i <= 20; i++) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", (long) i);
            item.put("settlementNo", "STL" + System.currentTimeMillis() / 1000 + i);
            item.put("workerId", (long) (100 + i));
            item.put("workerName", "工人" + i);
            item.put("workerPhone", "1380000" + String.format("%02d", i % 100));
            item.put("enterpriseId", (long) (10 + i % 5));
            item.put("enterpriseName", "企业" + (i % 5 + 1));
            item.put("jobId", (long) (i % 10 + 1));
            item.put("jobTitle", "职位名称" + (i % 10 + 1));
            item.put("shiftId", (long) (i * 10));
            item.put("totalHours", new BigDecimal((i % 8 + 1) + "." + (i % 10)));
            item.put("basePay", new BigDecimal((i % 8 + 1) * 20));
            item.put("bonus", new BigDecimal((i % 5) * 10));
            item.put("deduction", new BigDecimal((i % 3) * 5));
            item.put("totalAmount", new BigDecimal((i % 8 + 1) * 20 + (i % 5) * 10 - (i % 3) * 5));
            item.put("serviceFee", new BigDecimal((i % 8 + 1) * 2));
            item.put("actualPay", new BigDecimal((i % 8 + 1) * 18));
            item.put("status", statuses[i % 3]);
            item.put("settledAt", i % 3 == 1 ? LocalDateTime.now().minusDays(i).toString() : null);
            item.put("createdAt", LocalDateTime.now().minusDays(i).toString());
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "获取结算详情")
    @PostMapping("/detail")
    public ApiResponse<Map<String, Object>> detail(@RequestBody IdCmd body) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", body.getId());
        result.put("settlementNo", "STL" + System.currentTimeMillis() / 1000);
        result.put("workerName", "测试工人");
        result.put("workerPhone", "13800000001");
        result.put("enterpriseName", "测试企业");
        result.put("jobTitle", "测试职位");
        result.put("totalHours", new BigDecimal("8.5"));
        result.put("basePay", new BigDecimal("170.00"));
        result.put("bonus", new BigDecimal("20.00"));
        result.put("deduction", new BigDecimal("5.00"));
        result.put("totalAmount", new BigDecimal("185.00"));
        result.put("serviceFee", new BigDecimal("9.25"));
        result.put("actualPay", new BigDecimal("175.75"));
        result.put("status", "PENDING");
        result.put("createdAt", LocalDateTime.now().minusHours(2).toString());
        return ApiResponse.success(result);
    }

    @Operation(summary = "确认结算")
    @PostMapping("/confirm")
    public ApiResponse<Void> confirm(@RequestBody IdCmd body) {
        return ApiResponse.success();
    }

    @Operation(summary = "取消结算")
    @PostMapping("/cancel")
    public ApiResponse<Void> cancel(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }
}
