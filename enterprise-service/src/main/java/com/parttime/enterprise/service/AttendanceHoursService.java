package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.AttendanceHoursVO;
import com.parttime.enterprise.pojo.vo.PageVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceHoursService {
    PageVO<AttendanceHoursVO> list(Long companyId, String workerName, LocalDate dateFrom, LocalDate dateTo, String settlementStatus, Integer page, Integer pageSize);
    void update(Long id, BigDecimal totalHours, BigDecimal scheduledPay, BigDecimal payablePay, String salaryType, BigDecimal salaryAmount);
    void batchDelete(List<Long> ids);
}
