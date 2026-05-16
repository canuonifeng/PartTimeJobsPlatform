package com.parttime.cservice.service;

import com.parttime.cservice.pojo.cmd.ApplyJobCmd;
import com.parttime.cservice.pojo.entity.Job;
import com.parttime.cservice.pojo.vo.ApplicationVO;
import com.parttime.cservice.pojo.vo.JobDetailVO;
import com.parttime.cservice.pojo.vo.JobRateInfoVO;
import com.parttime.cservice.pojo.vo.JobScheduleInfoVO;
import com.parttime.cservice.pojo.vo.JobSummaryVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface JobService {
    List<JobSummaryVO> searchJobs(String keyword, Long categoryId, String location,
                                   BigDecimal minRate, BigDecimal maxRate);
    JobDetailVO getJobDetail(Long jobId);
    boolean applyForJob(Long workerId, Long jobId, List<Long> scheduleIds);
    List<ApplicationVO> getApplicationStatus(Long workerId, Long jobId);
}
