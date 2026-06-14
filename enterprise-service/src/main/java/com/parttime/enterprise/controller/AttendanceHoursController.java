package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.AttendanceHoursUpdateCmd;
import com.parttime.enterprise.pojo.cmd.IdsCmd;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.pojo.vo.AttendanceHoursVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.AttendanceHoursService;
import com.parttime.enterprise.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/enterprise/attendance/hours")
public class AttendanceHoursController {

    @Resource
    private AttendanceHoursService attendanceHoursService;

    @Resource
    private SettlementService settlementService;

    @Operation(summary = "查询考勤工时列表", description = "分页查询考勤工时数据")
    @GetMapping
    public ApiResponse<PageVO<AttendanceHoursVO>> list(
            @Parameter(description = "工人姓名") @RequestParam(required = false) String workerName,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = "结算状态: UNPAID/PAYING/PAID") @RequestParam(required = false) String settlementStatus,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(attendanceHoursService.list(companyId, workerName, dateFrom, dateTo, settlementStatus, page, pageSize));
    }

    @Operation(summary = "编辑考勤工时", description = "编辑考勤工时的工时数和应付薪资")
    @PostMapping("/update")
    public void update(@RequestBody AttendanceHoursUpdateCmd cmd) {
        attendanceHoursService.update(cmd.getId(), cmd.getTotalHours(), cmd.getScheduledPay(), cmd.getPayablePay());
    }

    @Operation(summary = "批量结算", description = "结算考勤记录，调用第三方支付并生成结算账单")
    @PostMapping("/pay")
    public void batchPay(@RequestBody IdsCmd cmd) {
        if (cmd == null || cmd.getIds() == null || cmd.getIds().isEmpty()) return;
        Long companyId = SecurityUtil.getCurrentCompanyId();
        settlementService.payFromAttendanceRecords(cmd.getIds(), companyId);
    }

    @Operation(summary = "批量删除", description = "删除指定的考勤记录，已发放的记录不可删除")
    @PostMapping("/delete")
    public void batchDelete(@RequestBody IdsCmd cmd) {
        attendanceHoursService.batchDelete(cmd.getIds());
    }
}
