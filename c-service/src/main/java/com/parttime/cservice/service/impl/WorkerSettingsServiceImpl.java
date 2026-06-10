package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.WorkerSettingsMapper;
import com.parttime.cservice.pojo.cmd.UpdateWorkerSettingsCmd;
import com.parttime.cservice.pojo.entity.WorkerSettings;
import com.parttime.cservice.pojo.vo.WorkerSettingsVO;
import com.parttime.cservice.service.WorkerSettingsService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class WorkerSettingsServiceImpl implements WorkerSettingsService {

    @Resource
    private WorkerSettingsMapper workerSettingsMapper;

    @Override
    public WorkerSettingsVO getSettings(Long workerId) {
        return toVO(workerSettingsMapper.findByWorkerId(workerId));
    }

    @Override
    public WorkerSettingsVO updateSettings(Long workerId, UpdateWorkerSettingsCmd cmd) {
        WorkerSettingsVO current = toVO(workerSettingsMapper.findByWorkerId(workerId));
        Boolean pushEnabled = cmd.getPushEnabled() == null ? current.getPushEnabled() : cmd.getPushEnabled();
        Boolean locationEnabled = cmd.getLocationEnabled() == null ? current.getLocationEnabled() : cmd.getLocationEnabled();
        Boolean quietEnabled = cmd.getQuietEnabled() == null ? current.getQuietEnabled() : cmd.getQuietEnabled();
        workerSettingsMapper.upsert(workerId, pushEnabled, locationEnabled, quietEnabled);
        WorkerSettingsVO result = new WorkerSettingsVO();
        result.setPushEnabled(pushEnabled);
        result.setLocationEnabled(locationEnabled);
        result.setQuietEnabled(quietEnabled);
        return result;
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
