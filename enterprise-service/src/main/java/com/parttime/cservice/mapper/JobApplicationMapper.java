package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.JobApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobApplicationMapper {
    int insert(JobApplication application);
    Optional<JobApplication> findById(Long id);
    List<JobApplication> findByWorkerId(Long workerId);
    List<JobApplication> findByJobId(Long jobId);
    List<JobApplication> findByWorkerIdAndJobId(@Param("workerId") Long workerId, @Param("jobId") Long jobId);
    int countByWorkerIdAndStatus(@Param("workerId") Long workerId, @Param("status") String status);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
