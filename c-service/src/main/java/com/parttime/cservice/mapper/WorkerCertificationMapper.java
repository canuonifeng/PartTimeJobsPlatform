package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.WorkerCertification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface WorkerCertificationMapper {

    Optional<WorkerCertification> findByWorkerAndCert(@Param("workerId") Long workerId, @Param("certificationId") Long certificationId);

    List<WorkerCertification> findByWorkerId(@Param("workerId") Long workerId);

    int insert(WorkerCertification certification);

    int update(WorkerCertification certification);
}
