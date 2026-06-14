package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.IdCmd;
import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.TemplateCreateCmd;
import com.parttime.enterprise.pojo.cmd.TemplateUpdateCmd;
import com.parttime.enterprise.pojo.vo.JobTemplateVO;
import com.parttime.enterprise.service.JobTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class JobTemplateController {

    @Resource
    private JobTemplateService jobTemplateService;

    @Operation(summary = "获取模版列表")
    @PostMapping("/list")
    public ApiResponse<List<JobTemplateVO>> list() {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(jobTemplateService.list(companyId));
    }

    @Operation(summary = "新增模版")
    @PostMapping("/create")
    public ApiResponse<JobTemplateVO> create(@RequestBody TemplateCreateCmd cmd) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(jobTemplateService.create(cmd, companyId));
    }

    @Operation(summary = "修改模版")
    @PostMapping("/update")
    public ApiResponse<JobTemplateVO> update(@RequestBody TemplateUpdateCmd cmd) {
        return ApiResponse.success(jobTemplateService.update(cmd));
    }

    @Operation(summary = "删除模版")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody IdCmd cmd) {
        jobTemplateService.delete(cmd.getId());
        return ApiResponse.success();
    }
}
