package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestRoleController {

    @Operation(summary = "测试管理员权限", description = "测试ADMIN角色的权限控制")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> admin() {
        return ApiResponse.success("admin ok");
    }

    @Operation(summary = "测试HR权限", description = "测试HR角色的权限控制")
    @GetMapping("/hr")
    @PreAuthorize("hasRole('HR')")
    public ApiResponse<String> hr() {
        return ApiResponse.success("hr ok");
    }

    @Operation(summary = "测试经理权限", description = "测试MANAGER角色的权限控制")
    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<String> manager() {
        return ApiResponse.success("manager ok");
    }

    @Operation(summary = "测试财务权限", description = "测试FINANCE角色的权限控制")
    @GetMapping("/finance")
    @PreAuthorize("hasRole('FINANCE')")
    public ApiResponse<String> finance() {
        return ApiResponse.success("finance ok");
    }
}
