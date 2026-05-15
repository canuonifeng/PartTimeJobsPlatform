package com.parttime.enterprise.infrastructure.mapper;

import com.parttime.enterprise.core.domain.JobApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobApplicationMapper {

    int insert(JobApplication application);

    Optional<JobApplication> findById(Long id);

    List<JobApplication> findByJobId(Long jobId);

    List<JobApplication> findByWorkerId(Long workerId);

    List<JobApplication> findAll();

    Optional<JobApplication> findByJobIdAndWorkerId(@Param("jobId") Long jobId, @Param("workerId") Long workerId);

    int countByJobIdAndStatus(@Param("jobId") Long jobId, @Param("status") String status);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int update(JobApplication application);

    int delete(Long id);
}
