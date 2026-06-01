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

    List<JobApplication> findAll();

    List<JobApplication> findByJobId(Long jobId);

    List<JobApplication> findByJobIdPaged(@Param("jobId") Long jobId, @Param("offset") int offset, @Param("limit") int limit);

    List<JobApplication> findByCompanyId(@Param("companyId") Long companyId);

    List<JobApplication> findByWorkerId(Long workerId);

    int countByJobId(Long jobId);

    int countByScheduleId(@Param("scheduleId") Long scheduleId);

    int countByJobIdAndStatus(@Param("jobId") Long jobId, @Param("status") String status);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
