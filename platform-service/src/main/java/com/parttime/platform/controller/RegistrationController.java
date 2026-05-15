package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.ReviewRegistrationCmd;
import com.parttime.platform.pojo.vo.RegistrationListVO;
import com.parttime.platform.pojo.vo.RegistrationVO;
import com.parttime.platform.service.RegistrationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public RegistrationListVO list(@RequestParam(required = false) String status) {
        return registrationService.getRegistrations(status);
    }

    @GetMapping("/{id}")
    public RegistrationVO get(@PathVariable Long id) {
        return registrationService.getRegistration(id);
    }

    @PutMapping("/{id}/approve")
    public RegistrationVO approve(@PathVariable Long id, Authentication authentication) {
        return registrationService.approveRegistration(id, authentication.getName());
    }

    @PutMapping("/{id}/reject")
    public RegistrationVO reject(@PathVariable Long id,
                                        @RequestBody ReviewRegistrationCmd cmd,
                                        Authentication authentication) {
        return registrationService.rejectRegistration(id, authentication.getName(), cmd);
    }
}
