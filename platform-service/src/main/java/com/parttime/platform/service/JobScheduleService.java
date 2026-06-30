package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.vo.JobScheduleVO;

import java.util.List;

public interface JobScheduleService {
    List<JobScheduleVO> list(JobQueryCmd cmd);
    List<JobScheduleVO> listByJobId(Long jobId);
    JobScheduleVO detail(Long id);
    void cancel(Long id);
}
