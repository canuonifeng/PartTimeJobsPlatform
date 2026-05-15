package com.parttime.enterprise.core.service;

import com.parttime.enterprise.api.dto.*;
import com.parttime.enterprise.core.domain.ScheduleShift;
import com.parttime.enterprise.core.domain.WorkerBlacklist;
import com.parttime.enterprise.core.domain.WorkerEvaluation;
import com.parttime.enterprise.core.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class WorkerProfileService {

    private final WorkerBlacklistRepository blacklistRepository;
    private final WorkerEvaluationRepository evaluationRepository;
    private final ScheduleShiftRepository shiftRepository;
    private final JobRepository jobRepository;

    public WorkerProfileService(WorkerBlacklistRepository blacklistRepository,
                                WorkerEvaluationRepository evaluationRepository,
                                ScheduleShiftRepository shiftRepository,
                                JobRepository jobRepository) {
        this.blacklistRepository = blacklistRepository;
        this.evaluationRepository = evaluationRepository;
        this.shiftRepository = shiftRepository;
        this.jobRepository = jobRepository;
    }

    public WorkerProfileResponse getWorkerProfile(Long companyId, Long workerId) {
        boolean isBlacklisted = blacklistRepository.findByCompanyIdAndWorkerId(companyId, workerId).isPresent();
        String blacklistReason = null;
        if (isBlacklisted) {
            blacklistReason = blacklistRepository.findByCompanyIdAndWorkerId(companyId, workerId)
                    .map(WorkerBlacklist::getReason).orElse(null);
        }

        Double avgRating = evaluationRepository.findAvgRatingByWorkerIdAndCompanyId(workerId, companyId);
        List<WorkerEvaluation> evaluations = evaluationRepository.findByWorkerIdAndCompanyId(workerId, companyId);

        List<ScheduleShift> completedShifts = shiftRepository.findCompletedByWorkerIdAndCompanyId(workerId, companyId);
        List<WorkHistoryResponse> workHistory = completedShifts.stream().map(shift -> {
            WorkHistoryResponse wh = new WorkHistoryResponse();
            wh.setShiftId(shift.getId());
            wh.setJobId(shift.getJobId());
            wh.setShiftDate(shift.getShiftDate());
            wh.setStartTime(shift.getStartTime());
            wh.setEndTime(shift.getEndTime());
            jobRepository.findById(shift.getJobId()).ifPresent(job -> wh.setJobTitle(job.getTitle()));
            return wh;
        }).collect(Collectors.toList());

        WorkerProfileResponse response = new WorkerProfileResponse();
        response.setWorkerId(workerId);
        response.setAvgRating(avgRating);
        response.setTotalEvaluations(evaluations.size());
        response.setIsBlacklisted(isBlacklisted);
        response.setBlacklistReason(blacklistReason);
        response.setWorkHistory(workHistory);
        return response;
    }

    public EvaluationResponse evaluateWorker(Long companyId, Long jobId, Long workerId, Integer rating, String comment) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        WorkerEvaluation evaluation = new WorkerEvaluation();
        evaluation.setCompanyId(companyId);
        evaluation.setJobId(jobId);
        evaluation.setWorkerId(workerId);
        evaluation.setRating(rating);
        evaluation.setComment(comment);
        evaluationRepository.save(evaluation);

        EvaluationResponse response = new EvaluationResponse();
        response.setId(evaluation.getId());
        response.setCompanyId(companyId);
        response.setJobId(jobId);
        response.setWorkerId(workerId);
        response.setRating(rating);
        response.setComment(comment);
        response.setCreatedAt(evaluation.getCreatedAt());
        return response;
    }

    public void addToBlacklist(Long companyId, Long workerId, String reason) {
        WorkerBlacklist blacklist = new WorkerBlacklist();
        blacklist.setCompanyId(companyId);
        blacklist.setWorkerId(workerId);
        blacklist.setReason(reason);
        blacklistRepository.save(blacklist);
    }

    public void removeFromBlacklist(Long companyId, Long workerId) {
        blacklistRepository.deleteByCompanyIdAndWorkerId(companyId, workerId);
    }

    public boolean isBlacklisted(Long companyId, Long workerId) {
        return blacklistRepository.findByCompanyIdAndWorkerId(companyId, workerId).isPresent();
    }

    public List<WorkHistoryResponse> getWorkHistory(Long workerId, Long companyId) {
        List<ScheduleShift> completedShifts = shiftRepository.findCompletedByWorkerIdAndCompanyId(workerId, companyId);
        return completedShifts.stream().map(shift -> {
            WorkHistoryResponse wh = new WorkHistoryResponse();
            wh.setShiftId(shift.getId());
            wh.setJobId(shift.getJobId());
            wh.setShiftDate(shift.getShiftDate());
            wh.setStartTime(shift.getStartTime());
            wh.setEndTime(shift.getEndTime());
            jobRepository.findById(shift.getJobId()).ifPresent(job -> wh.setJobTitle(job.getTitle()));
            return wh;
        }).collect(Collectors.toList());
    }

    public List<EvaluationResponse> getEvaluations(Long workerId, Long companyId) {
        return evaluationRepository.findByWorkerIdAndCompanyId(workerId, companyId).stream()
                .map(e -> {
                    EvaluationResponse response = new EvaluationResponse();
                    response.setId(e.getId());
                    response.setCompanyId(e.getCompanyId());
                    response.setJobId(e.getJobId());
                    response.setWorkerId(e.getWorkerId());
                    response.setRating(e.getRating());
                    response.setComment(e.getComment());
                    response.setCreatedAt(e.getCreatedAt());
                    return response;
                }).collect(Collectors.toList());
    }
}
