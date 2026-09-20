package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.OperationLogQueryCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.OperationLogVO;
import com.parttime.platform.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/operation-logs")
public class OperationLogController {

    @Resource
    private OperationLogService operationLogService;

    @Operation(summary = "获取操作日志列表")
    @PostMapping("/list")
    public ApiResponse<List<OperationLogVO>> list(@RequestBody(required = false) OperationLogQueryCmd body) {
        return ApiResponse.success(operationLogService.list(body));
    }
}
