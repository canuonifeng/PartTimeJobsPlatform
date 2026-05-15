package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.ReviewJobReportCmd;
import com.parttime.platform.pojo.vo.JobReportVO;

import java.util.List;

public interface JobReportService {

    List<JobReportVO> getJobReports(String status);

    JobReportVO getJobReport(Long id);

    JobReportVO dismissReport(Long id, String reviewerId, ReviewJobReportCmd cmd);

    JobReportVO banJobReport(Long id, String reviewerId, ReviewJobReportCmd cmd);
}
