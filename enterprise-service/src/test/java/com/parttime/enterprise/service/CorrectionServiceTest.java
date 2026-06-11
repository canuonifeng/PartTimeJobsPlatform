package com.parttime.enterprise.service;

import com.parttime.enterprise.mapper.AttendanceCorrectionMapper;
import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.pojo.entity.*;
import com.parttime.enterprise.pojo.vo.AttendanceHoursVO;
import com.parttime.enterprise.pojo.vo.CorrectionVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.impl.CorrectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CorrectionServiceTest {

    private CorrectionServiceImpl service;
    private InMemoryAttendanceCorrectionMapper correctionMapper;
    private InMemoryAttendanceRecordMapper recordMapper;
    private InMemoryScheduleShiftMapper shiftMapper;
    private TestWorkerSyncMapper workerMapper;
    private TestJobMapper jobMapper;

    @BeforeEach
    void setUp() {
        correctionMapper = new InMemoryAttendanceCorrectionMapper();
        recordMapper = new InMemoryAttendanceRecordMapper();
        shiftMapper = new InMemoryScheduleShiftMapper();
        workerMapper = new TestWorkerSyncMapper();
        jobMapper = new TestJobMapper();

        service = new CorrectionServiceImpl();
        service.setCorrectionMapper(correctionMapper);
        service.setAttendanceRecordMapper(recordMapper);
        service.setShiftMapper(shiftMapper);
        service.setWorkerSyncMapper(workerMapper);
        service.setJobMapper(jobMapper);

        // Seed a job
        Job job = new Job();
        job.setId(1L);
        job.setTitle("测试岗位");
        jobMapper.store.put(1L, job);

        // Seed a shift
        ScheduleShift shift = new ScheduleShift();
        shift.setId(1L);
        shift.setJobId(1L);
        shift.setWorkerId(1L);
        shift.setShiftDate(LocalDate.of(2026, 5, 21));
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndTime(LocalTime.of(18, 0));
        shiftMapper.store.put(1L, shift);

        // Seed correction
        AttendanceCorrection c = new AttendanceCorrection();
        c.setId(1L);
        c.setShiftId(1L);
        c.setWorkerId(1L);
        c.setReason("忘记打卡");
        c.setStatus("PENDING");
        c.setCreatedAt(LocalDateTime.now());
        correctionMapper.store.put(1L, c);
    }

    @Test
    void testListCorrections() {
        PageVO<CorrectionVO> result = service.listCorrections(null, null, null, null, 1, 20);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void testApproveWithoutExistingRecord() {
        service.approve(1L, 100L);

        AttendanceCorrection c = correctionMapper.findById(1L).orElseThrow();
        assertEquals("APPROVED", c.getStatus());
        assertNotNull(c.getProcessedAt());
        assertEquals(100L, c.getProcessorId());

        assertTrue(recordMapper.findByShiftId(1L).isPresent());
        assertEquals("补卡", recordMapper.findByShiftId(1L).get().getRemark());
    }

    @Test
    void testApproveWithExistingRecord() {
        AttendanceRecord existing = new AttendanceRecord();
        existing.setId(10L);
        existing.setShiftId(1L);
        existing.setCheckInTime(LocalDateTime.of(2026, 5, 21, 9, 5));
        existing.setStatus("CHECKED_IN");
        recordMapper.store.put(10L, existing);

        service.approve(1L, 100L);

        AttendanceRecord updated = recordMapper.findByShiftId(1L).orElseThrow();
        assertEquals("补卡", updated.getRemark());
    }

    @Test
    void testReject() {
        service.reject(1L, 100L, "理由不充分");

        AttendanceCorrection c = correctionMapper.findById(1L).orElseThrow();
        assertEquals("REJECTED", c.getStatus());
        assertEquals("理由不充分", c.getRejectReason());
        assertNotNull(c.getProcessedAt());
        assertEquals(100L, c.getProcessorId());
    }

    @Test
    void testApproveNonPendingShouldThrow() {
        correctionMapper.store.get(1L).setStatus("APPROVED");
        assertThrows(RuntimeException.class, () -> service.approve(1L, 100L));
    }

    // --- In-memory mapper implementations ---

    static class InMemoryAttendanceCorrectionMapper implements AttendanceCorrectionMapper {
        final Map<Long, AttendanceCorrection> store = new HashMap<>();
        @Override public int insert(AttendanceCorrection c) { store.put(c.getId(), c); return 1; }
        @Override public Optional<AttendanceCorrection> findById(Long id) {
            AttendanceCorrection c = store.get(id);
            if (c == null) return Optional.empty();
            AttendanceCorrection copy = new AttendanceCorrection();
            copy.setId(c.getId());
            copy.setShiftId(c.getShiftId());
            copy.setWorkerId(c.getWorkerId());
            copy.setReason(c.getReason());
            copy.setStatus(c.getStatus());
            copy.setRejectReason(c.getRejectReason());
            copy.setCreatedAt(c.getCreatedAt());
            copy.setProcessedAt(c.getProcessedAt());
            copy.setProcessorId(c.getProcessorId());
            return Optional.of(copy);
        }
        @Override public List<AttendanceCorrection> findByShiftId(Long shiftId) { return List.of(); }
        @Override public List<AttendanceCorrection> findByWorkerId(Long workerId) { return List.of(); }
        @Override public List<AttendanceCorrection> findByStatus(String status) { return List.of(); }
        @Override
        public List<AttendanceCorrection> search(String status, String keyword, String dateFrom, String dateTo, Integer offset, Integer limit) {
            return new ArrayList<>(store.values());
        }
        @Override public int countSearch(String status, String keyword, String dateFrom, String dateTo) { return store.size(); }
        @Override public int update(AttendanceCorrection c) {
            store.put(c.getId(), c);
            return 1;
        }
    }

    static class InMemoryAttendanceRecordMapper implements AttendanceRecordMapper {
        final Map<Long, AttendanceRecord> store = new HashMap<>();
        @Override public int insert(AttendanceRecord r) { store.put(r.getId(), r); return 1; }
        @Override public Optional<AttendanceRecord> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override
        public Optional<AttendanceRecord> findByShiftId(Long shiftId) {
            return store.values().stream().filter(r -> shiftId.equals(r.getShiftId())).findFirst();
        }
        @Override public List<AttendanceRecord> findByShiftIds(List<Long> shiftIds) { return List.of(); }
        @Override public int update(AttendanceRecord r) { store.put(r.getId(), r); return 1; }
        @Override public List<AttendanceHoursVO> findHours(Long companyId, String workerName, LocalDate dateFrom, LocalDate dateTo, String settlementStatus, int offset, int pageSize) { return List.of(); }
        @Override public long countHours(Long companyId, String workerName, LocalDate dateFrom, LocalDate dateTo, String settlementStatus) { return 0; }
        @Override public void deleteByIds(List<Long> ids) {}
        @Override public List<AttendanceRecord> findByIds(List<Long> ids) {
            List<AttendanceRecord> result = new ArrayList<>();
            for (Long id : ids) {
                AttendanceRecord r = store.get(id);
                if (r != null) result.add(r);
            }
            return result;
        }
    }

    static class InMemoryScheduleShiftMapper implements ScheduleShiftMapper {
        final Map<Long, ScheduleShift> store = new HashMap<>();
        @Override public int insert(ScheduleShift s) { store.put(s.getId(), s); return 1; }
        @Override public Optional<ScheduleShift> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<ScheduleShift> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<ScheduleShift> findByCompanyId(Long companyId) { return List.of(); }
        @Override public List<ScheduleShift> findByJobId(Long jobId) { return List.of(); }
        @Override public List<ScheduleShift> findByWorkerId(Long workerId) { return List.of(); }
        @Override public List<ScheduleShift> findByJobIdAndDate(Long jobId, LocalDate date) { return List.of(); }
        @Override public List<ScheduleShift> findByIds(List<Long> ids) {
            List<ScheduleShift> result = new ArrayList<>();
            for (Long id : ids) {
                ScheduleShift s = store.get(id);
                if (s != null) result.add(s);
            }
            return result;
        }
        @Override public List<ScheduleShift> findByWorkerIdAndDateRange(Long workerId, LocalDate startDate, LocalDate endDate) { return List.of(); }
        @Override public List<ScheduleShift> findByApplicationId(Long applicationId) { return List.of(); }
        @Override public List<ScheduleShift> findByDateRange(LocalDate startDate, LocalDate endDate) { return List.of(); }
        @Override public int update(ScheduleShift s) { store.put(s.getId(), s); return 1; }
        @Override public int delete(Long id) { store.remove(id); return 1; }
        @Override public int cancelShift(Long id) { return updateStatus(id, "CANCELLED"); }
        @Override public int updateStatus(Long id, String status) { return 0; }
        @Override public List<ScheduleShift> findCompletedByWorkerIdAndCompanyId(Long workerId, Long companyId) { return List.of(); }
    }

    static class TestWorkerSyncMapper implements WorkerSyncMapper {
        @Override public String findWorkerNameById(Long workerId) { return "测试工人"; }
        @Override public String findWorkerPhoneById(Long workerId) { return "13800138000"; }
        @Override public LocalDate findWorkerBirthdayById(Long workerId) { return null; }
        @Override public List<Map<String, Object>> findWorkerNamesByIds(List<Long> ids) { return List.of(); }
        @Override public List<Map<String, Object>> findWorkerPhonesByIds(List<Long> ids) { return List.of(); }
        @Override public List<Map<String, Object>> findWorkerGendersByIds(List<Long> ids) { return List.of(); }
        @Override public List<Map<String, Object>> findWorkerBirthdaysByIds(List<Long> ids) { return List.of(); }
    }

    static class TestJobMapper implements JobMapper {
        final Map<Long, Job> store = new HashMap<>();
        @Override public int insert(Job job) { store.put(job.getId(), job); return 1; }
        @Override public Optional<Job> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<Job> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<Job> findByCompanyId(Long companyId) { return List.of(); }
        @Override public int update(Job job) { store.put(job.getId(), job); return 1; }
        @Override public int delete(Long id) { store.remove(id); return 1; }
        @Override public List<Job> findByCompanyIdAndStatus(Long companyId, String status) { return List.of(); }
        @Override public List<Job> findByCategoryId(Long categoryId) { return List.of(); }
        @Override public int updateStatus(Long id, String status) { return 0; }
        @Override public List<Job> findByIds(List<Long> ids) {
            List<Job> result = new ArrayList<>();
            for (Long id : ids) {
                Job j = store.get(id);
                if (j != null) result.add(j);
            }
            return result;
        }
    }
}
