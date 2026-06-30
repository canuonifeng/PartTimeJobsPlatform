package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.vo.ScheduleVO;

import java.util.List;

public interface ScheduleService {
    List<ScheduleVO> list(JobQueryCmd cmd);
    ScheduleVO detail(Long id);
    void cancel(Long id);
}
