package com.parttime.cservice.service;

import com.parttime.cservice.pojo.entity.AttendanceRecordEntity;
import com.parttime.cservice.pojo.entity.ShiftEntity;
import com.parttime.cservice.pojo.vo.AttendanceVO;
import com.parttime.cservice.pojo.vo.WorkerShiftVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AttendanceService {
    ShiftEntity addShift(Long jobId, Long workerId,
                          LocalDate shiftDate, LocalTime startTime, LocalTime endTime,
                          BigDecimal locationLat, BigDecimal locationLng, Integer locationRadius,
                          String locationName);
    List<WorkerShiftVO> getMyShifts(Long workerId, LocalDate startDate, LocalDate endDate, Integer page, Integer pageSize);
    Long countMyShifts(Long workerId, LocalDate startDate, LocalDate endDate);
    AttendanceVO checkIn(Long workerId, Long shiftId, BigDecimal lat, BigDecimal lng);
    AttendanceVO checkOut(Long workerId, Long shiftId, BigDecimal lat, BigDecimal lng);
    List<AttendanceVO> getMyAttendance(Long workerId);
}
