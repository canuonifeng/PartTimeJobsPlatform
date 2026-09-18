package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.ExternalWorkerMappingCreateCmd;
import com.parttime.enterprise.pojo.entity.ExternalWorkerMapping;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.service.ExternalWorkerMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "外部工人映射", description = "外部系统人员映射管理")
@RestController
@RequestMapping("/api/enterprise/external-worker-mapping")
public class ExternalWorkerMappingController {

    @Resource
    private ExternalWorkerMappingService externalWorkerMappingService;

    @Operation(summary = "新增人员映射")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody ExternalWorkerMappingCreateCmd cmd) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        externalWorkerMappingService.createMapping(
                cmd.getWorkerId(),
                cmd.getExternalSystemType(),
                cmd.getExternalWorkerId(),
                companyId
        );
        return ApiResponse.success();
    }

    @Operation(summary = "查询人员映射列表")
    @GetMapping
    public ApiResponse<List<ExternalWorkerMapping>> list(@RequestParam(required = false) Long workerId) {
        if (workerId != null) {
            return ApiResponse.success(externalWorkerMappingService.getMappingsByWorkerId(workerId));
        }
        return ApiResponse.success(List.of());
    }
}
