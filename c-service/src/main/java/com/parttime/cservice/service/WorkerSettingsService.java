package com.parttime.cservice.service;

import com.parttime.cservice.pojo.cmd.UpdateWorkerSettingsCmd;
import com.parttime.cservice.pojo.vo.WorkerSettingsVO;

public interface WorkerSettingsService {
    WorkerSettingsVO getSettings(Long workerId);
    WorkerSettingsVO updateSettings(Long workerId, UpdateWorkerSettingsCmd cmd);
}
