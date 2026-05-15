package com.parttime.enterprise.infrastructure.mapper;

import com.parttime.enterprise.core.domain.ScheduleShift;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ScheduleShiftMapper {

    int insert(ScheduleShift shift);

    Optional<ScheduleShift> findById(Long id);

    List<ScheduleShift> findByJobId(Long jobId);

    List<ScheduleShift> findByWorkerId(Long workerId);

    List<ScheduleShift> findByJobIdAndDate(@Param("jobId") Long jobId, @Param("shiftDate") LocalDate shiftDate);

    List<ScheduleShift> findByWorkerIdAndDateRange(
            @Param("workerId") Long workerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<ScheduleShift> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int delete(Long id);

    List<ScheduleShift> findCompletedByWorkerIdAndCompanyId(@Param("workerId") Long workerId, @Param("companyId") Long companyId);
}
