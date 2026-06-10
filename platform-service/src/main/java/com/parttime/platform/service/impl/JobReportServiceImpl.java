package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.JobReportMapper;
import com.parttime.platform.pojo.cmd.ReviewJobReportCmd;
import com.parttime.platform.pojo.entity.JobReport;
import com.parttime.platform.pojo.vo.JobReportVO;
import com.parttime.platform.service.JobReportService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobReportServiceImpl implements JobReportService {

    @Resource
    private JobReportMapper jobReportMapper;

    @Override
    public List<JobReportVO> getJobReports(String status) {
        List<JobReport> list;
        if (status != null && !status.isBlank()) {
            list = jobReportMapper.findByStatus(status);
        } else {
            list = jobReportMapper.findAll();
        }
        return list.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public JobReportVO getJobReport(Long id) {
        JobReport report = jobReportMapper.findById(id)
                .orElseThrow(() -> new BusinessException("JobReport not found: " + id));
        return toVO(report);
    }

    @Override
    public JobReportVO dismissReport(Long id, Long reviewerId, ReviewJobReportCmd cmd) {
        JobReport report = jobReportMapper.findById(id)
                .orElseThrow(() -> new BusinessException("JobReport not found: " + id));
        if (!"PENDING".equals(report.getStatus())) {
            throw new BusinessException("Report is not in PENDING status");
        }
        report.setStatus("DISMISSED");
        report.setReviewerId(reviewerId);
        report.setReviewRemark(cmd.getRemark());
        report.setReviewedAt(LocalDateTime.now());
        jobReportMapper.update(report);
        return toVO(report);
    }

    @Override
    public JobReportVO banJobReport(Long id, Long reviewerId, ReviewJobReportCmd cmd) {
        JobReport report = jobReportMapper.findById(id)
                .orElseThrow(() -> new BusinessException("JobReport not found: " + id));
        if (!"PENDING".equals(report.getStatus())) {
            throw new BusinessException("Report is not in PENDING status");
        }
        report.setStatus("BANNED");
        report.setReviewerId(reviewerId);
        report.setReviewRemark(cmd.getRemark());
        report.setReviewedAt(LocalDateTime.now());
        jobReportMapper.update(report);
        return toVO(report);
    }

    private JobReportVO toVO(JobReport report) {
        JobReportVO vo = new JobReportVO();
        vo.setId(report.getId());
        vo.setJobId(report.getJobId());
        vo.setReporterId(report.getReporterId());
        vo.setReason(report.getReason());
        vo.setDescription(report.getDescription());
        vo.setStatus(report.getStatus());
        vo.setReviewerId(report.getReviewerId());
        vo.setReviewRemark(report.getReviewRemark());
        vo.setReviewedAt(report.getReviewedAt());
        vo.setCreatedAt(report.getCreatedAt());
        vo.setUpdatedAt(report.getUpdatedAt());
        return vo;
    }
}
