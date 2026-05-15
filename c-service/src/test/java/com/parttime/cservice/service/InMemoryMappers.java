package com.parttime.cservice.service;

import com.parttime.cservice.mapper.*;
import com.parttime.cservice.pojo.entity.*;
import com.parttime.cservice.pojo.vo.NotificationVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryMappers {

    public static WorkerMapper createWorkerMapper() {
        return new WorkerMapper() {
            private final ConcurrentHashMap<Long, Worker> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(Worker worker) {
                if (worker.getId() == null) worker.setId(idGen.getAndIncrement());
                store.put(worker.getId(), worker);
                return 1;
            }
            @Override public Optional<Worker> findById(Long id) { return Optional.ofNullable(store.get(id)); }
            @Override public Optional<Worker> findByPhone(String phone) {
                return store.values().stream().filter(w -> phone.equals(w.getPhone())).findFirst();
            }
            @Override public Optional<Worker> findByWechatCode(String code) {
                return store.values().stream().filter(w -> code.equals(w.getWechatCode())).findFirst();
            }
            @Override public Optional<Worker> findByOpenId(String openId) {
                return store.values().stream().filter(w -> openId.equals(w.getOpenId())).findFirst();
            }
            @Override public List<Worker> findAll() { return new ArrayList<>(store.values()); }
            @Override public int update(Worker worker) {
                store.put(worker.getId(), worker);
                return 1;
            }
        };
    }

    public static JobMapper createJobMapper() {
        return new JobMapper() {
            private final ConcurrentHashMap<Long, Job> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(Job job) {
                if (job.getId() == null) job.setId(idGen.getAndIncrement());
                if (job.getJobId() == null) job.setJobId(job.getId());
                store.put(job.getId(), job);
                return 1;
            }
            @Override public Optional<Job> findById(Long id) { return Optional.ofNullable(store.get(id)); }
            @Override public Optional<Job> findByJobId(Long jobId) {
                return store.values().stream().filter(j -> jobId.equals(j.getJobId())).findFirst();
            }
            @Override public List<Job> findAll() { return new ArrayList<>(store.values()); }
            @Override public List<Job> search(String keyword, String location, Long categoryId) {
                return store.values().stream()
                        .filter(job -> "PUBLISHED".equals(job.getStatus()))
                        .filter(job -> keyword == null || keyword.isEmpty()
                                || (job.getTitle() != null && job.getTitle().toLowerCase().contains(keyword.toLowerCase())))
                        .filter(job -> categoryId == null || categoryId.equals(job.getCategoryId()))
                        .filter(job -> location == null || location.isEmpty()
                                || (job.getLocation() != null && job.getLocation().toLowerCase().contains(location.toLowerCase())))
                        .collect(Collectors.toList());
            }
            @Override public List<Job> findByCompanyId(Long companyId) {
                return store.values().stream().filter(j -> companyId.equals(j.getCompanyId())).collect(Collectors.toList());
            }
            @Override public int update(Job job) {
                store.put(job.getId(), job);
                return 1;
            }
        };
    }

    public static JobApplicationMapper createJobApplicationMapper() {
        return new JobApplicationMapper() {
            private final ConcurrentHashMap<Long, JobApplication> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(JobApplication app) {
                if (app.getId() == null) app.setId(idGen.getAndIncrement());
                store.put(app.getId(), app);
                return 1;
            }
            @Override public Optional<JobApplication> findById(Long id) { return Optional.ofNullable(store.get(id)); }
            @Override public List<JobApplication> findByWorkerId(Long workerId) {
                return store.values().stream().filter(a -> workerId.equals(a.getWorkerId())).collect(Collectors.toList());
            }
            @Override public List<JobApplication> findByJobId(Long jobId) {
                return store.values().stream().filter(a -> jobId.equals(a.getJobId())).collect(Collectors.toList());
            }
            @Override public List<JobApplication> findByWorkerIdAndJobId(Long workerId, Long jobId) {
                return store.values().stream()
                        .filter(a -> workerId.equals(a.getWorkerId()) && jobId.equals(a.getJobId()))
                        .collect(Collectors.toList());
            }
            @Override public int countByWorkerIdAndStatus(Long workerId, String status) {
                return (int) store.values().stream()
                        .filter(a -> workerId.equals(a.getWorkerId()) && status.equals(a.getStatus()))
                        .count();
            }
            @Override public int updateStatus(Long id, String status) {
                JobApplication app = store.get(id);
                if (app != null) { app.setStatus(status); return 1; }
                return 0;
            }
        };
    }

    public static ShiftMapper createShiftMapper() {
        return new ShiftMapper() {
            private final ConcurrentHashMap<Long, ShiftEntity> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(ShiftEntity shift) {
                if (shift.getId() == null) shift.setId(idGen.getAndIncrement());
                store.put(shift.getId(), shift);
                return 1;
            }
            @Override public Optional<ShiftEntity> findById(Long id) { return Optional.ofNullable(store.get(id)); }
            @Override public List<ShiftEntity> findByWorkerId(Long workerId) {
                return store.values().stream().filter(s -> workerId.equals(s.getWorkerId())).collect(Collectors.toList());
            }
            @Override public List<ShiftEntity> findByWorkerIdAndDateRange(Long workerId, LocalDate startDate, LocalDate endDate) {
                return store.values().stream()
                        .filter(s -> s.getWorkerId().equals(workerId))
                        .filter(s -> startDate == null || !s.getShiftDate().isBefore(startDate))
                        .filter(s -> endDate == null || !s.getShiftDate().isAfter(endDate))
                        .collect(Collectors.toList());
            }
            @Override public List<ShiftEntity> findByJobId(Long jobId) {
                return store.values().stream().filter(s -> jobId.equals(s.getJobId())).collect(Collectors.toList());
            }
            @Override public int updateStatus(Long id, String status) {
                ShiftEntity s = store.get(id);
                if (s != null) { s.setStatus(status); return 1; }
                return 0;
            }
            @Override public int update(ShiftEntity shift) {
                store.put(shift.getId(), shift);
                return 1;
            }
        };
    }

    public static AttendanceRecordMapper createAttendanceRecordMapper() {
        return new AttendanceRecordMapper() {
            private final ConcurrentHashMap<Long, AttendanceRecordEntity> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(AttendanceRecordEntity record) {
                if (record.getId() == null) record.setId(idGen.getAndIncrement());
                store.put(record.getId(), record);
                return 1;
            }
            @Override public Optional<AttendanceRecordEntity> findById(Long id) { return Optional.ofNullable(store.get(id)); }
            @Override public Optional<AttendanceRecordEntity> findByShiftId(Long shiftId) {
                return store.values().stream().filter(r -> shiftId.equals(r.getShiftId())).findFirst();
            }
            @Override public List<AttendanceRecordEntity> findByWorkerId(Long workerId) {
                return store.values().stream().filter(r -> workerId.equals(r.getWorkerId())).collect(Collectors.toList());
            }
            @Override public int update(AttendanceRecordEntity record) {
                store.put(record.getId(), record);
                return 1;
            }
        };
    }

    public static WorkerProfileMapper createWorkerProfileMapper() {
        return new WorkerProfileMapper() {
            private final ConcurrentHashMap<Long, WorkerProfile> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(WorkerProfile profile) {
                if (profile.getId() == null) profile.setId(idGen.getAndIncrement());
                store.put(profile.getWorkerId(), profile);
                return 1;
            }
            @Override public Optional<WorkerProfile> findByWorkerId(Long workerId) {
                return Optional.ofNullable(store.get(workerId));
            }
            @Override public int update(WorkerProfile profile) {
                store.put(profile.getWorkerId(), profile);
                return 1;
            }
        };
    }

    public static WorkerResumeMapper createWorkerResumeMapper() {
        return new WorkerResumeMapper() {
            private final ConcurrentHashMap<Long, WorkerResume> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(WorkerResume resume) {
                if (resume.getId() == null) resume.setId(idGen.getAndIncrement());
                store.put(resume.getId(), resume);
                return 1;
            }
            @Override public List<WorkerResume> findByWorkerId(Long workerId) {
                return store.values().stream().filter(r -> workerId.equals(r.getWorkerId())).collect(Collectors.toList());
            }
            @Override public int deleteById(Long id) {
                store.remove(id);
                return 1;
            }
        };
    }

    public static WithdrawalRecordMapper createWithdrawalRecordMapper() {
        return new WithdrawalRecordMapper() {
            private final ConcurrentHashMap<Long, WithdrawalRecord> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(WithdrawalRecord record) {
                if (record.getId() == null) record.setId(idGen.getAndIncrement());
                store.put(record.getId(), record);
                return 1;
            }
            @Override public Optional<WithdrawalRecord> findById(Long id) { return Optional.ofNullable(store.get(id)); }
            @Override public List<WithdrawalRecord> findByWorkerId(Long workerId) {
                return store.values().stream().filter(r -> workerId.equals(r.getWorkerId())).collect(Collectors.toList());
            }
            @Override public int updateStatus(Long id, String status) {
                WithdrawalRecord r = store.get(id);
                if (r != null) { r.setStatus(status); return 1; }
                return 0;
            }
        };
    }

    public static NotificationMapper createNotificationMapper() {
        return new NotificationMapper() {
            private final ConcurrentHashMap<Long, NotificationVO> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(NotificationVO notification) {
                if (notification.getId() == null) notification.setId(idGen.getAndIncrement());
                store.put(notification.getId(), notification);
                return 1;
            }
            @Override public List<NotificationVO> findByRecipientId(Long recipientId, String recipientType) {
                return store.values().stream()
                        .filter(n -> recipientId.equals(n.getRecipientId()) && recipientType.equals(n.getRecipientType()))
                        .sorted((a, b) -> b.getSentAt().compareTo(a.getSentAt()))
                        .collect(Collectors.toList());
            }
        };
    }
}
