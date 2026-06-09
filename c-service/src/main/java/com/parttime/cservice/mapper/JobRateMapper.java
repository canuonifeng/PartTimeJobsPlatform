package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.JobRate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobRateMapper {
    List<JobRate> findByJobId(Long jobId);
    List<JobRate> findByJobIds(@Param("jobIds") List<Long> jobIds);
    int insert(JobRate jobRate);
}
