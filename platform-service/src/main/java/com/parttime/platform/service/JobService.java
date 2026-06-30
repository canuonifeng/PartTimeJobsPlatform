package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.vo.JobVO;

import java.util.List;

public interface JobService {
    List<JobVO> list(JobQueryCmd cmd);
    JobVO detail(Long id);
    void closeJob(Long id);
    void reopenJob(Long id);
    void setTop(Long id, Boolean isTop);
    void setRecommended(Long id, Boolean isRecommended);
}
