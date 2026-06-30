package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.JobSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobScheduleMapper {

    Optional<JobSchedule> findById(Long id);

    List<JobSchedule> findByJobId(Long jobId);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
