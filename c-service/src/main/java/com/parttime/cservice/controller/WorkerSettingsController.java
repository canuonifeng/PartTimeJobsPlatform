package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.cmd.UpdateWorkerSettingsCmd;
import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.pojo.vo.WorkerSettingsVO;
import com.parttime.cservice.service.WorkerSettingsService;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/worker/settings")
public class WorkerSettingsController {
    @Resource
    private WorkerSettingsService workerSettingsService;

    @GetMapping
    public ApiResponse<WorkerSettingsVO> getSettings() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) return ApiResponse.error(401, "未登录");
        return ApiResponse.success(workerSettingsService.getSettings(workerId));
    }

    @PostMapping
    public ApiResponse<WorkerSettingsVO> updateSettings(@RequestBody UpdateWorkerSettingsCmd cmd) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) return ApiResponse.error(401, "未登录");
        return ApiResponse.success(workerSettingsService.updateSettings(workerId, cmd));
    }

    private Long getCurrentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }
}
