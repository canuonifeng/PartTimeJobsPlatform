package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.pojo.vo.SettlementBillVO;

import java.time.LocalDate;
import java.util.List;

public interface SettlementService {

    PageVO<SettlementBillVO> listBills(Long companyId, String workerName, LocalDate dateFrom, LocalDate dateTo, int page, int pageSize);

    void payFromAttendanceRecords(List<Long> attendanceRecordIds, Long companyId);

    void unsettle(Long attendanceRecordId, Long companyId);
}
