package com.parttime.platform.controller;

import com.parttime.platform.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/finance")
public class FinanceReportController {

    @Operation(summary = "获取每日对账概览")
    @PostMapping("/daily-summary")
    public ApiResponse<List<Map<String, Object>>> dailySummary(@RequestBody(required = false) Map<String, String> body) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Map<String, Object> item = new HashMap<>();
            LocalDate date = LocalDate.now().minusDays(i);
            item.put("date", date.toString());
            item.put("totalTopUp", BigDecimal.valueOf(1000 + i * 500));
            item.put("totalWithdrawal", BigDecimal.valueOf(800 + i * 300));
            item.put("totalServiceFee", BigDecimal.valueOf(50 + i * 20));
            item.put("settlementCount", 10 + i * 5);
            item.put("status", "RECONCILED");
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "服务费统计")
    @PostMapping("/service-fee-stats")
    public ApiResponse<Map<String, Object>> serviceFeeStats(@RequestBody(required = false) Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalServiceFee", new BigDecimal("5000.00"));
        result.put("todayServiceFee", new BigDecimal("150.00"));
        result.put("weekServiceFee", new BigDecimal("1200.00"));
        result.put("monthServiceFee", new BigDecimal("4500.00"));
        
        List<Map<String, Object>> byCategory = new ArrayList<>();
        String[] cats = {"餐饮服务", "物流配送", "家政保洁", "商超零售", "其他"};
        for (String cat : cats) {
            Map<String, Object> c = new HashMap<>();
            c.put("category", cat);
            c.put("amount", BigDecimal.valueOf((int)(Math.random() * 1000 + 100)));
            byCategory.add(c);
        }
        result.put("byCategory", byCategory);
        return ApiResponse.success(result);
    }
}
