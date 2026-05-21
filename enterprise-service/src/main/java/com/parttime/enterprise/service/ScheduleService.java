package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.ScheduleShiftCmd;
import com.parttime.enterprise.pojo.cmd.ScheduleTemplateCmd;
import com.parttime.enterprise.pojo.vo.AttendanceReportVO;
import com.parttime.enterprise.pojo.vo.ScheduleShiftVO;
import com.parttime.enterprise.pojo.vo.ScheduleTemplateVO;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    ScheduleTemplateVO createTemplate(ScheduleTemplateCmd request);

    ScheduleTemplateVO getTemplateById(Long id);

    List<ScheduleTemplateVO> getTemplatesByCompany(Long companyId);

    ScheduleTemplateVO updateTemplate(Long id, ScheduleTemplateCmd request);

    void deleteTemplate(Long id);

    ScheduleShiftVO assignShift(ScheduleShiftCmd request);

    ScheduleShiftVO updateShift(Long id, ScheduleShiftCmd request);

    java.util.Map<String, Object> getShifts(Long jobId, Long workerId, LocalDate shiftDate, Integer page, Integer pageSize);

    void removeShift(Long id);

    List<AttendanceReportVO> getAttendanceReport(Long jobId, Long shiftId, LocalDate date);
}
