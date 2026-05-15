package com.parttime.platform.core.repository;

import com.parttime.platform.core.domain.JobReport;

import java.util.List;
import java.util.Optional;

public interface JobReportRepository {

    Optional<JobReport> findById(Long id);

    List<JobReport> findByStatus(String status);

    List<JobReport> findAll();

    void update(JobReport report);
}
