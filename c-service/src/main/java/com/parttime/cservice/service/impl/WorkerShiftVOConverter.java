package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.AttendanceCorrectionMapper;
import com.parttime.cservice.mapper.AttendanceRecordMapper;
import com.parttime.cservice.pojo.entity.AttendanceCorrectionEntity;
import com.parttime.cservice.pojo.entity.AttendanceRecordEntity;
import com.parttime.cservice.pojo.entity.ShiftEntity;
import com.parttime.cservice.pojo.vo.WorkerShiftVO;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Map;

@Component
public class WorkerShiftVOConverter {

    @Resource
    private AttendanceCorrectionMapper correctionMapper;
    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;

    public WorkerShiftVO toWorkerShiftResponse(ShiftEntity shift) {
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
        attendanceRecordMapper.findByShiftId(shift.getId()).ifPresent(record -> {
            resp.setCheckInTime(record.getCheckInTime());
            resp.setCheckOutTime(record.getCheckOutTime());
            resp.setWorkHours(record.getTotalHours());
        });
        correctionMapper.findByShiftId(shift.getId()).ifPresent(c ->
                resp.setCorrectionStatus(c.getStatus()));
        return resp;
    }

    public WorkerShiftVO toWorkerShiftResponseBatch(ShiftEntity shift,
                                                      Map<Long, AttendanceRecordEntity> recordMap,
                                                      Map<Long, AttendanceCorrectionEntity> correctionMap) {
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

        AttendanceRecordEntity record = recordMap.get(shift.getId());
        if (record != null) {
            resp.setCheckInTime(record.getCheckInTime());
            resp.setCheckOutTime(record.getCheckOutTime());
            resp.setWorkHours(record.getTotalHours());
        }

        AttendanceCorrectionEntity correction = correctionMap.get(shift.getId());
        if (correction != null) {
            resp.setCorrectionStatus(correction.getStatus());
        }

        return resp;
    }
}
