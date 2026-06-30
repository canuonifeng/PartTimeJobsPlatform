package com.parttime.platform.service.impl;

import com.parttime.platform.service.TransactionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Override
    public List<Map<String, Object>> list(String type, String status, String keyword) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] types = {"ENTERPRISE_TOPUP", "WORKER_WITHDRAWAL", "SETTLEMENT", "SERVICE_FEE"};
        String[] typeNames = {"企业充值", "工人提现", "工资结算", "平台服务费"};
        String[] statuses = {"SUCCESS", "PENDING", "FAILED"};
        for (int i = 1; i <= 30; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", (long) i);
            item.put("transactionNo", "TXN" + System.currentTimeMillis() + i);
            item.put("type", types[i % 4]);
            item.put("typeName", typeNames[i % 4]);
            item.put("amount", new BigDecimal((i % 10 + 1) * 100));
            item.put("balanceAfter", new BigDecimal(5000 + (i % 100) * 100));
            item.put("status", statuses[i % 3]);
            item.put("remark", typeNames[i % 4] + "备注" + i);
            item.put("createdAt", LocalDateTime.now().minusHours(i));
            list.add(item);
        }
        return list;
    }

    @Override
    public Map<String, Object> overview() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalTopUp", new BigDecimal("100000.00"));
        result.put("totalWithdrawal", new BigDecimal("80000.00"));
        result.put("totalServiceFee", new BigDecimal("5000.00"));
        result.put("pendingCount", 5);
        return result;
    }
}
