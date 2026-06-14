package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.EnterpriseIdCmd;
import com.parttime.platform.pojo.cmd.EnterpriseAccountCreateCmd;
import com.parttime.platform.pojo.cmd.EnterpriseAccountUpdateCmd;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.ResetPasswordCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.EnterpriseAccountVO;
import com.parttime.platform.service.EnterpriseAccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class EnterpriseAccountController {

    @Resource
    private EnterpriseAccountService accountService;

    @Operation(summary = "获取企业下账号列表")
    @PostMapping("/enterprises/accounts/list")
    public ApiResponse<List<EnterpriseAccountVO>> list(@RequestBody EnterpriseIdCmd body) {
        return ApiResponse.success(accountService.listByEnterprise(body.getEnterpriseId()));
    }

    @Operation(summary = "创建账号")
    @PostMapping("/enterprises/accounts/create")
    public ApiResponse<EnterpriseAccountVO> create(@RequestBody EnterpriseAccountCreateCmd cmd) {
        return ApiResponse.success(accountService.create(cmd));
    }

    @Operation(summary = "更新账号")
    @PostMapping("/accounts/update")
    public ApiResponse<EnterpriseAccountVO> update(@RequestBody EnterpriseAccountUpdateCmd cmd) {
        return ApiResponse.success(accountService.update(cmd));
    }

    @Operation(summary = "重置密码")
    @PostMapping("/accounts/reset-password")
    public void resetPassword(@RequestBody ResetPasswordCmd cmd) {
        accountService.resetPassword(cmd);
    }

    @Operation(summary = "删除账号")
    @PostMapping("/accounts/delete")
    public void delete(@RequestBody IdCmd body) {
        accountService.delete(body.getId());
    }
}
