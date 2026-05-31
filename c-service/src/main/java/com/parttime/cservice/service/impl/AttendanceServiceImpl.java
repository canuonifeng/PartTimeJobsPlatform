package com.parttime.cservice.service.impl;

import com.parttime.cservice.enums.ShiftStatus;
import com.parttime.cservice.mapper.AttendanceCheckInMapper;
import com.parttime.cservice.mapper.AttendanceCorrectionMapper;
import com.parttime.cservice.mapper.AttendanceRecordMapper;
import com.parttime.cservice.mapper.ShiftMapper;
import com.parttime.cservice.pojo.entity.AttendanceCheckIn;
import com.parttime.cservice.pojo.entity.AttendanceCorrectionEntity;
import com.parttime.cservice.pojo.entity.AttendanceRecordEntity;
import com.parttime.cservice.pojo.entity.ShiftEntity;
import com.parttime.cservice.pojo.vo.AttendanceVO;
import com.parttime.cservice.pojo.vo.WorkerShiftVO;
import com.parttime.cservice.service.AttendanceService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Resource
    private ShiftMapper shiftMapper;
    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;
    @Resource
    private AttendanceCorrectionMapper correctionMapper;
    @Resource
    private AttendanceCheckInMapper attendanceCheckInMapper;

    @Override
    public ShiftEntity addShift(Long jobId, String jobTitle, String jobLocation, Long workerId,
                                  LocalDate shiftDate, LocalTime startTime, LocalTime endTime,
                                  BigDecimal locationLat, BigDecimal locationLng, Integer locationRadius,
                                  String locationName) {
        ShiftEntity shift = new ShiftEntity();
        shift.setJobId(jobId);
        shift.setJobTitle(jobTitle);
        shift.setJobLocation(jobLocation);
        shift.setWorkerId(workerId);
        shift.setShiftDate(shiftDate);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shift.setLocationLat(locationLat);
        shift.setLocationLng(locationLng);
        shift.setLocationRadius(locationRadius);
        shift.setLocationName(locationName);
        shift.setStatus(ShiftStatus.SCHEDULED.name());
        shift.setCreatedAt(LocalDateTime.now());
        shift.setUpdatedAt(LocalDateTime.now());
        shiftMapper.insert(shift);
        return shift;
    }

    @Override
    public List<WorkerShiftVO> getMyShifts(Long workerId, LocalDate startDate, LocalDate endDate) {
        return shiftMapper.findByWorkerIdAndDateRange(workerId, startDate, endDate).stream()
                .map(this::toWorkerShiftResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AttendanceVO checkIn(Long workerId, Long shiftId, BigDecimal lat, BigDecimal lng) {
        ShiftEntity shift = shiftMapper.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftId));

        if (!shift.getWorkerId().equals(workerId)) {
            throw new RuntimeException("Shift does not belong to this worker");
        }

        // 已上岗/已签退/缺勤/迟到/早退 都不能再签到
        if (!ShiftStatus.SCHEDULED.name().equals(shift.getStatus())) {
            throw new RuntimeException("Cannot check in: shift status is " + shift.getStatus());
        }

        if (shift.getLocationLat() != null && shift.getLocationRadius() != null && lat != null) {
            double distance = haversine(
                    shift.getLocationLat().doubleValue(), shift.getLocationLng().doubleValue(),
                    lat.doubleValue(), lng.doubleValue());
            if (distance > shift.getLocationRadius()) {
                throw new RuntimeException("Location out of range: " + (int) distance + "m (max: " + shift.getLocationRadius() + "m)");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledStart = LocalDateTime.of(shift.getShiftDate(), shift.getStartTime());
        long lateSeconds = now.isAfter(scheduledStart) ? Duration.between(scheduledStart, now).getSeconds() : 0;

        ShiftStatus newStatus = lateSeconds > 0 ? ShiftStatus.LATE : ShiftStatus.ON_DUTY;

        shift.setStatus(newStatus.name());
        shift.setUpdatedAt(now);
        shiftMapper.update(shift);

        AttendanceCheckIn checkIn = new AttendanceCheckIn();
        checkIn.setShiftId(shiftId);
        checkIn.setWorkerId(workerId);
        checkIn.setCheckInTime(now);
        checkIn.setCheckInLat(lat);
        checkIn.setCheckInLng(lng);
        checkIn.setLateSeconds((int) lateSeconds);
        checkIn.setEarlyLeaveSeconds(0);
        checkIn.setCreatedAt(now);
        checkIn.setUpdatedAt(now);
        attendanceCheckInMapper.insert(checkIn);

        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setShiftId(shiftId);
        record.setJobId(shift.getJobId());
        record.setCompanyId(shift.getCompanyId());
        record.setWorkerId(workerId);
        record.setCheckInTime(now);
        record.setCheckInLat(lat);
        record.setCheckInLng(lng);
        record.setStatus(newStatus.name());
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        attendanceRecordMapper.insert(record);

        return toAttendanceResponse(record);
    }

    @Override
    public AttendanceVO checkOut(Long workerId, Long shiftId, BigDecimal lat, BigDecimal lng) {
        ShiftEntity shift = shiftMapper.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftId));

        if (!shift.getWorkerId().equals(workerId)) {
            throw new RuntimeException("Shift does not belong to this worker");
        }

        // 只能从已上岗/迟到状态签退
        if (!ShiftStatus.ON_DUTY.name().equals(shift.getStatus()) && !ShiftStatus.LATE.name().equals(shift.getStatus())) {
            throw new RuntimeException("Cannot check out: shift status is " + shift.getStatus());
        }

        AttendanceRecordEntity record = attendanceRecordMapper.findByShiftId(shiftId)
                .orElseThrow(() -> new RuntimeException("No check-in record found for this shift"));

        if (shift.getLocationLat() != null && shift.getLocationRadius() != null && lat != null) {
            double distance = haversine(
                    shift.getLocationLat().doubleValue(), shift.getLocationLng().doubleValue(),
                    lat.doubleValue(), lng.doubleValue());
            if (distance > shift.getLocationRadius()) {
                throw new RuntimeException("Location out of range: " + (int) distance + "m (max: " + shift.getLocationRadius() + "m)");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledEnd = LocalDateTime.of(shift.getShiftDate(), shift.getEndTime());
        long earlySeconds = now.isBefore(scheduledEnd) ? Duration.between(now, scheduledEnd).getSeconds() : 0;

        ShiftStatus newStatus = earlySeconds > 0 ? ShiftStatus.EARLY_LEAVE : ShiftStatus.COMPLETED;

        // 追加签到记录（支持多次签退）
        AttendanceCheckIn checkIn = new AttendanceCheckIn();
        checkIn.setShiftId(shiftId);
        checkIn.setWorkerId(workerId);
        checkIn.setCheckInTime(now);
        checkIn.setCheckInLat(lat);
        checkIn.setCheckInLng(lng);
        checkIn.setLateSeconds(0);
        checkIn.setEarlyLeaveSeconds((int) earlySeconds);
        checkIn.setCreatedAt(now);
        checkIn.setUpdatedAt(now);
        attendanceCheckInMapper.insert(checkIn);

        Duration duration = Duration.between(record.getCheckInTime(), now);
        BigDecimal hours = BigDecimal.valueOf(duration.toMinutes() / 60.0)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal scheduledPay = BigDecimal.ZERO;
        String rateType = shift.getSalaryType() != null ? shift.getSalaryType() : "HOURLY";
        BigDecimal rateAmount = shift.getSalaryAmount();
        if (rateAmount != null && rateAmount.compareTo(BigDecimal.ZERO) > 0) {
            if ("HOURLY".equals(rateType)) {
                scheduledPay = hours.multiply(rateAmount).setScale(2, RoundingMode.HALF_UP);
            } else if ("DAILY".equals(rateType)) {
                scheduledPay = rateAmount.setScale(2, RoundingMode.HALF_UP);
            }
        }

        record.setCheckOutTime(now);
        record.setCheckOutLat(lat);
        record.setCheckOutLng(lng);
        record.setTotalHours(hours);
        record.setScheduledPay(scheduledPay);
        record.setPayablePay(scheduledPay);
        record.setSettlementStatus("UNPAID");
        record.setCalculatedAt(now);
        record.setStatus(newStatus.name());
        record.setUpdatedAt(now);
        attendanceRecordMapper.update(record);

        shift.setStatus(newStatus.name());
        shift.setUpdatedAt(now);
        shiftMapper.update(shift);

        return toAttendanceResponse(record);
    }

    @Override
    public List<AttendanceVO> getMyAttendance(Long workerId) {
        return attendanceRecordMapper.findByWorkerId(workerId).stream()
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
        resp.setId(shift.getId());
        resp.setJobId(shift.getJobId());
        resp.setJobTitle(shift.getJobTitle());
        resp.setLocation(shift.getJobLocation());
        resp.setDate(shift.getShiftDate());
        resp.setStartTime(shift.getStartTime() != null ? shift.getStartTime().toString() : null);
        resp.setEndTime(shift.getEndTime() != null ? shift.getEndTime().toString() : null);
        resp.setStatus(shift.getStatus());
        resp.setLocationLat(shift.getLocationLat());
        resp.setLocationLng(shift.getLocationLng());
        resp.setLocationRadius(shift.getLocationRadius());
        resp.setLocationName(shift.getLocationName());
        correctionMapper.findByShiftId(shift.getId()).ifPresent(c ->
                resp.setCorrectionStatus(c.getStatus()));
        return resp;
    }

    private AttendanceVO toAttendanceResponse(AttendanceRecordEntity record) {
        AttendanceVO resp = new AttendanceVO();
        resp.setAttendanceId(record.getId());
        resp.setShiftId(record.getShiftId());
        resp.setCheckInTime(record.getCheckInTime());
        resp.setCheckOutTime(record.getCheckOutTime());
        resp.setTotalHours(record.getTotalHours());
        resp.setScheduledPay(record.getScheduledPay());
        resp.setCalculatedAt(record.getCalculatedAt());
        resp.setStatus(record.getStatus());
        return resp;
    }
}
