package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.AccountCreateCmd;
import com.parttime.enterprise.pojo.cmd.AccountResetPasswordCmd;
import com.parttime.enterprise.pojo.cmd.AccountUpdateCmd;
import com.parttime.enterprise.pojo.vo.AccountVO;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Resource
    private AccountService accountService;

    @Operation(summary = "获取账号列表")
    @PostMapping("/list")
    public ApiResponse<List<AccountVO>> list() {
        Long enterpriseId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(accountService.list(enterpriseId));
    }

    @Operation(summary = "新增账号")
    @PostMapping("/create")
    public ApiResponse<AccountVO> create(@RequestBody AccountCreateCmd cmd) {
        Long enterpriseId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(accountService.create(cmd, enterpriseId));
    }

    @Operation(summary = "编辑账号")
    @PostMapping("/update")
    public ApiResponse<AccountVO> update(@RequestBody AccountUpdateCmd cmd) {
        return ApiResponse.success(accountService.update(cmd));
    }

    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public void resetPassword(@RequestBody AccountResetPasswordCmd cmd) {
        accountService.resetPassword(cmd);
    }

    @Operation(summary = "删除账号")
    @PostMapping("/delete")
    public void delete(@RequestBody Map<String, Long> body) {
        accountService.delete(body.get("id"));
    }
}
