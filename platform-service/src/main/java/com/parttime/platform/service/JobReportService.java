package com.parttime.platform.service;

import com.parttime.platform.pojo.vo.JobReportVO;

import java.util.List;

public interface JobReportService {
    List<JobReportVO> list(String status);
    JobReportVO detail(Long id);
    void dismiss(Long id);
    void ban(Long id);
}
