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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/activities")
public class ActivityController {

    @Operation(summary = "获取活动配置列表")
    @PostMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestBody(required = false) Map<String, String> body) {
        List<Map<String, Object>> list = new ArrayList<>();
        
        Map<String, Object> a1 = new HashMap<>();
        a1.put("id", 1);
        a1.put("activityType", "NEW_USER_BONUS");
        a1.put("activityName", "新人红包");
        a1.put("bonusAmount", new BigDecimal("50.00"));
        a1.put("validDays", 30);
        a1.put("minWorkHours", 8);
        a1.put("enabled", true);
        a1.put("createdAt", LocalDateTime.now().minusDays(60).toString());
        list.add(a1);
        
        Map<String, Object> a2 = new HashMap<>();
        a2.put("id", 2);
        a2.put("activityType", "ORDER_BONUS");
        a2.put("activityName", "满单奖励");
        a2.put("orderCount", 7);
        a2.put("bonusAmount", new BigDecimal("30.00"));
        a2.put("enabled", true);
        a2.put("createdAt", LocalDateTime.now().minusDays(45).toString());
        list.add(a2);
        
        return ApiResponse.success(list);
    }

    @Operation(summary = "创建活动配置")
    @PostMapping("/create")
    public ApiResponse<Void> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }

    @Operation(summary = "更新活动配置")
    @PostMapping("/update")
    public ApiResponse<Void> update(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }

    @Operation(summary = "切换活动状态")
    @PostMapping("/toggle")
    public ApiResponse<Void> toggle(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }

    @Operation(summary = "获取活动效果统计")
    @PostMapping("/effect-stats")
    public ApiResponse<Map<String, Object>> effectStats(@RequestBody IdCmd body) {
        Map<String, Object> result = new HashMap<>();
        result.put("participantCount", 520);
        result.put("bonusTotal", new BigDecimal("15600.00"));
        result.put("incrementalJobs", 380);
        result.put("incrementalRevenue", new BigDecimal("45000.00"));
        result.put("roi", "1:2.88");
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取推送任务列表")
    @PostMapping("/push-tasks")
    public ApiResponse<List<Map<String, Object>>> pushTasks(@RequestBody(required = false) Map<String, String> body) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i);
            item.put("title", "推送标题" + i);
            item.put("content", "推送内容" + i);
            item.put("targetType", i % 2 == 0 ? "ALL_WORKERS" : "ALL_ENTERPRISES");
            item.put("targetCount", 1000 + (int)(Math.random() * 1000));
            item.put("sentCount", (int)(Math.random() * 1000));
            item.put("status", i % 3 == 0 ? "COMPLETED" : i % 3 == 1 ? "SENDING" : "PENDING");
            item.put("createdAt", LocalDateTime.now().minusDays(i).toString());
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "创建推送任务")
    @PostMapping("/push/create")
    public ApiResponse<Void> createPush(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }
}
