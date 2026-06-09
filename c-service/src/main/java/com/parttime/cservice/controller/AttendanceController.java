package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.cmd.CheckInCmd;
import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.pojo.vo.AttendanceVO;
import com.parttime.cservice.pojo.vo.WorkerShiftVO;
import com.parttime.cservice.service.AttendanceService;
import com.parttime.cservice.service.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    @Resource
    private AttendanceService attendanceService;

    @Resource
    private ReferralService referralService;

    private Long getCurrentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    @Operation(summary = "获取我的班次", description = "获取当前工人的班次列表，可按日期范围筛选")
    @GetMapping("/schedule-shifts/my")
    public ApiResponse<List<WorkerShiftVO>> getMyShifts(
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(workerId, startDate, endDate);
        return ApiResponse.success(shifts);
    }

    @Operation(summary = "签到", description = "工人进行上班签到打卡")
    @PostMapping("/attendance/check-in")
    public ApiResponse<AttendanceVO> checkIn(@RequestBody CheckInCmd request) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        try {
            AttendanceVO response = attendanceService.checkIn(workerId, request.getShiftId(), request.getLat(), request.getLng());
            return ApiResponse.success(response);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "签退", description = "工人进行下班签退打卡")
    @PostMapping("/attendance/check-out")
    public ApiResponse<AttendanceVO> checkOut(@RequestBody CheckInCmd request) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        try {
            AttendanceVO response = attendanceService.checkOut(workerId, request.getShiftId(), request.getLat(), request.getLng());
            referralService.checkAndGrantReward(workerId);
            return ApiResponse.success(response);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "获取我的考勤记录", description = "获取当前工人的考勤记录列表")
    @GetMapping("/attendance/my")
    public ApiResponse<List<AttendanceVO>> getMyAttendance() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        List<AttendanceVO> records = attendanceService.getMyAttendance(workerId);
        return ApiResponse.success(records);
    }
}
