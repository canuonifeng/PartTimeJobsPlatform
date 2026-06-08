package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.cmd.WorkerRealNameSubmitCmd;
import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.pojo.vo.WorkerRealNameAuthVO;
import com.parttime.cservice.service.WorkerRealNameAuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/worker/real-name")
public class WorkerRealNameAuthController {

    @Resource
    private WorkerRealNameAuthService workerRealNameAuthService;

    private Long getCurrentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    @Operation(summary = "提交兼职实名认证")
    @PostMapping
    public ApiResponse<WorkerRealNameAuthVO> submit(@RequestBody WorkerRealNameSubmitCmd cmd) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        try {
            WorkerRealNameAuthVO vo = workerRealNameAuthService.submit(workerId, cmd);
            return ApiResponse.success(vo);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "获取兼职实名认证状态")
    @GetMapping
    public ApiResponse<WorkerRealNameAuthVO> getStatus() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        return ApiResponse.success(workerRealNameAuthService.getStatus(workerId));
    }
}
