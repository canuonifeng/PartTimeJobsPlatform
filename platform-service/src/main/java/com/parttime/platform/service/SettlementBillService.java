package com.parttime.platform.service;

import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.SettlementBillVO;

import java.time.LocalDate;

public interface SettlementBillService {
    PageVO<SettlementBillVO> listBills(Long companyId, String workerName, LocalDate dateFrom, LocalDate dateTo, String status, int page, int pageSize);
}
