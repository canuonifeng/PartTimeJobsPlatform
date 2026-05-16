package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.CompanyWorkerInsertMapper;
import com.parttime.cservice.mapper.JobApplicationMapper;
import com.parttime.cservice.mapper.JobMapper;
import com.parttime.cservice.pojo.entity.Job;
import com.parttime.cservice.pojo.entity.JobApplication;
import com.parttime.cservice.pojo.vo.ApplicationVO;
import com.parttime.cservice.pojo.vo.JobDetailVO;
import com.parttime.cservice.pojo.vo.JobRateInfoVO;
import com.parttime.cservice.pojo.vo.JobScheduleInfoVO;
import com.parttime.cservice.pojo.vo.JobSummaryVO;
import com.parttime.cservice.service.JobService;
import com.parttime.cservice.utils.JsonConverter;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.emptyList;

@Service
public class JobServiceImpl implements JobService {

    @Resource
    private JobMapper jobMapper;
    @Resource
    private JobApplicationMapper jobApplicationMapper;
    @Resource
    private CompanyWorkerInsertMapper companyWorkerInsertMapper;

    @Override
    public List<JobSummaryVO> searchJobs(String keyword, Long categoryId, String location,
                                        BigDecimal minRate, BigDecimal maxRate) {
        List<Job> jobs = jobMapper.search(keyword, location, categoryId);
        return jobs.stream()
                .filter(job -> {
                    if (minRate == null) return true;
                    BigDecimal jobRate = job.getRateAmount() != null ? job.getRateAmount() : BigDecimal.ZERO;
                    return jobRate.compareTo(minRate) >= 0;
                })
                .filter(job -> {
                    if (maxRate == null) return true;
                    BigDecimal jobRate = job.getRateAmount() != null ? job.getRateAmount() : BigDecimal.ZERO;
                    return jobRate.compareTo(maxRate) <= 0;
                })
                .map(this::toSummary)
                .toList();
    }

    @Override
    public JobDetailVO getJobDetail(Long jobId) {
        Job job = jobMapper.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));
        return toDetail(job);
    }

    @Override
    public boolean applyForJob(Long workerId, Long jobId, List<Long> scheduleIds) {
        List<JobApplication> existing = jobApplicationMapper.findByWorkerIdAndJobId(workerId, jobId);
        if (!existing.isEmpty()) {
            return false;
        }
        JobApplication app = new JobApplication();
        app.setWorkerId(workerId);
        app.setJobId(jobId);
        app.setStatus("PENDING");
        app.setAppliedAt(LocalDateTime.now());
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());
        jobApplicationMapper.insert(app);
        Job job = jobMapper.findByJobId(jobId).orElse(null);
        if (job != null) {
            companyWorkerInsertMapper.upsert(job.getCompanyId(), workerId);
        }
        return true;
    }

    @Override
    public List<ApplicationVO> getApplicationStatus(Long workerId, Long jobId) {
        List<JobApplication> apps = jobApplicationMapper.findByWorkerIdAndJobId(workerId, jobId);
        if (apps.isEmpty()) {
            return Collections.emptyList();
        }
        JobApplication app = apps.get(0);
        ApplicationVO resp = new ApplicationVO();
        resp.setApplicationId(app.getId());
        resp.setJobId(app.getJobId());
        resp.setStatus(app.getStatus());
        resp.setAppliedAt(app.getAppliedAt());
        return List.of(resp);
    }

    private JobSummaryVO toSummary(Job job) {
        BigDecimal rate = job.getRateAmount() != null ? job.getRateAmount() : BigDecimal.ZERO;
        List<String> rateTypes = job.getRateType() != null ? List.of(job.getRateType()) : List.of();

        JobSummaryVO summary = new JobSummaryVO();
        summary.setId(job.getId());
        summary.setTitle(job.getTitle());
        summary.setLocation(job.getLocation());
        summary.setProvince(job.getProvince());
        summary.setCity(job.getCity());
        summary.setDistrict(job.getDistrict());
        summary.setCategoryName(job.getCategoryName());
        summary.setMinRate(rate);
        summary.setMaxRate(rate);
        summary.setRateTypes(rateTypes);
        return summary;
    }

    private JobDetailVO toDetail(Job job) {
        JobDetailVO detail = new JobDetailVO();
        detail.setId(job.getId());
        detail.setTitle(job.getTitle());
        detail.setDescription(job.getDescription());
        detail.setLocation(job.getLocation());
        detail.setProvince(job.getProvince());
        detail.setCity(job.getCity());
        detail.setDistrict(job.getDistrict());
        detail.setAddress(job.getAddress());
        detail.setLatitude(job.getLatitude());
        detail.setLongitude(job.getLongitude());
        detail.setCompanyName(job.getCompanyName());
        detail.setCategoryName(job.getCategoryName());
        detail.setStatus(job.getStatus());
        detail.setHeadcount(job.getHeadcount());
        detail.setDeadline(job.getDeadline());
        if (job.getRateType() != null && job.getRateAmount() != null) {
            JobRateInfoVO rate = new JobRateInfoVO();
            rate.setType(job.getRateType());
            rate.setAmount(job.getRateAmount());
            detail.setRates(List.of(rate));
        }
        if (job.getScheduleInfo() != null) {
            try {
                List<JobScheduleInfoVO> schedules = JsonConverter.create().toArray(job.getScheduleInfo(), JobScheduleInfoVO.class);
                detail.setSchedules(schedules);
            } catch (Exception e) {
                detail.setSchedules(emptyList());
            }
        }
        return detail;
    }
}
