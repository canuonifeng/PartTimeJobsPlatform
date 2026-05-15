package com.parttime.platform.core.repository;

import com.parttime.platform.core.domain.JobCategory;

import java.util.List;
import java.util.Optional;

public interface JobCategoryRepository {

    void save(JobCategory category);

    Optional<JobCategory> findById(Long id);

    List<JobCategory> findAll();

    void update(JobCategory category);

    void delete(Long id);
}
