package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.CompanyWorkerInsertMapper;
import com.parttime.cservice.mapper.JobMapper;
import com.parttime.cservice.mapper.JobScheduleMapper;
import com.parttime.cservice.mapper.JobTagRelationMapper;
import com.parttime.cservice.mapper.ScheduleApplicationMapper;
import com.parttime.cservice.pojo.entity.Job;
import com.parttime.cservice.pojo.entity.JobSchedule;
import com.parttime.cservice.pojo.entity.ScheduleApplication;
import com.parttime.cservice.pojo.vo.JobDetailVO;
import com.parttime.cservice.pojo.vo.JobRateInfoVO;
import com.parttime.cservice.pojo.vo.JobScheduleInfoVO;
import com.parttime.cservice.pojo.vo.JobSummaryVO;
import com.parttime.cservice.pojo.vo.JobTagVO;
import com.parttime.cservice.service.JobService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Service
public class JobServiceImpl implements JobService {

    @Resource
    private JobMapper jobMapper;
    @Resource
    private ScheduleApplicationMapper scheduleApplicationMapper;
    @Resource
    private CompanyWorkerInsertMapper companyWorkerInsertMapper;
    @Resource
    private JobScheduleMapper jobScheduleMapper;
    @Resource
    private JobTagRelationMapper jobTagRelationMapper;

    @Override
    public List<JobSummaryVO> searchJobs(String keyword, Long categoryId, String location,
                                         BigDecimal minRate, BigDecimal maxRate,
                                         BigDecimal latitude, BigDecimal longitude) {
        List<Job> jobs = jobMapper.search(keyword, location, categoryId);
        LocalDateTime now = LocalDateTime.now();
        // 自动关闭已过报名截止的岗位
        for (Job job : jobs) {
            if (job.getDeadline() != null && now.isAfter(job.getDeadline()) && !"CLOSED".equals(job.getStatus())) {
                job.setStatus("CLOSED");
                job.setCloseReason("报名已截止");
                jobMapper.update(job);
            }
        }
        jobs = jobs.stream()
                .filter(j -> !"CLOSED".equals(j.getStatus()))
                .toList();
        if (latitude != null && longitude != null) {
            jobs = jobs.stream()
                    .sorted((a, b) -> Double.compare(
                            distance(latitude, longitude, a.getLatitude(), a.getLongitude()),
                            distance(latitude, longitude, b.getLatitude(), b.getLongitude())))
                    .toList();
        }
        jobs = jobs.stream()
                .filter(job -> {
                    if (minRate == null) return true;
                    List<JobRateInfoVO> rates = job.getRates();
                    if (rates != null && !rates.isEmpty()) {
                        return rates.stream().anyMatch(r -> r.getAmount().compareTo(minRate) >= 0);
                    }
                    BigDecimal jobRate = job.getRateAmount() != null ? job.getRateAmount() : BigDecimal.ZERO;
                    return jobRate.compareTo(minRate) >= 0;
                })
                .filter(job -> {
                    if (maxRate == null) return true;
                    List<JobRateInfoVO> rates = job.getRates();
                    if (rates != null && !rates.isEmpty()) {
                        return rates.stream().anyMatch(r -> r.getAmount().compareTo(maxRate) <= 0);
                    }
                    BigDecimal jobRate = job.getRateAmount() != null ? job.getRateAmount() : BigDecimal.ZERO;
                    return jobRate.compareTo(maxRate) <= 0;
                })
                .toList();
        Map<Long, List<JobTagVO>> tagsByJobId = loadTagsByJobId(jobs);
        return jobs.stream()
                .map(job -> toSummary(job, latitude, longitude, tagsByJobId.get(job.getId())))
                .toList();
    }

    @Override
    public void addJob(Long id, String title, String description, String location, Long categoryId, String categoryName,
                       List<JobRateInfoVO> rates, List<JobScheduleInfoVO> schedules,
                       Integer headcount, Integer acceptedCount, LocalDateTime deadline, String status) {
        Job job = new Job();
        job.setId(id);
        job.setJobId(id);
        job.setCompanyId(companyIdForDemo(id));
        job.setLatitude(latitudeForDemo(id));
        job.setLongitude(longitudeForDemo(id));
        job.setTitle(title);
        job.setDescription(description);
        job.setLocation(location);
        job.setCategoryId(categoryId);
        job.setCategoryName(categoryName);
        job.setHeadcount(headcount);
        job.setAcceptedCount(acceptedCount);
        job.setDeadline(deadline);
        job.setStatus(status);
        if (rates != null && !rates.isEmpty()) {
            JobRateInfoVO first = rates.get(0);
            job.setRateType(first.getType());
            job.setRateAmount(first.getAmount());
            job.setRates(rates);
        }
        if (schedules != null && !schedules.isEmpty()) {
            job.setSchedules(schedules);
            for (JobScheduleInfoVO s : schedules) {
                JobSchedule js = new JobSchedule();
                js.setJobId(id);
                js.setScheduleDate(s.getDate());
                js.setStartTime(java.time.LocalTime.parse(s.getStartTime()));
                js.setEndTime(java.time.LocalTime.parse(s.getEndTime()));
                js.setSlotsAvailable(s.getSlotsAvailable());
                jobScheduleMapper.insert(js);
            }
        }
        jobMapper.insert(job);
    }

