package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.ScheduleApplicationVO;import com.parttime.enterprise.pojo.vo.PageVO;

import java.util.List;

public interface ApplicationService {

    default PageVO<ScheduleApplicationVO> getApplicationsByJob(Long companyId, Long jobId, String jobTitle, String status) {
        return getApplicationsByJob(companyId, jobId, jobTitle, null, status, null, null);
    }

    PageVO<ScheduleApplicationVO> getApplicationsByJob(Long companyId, Long jobId, String jobTitle, Long scheduleId, String status, Integer page, Integer pageSize);

    List<ScheduleApplicationVO> getApplicationsByWorker(Long workerId);

    ScheduleApplicationVO acceptApplication(Long applicationId);

    ScheduleApplicationVO rejectApplication(Long applicationId);
}
