package com.parttime.cservice.service.impl;

import com.parttime.cservice.pojo.cmd.CheckInCmd;
import com.parttime.cservice.pojo.entity.AttendanceRecordEntity;
import com.parttime.cservice.pojo.entity.ShiftEntity;
import com.parttime.cservice.pojo.vo.AttendanceVO;
import com.parttime.cservice.pojo.vo.WorkerShiftVO;
import com.parttime.cservice.service.AttendanceService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final ConcurrentHashMap<Long, ShiftEntity> shifts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, AttendanceRecordEntity> attendanceRecords = new ConcurrentHashMap<>();
    private final AtomicLong shiftIdCounter = new AtomicLong(1);
    private final AtomicLong attendanceIdCounter = new AtomicLong(1);

    @Override
    public ShiftEntity addShift(Long jobId, String jobTitle, String jobLocation, Long workerId,
                                  LocalDate shiftDate, LocalTime startTime, LocalTime endTime,
                                  BigDecimal locationLat, BigDecimal locationLng, Integer locationRadius,
                                  String locationName) {
        ShiftEntity shift = new ShiftEntity(
                shiftIdCounter.getAndIncrement(), jobId, jobTitle, jobLocation, workerId,
                shiftDate, startTime, endTime,
                locationLat, locationLng, locationRadius, locationName, "SCHEDULED");
        shifts.put(shift.getId(), shift);
        return shift;
    }

    @Override
    public List<WorkerShiftVO> getMyShifts(Long workerId, LocalDate startDate, LocalDate endDate) {
        return shifts.values().stream()
                .filter(s -> s.getWorkerId().equals(workerId))
                .filter(s -> startDate == null || !s.getShiftDate().isBefore(startDate))
                .filter(s -> endDate == null || !s.getShiftDate().isAfter(endDate))
                .map(this::toWorkerShiftResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceVO checkIn(Long workerId, Long shiftId, BigDecimal lat, BigDecimal lng) {
        ShiftEntity shift = shifts.get(shiftId);
        if (shift == null) {
            throw new RuntimeException("Shift not found: " + shiftId);
        }
        if (!shift.getWorkerId().equals(workerId)) {
            throw new RuntimeException("Shift does not belong to this worker");
        }
        if (!"SCHEDULED".equals(shift.getStatus())) {
            throw new RuntimeException("Cannot check in: shift status is " + shift.getStatus());
        }
        if (attendanceRecords.values().stream().anyMatch(r -> r.getShiftId().equals(shiftId))) {
            throw new RuntimeException("Already checked in for this shift");
        }

        if (shift.getLocationLat() != null && shift.getLocationRadius() != null && lat != null) {
            double distance = haversine(
                    shift.getLocationLat().doubleValue(), shift.getLocationLng().doubleValue(),
                    lat.doubleValue(), lng.doubleValue());
            if (distance > shift.getLocationRadius()) {
                throw new RuntimeException("Location out of range: " + (int) distance + "m (max: " + shift.getLocationRadius() + "m)");
            }
        }

        shift.setStatus("CHECKED_IN");
        shifts.put(shiftId, shift);

        AttendanceRecordEntity record = new AttendanceRecordEntity(
                attendanceIdCounter.getAndIncrement(), shiftId, workerId,
                LocalDateTime.now(), lat, lng, null, null, null, null, "CHECKED_IN");
        attendanceRecords.put(record.getId(), record);

        return toAttendanceResponse(record);
    }

    @Override
    public AttendanceVO checkOut(Long workerId, Long shiftId, BigDecimal lat, BigDecimal lng) {
        ShiftEntity shift = shifts.get(shiftId);
        if (shift == null) {
            throw new RuntimeException("Shift not found: " + shiftId);
        }
        if (!shift.getWorkerId().equals(workerId)) {
            throw new RuntimeException("Shift does not belong to this worker");
        }

        AttendanceRecordEntity record = attendanceRecords.values().stream()
                .filter(r -> r.getShiftId().equals(shiftId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No check-in record found for this shift"));

        if (!"CHECKED_IN".equals(record.getStatus())) {
            throw new RuntimeException("Cannot check out: status is " + record.getStatus());
        }

        LocalDateTime checkOutTime = LocalDateTime.now();
        Duration duration = Duration.between(record.getCheckInTime(), checkOutTime);
        BigDecimal hours = BigDecimal.valueOf(duration.toMinutes() / 60.0)
                .setScale(2, RoundingMode.HALF_UP);

        record.setCheckOutTime(checkOutTime);
        record.setCheckOutLat(lat);
        record.setCheckOutLng(lng);
        record.setTotalHours(hours);
        record.setStatus("CHECKED_OUT");

        shift.setStatus("CHECKED_OUT");

        return toAttendanceResponse(record);
    }

    @Override
    public List<AttendanceVO> getMyAttendance(Long workerId) {
        return attendanceRecords.values().stream()
                .filter(r -> r.getWorkerId().equals(workerId))
                .map(this::toAttendanceResponse)
                .collect(Collectors.toList());
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private WorkerShiftVO toWorkerShiftResponse(ShiftEntity shift) {
        WorkerShiftVO resp = new WorkerShiftVO();
        resp.setShiftId(shift.getId());
        resp.setJobId(shift.getJobId());
        resp.setJobTitle(shift.getJobTitle());
        resp.setJobLocation(shift.getJobLocation());
        resp.setShiftDate(shift.getShiftDate());
        resp.setStartTime(shift.getStartTime());
        resp.setEndTime(shift.getEndTime());
        resp.setStatus(shift.getStatus());
        resp.setLocationLat(shift.getLocationLat());
        resp.setLocationLng(shift.getLocationLng());
        resp.setLocationRadius(shift.getLocationRadius());
        resp.setLocationName(shift.getLocationName());
        return resp;
    }

    private AttendanceVO toAttendanceResponse(AttendanceRecordEntity record) {
        AttendanceVO resp = new AttendanceVO();
        resp.setAttendanceId(record.getId());
        resp.setShiftId(record.getShiftId());
        resp.setCheckInTime(record.getCheckInTime());
        resp.setCheckOutTime(record.getCheckOutTime());
        resp.setTotalHours(record.getTotalHours());
        resp.setStatus(record.getStatus());
        return resp;
    }
}
