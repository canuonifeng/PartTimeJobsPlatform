package com.parttime.cservice.service;

import com.parttime.cservice.pojo.cmd.WorkerRealNameSubmitCmd;
import com.parttime.cservice.pojo.vo.WorkerRealNameAuthVO;

public interface WorkerRealNameAuthService {
    WorkerRealNameAuthVO submit(Long workerId, WorkerRealNameSubmitCmd cmd);
    WorkerRealNameAuthVO getStatus(Long workerId);
}
