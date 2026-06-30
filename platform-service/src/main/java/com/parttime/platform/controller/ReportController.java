package com.parttime.platform.controller;

import com.parttime.platform.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
public class ReportController {

    @Operation(summary = "运营概览")
    @PostMapping("/overview")
    public ApiResponse<Map<String, Object>> overview(@RequestBody(required = false) Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        result.put("todayNewJobs", 15);
        result.put("todayNewApplications", 89);
        result.put("todayCompletedAttendance", 45);
        result.put("todayTotalSettlement", new BigDecimal("8560.00"));
        result.put("todayServiceFee", new BigDecimal("428.00"));
        result.put("todayNewEnterprises", 3);
        result.put("todayNewWorkers", 12);
        
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Map<String, Object> t = new HashMap<>();
            t.put("date", java.time.LocalDate.now().minusDays(i).toString());
            t.put("newJobs", 10 + (int)(Math.random() * 10));
            t.put("applications", 50 + (int)(Math.random() * 50));
            t.put("settlementAmount", new BigDecimal(5000 + (int)(Math.random() * 5000)));
            trend.add(t);
        }
        result.put("trend", trend);
        return ApiResponse.success(result);
    }

    @Operation(summary = "企业活跃度分析")
    @PostMapping("/enterprise-activity")
    public ApiResponse<List<Map<String, Object>>> enterpriseActivity(@RequestBody(required = false) Map<String, String> body) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("rank", i);
            item.put("enterpriseId", (long)i);
            item.put("enterpriseName", "企业名称" + i);
            item.put("jobCount", 10 + (int)(Math.random() * 20));
            item.put("applicationCount", 50 + (int)(Math.random() * 100));
            item.put("totalSettlement", new BigDecimal(10000 + (int)(Math.random() * 50000)));
            item.put("activityLevel", i <= 3 ? "HIGH" : i <= 7 ? "MEDIUM" : "LOW");
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "工人活跃度分析")
    @PostMapping("/worker-activity")
    public ApiResponse<List<Map<String, Object>>> workerActivity(@RequestBody(required = false) Map<String, String> body) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("rank", i);
            item.put("workerId", (long)i);
            item.put("workerName", "工人姓名" + i);
            item.put("workerPhone", "1380000" + String.format("%02d", i));
            item.put("completedJobs", 20 + (int)(Math.random() * 30));
            item.put("totalEarnings", new BigDecimal(5000 + (int)(Math.random() * 20000)));
            item.put("activityLevel", i <= 3 ? "HIGH" : i <= 7 ? "MEDIUM" : "LOW");
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "职位供需分析")
    @PostMapping("/supply-demand")
    public ApiResponse<Map<String, Object>> supplyDemand(@RequestBody(required = false) Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> byCategory = new ArrayList<>();
        String[] cats = {"餐饮服务", "物流配送", "家政保洁", "商超零售", "其他"};
        for (String cat : cats) {
            Map<String, Object> c = new HashMap<>();
            c.put("category", cat);
            c.put("supply", 50 + (int)(Math.random() * 100));
            c.put("demand", 80 + (int)(Math.random() * 100));
            byCategory.add(c);
        }
        result.put("byCategory", byCategory);
        return ApiResponse.success(result);
    }

    @Operation(summary = "转化漏斗分析")
    @PostMapping("/conversion-funnel")
    public ApiResponse<Map<String, Object>> conversionFunnel(@RequestBody(required = false) Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        result.put("browseCount", 10000);
        result.put("viewCount", 8500);
        result.put("applyCount", 3200);
        result.put("approvedCount", 2800);
        result.put("checkinCount", 2600);
        result.put("settlementCount", 2500);
        return ApiResponse.success(result);
    }
}
