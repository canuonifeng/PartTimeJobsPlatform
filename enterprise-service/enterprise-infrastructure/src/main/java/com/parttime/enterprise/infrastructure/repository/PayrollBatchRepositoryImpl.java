package com.parttime.enterprise.infrastructure.repository;

import com.parttime.enterprise.core.domain.PayrollBatch;
import com.parttime.enterprise.core.repository.PayrollBatchRepository;
import com.parttime.enterprise.infrastructure.mapper.PayrollBatchMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PayrollBatchRepositoryImpl implements PayrollBatchRepository {

    private final PayrollBatchMapper mapper;

    public PayrollBatchRepositoryImpl(PayrollBatchMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(PayrollBatch batch) {
        mapper.insert(batch);
    }

    @Override
    public Optional<PayrollBatch> findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public List<PayrollBatch> findByCompanyId(Long companyId) {
        return mapper.findByCompanyId(companyId);
    }

    @Override
    public List<PayrollBatch> findByCompanyIdAndStatus(Long companyId, String status) {
        return mapper.findByCompanyIdAndStatus(companyId, status);
    }

    @Override
    public void updateStatus(Long id, String status) {
        mapper.updateStatus(id, status);
    }

    @Override
    public void update(PayrollBatch batch) {
        mapper.update(batch);
    }
}
