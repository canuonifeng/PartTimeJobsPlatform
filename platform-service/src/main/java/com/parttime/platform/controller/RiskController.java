package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/risk")
public class RiskController {

    @Operation(summary = "获取黑名单列表")
    @PostMapping("/blacklist")
    public ApiResponse<List<Map<String, Object>>> blacklist(@RequestBody(required = false) Map<String, String> body) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i);
            item.put("userType", i % 2 == 0 ? "WORKER" : "ENTERPRISE");
            item.put("userName", "测试用户" + i);
            item.put("userPhone", "1380000" + String.format("%02d", i));
            item.put("reason", "违规操作");
            item.put("operator", "admin");
            item.put("createdAt", LocalDateTime.now().minusDays(i).toString());
            item.put("expireAt", LocalDateTime.now().plusDays(30 - i).toString());
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "加入黑名单")
    @PostMapping("/blacklist/add")
    public ApiResponse<Void> addToBlacklist(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }

    @Operation(summary = "移出黑名单")
    @PostMapping("/blacklist/remove")
    public ApiResponse<Void> removeFromBlacklist(@RequestBody IdCmd body) {
        return ApiResponse.success();
    }

    @Operation(summary = "获取白名单列表")
    @PostMapping("/whitelist")
    public ApiResponse<List<Map<String, Object>>> whitelist(@RequestBody(required = false) Map<String, String> body) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i);
            item.put("userType", "ENTERPRISE");
            item.put("userName", "白名单企业" + i);
            item.put("reason", "优质合作企业");
            item.put("operator", "admin");
            item.put("createdAt", LocalDateTime.now().minusDays(i * 10).toString());
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "获取风控规则列表")
    @PostMapping("/rules")
    public ApiResponse<List<Map<String, Object>>> rules() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[][] rules = {
            {"1", "频繁取消报名", "24小时内取消超过3次", "限制报名24小时", "true"},
            {"2", "异常打卡定位", "连续3次定位偏差超过500米", "标记异常考勤", "true"},
            {"3", "恶意投诉", "投诉成立超过3次", "限制投诉功能", "true"},
            {"4", "拖欠工资", "超过7天未结算", "企业标记预警", "true"},
        };
        for (String[] r : rules) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", Integer.parseInt(r[0]));
            item.put("ruleName", r[1]);
            item.put("triggerCondition", r[2]);
            item.put("action", r[3]);
            item.put("enabled", Boolean.parseBoolean(r[4]));
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "切换风控规则状态")
    @PostMapping("/rules/toggle")
    public ApiResponse<Void> toggleRule(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }
}
