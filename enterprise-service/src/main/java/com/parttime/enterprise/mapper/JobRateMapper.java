package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.JobRate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobRateMapper {

    int insert(JobRate rate);

    Optional<JobRate> findById(Long id);

    List<JobRate> findByJobId(Long jobId);

    int update(JobRate rate);

    int delete(Long id);

    int deleteByJobId(Long jobId);
}
