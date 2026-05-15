package com.parttime.enterprise.core.repository;

import com.parttime.enterprise.core.domain.WorkerBlacklist;

import java.util.Optional;

public interface WorkerBlacklistRepository {

    void save(WorkerBlacklist blacklist);

    Optional<WorkerBlacklist> findByCompanyIdAndWorkerId(Long companyId, Long workerId);

    void deleteByCompanyIdAndWorkerId(Long companyId, Long workerId);
}
