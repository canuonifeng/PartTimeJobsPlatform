package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.ScheduleShiftCmd;
import com.parttime.enterprise.pojo.dto.CorrectionRejectCmd;
import com.parttime.enterprise.pojo.vo.AttendanceReportVO;
import com.parttime.enterprise.pojo.vo.CorrectionVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.pojo.vo.ScheduleShiftVO;
import com.parttime.enterprise.service.CorrectionService;
import com.parttime.enterprise.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;
    @Resource
    private CorrectionService correctionService;

    @Operation(summary = "分配班次", description = "为工人分配班次")
    @PostMapping("/schedule-shifts")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleShiftVO assignShift(@RequestBody ScheduleShiftCmd request) {
        return scheduleService.assignShift(request);
    }

    @Operation(summary = "查询班次列表", description = "根据岗位、工人、日期等条件查询班次，支持分页")
    @GetMapping("/schedule-shifts")
    public PageVO<ScheduleShiftVO> getShifts(
            @Parameter(description = "岗位ID") @RequestParam(required = false) Long jobId,
            @Parameter(description = "工人ID") @RequestParam(required = false) Long workerId,
            @Parameter(description = "班次日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate shiftDate,
            @Parameter(hidden = true) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        if (shiftDate == null && date != null) shiftDate = date;
        return scheduleService.getShifts(jobId, workerId, shiftDate, page, pageSize);
    }

    @Operation(summary = "更新班次", description = "更新指定的班次信息")
    @PutMapping("/schedule-shifts")
    public ScheduleShiftVO updateShift(@Parameter(description = "班次ID") @RequestParam Long id,
                                        @RequestBody ScheduleShiftCmd request) {
        return scheduleService.updateShift(id, request);
    }

    @Operation(summary = "删除班次", description = "删除指定的班次")
    @DeleteMapping("/schedule-shifts")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeShift(@Parameter(description = "班次ID") @RequestParam Long id) {
        scheduleService.removeShift(id);
    }

    @Operation(summary = "获取考勤报表", description = "根据条件获取考勤报表数据")
    @GetMapping("/attendance/report")
    public List<AttendanceReportVO> getAttendanceReport(
            @Parameter(description = "岗位ID") @RequestParam(required = false) Long jobId,
            @Parameter(description = "班次ID") @RequestParam(required = false) Long shiftId,
            @Parameter(description = "日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.getAttendanceReport(jobId, shiftId, date);
    }

    @Operation(summary = "补卡申请列表", description = "查看补卡申请列表，支持分页和筛选")
    @GetMapping("/schedules/corrections")
    public PageVO<CorrectionVO> listCorrections(
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "岗位关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始日期") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期") @RequestParam(required = false) String dateTo,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        return correctionService.listCorrections(status, keyword, dateFrom, dateTo, page, pageSize);
    }

    @Operation(summary = "通过补卡申请", description = "通过补卡申请并生成/更新考勤记录")
    @PutMapping("/schedules/corrections/{id}/approve")
    public void approveCorrection(@Parameter(description = "补卡申请ID") @PathVariable Long id) {
        correctionService.approve(id, SecurityUtil.getCurrentUserId());
    }

    @Operation(summary = "拒绝补卡申请", description = "拒绝补卡申请")
    @PutMapping("/schedules/corrections/{id}/reject")
    public void rejectCorrection(@Parameter(description = "补卡申请ID") @PathVariable Long id,
                                  @RequestBody CorrectionRejectCmd cmd) {
        correctionService.reject(id, SecurityUtil.getCurrentUserId(), cmd.getRejectReason());
    }
}
