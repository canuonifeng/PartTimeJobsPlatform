package com.parttime.enterprise.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestRoleController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
        return "admin ok";
    }

    @GetMapping("/hr")
    @PreAuthorize("hasRole('HR')")
    public String hr() {
        return "hr ok";
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public String manager() {
        return "manager ok";
    }

    @GetMapping("/finance")
    @PreAuthorize("hasRole('FINANCE')")
    public String finance() {
        return "finance ok";
    }
}
