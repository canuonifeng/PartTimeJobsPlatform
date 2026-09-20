package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.EnterpriseBalanceMapper;
import com.parttime.platform.mapper.EnterpriseBalanceTransactionMapper;
import com.parttime.platform.mapper.EnterpriseTopUpMapper;
import com.parttime.platform.pojo.entity.EnterpriseBalance;
import com.parttime.platform.pojo.entity.EnterpriseBalanceTransaction;
import com.parttime.platform.pojo.entity.EnterpriseTopUp;
import com.parttime.platform.service.EnterpriseTopUpService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EnterpriseTopUpServiceImpl implements EnterpriseTopUpService {

    @Resource
    private EnterpriseTopUpMapper enterpriseTopUpMapper;

    @Resource
    private EnterpriseBalanceMapper enterpriseBalanceMapper;

    @Resource
    private EnterpriseBalanceTransactionMapper transactionMapper;

    @Override
    public List<EnterpriseTopUp> list(String status, String keyword) {
        return enterpriseTopUpMapper.findByFilters(status, keyword);
    }

    @Override
    public EnterpriseTopUp detail(Long id) {
        return enterpriseTopUpMapper.findById(id)
                .orElseThrow(() -> new BusinessException("充值记录不存在: " + id));
    }

    @Override
    @Transactional
    public void approve(Long id, String auditor, String remark) {
        EnterpriseTopUp record = enterpriseTopUpMapper.findById(id)
                .orElseThrow(() -> new BusinessException("充值记录不存在: " + id));
        String status = record.getStatus();
        if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
            throw new BusinessException("该充值记录已审核，不可重复操作");
        }

        Long companyId = record.getCompanyId();
        BigDecimal amount = record.getAmount();

        EnterpriseBalance eb = enterpriseBalanceMapper.findByCompanyId(companyId);
        BigDecimal balance = (eb == null || eb.getBalance() == null) ? BigDecimal.ZERO : eb.getBalance();
        BigDecimal totalTopUp = (eb == null || eb.getTotalTopUp() == null) ? BigDecimal.ZERO : eb.getTotalTopUp();
        BigDecimal totalSpent = (eb == null || eb.getTotalSpent() == null) ? BigDecimal.ZERO : eb.getTotalSpent();

        BigDecimal newBalance = balance.add(amount);
        BigDecimal newTotalTopUp = totalTopUp.add(amount);
        enterpriseBalanceMapper.upsert(companyId, newBalance, newTotalTopUp, totalSpent);

        EnterpriseBalanceTransaction txn = new EnterpriseBalanceTransaction();
        txn.setCompanyId(companyId);
        txn.setAmount(amount);
        txn.setType("TOP_UP");
        txn.setRelatedTopUpId(id);
        txn.setDescription("企业充值审核入账: +" + amount + "元, 审核人: " + auditor);
        txn.setCreatedAt(java.time.LocalDateTime.now());
        transactionMapper.insert(txn);

        enterpriseTopUpMapper.updateStatus(id, "APPROVED", auditor, remark);
    }

    @Override
    @Transactional
    public void reject(Long id, String auditor, String remark) {
        EnterpriseTopUp record = enterpriseTopUpMapper.findById(id)
                .orElseThrow(() -> new BusinessException("充值记录不存在: " + id));
        String status = record.getStatus();
        if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
            throw new BusinessException("该充值记录已审核，不可重复操作");
        }
        enterpriseTopUpMapper.updateStatus(id, "REJECTED", auditor, remark);
    }
}
