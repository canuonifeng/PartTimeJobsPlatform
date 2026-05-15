package com.parttime.enterprise.infrastructure.repository;

import com.parttime.enterprise.core.domain.AttendanceRecord;
import com.parttime.enterprise.core.repository.AttendanceRecordRepository;
import com.parttime.enterprise.infrastructure.mapper.AttendanceRecordMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AttendanceRecordRepositoryImpl implements AttendanceRecordRepository {

    private final AttendanceRecordMapper mapper;

    public AttendanceRecordRepositoryImpl(AttendanceRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(AttendanceRecord record) {
        mapper.insert(record);
    }

    @Override
    public Optional<AttendanceRecord> findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public Optional<AttendanceRecord> findByShiftId(Long shiftId) {
        return mapper.findByShiftId(shiftId);
    }

    @Override
    public List<AttendanceRecord> findByShiftIds(List<Long> shiftIds) {
        return mapper.findByShiftIds(shiftIds);
    }

    @Override
    public void update(AttendanceRecord record) {
        mapper.update(record);
    }
}
