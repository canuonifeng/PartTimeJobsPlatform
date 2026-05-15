package com.parttime.enterprise.core.service;

import com.parttime.enterprise.api.dto.*;
import com.parttime.enterprise.core.domain.Job;
import com.parttime.enterprise.core.domain.JobRate;
import com.parttime.enterprise.core.domain.JobSchedule;
import com.parttime.enterprise.core.repository.JobRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public JobResponse createJob(JobCreateRequest request) {
        Job job = new Job();
        job.setCompanyId(request.getCompanyId());
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setCategoryId(request.getCategoryId());
        job.setHeadcount(request.getHeadcount());
        job.setStatus("DRAFT");
        job.setDeadline(request.getDeadline());

        jobRepository.save(job);

        if (request.getRates() != null) {
            for (JobRateRequest rateReq : request.getRates()) {
                JobRate rate = new JobRate();
                rate.setJobId(job.getId());
                rate.setType(rateReq.getType().name());
                rate.setAmount(rateReq.getAmount());
                rate.setCurrency(rateReq.getCurrency());
                rate.setRules(rateReq.getRules());
                jobRepository.saveRate(rate);
            }
        }

        if (request.getSchedules() != null) {
            for (JobScheduleRequest scheduleReq : request.getSchedules()) {
                JobSchedule schedule = new JobSchedule();
                schedule.setJobId(job.getId());
                schedule.setScheduleDate(scheduleReq.getScheduleDate());
                schedule.setStartTime(scheduleReq.getStartTime());
                schedule.setEndTime(scheduleReq.getEndTime());
                schedule.setSlotsAvailable(scheduleReq.getSlotsAvailable());
                jobRepository.saveSchedule(schedule);
            }
        }

        return toResponse(job);
    }

    public JobResponse updateJob(Long id, JobCreateRequest request) {
        return null;
    }

    public void deleteJob(Long id) {
    }

    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        JobResponse response = toResponse(job);
        response.setRates(toRateResponses(jobRepository.findRatesByJobId(id)));
        response.setSchedules(toScheduleResponses(jobRepository.findSchedulesByJobId(id)));
        return response;
    }

    public List<JobResponse> getJobsByCompany(Long companyId, String status) {
        return List.of();
    }

    // === 3.3 Status Management ===

    public JobResponse publishJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        if (!"DRAFT".equals(job.getStatus())) {
            throw new RuntimeException("Cannot publish job in status: " + job.getStatus());
        }
        jobRepository.updateStatus(id, "PUBLISHED");
        job.setStatus("PUBLISHED");
        JobResponse response = toResponse(job);
        response.setRates(toRateResponses(jobRepository.findRatesByJobId(id)));
        response.setSchedules(toScheduleResponses(jobRepository.findSchedulesByJobId(id)));
        return response;
    }

    public JobResponse closeJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        if (!"PUBLISHED".equals(job.getStatus())) {
            throw new RuntimeException("Cannot close job in status: " + job.getStatus());
        }
        jobRepository.updateStatus(id, "CLOSED");
        job.setStatus("CLOSED");
        JobResponse response = toResponse(job);
        response.setRates(toRateResponses(jobRepository.findRatesByJobId(id)));
        response.setSchedules(toScheduleResponses(jobRepository.findSchedulesByJobId(id)));
        return response;
    }

    public JobResponse reopenJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        if (!"CLOSED".equals(job.getStatus())) {
            throw new RuntimeException("Cannot reopen job in status: " + job.getStatus());
        }
        jobRepository.updateStatus(id, "PUBLISHED");
        job.setStatus("PUBLISHED");
        JobResponse response = toResponse(job);
        response.setRates(toRateResponses(jobRepository.findRatesByJobId(id)));
        response.setSchedules(toScheduleResponses(jobRepository.findSchedulesByJobId(id)));
        return response;
    }

    public void expireJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        if (!"PUBLISHED".equals(job.getStatus())) {
            throw new RuntimeException("Cannot expire job in status: " + job.getStatus());
        }
        jobRepository.updateStatus(id, "EXPIRED");
    }

    // === 3.4 Mixed Salary Rate Configuration ===

    public List<JobRateResponse> getJobRates(Long jobId) {
        return toRateResponses(jobRepository.findRatesByJobId(jobId));
    }

    public JobRateResponse addJobRate(Long jobId, JobRateRequest request) {
        JobRate rate = new JobRate();
        rate.setJobId(jobId);
        rate.setType(request.getType().name());
        rate.setAmount(request.getAmount());
        rate.setCurrency(request.getCurrency());
        rate.setRules(request.getRules());
        jobRepository.saveRate(rate);
        return toRateResponse(rate);
    }

    public JobRateResponse updateJobRate(Long rateId, JobRateRequest request) {
        JobRate rate = jobRepository.findRateById(rateId)
                .orElseThrow(() -> new RuntimeException("JobRate not found: " + rateId));
        rate.setType(request.getType().name());
        rate.setAmount(request.getAmount());
        rate.setCurrency(request.getCurrency());
        rate.setRules(request.getRules());
        jobRepository.updateRate(rate);
        return toRateResponse(rate);
    }

    public void removeJobRate(Long rateId) {
        jobRepository.deleteRate(rateId);
    }

    // === 3.5 Optional Schedule Configuration ===

    public List<JobScheduleResponse> getJobSchedules(Long jobId) {
        return toScheduleResponses(jobRepository.findSchedulesByJobId(jobId));
    }

    public JobScheduleResponse addJobSchedule(Long jobId, JobScheduleRequest request) {
        JobSchedule schedule = new JobSchedule();
        schedule.setJobId(jobId);
        schedule.setScheduleDate(request.getScheduleDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setSlotsAvailable(request.getSlotsAvailable());
        jobRepository.saveSchedule(schedule);
        return toScheduleResponse(schedule);
    }

    public JobScheduleResponse updateJobSchedule(Long scheduleId, JobScheduleRequest request) {
        JobSchedule schedule = jobRepository.findScheduleById(scheduleId)
                .orElseThrow(() -> new RuntimeException("JobSchedule not found: " + scheduleId));
        schedule.setScheduleDate(request.getScheduleDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setSlotsAvailable(request.getSlotsAvailable());
        jobRepository.updateSchedule(schedule);
        return toScheduleResponse(schedule);
    }

    public void removeJobSchedule(Long scheduleId) {
        jobRepository.deleteSchedule(scheduleId);
    }

    // === Helper methods ===

    private List<JobRateResponse> toRateResponses(List<JobRate> rates) {
        return rates.stream().map(this::toRateResponse).collect(Collectors.toList());
    }

    private JobRateResponse toRateResponse(JobRate rate) {
        JobRateResponse response = new JobRateResponse();
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

    private List<JobScheduleResponse> toScheduleResponses(List<JobSchedule> schedules) {
        return schedules.stream().map(this::toScheduleResponse).collect(Collectors.toList());
    }

    private JobScheduleResponse toScheduleResponse(JobSchedule schedule) {
        JobScheduleResponse response = new JobScheduleResponse();
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

    private JobResponse toResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setCompanyId(job.getCompanyId());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setLocation(job.getLocation());
        response.setCategoryId(job.getCategoryId());
        response.setHeadcount(job.getHeadcount());
        response.setStatus(JobStatus.valueOf(job.getStatus()));
        response.setDeadline(job.getDeadline());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        return response;
    }
}