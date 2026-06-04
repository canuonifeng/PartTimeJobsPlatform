package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.ShiftEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ShiftMapper {
    int insert(ShiftEntity shift);
    Optional<ShiftEntity> findById(Long id);
    List<ShiftEntity> findByWorkerId(Long workerId);
    List<ShiftEntity> findByWorkerIdAndDateRange(@Param("workerId") Long workerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    List<ShiftEntity> findRecentByWorkerId(@Param("workerId") Long workerId);
    List<ShiftEntity> findTodayByWorkerId(@Param("workerId") Long workerId, @Param("date") LocalDate date);
    List<ShiftEntity> findFutureByWorkerId(@Param("workerId") Long workerId, @Param("date") LocalDate date, @Param("size") int size);
    List<ShiftEntity> findByJobId(Long jobId);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    int update(ShiftEntity shift);

}
