package com.parttime.enterprise.core.repository;

import com.parttime.enterprise.core.domain.PayrollItem;

import java.util.List;
import java.util.Optional;

public interface PayrollItemRepository {

    void save(PayrollItem item);

    void saveAll(List<PayrollItem> items);

    Optional<PayrollItem> findById(Long id);

    List<PayrollItem> findByBatchId(Long batchId);

    List<PayrollItem> findByBatchIdAndWorkerId(Long batchId, Long workerId);
}
