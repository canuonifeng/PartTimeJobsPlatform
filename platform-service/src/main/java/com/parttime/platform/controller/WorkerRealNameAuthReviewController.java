package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.RealNameReviewCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.WorkerRealNameAuthVO;
import com.parttime.platform.service.WorkerRealNameAuthReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/worker-real-name")
public class WorkerRealNameAuthReviewController {

    @Resource
    private WorkerRealNameAuthReviewService service;

    @Operation(summary = "兼职实名认证列表")
    @GetMapping
    public PageVO<WorkerRealNameAuthVO> list(@RequestParam(required = false) String status,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int pageSize) {
        return service.list(status, page, pageSize);
    }

    @Operation(summary = "通过兼职实名认证")
    @PostMapping("/approve")
    public ApiResponse<?> approve(@RequestBody RealNameReviewCmd cmd) {
        service.approve(cmd.getId(), null);
        return ApiResponse.success();
    }

    @Operation(summary = "拒绝兼职实名认证")
    @PostMapping("/reject")
    public ApiResponse<?> reject(@RequestBody RealNameReviewCmd cmd) {
        service.reject(cmd.getId(), null, cmd.getReason());
        return ApiResponse.success();
    }
}
