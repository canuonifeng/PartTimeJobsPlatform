package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.cmd.WorkerListQueryCmd;
import com.parttime.platform.pojo.cmd.WorkerUpdateCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.WorkerVO;
import com.parttime.platform.service.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/workers")
public class WorkerController {

    @Resource
    private WorkerService workerService;

    @Operation(summary = "获取兼职列表")
    @PostMapping("/list")
    public ApiResponse<List<WorkerVO>> list(@RequestBody(required = false) WorkerListQueryCmd body) {
        String status = body != null ? body.getStatus() : null;
        String keyword = body != null ? body.getKeyword() : null;
        return ApiResponse.success(workerService.list(status, keyword));
    }

    @Operation(summary = "获取兼职详情")
    @PostMapping("/detail")
    public ApiResponse<WorkerVO> detail(@RequestBody IdCmd body) {
        return ApiResponse.success(workerService.detail(body.getId()));
    }

    @Operation(summary = "编辑兼职信息")
    @PostMapping("/update")
    public ApiResponse<WorkerVO> update(@RequestBody WorkerUpdateCmd cmd) {
        return ApiResponse.success(workerService.update(cmd));
    }

    @Operation(summary = "封禁兼职")
    @PostMapping("/ban")
    public void ban(@RequestBody IdCmd body) {
        workerService.ban(body.getId());
    }

    @Operation(summary = "解封兼职")
    @PostMapping("/unban")
    public void unban(@RequestBody IdCmd body) {
        workerService.unban(body.getId());
    }
}
