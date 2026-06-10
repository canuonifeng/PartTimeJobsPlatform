package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.ScheduleShiftCmd;
import com.parttime.enterprise.pojo.vo.AttendanceReportVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.pojo.vo.ScheduleShiftVO;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    ScheduleShiftVO assignShift(ScheduleShiftCmd request);

    ScheduleShiftVO updateShift(Long id, ScheduleShiftCmd request);

    PageVO<ScheduleShiftVO> getShifts(Long companyId, Long jobId, Long workerId, LocalDate shiftDate, Integer page, Integer pageSize);

    void removeShift(Long id);

    List<AttendanceReportVO> getAttendanceReport(Long jobId, Long shiftId, LocalDate date);
}
