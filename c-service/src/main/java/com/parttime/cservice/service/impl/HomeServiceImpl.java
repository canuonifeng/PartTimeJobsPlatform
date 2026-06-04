package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.AttendanceRecordMapper;
import com.parttime.cservice.mapper.BalanceTransactionMapper;
import com.parttime.cservice.mapper.ShiftMapper;
import com.parttime.cservice.pojo.vo.HomeSchedulesVO;
import com.parttime.cservice.pojo.vo.HomeStatsVO;
import com.parttime.cservice.pojo.vo.WorkerShiftVO;
import com.parttime.cservice.service.HomeService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HomeServiceImpl implements HomeService {

    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;
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
        List<WorkerShiftVO> todayShifts = shiftMapper.findTodayByWorkerId(workerId, today).stream()
                .map(workerShiftVOConverter::toWorkerShiftResponse)
                .collect(Collectors.toList());
        List<WorkerShiftVO> futureShifts = shiftMapper.findFutureByWorkerId(workerId, today, 5).stream()
                .map(workerShiftVOConverter::toWorkerShiftResponse)
                .collect(Collectors.toList());

        HomeSchedulesVO vo = new HomeSchedulesVO();
        vo.setTodayShifts(todayShifts);
        vo.setFutureShifts(futureShifts);
        return vo;
    }

}