    private Long companyIdForDemo(Long jobId) {
        if (jobId == null) return null;
        if (jobId <= 3) return 1L;
        if (jobId <= 5) return 2L;
        if (jobId <= 7) return 3L;
        return 4L;
    }

    private BigDecimal latitudeForDemo(Long jobId) {
        if (jobId == null) return null;
        return switch (jobId.intValue()) {
            case 1, 3 -> new BigDecimal("39.9");
            case 2 -> new BigDecimal("31.2");
            default -> null;
        };
    }

    private BigDecimal longitudeForDemo(Long jobId) {
        if (jobId == null) return null;
        return switch (jobId.intValue()) {
            case 1, 3 -> new BigDecimal("116.4");
            case 2 -> new BigDecimal("121.5");
            default -> null;
        };
    }

    private double distance(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        if (lat2 == null || lng2 == null) return Double.MAX_VALUE;
        double earthRadius = 6371000D;
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue()))
                * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    @Override
    public JobDetailVO getJobDetail(Long jobId) {
        return getJobDetail(jobId, null);
    }

    @Override
    public JobDetailVO getJobDetail(Long jobId, Long workerId) {
        Job job = jobMapper.findByJobId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));
        return toDetail(job, workerId);
    }

    @Override
    public boolean applyForJob(Long workerId, Long jobId, List<Long> scheduleIds) {
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            return false;
        }
        Job job = jobMapper.findByJobId(jobId).orElse(null);
        if (job == null) {
            throw new RuntimeException("岗位不存在");
        }
        if (job.getDeadline() != null && LocalDateTime.now().isAfter(job.getDeadline())) {
            throw new RuntimeException("报名已截止");
        }
        if (job.getHeadcount() != null && job.getAcceptedCount() != null
                && job.getAcceptedCount() >= job.getHeadcount()) {
            throw new RuntimeException("该岗位已招满");
        }
        List<Long> alreadyApplied = scheduleApplicationMapper.findScheduleIdsByWorkerIdAndJobId(workerId, jobId);
        List<Long> newIds = scheduleIds.stream()
                .filter(id -> !alreadyApplied.contains(id))
                .toList();
        if (newIds.isEmpty()) {
            throw new RuntimeException("所选排班已全部报名");
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long sid : newIds) {
            JobSchedule sched = jobScheduleMapper.findById(sid).orElse(null);
            if (sched == null || !"ACTIVE".equals(sched.getStatus())) {
                throw new RuntimeException("排班已下架，无法报名");
            }
            LocalDateTime scheduleStart = LocalDateTime.of(sched.getScheduleDate(), sched.getStartTime());
            if (now.isAfter(scheduleStart)) {
                throw new RuntimeException("排班已开始，无法报名");
            }
        }
        for (Long scheduleId : newIds) {
            ScheduleApplication sa = new ScheduleApplication();
            sa.setScheduleId(scheduleId);
            sa.setWorkerId(workerId);
            sa.setStatus("PENDING");
            scheduleApplicationMapper.insert(sa);
        }
        if (job.getCompanyId() != null) {
            companyWorkerInsertMapper.upsert(job.getCompanyId(), workerId);
        }
        return true;
    }

    @Override
    public List<ScheduleApplication> getApplicationStatus(Long workerId, Long jobId) {
        return scheduleApplicationMapper.findByWorkerId(workerId);
    }

    private JobSummaryVO toSummary(Job job, BigDecimal latitude, BigDecimal longitude, List<JobTagVO> tags) {
        List<JobRateInfoVO> rates = job.getRates();
        BigDecimal minRate;
        BigDecimal maxRate;
        List<String> rateTypes;
        if (rates != null && !rates.isEmpty()) {
            minRate = rates.stream().map(JobRateInfoVO::getAmount).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            maxRate = rates.stream().map(JobRateInfoVO::getAmount).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            rateTypes = rates.stream().map(JobRateInfoVO::getType).toList();
        } else {
            minRate = job.getRateAmount() != null ? job.getRateAmount() : BigDecimal.ZERO;
            maxRate = minRate;
            rateTypes = job.getRateType() != null ? List.of(job.getRateType()) : List.of();
        }
        BigDecimal distanceKm = null;
        if (latitude != null && longitude != null && job.getLatitude() != null && job.getLongitude() != null) {
            distanceKm = BigDecimal.valueOf(distance(latitude, longitude, job.getLatitude(), job.getLongitude()) / 1000D)
                    .setScale(1, RoundingMode.HALF_UP);
        }

        JobSummaryVO summary = new JobSummaryVO();
        summary.setId(job.getId());
        summary.setTitle(job.getTitle());
        summary.setTags(tags == null ? emptyList() : tags);
        summary.setLocation(job.getAddress());
        summary.setProvince(job.getProvince());
        summary.setCity(job.getCity());
        summary.setDistrict(job.getDistrict());
        summary.setCategoryName(job.getCategoryName());
        summary.setCompanyName(job.getCompanyName());
        summary.setCompanyLogo(job.getCompanyLogo());
        summary.setImageUrl(job.getImageUrl());
        summary.setDistanceKm(distanceKm);
        summary.setMinRate(minRate);
        summary.setMaxRate(maxRate);
        summary.setRateTypes(rateTypes);
        summary.setRates(rates);
        return summary;
    }

    private List<JobScheduleInfoVO> toScheduleVOs(List<JobSchedule> entities) {
        if (entities == null || entities.isEmpty()) return emptyList();
        return entities.stream().map(e -> {
            JobScheduleInfoVO vo = new JobScheduleInfoVO();
            vo.setId(e.getId());
            vo.setDate(e.getScheduleDate());
            vo.setStartTime(e.getStartTime() != null ? e.getStartTime().toString() : null);
            vo.setEndTime(e.getEndTime() != null ? e.getEndTime().toString() : null);
            vo.setSlotsAvailable(e.getSlotsAvailable());
            return vo;
        }).toList();
    }

    private Map<Long, List<JobTagVO>> loadTagsByJobId(List<Job> jobs) {
        if (jobTagRelationMapper == null || jobs == null || jobs.isEmpty()) return Map.of();
        List<Long> jobIds = jobs.stream()
                .map(Job::getId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (jobIds.isEmpty()) return Map.of();
        List<JobTagVO> tags = jobTagRelationMapper.findTagsByJobIds(jobIds);
        if (tags == null || tags.isEmpty()) return Map.of();
        return tags.stream()
                .filter(tag -> tag.getJobId() != null)
                .collect(Collectors.groupingBy(JobTagVO::getJobId));
    }

    private List<JobTagVO> resolveTags(Job job) {
        if (job.getTags() != null) return job.getTags();
        if (jobTagRelationMapper == null || job.getId() == null) return emptyList();
        List<JobTagVO> tags = jobTagRelationMapper.findTagsByJobId(job.getId());
        return tags == null ? emptyList() : tags;
    }

    private JobDetailVO toDetail(Job job, Long workerId) {
        JobDetailVO detail = new JobDetailVO();
        detail.setId(job.getId());
        detail.setTitle(job.getTitle());
        detail.setDescription(job.getDescription());
        detail.setRequirements(job.getRequirements());
        detail.setContactPhone(job.getContactPhone());
        detail.setTags(resolveTags(job));
        detail.setLocation(job.getAddress());
        detail.setProvince(job.getProvince());
        detail.setCity(job.getCity());
        detail.setDistrict(job.getDistrict());
        detail.setAddress(job.getAddress());
        detail.setLatitude(job.getLatitude());
        detail.setLongitude(job.getLongitude());
        detail.setCompanyName(job.getCompanyName());
        detail.setCompanyLogo(job.getCompanyLogo());
        detail.setImageUrl(job.getImageUrl());
        detail.setCategoryName(job.getCategoryName());
        detail.setStatus(job.getStatus());
        detail.setHeadcount(job.getHeadcount());
        detail.setDeadline(job.getDeadline());
        if (job.getRates() != null && !job.getRates().isEmpty()) {
            detail.setRates(job.getRates());
        } else if (job.getRateType() != null && job.getRateAmount() != null) {
            JobRateInfoVO rate = new JobRateInfoVO();
            rate.setType(job.getRateType());
            rate.setAmount(job.getRateAmount());
            detail.setRates(List.of(rate));
        }
        // 报名截止自动关闭
        LocalDateTime now = LocalDateTime.now();
        if (job.getDeadline() != null && now.isAfter(job.getDeadline()) && !"CLOSED".equals(job.getStatus())) {
            job.setStatus("CLOSED");
            job.setCloseReason("报名已截止");
            jobMapper.update(job);
            detail.setStatus("CLOSED");
        }
        // 过滤过期排班
        List<JobSchedule> allActive = jobScheduleMapper.findActiveByJobId(job.getId());
        List<JobSchedule> validSchedules = allActive.stream()
                .filter(s -> LocalDateTime.of(s.getScheduleDate(), s.getStartTime()).isAfter(now))
                .toList();
        detail.setSchedules(toScheduleVOs(validSchedules));
        // 如果所有排班都已过期，自动关闭职位
        if (allActive.size() > 0 && validSchedules.size() == 0 && !"CLOSED".equals(job.getStatus())) {
            job.setStatus("CLOSED");
            job.setCloseReason("所有排班已过期");
            jobMapper.update(job);
            detail.setStatus("CLOSED");
        }
        if (workerId != null) {
            List<ScheduleApplication> apps = scheduleApplicationMapper.findByWorkerId(workerId);
            List<Long> scheduleIds = apps.stream()
                    .filter(a -> a.getScheduleId() != null)
                    .map(ScheduleApplication::getScheduleId)
                    .toList();
            if (!scheduleIds.isEmpty()) {
                detail.setAppliedScheduleIds(scheduleIds);
            }
        }
        return detail;
    }
}
