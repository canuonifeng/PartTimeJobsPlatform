package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.JobApplicationVO;

import java.util.List;

public interface ApplicationService {

    default List<JobApplicationVO> getApplicationsByJob(Long jobId, String jobTitle, String status) {
        return getApplicationsByJob(jobId, jobTitle, status, null, null);
    }

    List<JobApplicationVO> getApplicationsByJob(Long jobId, String jobTitle, String status, Integer page, Integer pageSize);

    List<JobApplicationVO> getApplicationsByWorker(Long workerId);

    JobApplicationVO acceptApplication(Long applicationId);

    JobApplicationVO rejectApplication(Long applicationId);
}
