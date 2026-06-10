package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.pojo.entity.SystemConfig;
import com.parttime.cservice.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/configs")
public class PublicConfigController {

    @Resource
    private SystemConfigService systemConfigService;

    @Operation(summary = "获取协议内容", description = "公开接口，获取用户协议或隐私政策内容")
    @GetMapping
    public ApiResponse<Map<String, String>> getConfig(@Parameter(description = "配置key") @RequestParam String key) {
        if (!"user_agreement".equals(key) && !"privacy_policy".equals(key) && !"check_in_radius_meters".equals(key)) {
            return ApiResponse.error(400, "不支持的配置项");
        }
        SystemConfig config = systemConfigService.findByKey(key).orElse(null);
        if (config == null) {
            return ApiResponse.error(404, "内容不存在");
        }
        return ApiResponse.success(Map.of(
                "key", config.getConfigKey(),
                "value", config.getConfigValue(),
                "updatedAt", config.getUpdatedAt() != null ? config.getUpdatedAt().toString() : ""
        ));
    }
}
