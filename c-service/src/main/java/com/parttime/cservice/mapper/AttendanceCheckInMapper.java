package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.AttendanceCheckIn;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AttendanceCheckInMapper {
    int insert(AttendanceCheckIn record);
    int update(AttendanceCheckIn record);
    List<AttendanceCheckIn> findByShiftId(Long shiftId);
    List<AttendanceCheckIn> findByShiftIdAndWorkerId(Long shiftId, Long workerId);
    AttendanceCheckIn findById(Long id);
}
