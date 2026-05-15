package com.parttime.platform.infrastructure.mapper;

import com.parttime.platform.core.domain.JobReport;
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
