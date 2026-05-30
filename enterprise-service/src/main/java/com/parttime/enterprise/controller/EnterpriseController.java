package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
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
    private com.parttime.enterprise.mapper.EnterpriseMapper enterpriseMapper;

    @Operation(summary = "获取企业信息")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getInfo() {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        String name = enterpriseMapper.findCompanyNameById(companyId);
        String logo = enterpriseMapper.findCompanyLogoById(companyId);
        return ResponseEntity.ok(Map.of("id", companyId, "companyName", name, "companyLogo", logo));
    }

    @Operation(summary = "更新企业Logo")
    @PutMapping("/logo")
    public ResponseEntity<Void> updateLogo(@RequestBody Map<String, String> body) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        String logoUrl = body.get("companyLogo");
        if (logoUrl == null) {
            return ResponseEntity.badRequest().build();
        }
        enterpriseMapper.updateLogo(companyId, logoUrl);
        return ResponseEntity.ok().build();
    }
}
