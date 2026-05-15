package com.parttime.enterprise.core.repository;

import com.parttime.enterprise.core.domain.WithdrawalRecord;

import java.util.List;
import java.util.Optional;

public interface WithdrawalRecordRepository {

    void save(WithdrawalRecord record);

    Optional<WithdrawalRecord> findById(Long id);

    List<WithdrawalRecord> findByWorkerId(Long workerId);

    List<WithdrawalRecord> findByWorkerIdAndStatus(Long workerId, String status);

    void updateStatus(Long id, String status);
}
