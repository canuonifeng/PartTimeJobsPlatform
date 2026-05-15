package com.parttime.enterprise.core.repository;

import com.parttime.enterprise.core.domain.ScheduleShift;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleShiftRepository {

    void save(ScheduleShift shift);

    Optional<ScheduleShift> findById(Long id);

    List<ScheduleShift> findByJobId(Long jobId);

    List<ScheduleShift> findByWorkerId(Long workerId);

    List<ScheduleShift> findByJobIdAndDate(Long jobId, LocalDate shiftDate);

    List<ScheduleShift> findByWorkerIdAndDateRange(Long workerId, LocalDate startDate, LocalDate endDate);

    void updateStatus(Long id, String status);

    void delete(Long id);
}
