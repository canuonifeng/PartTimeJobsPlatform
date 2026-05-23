package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.SettlementBillMapper;
import com.parttime.cservice.pojo.entity.SettlementBillEntity;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.service.SettlementBillService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettlementBillServiceImpl implements SettlementBillService {

    @Resource
    private SettlementBillMapper settlementBillMapper;

    @Override
    public PageVO<SettlementBillEntity> listSettledBills(Long workerId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<SettlementBillEntity> records = settlementBillMapper.findByWorkerId(workerId, offset, pageSize);
        long total = settlementBillMapper.countByWorkerId(workerId);
        return new PageVO<>(records, total);
    }

    @Override
    public List<SettlementBillEntity> getSettledShifts(Long workerId) {
        return settlementBillMapper.findByWorkerId(workerId, 0, 1000);
    }
}
