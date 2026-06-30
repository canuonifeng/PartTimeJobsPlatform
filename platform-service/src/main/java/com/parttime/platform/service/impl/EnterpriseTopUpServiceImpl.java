package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.EnterpriseBalanceMapper;
import com.parttime.platform.mapper.EnterpriseTopUpMapper;
import com.parttime.platform.pojo.entity.EnterpriseTopUp;
import com.parttime.platform.service.EnterpriseTopUpService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnterpriseTopUpServiceImpl implements EnterpriseTopUpService {

    @Resource
    private EnterpriseTopUpMapper topUpMapper;

    @Resource
    private EnterpriseBalanceMapper balanceMapper;

    @Override
    public List<EnterpriseTopUp> list(String status, String keyword) {
        return topUpMapper.findByFilters(status, keyword);
    }

    @Override
    public EnterpriseTopUp detail(Long id) {
        return topUpMapper.findById(id)
                .orElseThrow(() -> new BusinessException("充值记录不存在: " + id));
    }

    @Override
    @Transactional
    public void approve(Long id, String auditor, String remark) {
        EnterpriseTopUp record = topUpMapper.findById(id)
                .orElseThrow(() -> new BusinessException("充值记录不存在: " + id));
        topUpMapper.updateStatus(id, "APPROVED", auditor, remark);
        balanceMapper.addBalance(record.getEnterpriseId(), record.getAmount());
    }

    @Override
    public void reject(Long id, String auditor, String remark) {
        topUpMapper.updateStatus(id, "REJECTED", auditor, remark);
    }
}
