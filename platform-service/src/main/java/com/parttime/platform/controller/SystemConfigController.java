package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.SystemConfigUpdateCmd;
import com.parttime.platform.pojo.vo.SystemConfigVO;
import com.parttime.platform.service.SystemConfigService;
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
    public List<SystemConfigVO> list() {
        return systemConfigService.getAllConfigs();
    }

    @PutMapping("/{key}")
    public SystemConfigVO update(@PathVariable String key, @RequestBody SystemConfigUpdateCmd cmd) {
        return systemConfigService.updateConfig(key, cmd.getValue());
    }
}
