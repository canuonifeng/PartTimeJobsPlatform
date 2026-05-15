package com.parttime.enterprise.core.repository;

import com.parttime.enterprise.core.domain.JobApplication;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository {

    List<JobApplication> findByJobId(Long jobId);

    List<JobApplication> findByWorkerId(Long workerId);

    Optional<JobApplication> findById(Long id);

    int countByJobIdAndStatus(Long jobId, String status);

    void updateStatus(Long id, String status);
}
