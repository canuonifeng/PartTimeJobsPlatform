package com.parttime.platform.infrastructure.repository;

import com.parttime.platform.core.domain.JobReport;
import com.parttime.platform.core.repository.JobReportRepository;
import com.parttime.platform.infrastructure.mapper.JobReportMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JobReportRepositoryImpl implements JobReportRepository {

    private final JobReportMapper mapper;

    public JobReportRepositoryImpl(JobReportMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<JobReport> findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public List<JobReport> findByStatus(String status) {
        return mapper.findByStatus(status);
    }

    @Override
    public List<JobReport> findAll() {
        return mapper.findAll();
    }

    @Override
    public void update(JobReport report) {
        mapper.update(report);
    }
}
