package com.parttime.platform.service.impl;

import com.parttime.platform.service.SettlementService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettlementServiceImpl implements SettlementService {

    @Override
    public List<Map<String, Object>> list(String status, String keyword) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", (long) i);
            item.put("settlementNo", "STL" + System.currentTimeMillis() + i);
            item.put("workerName", "工人" + i);
            item.put("enterpriseName", "企业" + (i % 5 + 1));
            item.put("jobTitle", "职位" + i);
            item.put("totalHours", new BigDecimal("8.5"));
            item.put("totalAmount", new BigDecimal("170.00"));
            item.put("serviceFee", new BigDecimal("8.50"));
            item.put("actualPay", new BigDecimal("161.50"));
            item.put("status", i % 3 == 0 ? "PAID" : i % 3 == 1 ? "PENDING" : "CANCELLED");
            item.put("createdAt", LocalDateTime.now().minusDays(i));
            list.add(item);
        }
        return list;
    }

    @Override
    public Map<String, Object> detail(Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("settlementNo", "STL" + System.currentTimeMillis());
        result.put("workerName", "测试工人");
        result.put("enterpriseName", "测试企业");
        result.put("jobTitle", "测试职位");
        result.put("totalHours", new BigDecimal("8.5"));
        result.put("basePay", new BigDecimal("170.00"));
        result.put("bonus", new BigDecimal("20.00"));
        result.put("deduction", new BigDecimal("5.00"));
        result.put("totalAmount", new BigDecimal("185.00"));
        result.put("serviceFee", new BigDecimal("9.25"));
        result.put("actualPay", new BigDecimal("175.75"));
        result.put("status", "PENDING");
        result.put("createdAt", LocalDateTime.now().minusHours(2));
        return result;
    }

    @Override
    public void confirm(Long id) {
    }

    @Override
    public void cancel(Long id, String reason) {
    }
}
