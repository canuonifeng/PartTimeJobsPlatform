package com.parttime.enterprise.core.repository;

import com.parttime.enterprise.core.domain.AttendanceRecord;

import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository {

    void save(AttendanceRecord record);

    Optional<AttendanceRecord> findById(Long id);

    Optional<AttendanceRecord> findByShiftId(Long shiftId);

    List<AttendanceRecord> findByShiftIds(List<Long> shiftIds);

    void update(AttendanceRecord record);
}
