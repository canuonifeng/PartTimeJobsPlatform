package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.JobSchedule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobScheduleMapper {

    int insert(JobSchedule schedule);

    List<JobSchedule> findByJobId(Long jobId);

    List<JobSchedule> findActiveByJobId(Long jobId);

    Optional<JobSchedule> findById(Long id);
}
