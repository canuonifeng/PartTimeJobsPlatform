package com.parttime.cservice.service.impl;

import com.parttime.cservice.pojo.entity.Job;
import com.parttime.cservice.pojo.entity.JobApplication;
import com.parttime.cservice.pojo.vo.ApplicationVO;
import com.parttime.cservice.pojo.vo.JobDetailVO;
import com.parttime.cservice.pojo.vo.JobRateInfoVO;
import com.parttime.cservice.pojo.vo.JobScheduleInfoVO;
import com.parttime.cservice.pojo.vo.JobSummaryVO;
import com.parttime.cservice.service.JobService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class JobServiceImpl implements JobService {

    private final ConcurrentHashMap<Long, Job> jobs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, JobApplication> applications = new ConcurrentHashMap<>();
    private final AtomicLong applicationIdCounter = new AtomicLong(1);

    @Override
    public Job addJob(Long id, String title, String description, String location, Long categoryId,
                      String categoryName, List<JobRateInfoVO> rates, List<JobScheduleInfoVO> schedules,
                      Integer headcount, Integer acceptedCount, LocalDateTime deadline, String status) {
        Job job = new Job(id, title, description, location, categoryId, categoryName,
                rates, schedules, headcount, acceptedCount, deadline, status);
        jobs.put(id, job);
        return job;
    }

    @Override
    public List<JobSummaryVO> searchJobs(String keyword, Long categoryId, String location,
                                        BigDecimal minRate, BigDecimal maxRate) {
        return jobs.values().stream()
                .filter(job -> "PUBLISHED".equals(job.getStatus()))
                .filter(job -> keyword == null || keyword.isEmpty()
                        || job.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .filter(job -> categoryId == null || categoryId.equals(job.getCategoryId()))
                .filter(job -> location == null || location.isEmpty()
                        || job.getLocation().toLowerCase().contains(location.toLowerCase()))
                .filter(job -> {
                    if (minRate == null) return true;
                    BigDecimal jobMinRate = job.getRates().stream()
                            .map(JobRateInfoVO::getAmount)
                            .min(Comparator.naturalOrder())
                            .orElse(BigDecimal.ZERO);
                    return jobMinRate.compareTo(minRate) >= 0;
                })
                .filter(job -> {
                    if (maxRate == null) return true;
                    BigDecimal jobMaxRate = job.getRates().stream()
                            .map(JobRateInfoVO::getAmount)
                            .max(Comparator.naturalOrder())
                            .orElse(BigDecimal.ZERO);
                    return jobMaxRate.compareTo(maxRate) <= 0;
                })
                .map(this::toSummary)
                .toList();
    }

    @Override
    public JobDetailVO getJobDetail(Long jobId) {
        Job job = jobs.get(jobId);
        if (job == null) {
            throw new RuntimeException("Job not found with id: " + jobId);
        }
        return toDetail(job);
    }

    @Override
    public boolean applyForJob(Long workerId, Long jobId, List<Long> scheduleIds) {
        String key = workerId + ":" + jobId;
        if (applications.containsKey(key)) {
            return false;
        }
        JobApplication app = new JobApplication(
                applicationIdCounter.getAndIncrement(), workerId, jobId,
                scheduleIds, "PENDING", LocalDateTime.now());
        applications.put(key, app);
        return true;
    }

    @Override
    public List<ApplicationVO> getApplicationStatus(Long workerId, Long jobId) {
        String key = workerId + ":" + jobId;
        JobApplication app = applications.get(key);
        if (app == null) {
            return Collections.emptyList();
        }
        ApplicationVO resp = new ApplicationVO();
        resp.setApplicationId(app.getId());
        resp.setJobId(app.getJobId());
        resp.setStatus(app.getStatus());
        resp.setAppliedAt(app.getAppliedAt());
        return List.of(resp);
    }

    private JobSummaryVO toSummary(Job job) {
        BigDecimal minRate = job.getRates().stream()
                .map(JobRateInfoVO::getAmount)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        BigDecimal maxRate = job.getRates().stream()
                .map(JobRateInfoVO::getAmount)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        List<String> rateTypes = job.getRates().stream()
                .map(JobRateInfoVO::getType)
                .distinct()
                .toList();

        JobSummaryVO summary = new JobSummaryVO();
        summary.setId(job.getId());
        summary.setTitle(job.getTitle());
        summary.setLocation(job.getLocation());
        summary.setCategoryName(job.getCategoryName());
        summary.setMinRate(minRate);
        summary.setMaxRate(maxRate);
        summary.setRateTypes(rateTypes);
        return summary;
    }

    private JobDetailVO toDetail(Job job) {
        JobDetailVO detail = new JobDetailVO();
        detail.setId(job.getId());
        detail.setTitle(job.getTitle());
        detail.setDescription(job.getDescription());
        detail.setLocation(job.getLocation());
        detail.setCategoryName(job.getCategoryName());
        detail.setRates(job.getRates());
        detail.setSchedules(job.getSchedules());
        detail.setHeadcount(job.getHeadcount());
        detail.setAcceptedCount(job.getAcceptedCount());
        detail.setDeadline(job.getDeadline());
        detail.setStatus(job.getStatus());
        return detail;
    }
}
