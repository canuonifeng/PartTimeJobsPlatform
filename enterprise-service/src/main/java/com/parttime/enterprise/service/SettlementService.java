package com.parttime.enterprise.service;

import java.util.List;

public interface SettlementService {

    void payFromAttendanceRecords(List<Long> attendanceRecordIds, Long companyId);

    void unsettle(Long attendanceRecordId, Long companyId);
}
