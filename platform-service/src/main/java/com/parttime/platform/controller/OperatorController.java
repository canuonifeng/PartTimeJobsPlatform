package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.OperatorCreateCmd;
import com.parttime.platform.pojo.cmd.OperatorUpdateCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.OperatorVO;
import com.parttime.platform.pojo.vo.RoleVO;
import com.parttime.platform.service.OperatorService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/operators")
public class OperatorController {

    @Resource
    private OperatorService operatorService;

    @Operation(summary = "获取运营人员列表")
    @PostMapping("/list")
    public ApiResponse<List<OperatorVO>> list(@RequestBody(required = false) IdCmd body) {
        return ApiResponse.success(operatorService.list());
    }

    @Operation(summary = "创建运营人员")
    @PostMapping("/create")
    public ApiResponse<Void> create(@RequestBody OperatorCreateCmd body) {
        operatorService.create(body);
        return ApiResponse.success();
    }

    @Operation(summary = "更新运营人员")
    @PostMapping("/update")
    public ApiResponse<Void> update(@RequestBody OperatorUpdateCmd body) {
        operatorService.update(body);
        return ApiResponse.success();
    }

    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody IdCmd body) {
        operatorService.resetPassword(body);
        return ApiResponse.success();
    }

    @Operation(summary = "切换账号状态")
    @PostMapping("/toggle-status")
    public ApiResponse<Void> toggleStatus(@RequestBody IdCmd body) {
        operatorService.toggleStatus(body);
        return ApiResponse.success();
    }

    @Operation(summary = "获取角色列表与权限矩阵")
    @PostMapping("/roles")
    public ApiResponse<List<RoleVO>> roles() {
        return ApiResponse.success(operatorService.roles());
    }
}
