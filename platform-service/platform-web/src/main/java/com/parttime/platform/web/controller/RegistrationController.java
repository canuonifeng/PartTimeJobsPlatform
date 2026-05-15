package com.parttime.platform.web.controller;

import com.parttime.platform.api.dto.RegistrationListResponse;
import com.parttime.platform.api.dto.RegistrationResponse;
import com.parttime.platform.api.dto.RegistrationReviewRequest;
import com.parttime.platform.core.service.RegistrationService;
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
    public RegistrationListResponse list(@RequestParam(required = false) String status) {
        return registrationService.getRegistrations(status);
    }

    @GetMapping("/{id}")
    public RegistrationResponse get(@PathVariable Long id) {
        return registrationService.getRegistration(id);
    }

    @PutMapping("/{id}/approve")
    public RegistrationResponse approve(@PathVariable Long id, Authentication authentication) {
        return registrationService.approveRegistration(id, authentication.getName());
    }

    @PutMapping("/{id}/reject")
    public RegistrationResponse reject(@PathVariable Long id,
                                        @RequestBody RegistrationReviewRequest request,
                                        Authentication authentication) {
        return registrationService.rejectRegistration(id, authentication.getName(), request);
    }
}
