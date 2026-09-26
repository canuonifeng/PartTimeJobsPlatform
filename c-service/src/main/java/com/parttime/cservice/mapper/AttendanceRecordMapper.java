package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.AttendanceRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface AttendanceRecordMapper {
    int insert(AttendanceRecordEntity record);
    Optional<AttendanceRecordEntity> findById(Long id);
    Optional<AttendanceRecordEntity> findByShiftId(Long shiftId);
    List<AttendanceRecordEntity> findByShiftIds(@Param("shiftIds") List<Long> shiftIds);
    List<AttendanceRecordEntity> findByWorkerIdPage(@Param("workerId") Long workerId, @Param("offset") int offset, @Param("pageSize") int pageSize);
    long countByWorkerId(Long workerId);
    long countCompletedByWorkerId(@Param("workerId") Long workerId);
    BigDecimal sumMonthlyHours(@Param("workerId") Long workerId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    Integer countMonthlyAttendanceDays(@Param("workerId") Long workerId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    int update(AttendanceRecordEntity record);
}
