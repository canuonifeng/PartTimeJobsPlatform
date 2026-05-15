package com.parttime.enterprise.core.repository;

import com.parttime.enterprise.core.domain.WorkerEvaluation;

import java.util.List;

public interface WorkerEvaluationRepository {

    void save(WorkerEvaluation evaluation);

    List<WorkerEvaluation> findByWorkerIdAndCompanyId(Long workerId, Long companyId);

    List<WorkerEvaluation> findByWorkerId(Long workerId);

    Double findAvgRatingByWorkerIdAndCompanyId(Long workerId, Long companyId);
}
