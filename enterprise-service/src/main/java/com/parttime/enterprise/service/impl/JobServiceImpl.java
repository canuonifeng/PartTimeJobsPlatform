package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.enums.JobRateType;
import com.parttime.enterprise.enums.JobStatus;
import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.ScheduleApplicationMapper;
import com.parttime.enterprise.mapper.JobCategoryMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.JobRateMapper;
import com.parttime.enterprise.mapper.JobScheduleMapper;
import com.parttime.enterprise.mapper.JobTagMapper;
import com.parttime.enterprise.mapper.JobTagRelationMapper;
import com.parttime.enterprise.pojo.cmd.JobCreateCmd;
import com.parttime.enterprise.pojo.cmd.JobRateCmd;
import com.parttime.enterprise.pojo.cmd.JobScheduleCmd;
import com.parttime.enterprise.pojo.cmd.UpdateJobCmd;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobRate;
import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.pojo.entity.JobTag;
import com.parttime.enterprise.pojo.vo.JobRateVO;
import com.parttime.enterprise.pojo.vo.JobScheduleVO;
import com.parttime.enterprise.pojo.vo.JobTagVO;
import com.parttime.enterprise.pojo.vo.JobVO;
import com.parttime.enterprise.service.JobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    @Resource
    private JobMapper jobMapper;
    @Resource
    private JobRateMapper jobRateMapper;
    @Resource
    private JobScheduleMapper jobScheduleMapper;
    @Resource
    private ScheduleApplicationMapper scheduleApplicationMapper;
    @Resource
    private JobCategoryMapper jobCategoryMapper;
    @Resource
    private JobTagRelationMapper jobTagRelationMapper;
    @Resource
    private JobTagMapper jobTagMapper;

    @Override
    @Transactional
    public JobVO createJob(JobCreateCmd request) {
        Job job = new Job();
        job.setCompanyId(request.getCompanyId());
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setContactPhone(request.getContactPhone());
        job.setLocation(request.getLocation());
        job.setProvince(request.getProvince());
        job.setCity(request.getCity());
        job.setDistrict(request.getDistrict());
        job.setAddress(request.getAddress());
        job.setLatitude(request.getLatitude());
        job.setLongitude(request.getLongitude());
        if (request.getProvince() != null || request.getCity() != null || request.getDistrict() != null || request.getAddress() != null) {
            StringBuilder sb = new StringBuilder();
            if (request.getProvince() != null) sb.append(request.getProvince());
            if (request.getCity() != null) sb.append(' ').append(request.getCity());
            if (request.getDistrict() != null) sb.append(' ').append(request.getDistrict());
            if (request.getAddress() != null) sb.append(' ').append(request.getAddress());
            job.setLocation(sb.toString().trim());
        }
        job.setCategoryId(request.getCategoryId());
        job.setHeadcount(request.getHeadcount());
        job.setStatus("DRAFT");
        job.setDeadline(request.getDeadline());
        job.setImageUrl(request.getImageUrl());

        jobMapper.insert(job);
        replaceJobTags(job.getId(), request.getTagIds());

        if (request.getRates() != null) {
            for (JobRateCmd rateReq : request.getRates()) {
                JobRate rate = new JobRate();
                rate.setJobId(job.getId());
                rate.setType(rateReq.getType().name());
                rate.setAmount(rateReq.getAmount());
                rate.setCurrency(rateReq.getCurrency());
                rate.setRules(rateReq.getRules());
                jobRateMapper.insert(rate);
            }
        }

        if (request.getSchedules() != null) {
            for (JobScheduleCmd scheduleReq : request.getSchedules()) {
                JobSchedule schedule = new JobSchedule();
                schedule.setJobId(job.getId());
                schedule.setScheduleDate(scheduleReq.getScheduleDate());
                schedule.setStartTime(scheduleReq.getStartTime());
                schedule.setEndTime(scheduleReq.getEndTime());
                schedule.setSlotsAvailable(job.getHeadcount());
                jobScheduleMapper.insert(schedule);
            }
        }
        return toResponse(job);
    }

    @Override
    @Transactional
    public JobVO updateJob(Long id, UpdateJobCmd request) {
        Job job = jobMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        if (request.getTitle() != null) job.setTitle(request.getTitle());
        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getRequirements() != null) job.setRequirements(request.getRequirements());
        if (request.getContactPhone() != null) job.setContactPhone(request.getContactPhone());
        if (request.getLocation() != null) job.setLocation(request.getLocation());
        if (request.getProvince() != null) job.setProvince(request.getProvince());
        if (request.getCity() != null) job.setCity(request.getCity());
        if (request.getDistrict() != null) job.setDistrict(request.getDistrict());
        if (request.getAddress() != null) job.setAddress(request.getAddress());
        if (request.getLatitude() != null) job.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) job.setLongitude(request.getLongitude());
        if (request.getCategoryId() != null) job.setCategoryId(request.getCategoryId());
        if (request.getHeadcount() != null) job.setHeadcount(request.getHeadcount());
        if (request.getDeadline() != null) job.setDeadline(request.getDeadline());
        if (request.getImageUrl() != null) job.setImageUrl(request.getImageUrl());
        jobMapper.update(job);
        if (request.getTagIds() != null) {
            replaceJobTags(id, request.getTagIds());
        }
        if (request.getRates() != null) {
            jobRateMapper.deleteByJobId(id);
            for (JobRateCmd rateReq : request.getRates()) {
                JobRate rate = new JobRate();
                rate.setJobId(id);
                rate.setType(rateReq.getType().name());
                rate.setAmount(rateReq.getAmount());
                rate.setCurrency(rateReq.getCurrency());
                rate.setRules(rateReq.getRules());
                jobRateMapper.insert(rate);
            }
        }
        if (request.getSchedules() != null) {
            // 获取数据库中现有的排班ID
            List<Long> existingIds = jobScheduleMapper.findByJobId(id).stream()
                    .map(JobSchedule::getId)
                    .toList();
            // 请求中保留的排班ID
            List<Long> keptIds = request.getSchedules().stream()
                    .map(JobScheduleCmd::getId)
                    .filter(Objects::nonNull)
                    .toList();
            // 取消/删除不再需要的排班
            for (Long sid : existingIds) {
                if (!keptIds.contains(sid)) {
                    long appCount = scheduleApplicationMapper.countByScheduleId(sid);
                    if (appCount > 0) {
                        jobScheduleMapper.cancelSchedule(sid);
                    } else {
                        jobScheduleMapper.delete(sid);
                    }
                }
            }
            // 新增排班
            for (JobScheduleCmd scheduleReq : request.getSchedules()) {
                if (scheduleReq.getId() == null) {
                    JobSchedule schedule = new JobSchedule();
                    schedule.setJobId(id);
                    schedule.setScheduleDate(scheduleReq.getScheduleDate());
                    schedule.setStartTime(scheduleReq.getStartTime());
                    schedule.setEndTime(scheduleReq.getEndTime());
                    schedule.setSlotsAvailable(job.getHeadcount());
                    schedule.setStatus("ACTIVE");
                    jobScheduleMapper.insert(schedule);
                } else {
                    JobSchedule schedule = new JobSchedule();
                    schedule.setId(scheduleReq.getId());
                    schedule.setJobId(id);
                    schedule.setScheduleDate(scheduleReq.getScheduleDate());
                    schedule.setStartTime(scheduleReq.getStartTime());
                    schedule.setEndTime(scheduleReq.getEndTime());
                    schedule.setSlotsAvailable(job.getHeadcount());
                    jobScheduleMapper.update(schedule);
                }
            }
        }
        return toResponse(job);
    }

    @Override
    public void deleteJob(Long id) {
        Job job = jobMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        if (!"CLOSED".equals(job.getStatus())) {
            throw new BusinessException("只有关闭后的职位才能删除");
        }
        if (scheduleApplicationMapper.countByJobId(id) > 0) {
            throw new BusinessException("已有报名记录的职位不能删除");
        }
        jobMapper.delete(id);
    }

    @Override
    public JobVO getJobById(Long id) {
        Job job = jobMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        JobVO response = toResponse(job);
        response.setRates(toRateResponses(jobRateMapper.findByJobId(id)));
        response.setSchedules(toScheduleResponses(jobScheduleMapper.findActiveByJobId(id)));
        return response;
    }

    @Override
    public List<JobVO> getJobsByCompany(Long companyId, String status, Integer page, Integer pageSize) {
        List<Job> jobs;
        if (status != null && !status.isEmpty()) {
            jobs = jobMapper.findByCompanyIdAndStatus(companyId, status);
        } else {
            jobs = jobMapper.findByCompanyId(companyId);
        }
        return jobs.stream().map(job -> {
            JobVO response = toResponse(job);
            response.setRates(toRateResponses(jobRateMapper.findByJobId(job.getId())));
            response.setSchedules(toScheduleResponses(jobScheduleMapper.findActiveByJobId(job.getId())));
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public JobVO publishJob(Long id) {
        Job job = jobMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        if (!"DRAFT".equals(job.getStatus())) {
            throw new RuntimeException("Cannot publish job in status: " + job.getStatus());
        }
        jobMapper.updateStatus(id, "PUBLISHED");
        job.setStatus("PUBLISHED");
        JobVO response = toResponse(job);
        response.setRates(toRateResponses(jobRateMapper.findByJobId(id)));
        response.setSchedules(toScheduleResponses(jobScheduleMapper.findActiveByJobId(id)));
        return response;
    }

    @Override
    public JobVO closeJob(Long id) {
        Job job = jobMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        if (!"PUBLISHED".equals(job.getStatus())) {
            throw new RuntimeException("Cannot close job in status: " + job.getStatus());
        }
        jobMapper.updateStatus(id, "CLOSED");
        job.setStatus("CLOSED");
        JobVO response = toResponse(job);
        response.setRates(toRateResponses(jobRateMapper.findByJobId(id)));
        response.setSchedules(toScheduleResponses(jobScheduleMapper.findActiveByJobId(id)));
        return response;
    }

    @Override
    public JobVO reopenJob(Long id) {
        Job job = jobMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        if (!"CLOSED".equals(job.getStatus())) {
            throw new RuntimeException("Cannot reopen job in status: " + job.getStatus());
        }
        jobMapper.updateStatus(id, "PUBLISHED");
        job.setStatus("PUBLISHED");
        JobVO response = toResponse(job);
        response.setRates(toRateResponses(jobRateMapper.findByJobId(id)));
        response.setSchedules(toScheduleResponses(jobScheduleMapper.findActiveByJobId(id)));
        return response;
    }

    @Override
    public void expireJob(Long id) {
        Job job = jobMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        if (!"PUBLISHED".equals(job.getStatus())) {
            throw new RuntimeException("Cannot expire job in status: " + job.getStatus());
        }
        jobMapper.updateStatus(id, "EXPIRED");
    }

    @Override
    public List<JobRateVO> getJobRates(Long jobId) {
        return toRateResponses(jobRateMapper.findByJobId(jobId));
    }

    @Override
    public JobRateVO addJobRate(Long jobId, JobRateCmd request) {
        JobRate rate = new JobRate();
        rate.setJobId(jobId);
        rate.setType(request.getType().name());
        rate.setAmount(request.getAmount());
        rate.setCurrency(request.getCurrency());
        rate.setRules(request.getRules());
        jobRateMapper.insert(rate);
        return toRateResponse(rate);
    }

    @Override
    public JobRateVO updateJobRate(Long rateId, JobRateCmd request) {
        JobRate rate = jobRateMapper.findById(rateId)
                .orElseThrow(() -> new RuntimeException("JobRate not found: " + rateId));
        rate.setType(request.getType().name());
        rate.setAmount(request.getAmount());
        rate.setCurrency(request.getCurrency());
        rate.setRules(request.getRules());
        jobRateMapper.update(rate);
        return toRateResponse(rate);
    }

    @Override
    public void removeJobRate(Long rateId) {
        jobRateMapper.delete(rateId);
    }

    @Override
    public List<JobScheduleVO> getJobSchedules(Long jobId) {
        return toScheduleResponses(jobScheduleMapper.findActiveByJobId(jobId));
    }

    @Override
    public JobScheduleVO addJobSchedule(Long jobId, JobScheduleCmd request) {
        Job job = jobMapper.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        JobSchedule schedule = new JobSchedule();
        schedule.setJobId(jobId);
        schedule.setScheduleDate(request.getScheduleDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
                schedule.setSlotsAvailable(job.getHeadcount());
                schedule.setStatus("ACTIVE");
                jobScheduleMapper.insert(schedule);
        return toScheduleResponse(schedule);
    }

    @Override
    public JobScheduleVO updateJobSchedule(Long scheduleId, JobScheduleCmd request) {
        JobSchedule schedule = jobScheduleMapper.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("JobSchedule not found: " + scheduleId));
        Job job = jobMapper.findById(schedule.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found: " + schedule.getJobId()));
        schedule.setScheduleDate(request.getScheduleDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setSlotsAvailable(job.getHeadcount());
        jobScheduleMapper.update(schedule);
        return toScheduleResponse(schedule);
    }

    @Override
    public void removeJobSchedule(Long scheduleId) {
        JobSchedule schedule = jobScheduleMapper.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("JobSchedule not found: " + scheduleId));
        Long jobId = schedule.getJobId();
        jobScheduleMapper.delete(scheduleId);
    }

    private void replaceJobTags(Long jobId, List<Long> tagIds) {
        jobTagRelationMapper.deleteByJobId(jobId);
        if (tagIds == null) {
            return;
        }
        List<Long> distinctTagIds = tagIds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctTagIds.isEmpty()) {
            return;
        }
        List<Long> activeTagIds = jobTagMapper.findActiveExistingIds(distinctTagIds);
        if (activeTagIds.size() != distinctTagIds.size() || !activeTagIds.containsAll(distinctTagIds)) {
            throw new RuntimeException("存在无效或已停用的岗位标签");
        }
        for (Long tagId : distinctTagIds) {
            jobTagRelationMapper.insert(jobId, tagId);
        }
    }

    private List<JobTagVO> toTagResponses(List<JobTag> tags) {
        return tags.stream().map(this::toTagResponse).collect(Collectors.toList());
    }

    private JobTagVO toTagResponse(JobTag tag) {
        JobTagVO response = new JobTagVO();
        response.setId(tag.getId());
        response.setGroupId(tag.getGroupId());
        response.setGroupName(tag.getGroupName());
        response.setName(tag.getName());
        response.setCode(tag.getCode());
        response.setSortOrder(tag.getSortOrder());
        response.setStatus(tag.getStatus());
        response.setCreatedAt(tag.getCreatedAt());
        response.setUpdatedAt(tag.getUpdatedAt());
        return response;
    }

    private List<JobRateVO> toRateResponses(List<JobRate> rates) {
        return rates.stream().map(this::toRateResponse).collect(Collectors.toList());
    }

    private JobRateVO toRateResponse(JobRate rate) {
        JobRateVO response = new JobRateVO();
        response.setId(rate.getId());
        response.setJobId(rate.getJobId());
        response.setType(JobRateType.valueOf(rate.getType()));
        response.setAmount(rate.getAmount());
        response.setCurrency(rate.getCurrency());
        response.setRules(rate.getRules());
        response.setCreatedAt(rate.getCreatedAt());
        response.setUpdatedAt(rate.getUpdatedAt());
        return response;
    }

    private List<JobScheduleVO> toScheduleResponses(List<JobSchedule> schedules) {
        return schedules.stream().map(this::toScheduleResponse).collect(Collectors.toList());
    }

    private JobScheduleVO toScheduleResponse(JobSchedule schedule) {
        JobScheduleVO response = new JobScheduleVO();
        response.setId(schedule.getId());
        response.setJobId(schedule.getJobId());
        response.setScheduleDate(schedule.getScheduleDate());
        response.setStartTime(schedule.getStartTime());
        response.setEndTime(schedule.getEndTime());
        response.setSlotsAvailable(schedule.getSlotsAvailable());
        response.setStatus(schedule.getStatus());
        response.setCreatedAt(schedule.getCreatedAt());
        response.setUpdatedAt(schedule.getUpdatedAt());
        return response;
    }

    private JobVO toResponse(Job job) {
        JobVO response = new JobVO();
        response.setId(job.getId());
        response.setCompanyId(job.getCompanyId());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setRequirements(job.getRequirements());
        response.setContactPhone(job.getContactPhone());
        List<Long> tagIds = jobTagRelationMapper.findTagIdsByJobId(job.getId());
        List<JobTag> tags = jobTagRelationMapper.findTagsByJobId(job.getId());
        response.setTagIds(tagIds == null ? List.of() : tagIds);
        response.setTags(tags == null ? List.of() : toTagResponses(tags));
        response.setLocation(job.getLocation());
        response.setProvince(job.getProvince());
        response.setCity(job.getCity());
        response.setDistrict(job.getDistrict());
        response.setAddress(job.getAddress());
        response.setLatitude(job.getLatitude());
        response.setLongitude(job.getLongitude());
        response.setCategoryId(job.getCategoryId());
        if (job.getCategoryId() != null) {
            String categoryName = jobCategoryMapper.findById(job.getCategoryId())
                    .map(cat -> cat.getName())
                    .orElse(null);
            response.setCategoryName(categoryName);
        }
        response.setHeadcount(job.getHeadcount());
        response.setImageUrl(job.getImageUrl());
        response.setStatus(JobStatus.valueOf(job.getStatus()));
        response.setApplicationCount(scheduleApplicationMapper.countByJobId(job.getId()));
        response.setPendingApplicationCount(scheduleApplicationMapper.countByJobIdAndStatus(job.getId(), "PENDING"));
        response.setDeadline(job.getDeadline());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }
}
