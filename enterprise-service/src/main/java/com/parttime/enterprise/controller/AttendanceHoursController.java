package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.pojo.vo.AttendanceHoursVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance/hours")
public class AttendanceHoursController {

    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;

    @Operation(summary = "查询考勤工时列表", description = "分页查询考勤工时数据")
    @GetMapping
    public Map<String, Object> list(
            @Parameter(description = "工人姓名") @RequestParam(required = false) String workerName,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = "是否已发放") @RequestParam(required = false) Boolean isPaid,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        int offset = (page - 1) * pageSize;
        List<AttendanceHoursVO> records = attendanceRecordMapper.findHours(companyId, workerName, dateFrom, dateTo, isPaid, offset, pageSize);
        long total = attendanceRecordMapper.countHours(companyId, workerName, dateFrom, dateTo, isPaid);
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        return result;
    }

    @Operation(summary = "编辑考勤工时", description = "编辑考勤工时的工时数和应付薪资")
    @PutMapping("/{id}")
    @Transactional
    public void update(@Parameter(description = "考勤记录ID") @PathVariable Long id,
                       @RequestBody AttendanceHoursUpdateCmd cmd) {
        AttendanceRecord record = attendanceRecordMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found: " + id));
        if (Boolean.TRUE.equals(record.getIsPaid())) {
            throw new RuntimeException("Cannot edit a paid record");
        }
        if (cmd.getTotalHours() != null) record.setTotalHours(cmd.getTotalHours());
        if (cmd.getScheduledPay() != null) record.setScheduledPay(cmd.getScheduledPay());
        if (cmd.getPayablePay() != null) record.setPayablePay(cmd.getPayablePay());
        attendanceRecordMapper.update(record);
    }

    @Operation(summary = "批量发放薪资", description = "将指定考勤记录标记为已发放，实付金额默认等于应付金额")
    @PutMapping("/pay")
    @Transactional
    public void batchPay(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            AttendanceRecord record = attendanceRecordMapper.findById(id)
                    .orElseThrow(() -> new RuntimeException("Attendance record not found: " + id));
            if (Boolean.TRUE.equals(record.getIsPaid())) continue;
            record.setIsPaid(true);
            if (record.getPayablePay() == null) record.setPayablePay(record.getScheduledPay());
            attendanceRecordMapper.update(record);
        }
    }

    @Operation(summary = "批量删除", description = "删除指定的考勤记录，已发放的记录不可删除")
    @DeleteMapping
    @Transactional
    public void batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            AttendanceRecord record = attendanceRecordMapper.findById(id)
                    .orElseThrow(() -> new RuntimeException("Attendance record not found: " + id));
            if (Boolean.TRUE.equals(record.getIsPaid())) {
                throw new RuntimeException("Cannot delete paid record: " + id);
            }
        }
        attendanceRecordMapper.deleteByIds(ids);
    }

    @Data
    public static class AttendanceHoursUpdateCmd {
        private BigDecimal totalHours;
        private BigDecimal scheduledPay;
        private BigDecimal payablePay;
        private Boolean isPaid;
    }
}
