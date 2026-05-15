package com.parttime.enterprise.infrastructure.mapper;

import com.parttime.enterprise.core.domain.WorkerEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkerEvaluationMapper {

    int insert(WorkerEvaluation evaluation);

    List<WorkerEvaluation> findByWorkerIdAndCompanyId(@Param("workerId") Long workerId, @Param("companyId") Long companyId);

    List<WorkerEvaluation> findByWorkerId(@Param("workerId") Long workerId);

    Double findAvgRatingByWorkerIdAndCompanyId(@Param("workerId") Long workerId, @Param("companyId") Long companyId);
}
