package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.ReviewRegistrationCmd;
import com.parttime.platform.pojo.vo.RegistrationListVO;
import com.parttime.platform.pojo.vo.RegistrationVO;
import com.parttime.platform.service.RegistrationService;
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

    @GetMapping
    public RegistrationListVO list(@RequestParam(required = false) String status) {
        return registrationService.getRegistrations(status);
    }

    @GetMapping(params = "id")
    public RegistrationVO get(@RequestParam Long id) {
        return registrationService.getRegistration(id);
    }

    @PutMapping("/approve")
    public RegistrationVO approve(@RequestParam Long id, Authentication authentication) {
        return registrationService.approveRegistration(id, authentication.getName());
    }

    @PutMapping("/reject")
    public RegistrationVO reject(@RequestParam Long id,
                                        @RequestBody ReviewRegistrationCmd cmd,
                                        Authentication authentication) {
        return registrationService.rejectRegistration(id, authentication.getName(), cmd);
    }
}
