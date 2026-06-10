package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.CompanyWorkerInsertMapper;
import com.parttime.cservice.mapper.EnterpriseMapper;
import com.parttime.cservice.mapper.JobCategoryMapper;
import com.parttime.cservice.mapper.JobMapper;
import com.parttime.cservice.mapper.JobRateMapper;
import com.parttime.cservice.mapper.JobScheduleMapper;
import com.parttime.cservice.mapper.JobTagGroupMapper;
import com.parttime.cservice.mapper.JobTagRelationMapper;
import com.parttime.cservice.mapper.NotificationMapper;
import com.parttime.cservice.mapper.ScheduleApplicationMapper;
import com.parttime.cservice.mapper.ShiftMapper;
import com.parttime.cservice.mapper.SystemConfigMapper;
import com.parttime.cservice.pojo.entity.Enterprise;
import com.parttime.cservice.pojo.entity.Job;
import com.parttime.cservice.pojo.entity.JobCategory;
import com.parttime.cservice.pojo.entity.JobRate;
import com.parttime.cservice.pojo.entity.JobSchedule;
import com.parttime.cservice.pojo.entity.JobTagGroup;
import com.parttime.cservice.pojo.entity.ScheduleApplication;
import com.parttime.cservice.pojo.entity.ShiftEntity;
import com.parttime.cservice.pojo.entity.SystemConfig;
import com.parttime.cservice.pojo.vo.JobDetailVO;
import com.parttime.cservice.pojo.vo.JobRateInfoVO;
import com.parttime.cservice.pojo.vo.JobScheduleInfoVO;
import com.parttime.cservice.pojo.vo.JobSummaryVO;
import com.parttime.cservice.pojo.vo.JobTagVO;
import com.parttime.cservice.pojo.vo.NotificationVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.WorkerSignupVO;
import com.parttime.cservice.service.JobService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Service
public class JobServiceImpl implements JobService {

    @Resource
    private JobMapper jobMapper;
    @Resource
    private EnterpriseMapper enterpriseMapper;
    @Resource
    private JobCategoryMapper jobCategoryMapper;
    @Resource
    private JobRateMapper jobRateMapper;
    @Resource
    private ScheduleApplicationMapper scheduleApplicationMapper;
    @Resource
    private CompanyWorkerInsertMapper companyWorkerInsertMapper;
    @Resource
    private JobScheduleMapper jobScheduleMapper;
    @Resource
    private JobTagRelationMapper jobTagRelationMapper;
    @Resource
    private JobTagGroupMapper jobTagGroupMapper;
    @Resource
    private SystemConfigMapper systemConfigMapper;
    @Resource
    private ShiftMapper shiftMapper;
    @Resource
    private NotificationMapper notificationMapper;

