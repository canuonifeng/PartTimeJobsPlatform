package com.parttime.enterprise.infrastructure.mapper;

import com.parttime.enterprise.core.domain.JobSchedule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobScheduleMapper {

    int insert(JobSchedule schedule);

    Optional<JobSchedule> findById(Long id);

    List<JobSchedule> findByJobId(Long jobId);

    List<JobSchedule> findAll();

    int update(JobSchedule schedule);

    int delete(Long id);

    int deleteByJobId(Long jobId);
}
