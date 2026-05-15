package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.cmd.CheckInCmd;
import com.parttime.cservice.pojo.vo.AttendanceVO;
import com.parttime.cservice.pojo.vo.WorkerShiftVO;
import com.parttime.cservice.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    private Long getCurrentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    @GetMapping("/schedule-shifts/my")
    public ResponseEntity<?> getMyShifts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(workerId, startDate, endDate);
        return ResponseEntity.ok(shifts);
    }

    @PostMapping("/attendance/check-in")
    public ResponseEntity<?> checkIn(@RequestBody CheckInCmd request) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            AttendanceVO response = attendanceService.checkIn(workerId, request.getShiftId(), request.getLat(), request.getLng());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/attendance/check-out")
    public ResponseEntity<?> checkOut(@RequestBody CheckInCmd request) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            AttendanceVO response = attendanceService.checkOut(workerId, request.getShiftId(), request.getLat(), request.getLng());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/attendance/my")
    public ResponseEntity<?> getMyAttendance() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<AttendanceVO> records = attendanceService.getMyAttendance(workerId);
        return ResponseEntity.ok(records);
    }
}