    @Override
    public List<JobSummaryVO> searchJobs(String keyword, Long categoryId, String location,
                                         BigDecimal minRate, BigDecimal maxRate,
                                         BigDecimal latitude, BigDecimal longitude) {
        List<Job> jobs = jobMapper.search(keyword, location, categoryId);
        LocalDateTime now = LocalDateTime.now();
        // 自动关闭已过报名截止的岗位
        List<Long> expiredIds = jobs.stream()
                .filter(job -> job.getDeadline() != null && now.isAfter(job.getDeadline()) && !"CLOSED".equals(job.getStatus()))
                .map(Job::getId)
                .collect(Collectors.toList());
        if (!expiredIds.isEmpty()) {
            jobMapper.batchCloseJobs(expiredIds, "报名已截止");
            jobs.forEach(job -> {
                if (expiredIds.contains(job.getId())) {
                    job.setStatus("CLOSED");
                    job.setCloseReason("报名已截止");
                }
            });
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
        // 批量加载关联数据
        Map<Long, Enterprise> companyMap = loadCompanies(jobs);
        Map<Long, JobCategory> categoryMap = loadCategories(jobs);
        Map<Long, List<JobRate>> ratesMap = loadRates(jobs);
        Map<Long, List<JobTagVO>> tagsByJobId = loadTagsByJobId(jobs);
        // 薪资筛选
        jobs = jobs.stream()
                .filter(job -> {
                    if (minRate == null) return true;
                    List<JobRate> rates = ratesMap.get(job.getId());
                    if (rates != null && !rates.isEmpty()) {
                        return rates.stream().anyMatch(r -> r.getAmount().compareTo(minRate) >= 0);
                    }
                    BigDecimal jobRate = job.getRateAmount() != null ? job.getRateAmount() : BigDecimal.ZERO;
                    return jobRate.compareTo(minRate) >= 0;
                })
                .filter(job -> {
                    if (maxRate == null) return true;
                    List<JobRate> rates = ratesMap.get(job.getId());
                    if (rates != null && !rates.isEmpty()) {
                        return rates.stream().anyMatch(r -> r.getAmount().compareTo(maxRate) <= 0);
                    }
                    BigDecimal jobRate = job.getRateAmount() != null ? job.getRateAmount() : BigDecimal.ZERO;
                    return jobRate.compareTo(maxRate) <= 0;
                })
                .toList();
        return jobs.stream()
                .map(job -> toSummary(job, latitude, longitude, companyMap.get(job.getCompanyId()),
                        categoryMap.get(job.getCategoryId()), ratesMap.get(job.getId()),
                        tagsByJobId.get(job.getId())))
                .toList();
    }

    @Override
    public void addJob(Long id, String title, String description, String location, Long categoryId, String categoryName,
                       List<JobRateInfoVO> rates, List<JobScheduleInfoVO> schedules,
                       Integer headcount, Integer acceptedCount, LocalDateTime deadline, String status) {
        Job job = new Job();
        job.setId(id);
        job.setCompanyId(companyIdForDemo(id));
        job.setLatitude(latitudeForDemo(id));
        job.setLongitude(longitudeForDemo(id));
        job.setTitle(title);
        job.setDescription(description);
        job.setLocation(location);
        job.setCategoryId(categoryId);
        job.setHeadcount(headcount);
        job.setAcceptedCount(acceptedCount);
        job.setDeadline(deadline);
        job.setStatus(status);
        if (rates != null && !rates.isEmpty()) {
            JobRateInfoVO first = rates.get(0);
            job.setRateType(first.getType());
            job.setRateAmount(first.getAmount());
        }
        jobMapper.insert(job);
        if (schedules != null && !schedules.isEmpty()) {
            List<JobSchedule> scheduleEntities = schedules.stream().map(s -> {
                JobSchedule js = new JobSchedule();
                js.setJobId(id);
                js.setScheduleDate(s.getDate());
                js.setStartTime(java.time.LocalTime.parse(s.getStartTime()));
                js.setEndTime(java.time.LocalTime.parse(s.getEndTime()));
                js.setSlotsAvailable(s.getSlotsAvailable());
                js.setStatus("ACTIVE");
                return js;
            }).collect(Collectors.toList());
            jobScheduleMapper.batchInsert(scheduleEntities);
        }
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
        Enterprise company = enterpriseMapper.findById(job.getCompanyId());
        JobCategory category = job.getCategoryId() != null ? jobCategoryMapper.findById(job.getCategoryId()) : null;
        List<JobRate> rates = jobRateMapper.findByJobId(job.getId());
        List<JobTagVO> tags = jobTagRelationMapper.findTagsByJobId(job.getId());
        return toDetail(job, workerId, company, category, rates, tags);
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
        List<JobSchedule> scheds = jobScheduleMapper.findByIds(newIds);
        Map<Long, JobSchedule> schedMap = scheds.stream().collect(Collectors.toMap(JobSchedule::getId, s -> s));
        for (Long sid : newIds) {
            JobSchedule sched = schedMap.get(sid);
            if (sched == null || !"ACTIVE".equals(sched.getStatus())) {
                throw new RuntimeException("排班已下架，无法报名");
            }
            LocalDateTime scheduleStart = LocalDateTime.of(sched.getScheduleDate(), sched.getStartTime());
            if (now.isAfter(scheduleStart)) {
                throw new RuntimeException("排班已开始，无法报名");
            }
        }
        // Check auto-approve: job-level overrides platform-level
        Boolean jobAutoApprove = job.getAutoApprove();
        boolean autoApprove;
        if (jobAutoApprove != null) {
            autoApprove = jobAutoApprove;
        } else {
            SystemConfig config = systemConfigMapper.findByKey("auto_approve_applications").orElse(null);
            autoApprove = config != null && "true".equalsIgnoreCase(config.getConfigValue());
        }
        // Batch insert all applications
        List<ScheduleApplication> applications = newIds.stream().map(scheduleId -> {
            ScheduleApplication sa = new ScheduleApplication();
            sa.setScheduleId(scheduleId);
            sa.setWorkerId(workerId);
            sa.setStatus(autoApprove ? "ACCEPTED" : "PENDING");
            return sa;
        }).collect(Collectors.toList());
        scheduleApplicationMapper.batchInsert(applications);

        // Handle auto-approve: create shifts and send notifications
        if (autoApprove) {
            for (ScheduleApplication sa : applications) {
                createShiftForApplication(sa, job, schedMap.get(sa.getScheduleId()));
                sendAutoApproveNotification(sa, job);
            }
        }
        if (job.getCompanyId() != null) {
            companyWorkerInsertMapper.upsert(job.getCompanyId(), workerId);
        }
        return true;
    }

    @Override
    public PageVO<WorkerSignupVO> getMySignups(Long workerId, Integer page, Integer pageSize) {
        int currentPage = page == null || page < 1 ? 1 : page;
        int currentPageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (currentPage - 1) * currentPageSize;
        List<WorkerSignupVO> records = scheduleApplicationMapper.findMySignups(workerId, offset, currentPageSize);
        long total = scheduleApplicationMapper.countMySignups(workerId);
        return new PageVO<>(records, total);
    }

    @Override
    public List<ScheduleApplication> getApplicationStatus(Long workerId, Long jobId) {
        return scheduleApplicationMapper.findByWorkerId(workerId);
    }

    // ========== 批量加载关联数据 ==========

    private Map<Long, Enterprise> loadCompanies(List<Job> jobs) {
        List<Long> companyIds = jobs.stream().map(Job::getCompanyId).filter(id -> id != null).distinct().toList();
        if (companyIds.isEmpty()) return Map.of();
        return enterpriseMapper.findByIds(companyIds).stream()
                .collect(Collectors.toMap(Enterprise::getId, e -> e));
    }

    private Map<Long, JobCategory> loadCategories(List<Job> jobs) {
        List<Long> categoryIds = jobs.stream().map(Job::getCategoryId).filter(id -> id != null).distinct().toList();
        if (categoryIds.isEmpty()) return Map.of();
        return jobCategoryMapper.findByIds(categoryIds).stream()
                .collect(Collectors.toMap(JobCategory::getId, c -> c));
    }

    private Map<Long, List<JobRate>> loadRates(List<Job> jobs) {
        List<Long> jobIds = jobs.stream().map(Job::getId).filter(id -> id != null).distinct().toList();
        if (jobIds.isEmpty()) return Map.of();
        List<JobRate> allRates = jobRateMapper.findByJobIds(jobIds);
        return allRates.stream().collect(Collectors.groupingBy(JobRate::getJobId));
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
        Map<Long, String> groupNameMap = loadGroupNameMap();
        tags.forEach(tag -> tag.setGroupName(groupNameMap.getOrDefault(tag.getGroupId(), "")));
        return tags.stream()
                .filter(tag -> tag.getJobId() != null)
                .collect(Collectors.groupingBy(JobTagVO::getJobId));
    }

    private List<JobTagVO> resolveTags(Job job, List<JobTagVO> tags) {
        if (tags != null) return tags;
        if (jobTagRelationMapper == null || job.getId() == null) return emptyList();
        List<JobTagVO> loaded = jobTagRelationMapper.findTagsByJobId(job.getId());
        if (loaded == null || loaded.isEmpty()) return emptyList();
        Map<Long, String> groupNameMap = loadGroupNameMap();
        loaded.forEach(tag -> tag.setGroupName(groupNameMap.getOrDefault(tag.getGroupId(), "")));
        return loaded;
    }

    private Map<Long, String> loadGroupNameMap() {
        return jobTagGroupMapper.findActiveGroups().stream()
                .collect(Collectors.toMap(JobTagGroup::getId, JobTagGroup::getName, (a, b) -> a));
    }

    // ========== VO 转换 ==========

    private JobSummaryVO toSummary(Job job, BigDecimal latitude, BigDecimal longitude,
                                   Enterprise company, JobCategory category,
                                   List<JobRate> rates, List<JobTagVO> tags) {
        List<JobRateInfoVO> rateVOs = rates != null ? rates.stream().map(r -> {
            JobRateInfoVO vo = new JobRateInfoVO();
            vo.setId(r.getId());
            vo.setType(r.getType());
            vo.setAmount(r.getAmount());
            vo.setCurrency(r.getCurrency());
            return vo;
        }).toList() : null;

        BigDecimal minRate;
        BigDecimal maxRate;
        List<String> rateTypes;
        if (rateVOs != null && !rateVOs.isEmpty()) {
            minRate = rateVOs.stream().map(JobRateInfoVO::getAmount).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            maxRate = rateVOs.stream().map(JobRateInfoVO::getAmount).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            rateTypes = rateVOs.stream().map(JobRateInfoVO::getType).toList();
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
        summary.setLocation(job.getAddress() != null ? job.getAddress() : job.getLocation());
        summary.setProvince(job.getProvince());
        summary.setCity(job.getCity());
        summary.setDistrict(job.getDistrict());
        summary.setCategoryName(category != null ? category.getName() : null);
        summary.setCompanyName(company != null ? company.getCompanyName() : null);
        summary.setCompanyLogo(company != null ? company.getCompanyLogo() : null);
        summary.setImageUrl(job.getImageUrl());
        summary.setDistanceKm(distanceKm);
        summary.setMinRate(minRate);
        summary.setMaxRate(maxRate);
        summary.setRateTypes(rateTypes);
        summary.setRates(rateVOs);
        return summary;
    }

    private List<JobScheduleInfoVO> toScheduleVOs(List<JobSchedule> entities) {
        if (entities == null || entities.isEmpty()) return emptyList();
        List<Long> scheduleIds = entities.stream().map(JobSchedule::getId).collect(Collectors.toList());
        Map<Long, Integer> countMap = scheduleApplicationMapper.countByScheduleIds(scheduleIds);
        return entities.stream().map(e -> {
            JobScheduleInfoVO vo = new JobScheduleInfoVO();
            vo.setId(e.getId());
            vo.setDate(e.getScheduleDate());
            vo.setStartTime(e.getStartTime() != null ? e.getStartTime().toString() : null);
            vo.setEndTime(e.getEndTime() != null ? e.getEndTime().toString() : null);
            vo.setSlotsAvailable(e.getSlotsAvailable());
            Integer capacity = e.getSlotsAvailable();
            if (capacity != null) {
                vo.setRemainingSlots(Math.max(0, capacity - countMap.getOrDefault(e.getId(), 0)));
            }
            return vo;
        }).toList();
    }

    private JobDetailVO toDetail(Job job, Long workerId, Enterprise company, JobCategory category,
                                 List<JobRate> rates, List<JobTagVO> tags) {
        JobDetailVO detail = new JobDetailVO();
        detail.setId(job.getId());
        detail.setTitle(job.getTitle());
        detail.setDescription(job.getDescription());
        detail.setRequirements(job.getRequirements());
        detail.setContactPhone(job.getContactPhone());
        detail.setTags(resolveTags(job, tags));
        detail.setLocation(job.getAddress() != null ? job.getAddress() : job.getLocation());
        detail.setProvince(job.getProvince());
        detail.setCity(job.getCity());
        detail.setDistrict(job.getDistrict());
        detail.setAddress(job.getAddress());
        detail.setLatitude(job.getLatitude());
        detail.setLongitude(job.getLongitude());
        detail.setCompanyName(company != null ? company.getCompanyName() : null);
        detail.setCompanyLogo(company != null ? company.getCompanyLogo() : null);
        detail.setImageUrl(job.getImageUrl());
        detail.setCategoryName(category != null ? category.getName() : null);
        detail.setStatus(job.getStatus());
        detail.setHeadcount(job.getHeadcount());
        detail.setDeadline(job.getDeadline());
        if (rates != null && !rates.isEmpty()) {
            detail.setRates(rates.stream().map(r -> {
                JobRateInfoVO vo = new JobRateInfoVO();
                vo.setId(r.getId());
                vo.setType(r.getType());
                vo.setAmount(r.getAmount());
                vo.setCurrency(r.getCurrency());
                return vo;
            }).toList());
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

    private void createShiftForApplication(ScheduleApplication sa, Job job, JobSchedule sched) {
        ShiftEntity shift = new ShiftEntity();
        shift.setJobId(job.getId());
        shift.setCompanyId(job.getCompanyId());
        shift.setWorkerId(sa.getWorkerId());
        shift.setShiftDate(sched.getScheduleDate());
        shift.setStartTime(sched.getStartTime());
        shift.setEndTime(sched.getEndTime());
        shift.setLocationLat(job.getLatitude());
        shift.setLocationLng(job.getLongitude());
        shift.setLocationName(job.getAddress());
        shift.setSalaryType(job.getRateType());
        shift.setSalaryAmount(job.getRateAmount());
        shift.setStatus("SCHEDULED");
        shift.setCreatedAt(LocalDateTime.now());
        shift.setUpdatedAt(LocalDateTime.now());
        try {
            shiftMapper.insert(shift);
        } catch (org.springframework.dao.DuplicateKeyException ignored) {
        }
    }

    private void sendAutoApproveNotification(ScheduleApplication sa, Job job) {
        NotificationVO notification = new NotificationVO();
        notification.setRecipientId(sa.getWorkerId());
        notification.setRecipientType("WORKER");
        notification.setType("APPLICATION_ACCEPTED");
        notification.setCategory("application");
        notification.setTitle("报名已通过");
        notification.setContent("您报名的" + job.getTitle() + "已通过审核");
        notification.setStatus("SENT");
        notification.setRead(false);
        notification.setRelatedType("APPLICATION");
        notification.setRelatedId(sa.getId());
        notification.setSentAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }
}
