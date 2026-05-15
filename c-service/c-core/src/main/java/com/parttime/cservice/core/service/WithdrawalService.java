package com.parttime.cservice.core.service;

import com.parttime.cservice.core.dto.EarningsSummaryResponse;
import com.parttime.cservice.core.dto.WithdrawalResponse;
import com.parttime.cservice.core.model.WithdrawalRecord;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class WithdrawalService {

    private final ConcurrentHashMap<Long, WithdrawalRecord> records = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public WithdrawalResponse requestWithdrawal(Long workerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid withdrawal amount");
        }

        BigDecimal totalPending = records.values().stream()
                .filter(r -> r.getWorkerId().equals(workerId) && "PENDING".equals(r.getStatus()))
                .map(WithdrawalRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPending.add(amount).compareTo(new BigDecimal("100000")) > 0) {
            throw new RuntimeException("Insufficient balance: total pending withdrawals exceed available balance");
        }

        WithdrawalRecord record = new WithdrawalRecord(
                idCounter.getAndIncrement(), workerId, amount, "PENDING",
                LocalDateTime.now(), null);
        records.put(record.getId(), record);

        return toResponse(record);
    }

    public List<WithdrawalResponse> getWithdrawalHistory(Long workerId) {
        return records.values().stream()
                .filter(r -> r.getWorkerId().equals(workerId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EarningsSummaryResponse getEarningsSummary(Long workerId) {
        List<WithdrawalRecord> workerRecords = records.values().stream()
                .filter(r -> r.getWorkerId().equals(workerId))
                .collect(Collectors.toList());

        BigDecimal totalWithdrawn = workerRecords.stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .map(WithdrawalRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingWithdrawal = workerRecords.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .map(WithdrawalRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEarned = totalWithdrawn.add(pendingWithdrawal);

        EarningsSummaryResponse resp = new EarningsSummaryResponse();
        resp.setTotalEarned(totalEarned);
        resp.setTotalWithdrawn(totalWithdrawn);
        resp.setPendingWithdrawal(pendingWithdrawal);
        return resp;
    }

    private WithdrawalResponse toResponse(WithdrawalRecord record) {
        WithdrawalResponse resp = new WithdrawalResponse();
        resp.setId(record.getId());
        resp.setWorkerId(record.getWorkerId());
        resp.setAmount(record.getAmount());
        resp.setStatus(record.getStatus());
        resp.setRequestedAt(record.getRequestedAt());
        return resp;
    }
}
