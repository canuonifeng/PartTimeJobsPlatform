package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.IdCmd;
import com.parttime.enterprise.pojo.cmd.ScheduleBatchCreateCmd;
import com.parttime.enterprise.pojo.cmd.ScheduleCopyCmd;
import com.parttime.enterprise.pojo.cmd.ScheduleExportCmd;
import com.parttime.enterprise.pojo.cmd.ScheduleManageUpdateCmd;
import com.parttime.enterprise.pojo.cmd.ScheduleShiftCmd;
import com.parttime.enterprise.pojo.cmd.CorrectionRejectCmd;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.pojo.vo.AttendanceReportVO;
import com.parttime.enterprise.pojo.vo.CorrectionVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.pojo.vo.ScheduleApplicantVO;
import com.parttime.enterprise.pojo.vo.ScheduleExportVO;
import com.parttime.enterprise.pojo.vo.ScheduleManagementVO;
import com.parttime.enterprise.pojo.vo.ScheduleShiftVO;
import com.parttime.enterprise.service.CorrectionService;
import com.parttime.enterprise.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/enterprise")
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;
    @Resource
    private CorrectionService correctionService;

    @Operation(summary = "分配班次", description = "为工人分配班次")
    @PostMapping("/schedule-shifts")
    public ApiResponse<ScheduleShiftVO> assignShift(@RequestBody ScheduleShiftCmd request) {
        return ApiResponse.success(scheduleService.assignShift(request));
    }

    @Operation(summary = "查询班次列表", description = "根据岗位、工人、日期等条件查询班次，支持分页")
    @GetMapping("/schedule-shifts")
    public ApiResponse<PageVO<ScheduleShiftVO>> getShifts(
            @Parameter(description = "企业ID") @RequestParam(required = false) Long companyId,
            @Parameter(description = "岗位ID") @RequestParam(required = false) Long jobId,
            @Parameter(description = "工人ID") @RequestParam(required = false) Long workerId,
            @Parameter(description = "班次日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate shiftDate,
            @Parameter(hidden = true) @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "班次状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        if (shiftDate == null && date != null) shiftDate = date;
        Long resolvedCompanyId = companyId != null ? companyId : SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(scheduleService.getShifts(resolvedCompanyId, jobId, workerId, shiftDate, status, page, pageSize));
    }

    @Operation(summary = "班次管理列表", description = "按可报名班次聚合报名、容量和考勤概览")
    @GetMapping("/schedules")
    public ApiResponse<PageVO<ScheduleManagementVO>> getManagedSchedules(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        return ApiResponse.success(scheduleService.getManagedSchedules(SecurityUtil.getCurrentCompanyId(), jobId, keyword, status, startDate, endDate, page, pageSize));
    }

    @Operation(summary = "班次报名人列表", description = "查看某个班次的报名人及考勤情况")
    @GetMapping("/schedules/{scheduleId}/applicants")
    public ApiResponse<PageVO<ScheduleApplicantVO>> getScheduleApplicants(
            @PathVariable Long scheduleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        return ApiResponse.success(scheduleService.getScheduleApplicants(scheduleId, status, page, pageSize));
    }

    @Operation(summary = "修改班次信息", description = "修改只影响后续报名和后续生成排班")
    @PostMapping("/schedules/update")
    public ApiResponse<ScheduleManagementVO> updateManagedSchedule(@RequestBody ScheduleManageUpdateCmd request) {
        return ApiResponse.success(scheduleService.updateManagedSchedule(request));
    }

    @Operation(summary = "复制班次", description = "复制已有班次到新日期或时间")
    @PostMapping("/schedules/copy")
    public ApiResponse<ScheduleManagementVO> copyManagedSchedule(@RequestBody ScheduleCopyCmd request) {
        return ApiResponse.success(scheduleService.copyManagedSchedule(request));
    }

    @Operation(summary = "批量创建班次", description = "按日期范围和周几批量创建班次")
    @PostMapping("/schedules/batch-create")
    public ApiResponse<List<ScheduleManagementVO>> batchCreateManagedSchedules(@RequestBody ScheduleBatchCreateCmd request) {
        return ApiResponse.success(scheduleService.batchCreateManagedSchedules(request));
    }

    @Operation(summary = "导出班次报名和考勤", description = "导出某个班次的报名人、考勤、补卡和结算状态")
    @GetMapping("/schedules/{scheduleId}/export")
    public ApiResponse<ScheduleExportVO> exportScheduleApplicants(
            @PathVariable Long scheduleId,
            @RequestParam(required = false) String status) {
        ScheduleExportCmd request = new ScheduleExportCmd();
        request.setScheduleId(scheduleId);
        request.setStatus(status);
        return ApiResponse.success(scheduleService.exportScheduleApplicants(request));
    }

    @Operation(summary = "更新班次", description = "更新指定的班次信息")
    @PostMapping("/schedule-shifts/update")
    public ApiResponse<ScheduleShiftVO> updateShift(@RequestBody ScheduleShiftCmd request) {
        return ApiResponse.success(scheduleService.updateShift(request.getId(), request));
    }

    @Operation(summary = "删除班次", description = "删除指定的班次")
    @PostMapping("/schedule-shifts/delete")
    public void removeShift(@RequestBody IdCmd cmd) {
        scheduleService.removeShift(cmd.getId());
    }

    @Operation(summary = "取消排班", description = "取消排班（保留记录，状态改为CANCELLED）")
    @PostMapping("/schedule-shifts/cancel")
    public void cancelShift(@RequestBody IdCmd cmd) {
        scheduleService.removeShift(cmd.getId());
    }

    @Operation(summary = "获取考勤报表", description = "根据条件获取考勤报表数据")
    @GetMapping("/attendance/report")
    public ApiResponse<List<AttendanceReportVO>> getAttendanceReport(
            @Parameter(description = "岗位ID") @RequestParam(required = false) Long jobId,
            @Parameter(description = "班次ID") @RequestParam(required = false) Long shiftId,
            @Parameter(description = "日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(scheduleService.getAttendanceReport(jobId, shiftId, date));
    }

    @Operation(summary = "补卡申请列表", description = "查看补卡申请列表，支持分页和筛选")
    @GetMapping("/schedules/corrections")
    public ApiResponse<PageVO<CorrectionVO>> listCorrections(
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "岗位关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始日期") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期") @RequestParam(required = false) String dateTo,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        return ApiResponse.success(correctionService.listCorrections(status, keyword, dateFrom, dateTo, page, pageSize));
    }

    @Operation(summary = "通过补卡申请", description = "通过补卡申请并生成/更新考勤记录")
    @PostMapping("/schedules/corrections/approve")
    public void approveCorrection(@RequestBody IdCmd cmd) {
        correctionService.approve(cmd.getId(), SecurityUtil.getCurrentUserId());
    }

    @Operation(summary = "拒绝补卡申请", description = "拒绝补卡申请")
    @PostMapping("/schedules/corrections/reject")
    public void rejectCorrection(@RequestBody CorrectionRejectCmd cmd) {
        correctionService.reject(cmd.getId(), SecurityUtil.getCurrentUserId(), cmd.getRejectReason());
    }
}
