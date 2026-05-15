package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.JobApplicationVO;

import java.util.List;

public interface ApplicationService {

    List<JobApplicationVO> getApplicationsByJob(Long jobId);

    List<JobApplicationVO> getApplicationsByWorker(Long workerId);

    JobApplicationVO acceptApplication(Long applicationId);

    JobApplicationVO rejectApplication(Long applicationId);
}
