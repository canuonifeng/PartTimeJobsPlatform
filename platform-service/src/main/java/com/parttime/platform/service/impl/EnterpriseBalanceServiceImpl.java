package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.EnterpriseBalanceMapper;
import com.parttime.platform.pojo.cmd.EnterpriseBalanceAdjustCmd;
import com.parttime.platform.pojo.entity.EnterpriseBalance;
import com.parttime.platform.service.EnterpriseBalanceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class EnterpriseBalanceServiceImpl implements EnterpriseBalanceService {

    @Resource
    private EnterpriseBalanceMapper enterpriseBalanceMapper;

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
    }
}
