package com.parttime.cservice.controller;

import com.parttime.cservice.mapper.WorkerSettingsMapper;
import com.parttime.cservice.pojo.cmd.UpdateWorkerSettingsCmd;
import com.parttime.cservice.pojo.entity.WorkerSettings;
import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.pojo.vo.WorkerSettingsVO;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class WorkerSettingsController {
    @Resource
    private WorkerSettingsMapper workerSettingsMapper;

    @GetMapping
    public ApiResponse<WorkerSettingsVO> getSettings() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) return ApiResponse.error(401, "未登录");
        return ApiResponse.success(toVO(workerSettingsMapper.findByWorkerId(workerId)));
    }

    @PutMapping
    public ApiResponse<WorkerSettingsVO> updateSettings(@RequestBody UpdateWorkerSettingsCmd cmd) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) return ApiResponse.error(401, "未登录");
        WorkerSettingsVO current = toVO(workerSettingsMapper.findByWorkerId(workerId));
        Boolean pushEnabled = cmd.getPushEnabled() == null ? current.getPushEnabled() : cmd.getPushEnabled();
        Boolean locationEnabled = cmd.getLocationEnabled() == null ? current.getLocationEnabled() : cmd.getLocationEnabled();
        Boolean quietEnabled = cmd.getQuietEnabled() == null ? current.getQuietEnabled() : cmd.getQuietEnabled();
        workerSettingsMapper.upsert(workerId, pushEnabled, locationEnabled, quietEnabled);
        WorkerSettingsVO result = new WorkerSettingsVO();
        result.setPushEnabled(pushEnabled);
        result.setLocationEnabled(locationEnabled);
        result.setQuietEnabled(quietEnabled);
        return ApiResponse.success(result);
    }

    private Long getCurrentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    private WorkerSettingsVO toVO(WorkerSettings settings) {
        if (settings == null) return WorkerSettingsVO.defaults();
        WorkerSettingsVO vo = new WorkerSettingsVO();
        vo.setPushEnabled(settings.getPushEnabled() == null ? true : settings.getPushEnabled());
        vo.setLocationEnabled(settings.getLocationEnabled() == null ? true : settings.getLocationEnabled());
        vo.setQuietEnabled(settings.getQuietEnabled() == null ? false : settings.getQuietEnabled());
        return vo;
    }
}
