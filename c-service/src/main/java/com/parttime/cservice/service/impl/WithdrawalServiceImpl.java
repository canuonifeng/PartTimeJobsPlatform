package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.WithdrawalRecordMapper;
import com.parttime.cservice.pojo.entity.WithdrawalRecord;
import com.parttime.cservice.pojo.vo.EarningsSummaryVO;
import com.parttime.cservice.pojo.vo.WithdrawalVO;
import com.parttime.cservice.service.WithdrawalService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WithdrawalServiceImpl implements WithdrawalService {

    @Resource
    private WithdrawalRecordMapper withdrawalRecordMapper;

    public WithdrawalVO requestWithdrawal(Long workerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid withdrawal amount");
        }

        List<WithdrawalRecord> pending = withdrawalRecordMapper.findByWorkerId(workerId).stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .collect(Collectors.toList());

        BigDecimal totalPending = pending.stream()
                .map(WithdrawalRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPending.add(amount).compareTo(new BigDecimal("100000")) > 0) {
            throw new RuntimeException("Insufficient balance: total pending withdrawals exceed available balance");
        }

        WithdrawalRecord record = new WithdrawalRecord();
        record.setWorkerId(workerId);
        record.setAmount(amount);
        record.setStatus("PENDING");
        record.setRequestedAt(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        withdrawalRecordMapper.insert(record);

        return toResponse(record);
    }

    public List<WithdrawalVO> getWithdrawalHistory(Long workerId) {
        return withdrawalRecordMapper.findByWorkerId(workerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EarningsSummaryVO getEarningsSummary(Long workerId) {
        List<WithdrawalRecord> workerRecords = withdrawalRecordMapper.findByWorkerId(workerId);

        BigDecimal totalWithdrawn = workerRecords.stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .map(WithdrawalRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingWithdrawal = workerRecords.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .map(WithdrawalRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEarned = totalWithdrawn.add(pendingWithdrawal);

        EarningsSummaryVO resp = new EarningsSummaryVO();
        resp.setTotalEarned(totalEarned);
        resp.setTotalWithdrawn(totalWithdrawn);
        resp.setPendingWithdrawal(pendingWithdrawal);
        return resp;
    }

    private WithdrawalVO toResponse(WithdrawalRecord record) {
        WithdrawalVO resp = new WithdrawalVO();
        resp.setId(record.getId());
        resp.setWorkerId(record.getWorkerId());
        resp.setAmount(record.getAmount());
        resp.setStatus(record.getStatus());
        resp.setRequestedAt(record.getRequestedAt());
        return resp;
    }
}
