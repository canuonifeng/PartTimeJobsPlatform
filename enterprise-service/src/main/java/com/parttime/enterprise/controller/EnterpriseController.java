package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.service.EnterpriseService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {

    @Resource
    private EnterpriseService enterpriseService;

    @Operation(summary = "获取企业信息")
    @GetMapping
    public ApiResponse<Map<String, Object>> getInfo() {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(enterpriseService.getEnterpriseInfo(companyId));
    }

    @Operation(summary = "更新企业Logo")
    @PutMapping("/logo")
    public ApiResponse<Void> updateLogo(@RequestBody Map<String, String> body) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        String logoUrl = body.get("companyLogo");
        if (logoUrl == null) {
            return ApiResponse.error("公司Logo不能为空");
        }
        enterpriseService.updateLogo(companyId, logoUrl);
        return ApiResponse.success();
    }
}
