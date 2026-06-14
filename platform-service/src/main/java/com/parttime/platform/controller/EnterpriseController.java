package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.StatusQueryCmd;
import com.parttime.platform.pojo.cmd.EnterpriseCreateCmd;
import com.parttime.platform.pojo.cmd.EnterpriseUpdateCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.EnterpriseVO;
import com.parttime.platform.service.EnterpriseService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/enterprises")
public class EnterpriseController {

    @Resource
    private EnterpriseService enterpriseService;

    @Operation(summary = "获取企业列表")
    @PostMapping("/list")
    public ApiResponse<List<EnterpriseVO>> list(@RequestBody(required = false) StatusQueryCmd body) {
        String status = body != null ? body.getStatus() : null;
        return ApiResponse.success(enterpriseService.list(status));
    }

    @Operation(summary = "获取企业详情")
    @PostMapping("/detail")
    public ApiResponse<EnterpriseVO> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(enterpriseService.detail(body.getId()));
    }

    @Operation(summary = "新增企业")
    @PostMapping("/create")
    public ApiResponse<EnterpriseVO> create(@RequestBody EnterpriseCreateCmd cmd) {
        return ApiResponse.success(enterpriseService.create(cmd));
    }

    @Operation(summary = "更新企业信息")
    @PostMapping("/update")
    public ApiResponse<EnterpriseVO> update(@RequestBody EnterpriseUpdateCmd cmd) {
        return ApiResponse.success(enterpriseService.update(cmd));
    }

    @Operation(summary = "停用企业")
    @PostMapping("/suspend")
    public void suspend(@RequestBody IdCmd body) {
        enterpriseService.suspend(body.getId());
    }

    @Operation(summary = "启用企业")
    @PostMapping("/activate")
    public void activate(@RequestBody IdCmd body) {
        enterpriseService.activate(body.getId());
    }
}
