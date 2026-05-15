package com.parttime.enterprise.infrastructure.mapper;

import com.parttime.enterprise.core.domain.JobRate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobRateMapper {

    int insert(JobRate jobRate);

    Optional<JobRate> findById(Long id);

    List<JobRate> findByJobId(Long jobId);

    List<JobRate> findAll();

    int update(JobRate jobRate);

    int delete(Long id);

    int deleteByJobId(Long jobId);
}
