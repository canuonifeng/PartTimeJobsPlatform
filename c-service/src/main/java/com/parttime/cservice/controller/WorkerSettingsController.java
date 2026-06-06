package com.parttime.cservice.controller;

import com.parttime.cservice.mapper.WorkerSettingsMapper;
import com.parttime.cservice.pojo.cmd.UpdateWorkerSettingsCmd;
import com.parttime.cservice.pojo.entity.WorkerSettings;
import com.parttime.cservice.pojo.vo.WorkerSettingsVO;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<WorkerSettingsVO> getSettings() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(toVO(workerSettingsMapper.findByWorkerId(workerId)));
    }

    @PutMapping
    public ResponseEntity<WorkerSettingsVO> updateSettings(@RequestBody UpdateWorkerSettingsCmd cmd) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        WorkerSettingsVO current = toVO(workerSettingsMapper.findByWorkerId(workerId));
        Boolean pushEnabled = cmd.getPushEnabled() == null ? current.getPushEnabled() : cmd.getPushEnabled();
        Boolean locationEnabled = cmd.getLocationEnabled() == null ? current.getLocationEnabled() : cmd.getLocationEnabled();
        Boolean quietEnabled = cmd.getQuietEnabled() == null ? current.getQuietEnabled() : cmd.getQuietEnabled();
        workerSettingsMapper.upsert(workerId, pushEnabled, locationEnabled, quietEnabled);
        WorkerSettingsVO result = new WorkerSettingsVO();
        result.setPushEnabled(pushEnabled);
        result.setLocationEnabled(locationEnabled);
        result.setQuietEnabled(quietEnabled);
        return ResponseEntity.ok(result);
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
