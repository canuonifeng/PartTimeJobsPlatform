package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.enums.JobRateType;
import com.parttime.enterprise.enums.JobStatus;
import com.parttime.enterprise.mapper.CJobMapper;
import com.parttime.enterprise.mapper.EnterpriseMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.JobRateMapper;
import com.parttime.enterprise.mapper.JobScheduleMapper;
import com.parttime.enterprise.pojo.cmd.JobCreateCmd;
import com.parttime.enterprise.pojo.cmd.JobRateCmd;
import com.parttime.enterprise.pojo.cmd.JobScheduleCmd;
import com.parttime.enterprise.pojo.cmd.UpdateJobCmd;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.JobRate;
import com.parttime.enterprise.pojo.entity.JobSchedule;
import com.parttime.enterprise.pojo.vo.JobRateVO;
import com.parttime.enterprise.pojo.vo.JobScheduleVO;
import com.parttime.enterprise.pojo.vo.JobVO;
import com.parttime.enterprise.service.JobService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Resource;

import java.util.List;
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
    private CJobMapper cJobMapper;
    @Resource
    private EnterpriseMapper enterpriseMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public JobVO createJob(JobCreateCmd request) {
        Job job = new Job();
        job.setCompanyId(request.getCompanyId());
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
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

        jobMapper.insert(job);

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
                schedule.setSlotsAvailable(scheduleReq.getSlotsAvailable());
                jobScheduleMapper.insert(schedule);
            }
        }

        return toResponse(job);
    }

    @Override
    public JobVO updateJob(Long id, UpdateJobCmd request) {
        Job job = jobMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        if (request.getTitle() != null) job.setTitle(request.getTitle());
        if (request.getDescription() != null) job.setDescription(request.getDescription());
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
        jobMapper.update(job);
        return toResponse(job);
    }

    @Override
    public void deleteJob(Long id) {
        jobMapper.delete(id);
    }

    @Override
    public JobVO getJobById(Long id) {
        Job job = jobMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        JobVO response = toResponse(job);
        response.setRates(toRateResponses(jobRateMapper.findByJobId(id)));
        response.setSchedules(toScheduleResponses(jobScheduleMapper.findByJobId(id)));
        return response;
    }

    @Override
    public List<JobVO> getJobsByCompany(Long companyId, String status) {
        List<Job> jobs;
        if (status != null && !status.isEmpty()) {
            jobs = jobMapper.findByCompanyIdAndStatus(companyId, status);
        } else {
            jobs = jobMapper.findByCompanyId(companyId);
        }
        return jobs.stream().map(job -> {
            JobVO response = toResponse(job);
            response.setRates(toRateResponses(jobRateMapper.findByJobId(job.getId())));
            response.setSchedules(toScheduleResponses(jobScheduleMapper.findByJobId(job.getId())));
            return response;
        }).collect(Collectors.toList());
    }

    private void syncToCJob(Long jobId) {
        Job job = jobMapper.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        try {
            List<JobSchedule> schedules = jobScheduleMapper.findByJobId(jobId);
            List<JobScheduleVO> scheduleVOs = toScheduleResponses(schedules);
            String scheduleInfo = objectMapper.writeValueAsString(scheduleVOs);
            String companyName = enterpriseMapper.findCompanyNameById(job.getCompanyId());
            String companyLogo = enterpriseMapper.findCompanyLogoById(job.getCompanyId());
            cJobMapper.upsert(
                    job.getId(), job.getCompanyId(), companyName, companyLogo,
                    job.getTitle(), job.getDescription(), job.getLocation(),
                    job.getProvince(), job.getCity(), job.getDistrict(), job.getAddress(),
                    job.getLatitude(), job.getLongitude(),
                    job.getCategoryId(),
                    null, null,
                    job.getStatus(),
                    scheduleInfo,
                    job.getHeadcount(),
                    job.getDeadline()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize schedule info", e);
        }
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
        syncToCJob(job.getId());
        JobVO response = toResponse(job);
        response.setRates(toRateResponses(jobRateMapper.findByJobId(id)));
        response.setSchedules(toScheduleResponses(jobScheduleMapper.findByJobId(id)));
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
        response.setSchedules(toScheduleResponses(jobScheduleMapper.findByJobId(id)));
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
        response.setSchedules(toScheduleResponses(jobScheduleMapper.findByJobId(id)));
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
        return toScheduleResponses(jobScheduleMapper.findByJobId(jobId));
    }

    @Override
    public JobScheduleVO addJobSchedule(Long jobId, JobScheduleCmd request) {
        JobSchedule schedule = new JobSchedule();
        schedule.setJobId(jobId);
        schedule.setScheduleDate(request.getScheduleDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setSlotsAvailable(request.getSlotsAvailable());
        jobScheduleMapper.insert(schedule);
        syncToCJob(jobId);
        return toScheduleResponse(schedule);
    }

    @Override
    public JobScheduleVO updateJobSchedule(Long scheduleId, JobScheduleCmd request) {
        JobSchedule schedule = jobScheduleMapper.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("JobSchedule not found: " + scheduleId));
        Long jobId = schedule.getJobId();
        schedule.setScheduleDate(request.getScheduleDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setSlotsAvailable(request.getSlotsAvailable());
        jobScheduleMapper.update(schedule);
        syncToCJob(jobId);
        return toScheduleResponse(schedule);
    }

    @Override
    public void removeJobSchedule(Long scheduleId) {
        JobSchedule schedule = jobScheduleMapper.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("JobSchedule not found: " + scheduleId));
        Long jobId = schedule.getJobId();
        jobScheduleMapper.delete(scheduleId);
        syncToCJob(jobId);
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
        response.setLocation(job.getLocation());
        response.setProvince(job.getProvince());
        response.setCity(job.getCity());
        response.setDistrict(job.getDistrict());
        response.setAddress(job.getAddress());
        response.setLatitude(job.getLatitude());
        response.setLongitude(job.getLongitude());
        response.setCategoryId(job.getCategoryId());
        response.setHeadcount(job.getHeadcount());
        response.setStatus(JobStatus.valueOf(job.getStatus()));
        response.setDeadline(job.getDeadline());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }
}
