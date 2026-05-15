package com.parttime.enterprise.infrastructure.repository;

import com.parttime.enterprise.core.domain.WorkerEvaluation;
import com.parttime.enterprise.core.repository.WorkerEvaluationRepository;
import com.parttime.enterprise.infrastructure.mapper.WorkerEvaluationMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WorkerEvaluationRepositoryImpl implements WorkerEvaluationRepository {

    private final WorkerEvaluationMapper mapper;

    public WorkerEvaluationRepositoryImpl(WorkerEvaluationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(WorkerEvaluation evaluation) {
        mapper.insert(evaluation);
    }

    @Override
    public List<WorkerEvaluation> findByWorkerIdAndCompanyId(Long workerId, Long companyId) {
        return mapper.findByWorkerIdAndCompanyId(workerId, companyId);
    }

    @Override
    public List<WorkerEvaluation> findByWorkerId(Long workerId) {
        return mapper.findByWorkerId(workerId);
    }

    @Override
    public Double findAvgRatingByWorkerIdAndCompanyId(Long workerId, Long companyId) {
        return mapper.findAvgRatingByWorkerIdAndCompanyId(workerId, companyId);
    }
}
