package com.parttime.platform.service.impl;

import com.parttime.platform.pojo.entity.EnterpriseTopUp;
import com.parttime.platform.service.EnterpriseTopUpService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EnterpriseTopUpServiceImpl implements EnterpriseTopUpService {

    @Override
    public List<EnterpriseTopUp> list(String status, String keyword) {
        List<EnterpriseTopUp> list = new ArrayList<>();
        String[] statuses = {"PENDING", "APPROVED", "REJECTED"};
        for (int i = 1; i <= 10; i++) {
            EnterpriseTopUp item = new EnterpriseTopUp();
            item.setId((long) i);
            item.setEnterpriseId((long) (i % 5 + 1));
            item.setEnterpriseName("企业" + (i % 5 + 1));
            item.setAmount(new BigDecimal((i % 10 + 1) * 1000));
            item.setStatus(statuses[i % 3]);
            item.setAuditor("操作员" + (i % 3 + 1));
            item.setCreatedAt(LocalDateTime.now().minusDays(i));
            list.add(item);
        }
        return list;
    }

    @Override
    public EnterpriseTopUp detail(Long id) {
        EnterpriseTopUp item = new EnterpriseTopUp();
        item.setId(id);
        item.setEnterpriseId(1L);
        item.setEnterpriseName("测试企业");
        item.setAmount(new BigDecimal("5000.00"));
        item.setStatus("PENDING");
        item.setPaymentMethod("BANK_TRANSFER");
        item.setAuditRemark("测试充值");
        item.setCreatedAt(LocalDateTime.now().minusHours(2));
        return item;
    }

    @Override
    public void approve(Long id, String auditor, String remark) {
    }

    @Override
    public void reject(Long id, String auditor, String remark) {
    }
}
