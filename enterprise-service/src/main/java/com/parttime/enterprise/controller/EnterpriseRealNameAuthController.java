package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.EnterpriseRealNameSubmitCmd;
import com.parttime.enterprise.pojo.vo.EnterpriseRealNameAuthVO;
import com.parttime.enterprise.service.EnterpriseRealNameAuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/enterprise/real-name")
public class EnterpriseRealNameAuthController {

    @Resource
    private EnterpriseRealNameAuthService service;

    @Operation(summary = "提交企业实名认证")
    @PostMapping
    public ResponseEntity<?> submit(@RequestBody EnterpriseRealNameSubmitCmd cmd) {
        Long enterpriseId = SecurityUtil.getCurrentCompanyId();
        try {
            EnterpriseRealNameAuthVO vo = service.submit(enterpriseId, cmd);
            return ResponseEntity.ok(vo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "获取企业实名认证状态")
    @GetMapping
    public ResponseEntity<?> getStatus() {
        Long enterpriseId = SecurityUtil.getCurrentCompanyId();
        return ResponseEntity.ok(service.getStatus(enterpriseId));
    }
}
