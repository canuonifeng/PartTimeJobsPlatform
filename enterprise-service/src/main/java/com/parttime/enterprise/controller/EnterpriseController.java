package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.CompanyLogoUpdateCmd;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.pojo.vo.EnterpriseInfoVO;
import com.parttime.enterprise.service.EnterpriseService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {

    @Resource
    private EnterpriseService enterpriseService;

    @Operation(summary = "获取企业信息")
    @GetMapping
    public ApiResponse<EnterpriseInfoVO> getInfo() {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(enterpriseService.getEnterpriseInfo(companyId));
    }

    @Operation(summary = "更新企业Logo")
    @PostMapping("/logo")
    public ApiResponse<Void> updateLogo(@RequestBody CompanyLogoUpdateCmd cmd) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        String logoUrl = cmd.getCompanyLogo();
        if (logoUrl == null) {
            return ApiResponse.error("公司Logo不能为空");
        }
        enterpriseService.updateLogo(companyId, logoUrl);
        return ApiResponse.success();
    }
}
