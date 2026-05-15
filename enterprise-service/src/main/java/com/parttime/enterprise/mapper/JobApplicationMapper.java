package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.JobApplication;
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

    int countByJobIdAndStatus(@Param("jobId") Long jobId, @Param("status") String status);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
