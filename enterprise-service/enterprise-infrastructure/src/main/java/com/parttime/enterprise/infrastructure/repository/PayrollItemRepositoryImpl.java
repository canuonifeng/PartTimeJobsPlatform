package com.parttime.enterprise.infrastructure.repository;

import com.parttime.enterprise.core.domain.PayrollItem;
import com.parttime.enterprise.core.repository.PayrollItemRepository;
import com.parttime.enterprise.infrastructure.mapper.PayrollItemMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PayrollItemRepositoryImpl implements PayrollItemRepository {

    private final PayrollItemMapper mapper;

    public PayrollItemRepositoryImpl(PayrollItemMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(PayrollItem item) {
        mapper.insert(item);
    }

    @Override
    public void saveAll(List<PayrollItem> items) {
        mapper.insertBatch(items);
    }

    @Override
    public Optional<PayrollItem> findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public List<PayrollItem> findByBatchId(Long batchId) {
        return mapper.findByBatchId(batchId);
    }

    @Override
    public List<PayrollItem> findByBatchIdAndWorkerId(Long batchId, Long workerId) {
        return mapper.findByBatchIdAndWorkerId(batchId, workerId);
    }
}
