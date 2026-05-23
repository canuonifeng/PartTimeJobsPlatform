package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.AttendanceRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AttendanceRecordMapper {
    int insert(AttendanceRecordEntity record);
    Optional<AttendanceRecordEntity> findById(Long id);
    Optional<AttendanceRecordEntity> findByShiftId(Long shiftId);
    List<AttendanceRecordEntity> findByWorkerId(Long workerId);
    int update(AttendanceRecordEntity record);
}
