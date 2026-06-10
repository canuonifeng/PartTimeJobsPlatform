package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.AttendanceCorrectionMapper;
import com.parttime.cservice.mapper.AttendanceRecordMapper;
import com.parttime.cservice.pojo.entity.AttendanceCorrectionEntity;
import com.parttime.cservice.pojo.entity.AttendanceRecordEntity;
import com.parttime.cservice.pojo.entity.Job;
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
        return toWorkerShiftResponseBatch(shift, Map.of(), Map.of(), Map.of());
    }

    public WorkerShiftVO toWorkerShiftResponse(ShiftEntity shift, Map<Long, Job> jobMap) {
        return toWorkerShiftResponseBatch(shift, Map.of(), Map.of(), jobMap);
    }

    public WorkerShiftVO toWorkerShiftResponseBatch(ShiftEntity shift,
                                                      Map<Long, AttendanceRecordEntity> recordMap,
                                                      Map<Long, AttendanceCorrectionEntity> correctionMap) {
        return toWorkerShiftResponseBatch(shift, recordMap, correctionMap, Map.of());
    }

    public WorkerShiftVO toWorkerShiftResponseBatch(ShiftEntity shift,
                                                      Map<Long, AttendanceRecordEntity> recordMap,
                                                      Map<Long, AttendanceCorrectionEntity> correctionMap,
                                                      Map<Long, Job> jobMap) {
        WorkerShiftVO resp = new WorkerShiftVO();
        resp.setId(shift.getId());
        resp.setJobId(shift.getJobId());
        Job job = jobMap.get(shift.getJobId());
        if (job != null) {
            resp.setJobTitle(job.getTitle());
            resp.setLocation(job.getLocation());
        }
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
