package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.JobApplicationVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.pojo.vo.ScheduleApplicationVO;

import java.util.List;

public interface ApplicationService {

    default PageVO<ScheduleApplicationVO> getApplicationsByJob(Long companyId, Long jobId, String jobTitle, String status) {
        return getApplicationsByJob(companyId, jobId, jobTitle, status, null, null);
    }

    PageVO<ScheduleApplicationVO> getApplicationsByJob(Long companyId, Long jobId, String jobTitle, String status, Integer page, Integer pageSize);

    List<JobApplicationVO> getApplicationsByWorker(Long workerId);

    JobApplicationVO acceptApplication(Long applicationId);

    JobApplicationVO rejectApplication(Long applicationId);
}
