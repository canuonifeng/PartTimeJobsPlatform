package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.SettlementBillMapper;
import com.parttime.cservice.pojo.entity.SettlementBillEntity;
import com.parttime.cservice.service.SettlementBillService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettlementBillServiceImpl implements SettlementBillService {

    @Resource
    private SettlementBillMapper settlementBillMapper;

    @Override
    public Map<String, Object> listSettledBills(Long workerId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<SettlementBillEntity> records = settlementBillMapper.findByWorkerId(workerId, offset, pageSize);
        long total = settlementBillMapper.countByWorkerId(workerId);
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        return result;
    }

    @Override
    public List<SettlementBillEntity> getSettledShifts(Long workerId) {
        return settlementBillMapper.findByWorkerId(workerId, 0, 1000);
    }
}
