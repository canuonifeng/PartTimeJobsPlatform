package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.EnterpriseBalanceMapper;
import com.parttime.platform.mapper.EnterpriseBalanceTransactionMapper;
import com.parttime.platform.pojo.cmd.EnterpriseBalanceAdjustCmd;
import com.parttime.platform.pojo.entity.EnterpriseBalance;
import com.parttime.platform.pojo.entity.EnterpriseBalanceTransaction;
import com.parttime.platform.service.EnterpriseBalanceService;
import jakarta.annotation.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class EnterpriseBalanceServiceImpl implements EnterpriseBalanceService {

    @Resource
    private EnterpriseBalanceMapper enterpriseBalanceMapper;

    @Resource
    private EnterpriseBalanceTransactionMapper transactionMapper;

    @Override
    @Transactional
    public void adjust(EnterpriseBalanceAdjustCmd cmd) {
        EnterpriseBalance eb = enterpriseBalanceMapper.findByCompanyId(cmd.getCompanyId());
        BigDecimal balance = (eb == null ? BigDecimal.ZERO : eb.getBalance());
        BigDecimal totalTopUp = (eb == null ? BigDecimal.ZERO : eb.getTotalTopUp());
        BigDecimal totalSpent = (eb == null ? BigDecimal.ZERO : eb.getTotalSpent());

        BigDecimal newBalance = balance.add(cmd.getAmount());
        BigDecimal newTotalTopUp = totalTopUp.add(cmd.getAmount());
        enterpriseBalanceMapper.upsert(cmd.getCompanyId(), newBalance, newTotalTopUp, totalSpent);

        String operatorName = SecurityContextHolder.getContext().getAuthentication().getName();

        EnterpriseBalanceTransaction txn = new EnterpriseBalanceTransaction();
        txn.setCompanyId(cmd.getCompanyId());
        txn.setAmount(cmd.getAmount());
        txn.setType("TOP_UP");
        txn.setDescription("运营后台充值: +" + cmd.getAmount() + "元, 操作人: " + operatorName);
        txn.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(txn);
    }
}
