package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.BalanceTransactionMapper;
import com.parttime.cservice.mapper.WithdrawalRecordMapper;
import com.parttime.cservice.mapper.WorkerBalanceMapper;
import com.parttime.cservice.pojo.entity.BalanceTransaction;
import com.parttime.cservice.pojo.entity.WithdrawalRecord;
import com.parttime.cservice.pojo.entity.WorkerBalance;
import com.parttime.cservice.pojo.vo.EarningsSummaryVO;
import com.parttime.cservice.pojo.vo.TransactionVO;
import com.parttime.cservice.pojo.vo.WithdrawalVO;
import com.parttime.cservice.service.WithdrawalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class WithdrawalServiceImpl implements WithdrawalService {

    @Resource
    private WithdrawalRecordMapper withdrawalRecordMapper;

    @Resource
    private WorkerBalanceMapper workerBalanceMapper;

    @Resource
    private BalanceTransactionMapper balanceTransactionMapper;

    private static final Random RANDOM = new Random();

    @Override
    @Transactional
    public WithdrawalVO requestWithdrawal(Long workerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid withdrawal amount");
        }

        WorkerBalance wb = workerBalanceMapper.findByWorkerId(workerId);
        if (wb == null || wb.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        WithdrawalRecord record = new WithdrawalRecord();
        record.setWorkerId(workerId);
        record.setAmount(amount);
        record.setStatus("PROCESSING");
        record.setRequestedAt(LocalDateTime.now());
        record.setProcessedAt(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        withdrawalRecordMapper.insert(record);

        String thirdPartySerial = "WTHD" + System.currentTimeMillis() + RANDOM.nextInt(1000);

        withdrawalRecordMapper.updateCompletion(record.getId(), "COMPLETED",
                thirdPartySerial, "SIMULATED_PAY", LocalDateTime.now());

        workerBalanceMapper.upsert(workerId,
                wb.getBalance().subtract(amount),
                wb.getTotalEarned(),
                wb.getTotalWithdrawn().add(amount));

        BalanceTransaction bt = new BalanceTransaction();
        bt.setWorkerId(workerId);
        bt.setAmount(amount.negate());
        bt.setType("WITHDRAWAL");
        bt.setRelatedWithdrawalId(record.getId());
        bt.setDescription("提现支出: " + amount);
        balanceTransactionMapper.insert(bt);

        return toResponse(record);
    }

    @Override
    public List<WithdrawalVO> getWithdrawalHistory(Long workerId) {
        return withdrawalRecordMapper.findByWorkerId(workerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EarningsSummaryVO getEarningsSummary(Long workerId) {
        WorkerBalance wb = workerBalanceMapper.findByWorkerId(workerId);

        EarningsSummaryVO resp = new EarningsSummaryVO();
        if (wb == null) {
            resp.setTotalEarned(BigDecimal.ZERO);
            resp.setTotalWithdrawn(BigDecimal.ZERO);
            resp.setPendingWithdrawal(BigDecimal.ZERO);
        } else {
            resp.setTotalEarned(wb.getTotalEarned());
            resp.setTotalWithdrawn(wb.getTotalWithdrawn());
            resp.setPendingWithdrawal(wb.getBalance());
        }
        return resp;
    }

    @Override
    public List<TransactionVO> getTransactions(Long workerId) {
        return balanceTransactionMapper.findByWorkerId(workerId).stream()
                .map(t -> {
                    TransactionVO vo = new TransactionVO();
                    vo.setId(t.getId());
                    vo.setAmount(t.getAmount());
                    vo.setType(t.getType());
                    vo.setDescription(t.getDescription());
                    vo.setCreatedAt(t.getCreatedAt());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private WithdrawalVO toResponse(WithdrawalRecord record) {
        WithdrawalVO resp = new WithdrawalVO();
        resp.setId(record.getId());
        resp.setWorkerId(record.getWorkerId());
        resp.setAmount(record.getAmount());
        resp.setStatus(record.getStatus());
        resp.setRequestedAt(record.getRequestedAt());
        resp.setThirdPartySerialNo(record.getThirdPartySerialNo());
        resp.setThirdPartyPlatform(record.getThirdPartyPlatform());
        resp.setCompletedAt(record.getCompletedAt());
        return resp;
    }
}
