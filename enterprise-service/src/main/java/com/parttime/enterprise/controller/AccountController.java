package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.AccountCreateCmd;
import com.parttime.enterprise.pojo.cmd.AccountPasswordUpdateCmd;
import com.parttime.enterprise.pojo.cmd.AccountResetPasswordCmd;
import com.parttime.enterprise.pojo.cmd.AccountSecurityUpdateCmd;
import com.parttime.enterprise.pojo.cmd.AccountUpdateCmd;
import com.parttime.enterprise.pojo.cmd.IdCmd;
import com.parttime.enterprise.pojo.cmd.PageQueryCmd;
import com.parttime.enterprise.pojo.vo.AccountVO;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enterprise/accounts")
public class AccountController {

    @Resource
    private AccountService accountService;

    @Operation(summary = "获取账号列表")
    @PostMapping("/list")
    public ApiResponse<PageVO<AccountVO>> list(@RequestBody(required = false) PageQueryCmd cmd) {
        Long enterpriseId = SecurityUtil.getCurrentCompanyId();
        Integer page = cmd == null ? null : cmd.getPage();
        Integer pageSize = cmd == null ? null : cmd.getPageSize();
        return ApiResponse.success(accountService.list(enterpriseId, page, pageSize));
    }

    @Operation(summary = "获取当前账号安全信息")
    @GetMapping("/me")
    public ApiResponse<AccountVO> me() {
        Long enterpriseId = SecurityUtil.getCurrentCompanyId();
        Long accountId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(accountService.getCurrent(accountId, enterpriseId));
    }

    @Operation(summary = "更新当前账号安全信息")
    @PostMapping("/me")
    public ApiResponse<AccountVO> updateMe(@RequestBody AccountSecurityUpdateCmd cmd) {
        Long enterpriseId = SecurityUtil.getCurrentCompanyId();
        Long accountId = SecurityUtil.getCurrentUserId();
        return ApiResponse.success(accountService.updateCurrent(accountId, enterpriseId, cmd));
    }

    @Operation(summary = "修改当前账号密码")
    @PostMapping("/me/password")
    public ApiResponse<Void> updateMyPassword(@RequestBody AccountPasswordUpdateCmd cmd) {
        Long enterpriseId = SecurityUtil.getCurrentCompanyId();
        Long accountId = SecurityUtil.getCurrentUserId();
        accountService.updateCurrentPassword(accountId, enterpriseId, cmd);
        return ApiResponse.success();
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
    public void delete(@RequestBody IdCmd cmd) {
        accountService.delete(cmd.getId());
    }
}
