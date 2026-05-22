package com.parttime.cservice.service;

import com.parttime.cservice.pojo.entity.SettlementBillEntity;
import com.parttime.cservice.pojo.vo.PageVO;

import java.util.List;

public interface SettlementBillService {
    PageVO<SettlementBillEntity> listSettledBills(Long workerId, int page, int pageSize);
    List<SettlementBillEntity> getSettledShifts(Long workerId);
}
