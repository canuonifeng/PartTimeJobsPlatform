package com.parttime.enterprise.core.repository;

import com.parttime.enterprise.core.domain.PayrollBatch;

import java.util.List;
import java.util.Optional;

public interface PayrollBatchRepository {

    void save(PayrollBatch batch);

    Optional<PayrollBatch> findById(Long id);

    List<PayrollBatch> findByCompanyId(Long companyId);

    List<PayrollBatch> findByCompanyIdAndStatus(Long companyId, String status);

    void updateStatus(Long id, String status);

    void update(PayrollBatch batch);
}
