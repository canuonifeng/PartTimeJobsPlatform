package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.SettlementBillVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SettlementService {

    Map<String, Object> listBills(Long companyId, String workerName, LocalDate dateFrom, LocalDate dateTo, int page, int pageSize);

    void payFromAttendanceRecords(List<Long> attendanceRecordIds, Long companyId);
}
