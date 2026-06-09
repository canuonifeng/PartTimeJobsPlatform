package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.AttendanceCorrectionMapper;
import com.parttime.cservice.mapper.AttendanceRecordMapper;
import com.parttime.cservice.mapper.BalanceTransactionMapper;
import com.parttime.cservice.mapper.ShiftMapper;
import com.parttime.cservice.pojo.entity.AttendanceCorrectionEntity;
import com.parttime.cservice.pojo.entity.AttendanceRecordEntity;
import com.parttime.cservice.pojo.entity.ShiftEntity;
import com.parttime.cservice.pojo.vo.HomeSchedulesVO;
import com.parttime.cservice.pojo.vo.HomeStatsVO;
import com.parttime.cservice.pojo.vo.WorkerShiftVO;
import com.parttime.cservice.service.HomeService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HomeServiceImpl implements HomeService {

    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;
    @Resource
    private AttendanceCorrectionMapper correctionMapper;
    @Resource
    private BalanceTransactionMapper balanceTransactionMapper;
    @Resource
    private ShiftMapper shiftMapper;
    @Resource
    private WorkerShiftVOConverter workerShiftVOConverter;

    @Override
    public HomeStatsVO getStats(Long workerId) {
        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = today.withDayOfMonth(1).plusMonths(1).atStartOfDay();
        BigDecimal monthHours = attendanceRecordMapper.sumMonthlyHours(workerId, monthStart, nextMonthStart);
        BigDecimal monthIncome = balanceTransactionMapper.sumMonthlyEarnings(workerId, monthStart, nextMonthStart);
        Integer attendanceDays = attendanceRecordMapper.countMonthlyAttendanceDays(workerId, monthStart, nextMonthStart);

        HomeStatsVO vo = new HomeStatsVO();
        vo.setMonthHours(monthHours != null ? monthHours : BigDecimal.ZERO);
        vo.setMonthIncome(monthIncome != null ? monthIncome : BigDecimal.ZERO);
        vo.setAttendanceDays(attendanceDays != null ? attendanceDays : 0);
        return vo;
    }

    @Override
    public HomeSchedulesVO getSchedules(Long workerId) {
        LocalDate today = LocalDate.now();
        List<ShiftEntity> todayRaw = shiftMapper.findTodayByWorkerId(workerId, today);
        List<ShiftEntity> futureRaw = shiftMapper.findFutureByWorkerId(workerId, today, 5);

        List<Long> allShiftIds = new ArrayList<>();
        todayRaw.forEach(s -> allShiftIds.add(s.getId()));
        futureRaw.forEach(s -> allShiftIds.add(s.getId()));

        Map<Long, AttendanceRecordEntity> recordMap = Map.of();
        Map<Long, AttendanceCorrectionEntity> correctionMap = Map.of();
        if (!allShiftIds.isEmpty()) {
            recordMap = attendanceRecordMapper.findByShiftIds(allShiftIds).stream()
                    .collect(Collectors.toMap(AttendanceRecordEntity::getShiftId, r -> r));
            correctionMap = correctionMapper.findByShiftIds(allShiftIds).stream()
                    .collect(Collectors.toMap(AttendanceCorrectionEntity::getShiftId, c -> c));
        }

        List<WorkerShiftVO> todayShifts = todayRaw.stream()
                .map(s -> workerShiftVOConverter.toWorkerShiftResponseBatch(s, recordMap, correctionMap))
                .collect(Collectors.toList());
        List<WorkerShiftVO> futureShifts = futureRaw.stream()
                .map(s -> workerShiftVOConverter.toWorkerShiftResponseBatch(s, recordMap, correctionMap))
                .collect(Collectors.toList());

        HomeSchedulesVO vo = new HomeSchedulesVO();
        vo.setTodayShifts(todayShifts);
        vo.setFutureShifts(futureShifts);
        return vo;
    }

}
