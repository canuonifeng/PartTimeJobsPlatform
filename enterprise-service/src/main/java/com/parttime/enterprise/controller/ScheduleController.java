package com.parttime.enterprise.controller;

import com.parttime.enterprise.pojo.cmd.ScheduleShiftCmd;
import com.parttime.enterprise.pojo.cmd.ScheduleTemplateCmd;
import com.parttime.enterprise.pojo.vo.AttendanceReportVO;
import com.parttime.enterprise.pojo.vo.ScheduleShiftVO;
import com.parttime.enterprise.pojo.vo.ScheduleTemplateVO;
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

    @Operation(summary = "创建排班模板", description = "创建新的排班模板，包含多个时段")
    @PostMapping("/schedule-templates")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleTemplateVO createTemplate(@RequestBody ScheduleTemplateCmd request) {
        return scheduleService.createTemplate(request);
    }

    @Operation(summary = "获取企业排班模板列表", description = "根据企业ID获取所有排班模板")
    @GetMapping("/schedule-templates")
    public List<ScheduleTemplateVO> getTemplates(@Parameter(description = "企业ID") @RequestParam Long companyId) {
        return scheduleService.getTemplatesByCompany(companyId);
    }

    @Operation(summary = "获取排班模板详情", description = "根据ID获取排班模板详情")
    @GetMapping(value = "/schedule-templates", params = "id")
    public ScheduleTemplateVO getTemplate(@Parameter(description = "模板ID") @RequestParam Long id) {
        return scheduleService.getTemplateById(id);
    }

    @Operation(summary = "更新排班模板", description = "更新排班模板信息")
    @PutMapping("/schedule-templates")
    public ScheduleTemplateVO updateTemplate(@Parameter(description = "模板ID") @RequestParam Long id, @RequestBody ScheduleTemplateCmd request) {
        return scheduleService.updateTemplate(id, request);
    }

    @Operation(summary = "删除排班模板", description = "删除指定的排班模板")
    @DeleteMapping("/schedule-templates")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTemplate(@Parameter(description = "模板ID") @RequestParam Long id) {
        scheduleService.deleteTemplate(id);
    }

    @Operation(summary = "分配班次", description = "为工人分配班次")
    @PostMapping("/schedule-shifts")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleShiftVO assignShift(@RequestBody ScheduleShiftCmd request) {
        return scheduleService.assignShift(request);
    }

    @Operation(summary = "查询班次列表", description = "根据岗位、工人、日期等条件查询班次")
    @GetMapping("/schedule-shifts")
    public List<ScheduleShiftVO> getShifts(
            @Parameter(description = "岗位ID") @RequestParam(required = false) Long jobId,
            @Parameter(description = "工人ID") @RequestParam(required = false) Long workerId,
            @Parameter(description = "班次日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate shiftDate) {
        return scheduleService.getShifts(jobId, workerId, shiftDate);
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
}
