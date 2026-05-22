package com.parttime.cservice.service;

import com.parttime.cservice.pojo.entity.SettlementBillEntity;

import java.util.List;
import java.util.Map;

public interface SettlementBillService {
    Map<String, Object> listSettledBills(Long workerId, int page, int pageSize);
    List<SettlementBillEntity> getSettledShifts(Long workerId);
}
