package com.parttime.platform.infrastructure.repository;

import com.parttime.platform.core.domain.JobCategory;
import com.parttime.platform.core.repository.JobCategoryRepository;
import com.parttime.platform.infrastructure.mapper.JobCategoryMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JobCategoryRepositoryImpl implements JobCategoryRepository {

    private final JobCategoryMapper mapper;

    public JobCategoryRepositoryImpl(JobCategoryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(JobCategory category) {
        mapper.insert(category);
    }

    @Override
    public Optional<JobCategory> findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public List<JobCategory> findAll() {
        return mapper.findAll();
    }

    @Override
    public void update(JobCategory category) {
        mapper.update(category);
    }

    @Override
    public void delete(Long id) {
        mapper.delete(id);
    }
}
