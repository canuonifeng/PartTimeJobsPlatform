package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.JobReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobReportMapper {

    Optional<JobReport> findById(Long id);

    List<JobReport> findByStatus(String status);

    List<JobReport> findAll();

    int update(JobReport report);
}
