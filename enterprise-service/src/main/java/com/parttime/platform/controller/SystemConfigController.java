package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.SystemConfigUpdateCmd;
import com.parttime.platform.pojo.vo.SystemConfigVO;
import com.parttime.platform.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/platform/configs")
public class SystemConfigController {

    @Resource
    private SystemConfigService systemConfigService;

    @Operation(summary = "获取系统配置列表", description = "获取平台所有系统配置项")
    @GetMapping
    public List<SystemConfigVO> list() {
        return systemConfigService.getAllConfigs();
    }

    @Operation(summary = "更新系统配置", description = "根据配置键更新系统配置值")
    @PutMapping
    public SystemConfigVO update(@Parameter(description = "配置键") @RequestParam String key,
                                 @RequestBody SystemConfigUpdateCmd cmd) {
        return systemConfigService.updateConfig(key, cmd.getValue());
    }
}
