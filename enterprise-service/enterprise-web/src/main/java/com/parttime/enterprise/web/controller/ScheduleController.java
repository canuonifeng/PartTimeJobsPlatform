package com.parttime.enterprise.web.controller;

import com.parttime.enterprise.api.dto.*;
import com.parttime.enterprise.core.service.ScheduleService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping("/schedule-templates")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleTemplateResponse createTemplate(@RequestBody ScheduleTemplateRequest request) {
        return scheduleService.createTemplate(request);
    }

    @GetMapping("/schedule-templates")
    public List<ScheduleTemplateResponse> getTemplates(@RequestParam Long companyId) {
        return scheduleService.getTemplatesByCompany(companyId);
    }

    @GetMapping("/schedule-templates/{id}")
    public ScheduleTemplateResponse getTemplate(@PathVariable Long id) {
        return scheduleService.getTemplateById(id);
    }

    @PutMapping("/schedule-templates/{id}")
    public ScheduleTemplateResponse updateTemplate(@PathVariable Long id, @RequestBody ScheduleTemplateRequest request) {
        return scheduleService.updateTemplate(id, request);
    }

    @DeleteMapping("/schedule-templates/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTemplate(@PathVariable Long id) {
        scheduleService.deleteTemplate(id);
    }

    @PostMapping("/schedule-shifts")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleShiftResponse assignShift(@RequestBody ScheduleShiftRequest request) {
        return scheduleService.assignShift(request);
    }

    @GetMapping("/schedule-shifts")
    public List<ScheduleShiftResponse> getShifts(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate shiftDate) {
        return scheduleService.getShifts(jobId, workerId, shiftDate);
    }

    @DeleteMapping("/schedule-shifts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeShift(@PathVariable Long id) {
        scheduleService.removeShift(id);
    }

    @GetMapping("/attendance/report")
    public List<AttendanceReportResponse> getAttendanceReport(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) Long shiftId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.getAttendanceReport(jobId, shiftId, date);
    }
}
