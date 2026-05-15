package com.parttime.enterprise.infrastructure.repository;

import com.parttime.enterprise.core.domain.WorkerBlacklist;
import com.parttime.enterprise.core.repository.WorkerBlacklistRepository;
import com.parttime.enterprise.infrastructure.mapper.WorkerBlacklistMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class WorkerBlacklistRepositoryImpl implements WorkerBlacklistRepository {

    private final WorkerBlacklistMapper mapper;

    public WorkerBlacklistRepositoryImpl(WorkerBlacklistMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(WorkerBlacklist blacklist) {
        mapper.insert(blacklist);
    }

    @Override
    public Optional<WorkerBlacklist> findByCompanyIdAndWorkerId(Long companyId, Long workerId) {
        return mapper.findByCompanyIdAndWorkerId(companyId, workerId);
    }

    @Override
    public void deleteByCompanyIdAndWorkerId(Long companyId, Long workerId) {
        mapper.deleteByCompanyIdAndWorkerId(companyId, workerId);
    }
}
