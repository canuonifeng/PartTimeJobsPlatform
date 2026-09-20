package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.vo.ApplicationVO;

import java.util.List;

public interface ApplicationService {
    List<ApplicationVO> list(JobQueryCmd cmd);
    List<ApplicationVO> listByJobId(Long jobId);
    ApplicationVO detail(Long id);
    void accept(Long id);
    void reject(Long id);
    void batchAccept(List<Long> ids);
    void batchReject(List<Long> ids);
}
