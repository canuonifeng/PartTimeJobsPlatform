package com.parttime.enterprise.infrastructure.repository;

import com.parttime.enterprise.core.domain.WithdrawalRecord;
import com.parttime.enterprise.core.repository.WithdrawalRecordRepository;
import com.parttime.enterprise.infrastructure.mapper.WithdrawalRecordMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class WithdrawalRecordRepositoryImpl implements WithdrawalRecordRepository {

    private final WithdrawalRecordMapper mapper;

    public WithdrawalRecordRepositoryImpl(WithdrawalRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(WithdrawalRecord record) {
        mapper.insert(record);
    }

    @Override
    public Optional<WithdrawalRecord> findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public List<WithdrawalRecord> findByWorkerId(Long workerId) {
        return mapper.findByWorkerId(workerId);
    }

    @Override
    public List<WithdrawalRecord> findByWorkerIdAndStatus(Long workerId, String status) {
        return mapper.findByWorkerIdAndStatus(workerId, status);
    }

    @Override
    public void updateStatus(Long id, String status) {
        mapper.updateStatus(id, status);
    }
}
