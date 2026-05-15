package com.parttime.platform.web.controller;

import com.parttime.platform.api.dto.SystemConfigResponse;
import com.parttime.platform.api.dto.SystemConfigUpdateRequest;
import com.parttime.platform.core.service.SystemConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/configs")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping
    public List<SystemConfigResponse> list() {
        return systemConfigService.getAllConfigs();
    }

    @PutMapping("/{key}")
    public SystemConfigResponse update(@PathVariable String key, @RequestBody SystemConfigUpdateRequest request) {
        return systemConfigService.updateConfig(key, request.getValue());
    }
}
