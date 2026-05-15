package com.parttime.enterprise.infrastructure.repository;

import com.parttime.enterprise.core.domain.ScheduleShift;
import com.parttime.enterprise.core.repository.ScheduleShiftRepository;
import com.parttime.enterprise.infrastructure.mapper.ScheduleShiftMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ScheduleShiftRepositoryImpl implements ScheduleShiftRepository {

    private final ScheduleShiftMapper mapper;

    public ScheduleShiftRepositoryImpl(ScheduleShiftMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(ScheduleShift shift) {
        mapper.insert(shift);
    }

    @Override
    public Optional<ScheduleShift> findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public List<ScheduleShift> findByJobId(Long jobId) {
        return mapper.findByJobId(jobId);
    }

    @Override
    public List<ScheduleShift> findByWorkerId(Long workerId) {
        return mapper.findByWorkerId(workerId);
    }

    @Override
    public List<ScheduleShift> findByJobIdAndDate(Long jobId, LocalDate shiftDate) {
        return mapper.findByJobIdAndDate(jobId, shiftDate);
    }

    @Override
    public List<ScheduleShift> findByWorkerIdAndDateRange(Long workerId, LocalDate startDate, LocalDate endDate) {
        return mapper.findByWorkerIdAndDateRange(workerId, startDate, endDate);
    }

    @Override
    public void updateStatus(Long id, String status) {
        mapper.updateStatus(id, status);
    }

    @Override
    public void delete(Long id) {
        mapper.delete(id);
    }
}
