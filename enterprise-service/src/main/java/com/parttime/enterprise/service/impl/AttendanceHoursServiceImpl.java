package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.pojo.vo.AttendanceHoursVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.AttendanceHoursService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceHoursServiceImpl implements AttendanceHoursService {

    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;

    @Override
    public PageVO<AttendanceHoursVO> list(Long companyId, String workerName, LocalDate dateFrom, LocalDate dateTo, String settlementStatus, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<AttendanceHoursVO> records = attendanceRecordMapper.findHours(companyId, workerName, dateFrom, dateTo, settlementStatus, offset, pageSize);
        long total = attendanceRecordMapper.countHours(companyId, workerName, dateFrom, dateTo, settlementStatus);
        return new PageVO<>(records, total);
    }

    @Override
    @Transactional
    public void update(Long id, BigDecimal totalHours, BigDecimal scheduledPay, BigDecimal payablePay) {
        AttendanceRecord record = attendanceRecordMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found: " + id));
        if ("PAID".equals(record.getSettlementStatus())) {
            throw new RuntimeException("Cannot edit a paid record");
        }
        if (totalHours != null) record.setTotalHours(totalHours);
        if (scheduledPay != null) record.setScheduledPay(scheduledPay);
        if (payablePay != null) record.setPayablePay(payablePay);
        attendanceRecordMapper.update(record);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        List<AttendanceRecord> records = attendanceRecordMapper.findByIds(ids);
        for (AttendanceRecord record : records) {
            if ("PAID".equals(record.getSettlementStatus())) {
                throw new RuntimeException("Cannot delete paid record: " + record.getId());
            }
        }
        attendanceRecordMapper.deleteByIds(ids);
    }
}
