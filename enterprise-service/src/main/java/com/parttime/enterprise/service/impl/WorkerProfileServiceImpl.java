package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.dao.JobDao;
import com.parttime.enterprise.dao.ScheduleShiftDao;
import com.parttime.enterprise.dao.WorkerBlacklistDao;
import com.parttime.enterprise.dao.WorkerEvaluationDao;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.entity.WorkerBlacklist;
import com.parttime.enterprise.pojo.entity.WorkerEvaluation;
import com.parttime.enterprise.pojo.vo.EvaluationVO;
import com.parttime.enterprise.pojo.vo.WorkHistoryVO;
import com.parttime.enterprise.pojo.vo.WorkerProfileVO;
import com.parttime.enterprise.service.WorkerProfileService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkerProfileServiceImpl implements WorkerProfileService {

    private final WorkerBlacklistDao blacklistDao;
    private final WorkerEvaluationDao evaluationDao;
    private final ScheduleShiftDao shiftDao;
    private final JobDao jobDao;

    public WorkerProfileServiceImpl(WorkerBlacklistDao blacklistDao,
                                    WorkerEvaluationDao evaluationDao,
                                    ScheduleShiftDao shiftDao,
                                    JobDao jobDao) {
        this.blacklistDao = blacklistDao;
        this.evaluationDao = evaluationDao;
        this.shiftDao = shiftDao;
        this.jobDao = jobDao;
    }

    @Override
    public WorkerProfileVO getWorkerProfile(Long companyId, Long workerId) {
        boolean isBlacklisted = blacklistDao.findByCompanyIdAndWorkerId(companyId, workerId).isPresent();
        String blacklistReason = null;
        if (isBlacklisted) {
            blacklistReason = blacklistDao.findByCompanyIdAndWorkerId(companyId, workerId)
                    .map(WorkerBlacklist::getReason).orElse(null);
        }

        Double avgRating = evaluationDao.findAvgRatingByWorkerIdAndCompanyId(workerId, companyId);
        List<WorkerEvaluation> evaluations = evaluationDao.findByWorkerIdAndCompanyId(workerId, companyId);

        List<ScheduleShift> completedShifts = shiftDao.findCompletedByWorkerIdAndCompanyId(workerId, companyId);
        List<WorkHistoryVO> workHistory = completedShifts.stream().map(shift -> {
            WorkHistoryVO wh = new WorkHistoryVO();
            wh.setShiftId(shift.getId());
            wh.setJobId(shift.getJobId());
            wh.setShiftDate(shift.getShiftDate());
            wh.setStartTime(shift.getStartTime());
            wh.setEndTime(shift.getEndTime());
            jobDao.findById(shift.getJobId()).ifPresent(job -> wh.setJobTitle(job.getTitle()));
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
        evaluationDao.save(evaluation);

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
        blacklistDao.save(blacklist);
    }

    @Override
    public void removeFromBlacklist(Long companyId, Long workerId) {
        blacklistDao.deleteByCompanyIdAndWorkerId(companyId, workerId);
    }

    @Override
    public boolean isBlacklisted(Long companyId, Long workerId) {
        return blacklistDao.findByCompanyIdAndWorkerId(companyId, workerId).isPresent();
    }

    @Override
    public List<WorkHistoryVO> getWorkHistory(Long workerId, Long companyId) {
        List<ScheduleShift> completedShifts = shiftDao.findCompletedByWorkerIdAndCompanyId(workerId, companyId);
        return completedShifts.stream().map(shift -> {
            WorkHistoryVO wh = new WorkHistoryVO();
            wh.setShiftId(shift.getId());
            wh.setJobId(shift.getJobId());
            wh.setShiftDate(shift.getShiftDate());
            wh.setStartTime(shift.getStartTime());
            wh.setEndTime(shift.getEndTime());
            jobDao.findById(shift.getJobId()).ifPresent(job -> wh.setJobTitle(job.getTitle()));
            return wh;
        }).collect(Collectors.toList());
    }

    @Override
    public List<EvaluationVO> getEvaluations(Long workerId, Long companyId) {
        return evaluationDao.findByWorkerIdAndCompanyId(workerId, companyId).stream()
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
