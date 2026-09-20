package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.JobCreateCmd;
import com.parttime.enterprise.pojo.cmd.JobRateCmd;
import com.parttime.enterprise.pojo.cmd.JobScheduleCmd;
import com.parttime.enterprise.pojo.cmd.UpdateJobCmd;
import com.parttime.enterprise.pojo.vo.JobRateVO;
import com.parttime.enterprise.pojo.vo.JobScheduleVO;
import com.parttime.enterprise.pojo.vo.JobVO;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface JobService {

    JobVO createJob(JobCreateCmd request);

    JobVO updateJob(Long id, UpdateJobCmd request);

    void deleteJob(Long id);

    JobVO getJobById(Long id);

    default List<JobVO> getJobsByCompany(Long companyId, String status) {
        return getJobsByCompany(companyId, status, null, null, null);
    }

    List<JobVO> getJobsByCompany(Long companyId, String status, String taskType, Integer page, Integer pageSize);

    JobVO publishJob(Long id);

    JobVO closeJob(Long id);

    JobVO reopenJob(Long id);

    void expireJob(Long id);

    List<JobRateVO> getJobRates(Long jobId);

    JobRateVO addJobRate(Long jobId, JobRateCmd request);

    JobRateVO updateJobRate(Long rateId, JobRateCmd request);

    void removeJobRate(Long rateId);

    List<JobScheduleVO> getJobSchedules(Long jobId);

    JobScheduleVO addJobSchedule(Long jobId, JobScheduleCmd request);

    JobScheduleVO updateJobSchedule(Long scheduleId, JobScheduleCmd request);

    void removeJobSchedule(Long scheduleId);
}
