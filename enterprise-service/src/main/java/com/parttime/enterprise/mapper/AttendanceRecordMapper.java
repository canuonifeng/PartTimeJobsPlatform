package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AttendanceRecordMapper {

    int insert(AttendanceRecord record);

    Optional<AttendanceRecord> findById(Long id);

    Optional<AttendanceRecord> findByShiftId(Long shiftId);

    List<AttendanceRecord> findByShiftIds(@Param("shiftIds") List<Long> shiftIds);

    int update(AttendanceRecord record);
}
