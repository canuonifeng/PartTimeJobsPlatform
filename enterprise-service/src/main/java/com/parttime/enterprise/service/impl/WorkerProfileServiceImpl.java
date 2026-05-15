package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerBlacklistMapper;
import com.parttime.enterprise.mapper.WorkerEvaluationMapper;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.entity.WorkerBlacklist;
import com.parttime.enterprise.pojo.entity.WorkerEvaluation;
import com.parttime.enterprise.pojo.vo.EvaluationVO;
import com.parttime.enterprise.pojo.vo.WorkHistoryVO;
import com.parttime.enterprise.pojo.vo.WorkerProfileVO;
import com.parttime.enterprise.service.WorkerProfileService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkerProfileServiceImpl implements WorkerProfileService {

    @Resource
    private WorkerBlacklistMapper blacklistMapper;
    @Resource
    private WorkerEvaluationMapper evaluationMapper;
    @Resource
    private ScheduleShiftMapper shiftMapper;
    @Resource
    private JobMapper jobMapper;

    @Override
    public WorkerProfileVO getWorkerProfile(Long companyId, Long workerId) {
        boolean isBlacklisted = blacklistMapper.findByCompanyIdAndWorkerId(companyId, workerId).isPresent();
        String blacklistReason = null;
        if (isBlacklisted) {
            blacklistReason = blacklistMapper.findByCompanyIdAndWorkerId(companyId, workerId)
                    .map(WorkerBlacklist::getReason).orElse(null);
        }

        Double avgRating = evaluationMapper.findAvgRatingByWorkerIdAndCompanyId(workerId, companyId);
        List<WorkerEvaluation> evaluations = evaluationMapper.findByWorkerIdAndCompanyId(workerId, companyId);

        List<ScheduleShift> completedShifts = shiftMapper.findCompletedByWorkerIdAndCompanyId(workerId, companyId);
        List<WorkHistoryVO> workHistory = completedShifts.stream().map(shift -> {
            WorkHistoryVO wh = new WorkHistoryVO();
            wh.setShiftId(shift.getId());
            wh.setJobId(shift.getJobId());
            wh.setShiftDate(shift.getShiftDate());
            wh.setStartTime(shift.getStartTime());
            wh.setEndTime(shift.getEndTime());
            jobMapper.findById(shift.getJobId()).ifPresent(job -> wh.setJobTitle(job.getTitle()));
            return wh;
        }).collect(Collectors.toList());

        WorkerProfileVO response = new WorkerProfileVO();
        response.setWorkerId(workerId);
        response.setAvgRating(avgRating);
        response.setTotalEvaluations(evaluations.size());
        response.setIsBlacklisted(isBlacklisted);
        response.setBlacklistReason(blacklistReason);
        response.setWorkHistory(workHistory);
        return response;
    }

    @Override
    public EvaluationVO evaluateWorker(Long companyId, Long jobId, Long workerId, Integer rating, String comment) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        WorkerEvaluation evaluation = new WorkerEvaluation();
        evaluation.setCompanyId(companyId);
        evaluation.setJobId(jobId);
        evaluation.setWorkerId(workerId);
        evaluation.setRating(rating);
        evaluation.setComment(comment);
        evaluationMapper.insert(evaluation);

        EvaluationVO response = new EvaluationVO();
        response.setId(evaluation.getId());
        response.setCompanyId(companyId);
        response.setJobId(jobId);
        response.setWorkerId(workerId);
        response.setRating(rating);
        response.setComment(comment);
        response.setCreatedAt(evaluation.getCreatedAt());
        return response;
    }

    @Override
    public void addToBlacklist(Long companyId, Long workerId, String reason) {
        WorkerBlacklist blacklist = new WorkerBlacklist();
        blacklist.setCompanyId(companyId);
        blacklist.setWorkerId(workerId);
        blacklist.setReason(reason);
        blacklistMapper.insert(blacklist);
    }

    @Override
    public void removeFromBlacklist(Long companyId, Long workerId) {
        blacklistMapper.deleteByCompanyIdAndWorkerId(companyId, workerId);
    }

    @Override
    public boolean isBlacklisted(Long companyId, Long workerId) {
        return blacklistMapper.findByCompanyIdAndWorkerId(companyId, workerId).isPresent();
    }

    @Override
    public List<WorkHistoryVO> getWorkHistory(Long workerId, Long companyId) {
        List<ScheduleShift> completedShifts = shiftMapper.findCompletedByWorkerIdAndCompanyId(workerId, companyId);
        return completedShifts.stream().map(shift -> {
            WorkHistoryVO wh = new WorkHistoryVO();
            wh.setShiftId(shift.getId());
            wh.setJobId(shift.getJobId());
            wh.setShiftDate(shift.getShiftDate());
            wh.setStartTime(shift.getStartTime());
            wh.setEndTime(shift.getEndTime());
            jobMapper.findById(shift.getJobId()).ifPresent(job -> wh.setJobTitle(job.getTitle()));
            return wh;
        }).collect(Collectors.toList());
    }

    @Override
    public List<EvaluationVO> getEvaluations(Long workerId, Long companyId) {
        return evaluationMapper.findByWorkerIdAndCompanyId(workerId, companyId).stream()
                .map(e -> {
                    EvaluationVO response = new EvaluationVO();
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
