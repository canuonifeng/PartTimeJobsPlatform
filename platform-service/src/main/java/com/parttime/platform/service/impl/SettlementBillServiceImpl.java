package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.SettlementBillMapper;
import com.parttime.platform.pojo.entity.SettlementBill;
import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.SettlementBillVO;
import com.parttime.platform.service.SettlementBillService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SettlementBillServiceImpl implements SettlementBillService {

    @Resource
    private SettlementBillMapper settlementBillMapper;

    @Override
    public PageVO<SettlementBillVO> listBills(Long companyId, String workerName, LocalDate dateFrom, LocalDate dateTo, String status, int page, int pageSize) {
        int offset = Math.max(page - 1, 0) * pageSize;
        List<SettlementBillVO> records = settlementBillMapper.findPage(companyId, workerName, dateFrom, dateTo, status, offset, pageSize)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        long total = settlementBillMapper.countPage(companyId, workerName, dateFrom, dateTo, status);
        return new PageVO<>(records, total);
    }

    private SettlementBillVO toVO(SettlementBill bill) {
        SettlementBillVO vo = new SettlementBillVO();
        BeanUtils.copyProperties(bill, vo);
        return vo;
    }
}
