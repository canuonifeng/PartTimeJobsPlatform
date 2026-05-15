package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.ReviewRegistrationCmd;
import com.parttime.platform.pojo.vo.RegistrationListVO;
import com.parttime.platform.pojo.vo.RegistrationVO;
import com.parttime.platform.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    @Resource
    private RegistrationService registrationService;

    @Operation(summary = "获取入驻申请列表", description = "分页查询企业入驻申请，可按状态筛选")
    @GetMapping
    public RegistrationListVO list(@Parameter(description = "筛选状态: PENDING/APPROVED/REJECTED") @RequestParam(required = false) String status) {
        return registrationService.getRegistrations(status);
    }

    @Operation(summary = "获取入驻申请详情", description = "根据ID查询企业入驻申请的详细信息")
    @GetMapping(params = "id")
    public RegistrationVO get(@Parameter(description = "申请ID") @RequestParam Long id) {
        return registrationService.getRegistration(id);
    }

    @Operation(summary = "审核通过入驻申请", description = "审核通过企业的入驻申请")
    @PutMapping("/approve")
    public RegistrationVO approve(@Parameter(description = "申请ID") @RequestParam Long id,
                                  Authentication authentication) {
        return registrationService.approveRegistration(id, authentication.getName());
    }

    @Operation(summary = "驳回入驻申请", description = "驳回企业的入驻申请，需填写驳回原因")
    @PutMapping("/reject")
    public RegistrationVO reject(@Parameter(description = "申请ID") @RequestParam Long id,
                                 @RequestBody ReviewRegistrationCmd cmd,
                                 Authentication authentication) {
        return registrationService.rejectRegistration(id, authentication.getName(), cmd);
    }
}
