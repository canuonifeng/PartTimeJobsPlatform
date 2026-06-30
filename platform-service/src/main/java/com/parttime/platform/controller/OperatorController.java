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
@RequestMapping("/api/admin/operators")
public class OperatorController {

    @Operation(summary = "获取运营人员列表")
    @PostMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestBody(required = false) Map<String, String> body) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] roles = {"SUPER_ADMIN", "OPERATOR", "FINANCE", "CUSTOMER_SERVICE"};
        String[] roleNames = {"超级管理员", "运营专员", "财务专员", "客服专员"};
        String[] names = {"Admin", "张三", "李四", "王五"};
        for (int i = 0; i < 4; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i + 1);
            item.put("username", "operator" + (i + 1));
            item.put("name", names[i]);
            item.put("phone", "1380000" + String.format("%02d", i + 1));
            item.put("role", roles[i]);
            item.put("roleName", roleNames[i]);
            item.put("status", i < 3 ? "ACTIVE" : "DISABLED");
            item.put("lastLoginAt", LocalDateTime.now().minusDays(i).toString());
            item.put("createdAt", LocalDateTime.now().minusDays(i * 30).toString());
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "创建运营人员")
    @PostMapping("/create")
    public ApiResponse<Void> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }

    @Operation(summary = "更新运营人员")
    @PostMapping("/update")
    public ApiResponse<Void> update(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }

    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody IdCmd body) {
        return ApiResponse.success();
    }

    @Operation(summary = "切换账号状态")
    @PostMapping("/toggle-status")
    public ApiResponse<Void> toggleStatus(@RequestBody IdCmd body) {
        return ApiResponse.success();
    }

    @Operation(summary = "获取角色列表")
    @PostMapping("/roles")
    public ApiResponse<List<Map<String, Object>>> roles() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] roles = {"SUPER_ADMIN", "OPERATOR", "FINANCE", "CUSTOMER_SERVICE", "RISK_CONTROL"};
        String[] names = {"超级管理员", "运营专员", "财务专员", "客服专员", "风控专员"};
        for (int i = 0; i < roles.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i + 1);
            item.put("roleCode", roles[i]);
            item.put("roleName", names[i]);
            list.add(item);
        }
        return ApiResponse.success(list);
    }
}
