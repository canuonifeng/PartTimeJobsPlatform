package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.IdCmd;
import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.LocationCreateCmd;
import com.parttime.enterprise.pojo.cmd.LocationUpdateCmd;
import com.parttime.enterprise.pojo.cmd.PageQueryCmd;
import com.parttime.enterprise.pojo.vo.CompanyLocationVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.CompanyLocationService;
import io.swagger.v3.oas.annotations.Operation;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/enterprise/locations")
public class CompanyLocationController {

    @Resource
    private CompanyLocationService companyLocationService;

    @Operation(summary = "获取地点列表")
    @PostMapping("/list")
    public ApiResponse<PageVO<CompanyLocationVO>> list(@RequestBody(required = false) PageQueryCmd cmd) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        Integer page = cmd == null ? null : cmd.getPage();
        Integer pageSize = cmd == null ? null : cmd.getPageSize();
        return ApiResponse.success(companyLocationService.list(companyId, page, pageSize));
    }

    @Operation(summary = "新增地点")
    @PostMapping("/create")
    public ApiResponse<CompanyLocationVO> create(@RequestBody LocationCreateCmd cmd) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(companyLocationService.create(cmd, companyId));
    }

    @Operation(summary = "修改地点")
    @PostMapping("/update")
    public ApiResponse<CompanyLocationVO> update(@RequestBody LocationUpdateCmd cmd) {
        return ApiResponse.success(companyLocationService.update(cmd));
    }

    @Operation(summary = "删除地点")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody IdCmd cmd) {
        companyLocationService.delete(cmd.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "启用地点")
    @PostMapping("/enable")
    public ApiResponse<Void> enable(@RequestBody IdCmd cmd) {
        companyLocationService.enable(cmd.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "禁用地点")
    @PostMapping("/disable")
    public ApiResponse<Void> disable(@RequestBody IdCmd cmd) {
        companyLocationService.disable(cmd.getId());
        return ApiResponse.success();
    }
}
