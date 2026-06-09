package com.parttime.cservice.service;

import com.parttime.cservice.mapper.*;
import com.parttime.cservice.pojo.entity.*;
import com.parttime.cservice.pojo.vo.NotificationVO;
import com.parttime.cservice.pojo.vo.WorkerSignupVO;

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
            @Override public int bindOpenId(Long id, String openId) {
                Worker w = store.get(id);
                if (w != null) { w.setOpenId(openId); return 1; }
                return 0;
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
            @Override public int batchCloseJobs(List<Long> ids, String closeReason) {
                int count = 0;
                for (Long id : ids) {
                    Job job = store.get(id);
                    if (job != null) { job.setStatus("CLOSED"); job.setCloseReason(closeReason); count++; }
                }
                return count;
            }
        };
    }

    public static ScheduleApplicationMapper createScheduleApplicationMapper() {
        return new ScheduleApplicationMapper() {
            private final ConcurrentHashMap<Long, ScheduleApplication> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(ScheduleApplication app) {
                if (app.getId() == null) app.setId(idGen.getAndIncrement());
                if (app.getAppliedAt() == null) app.setAppliedAt(LocalDateTime.now());
                store.put(app.getId(), app);
                return 1;
            }
            @Override public Optional<ScheduleApplication> findByScheduleIdAndWorkerId(Long scheduleId, Long workerId) {
                return store.values().stream()
                        .filter(a -> scheduleId.equals(a.getScheduleId()) && workerId.equals(a.getWorkerId()))
                        .findFirst();
            }
            @Override public List<ScheduleApplication> findByWorkerId(Long workerId) {
                return store.values().stream().filter(a -> workerId.equals(a.getWorkerId())).collect(Collectors.toList());
            }
            @Override public List<WorkerSignupVO> findMySignups(Long workerId, int offset, int pageSize) {
                return store.values().stream()
                        .filter(a -> workerId.equals(a.getWorkerId()))
                        .skip(offset)
                        .limit(pageSize)
                        .map(a -> {
                            WorkerSignupVO vo = new WorkerSignupVO();
                            vo.setApplicationId(a.getId());
                            vo.setScheduleId(a.getScheduleId());
                            vo.setStatus(a.getStatus());
                            vo.setAppliedAt(a.getAppliedAt());
                            return vo;
                        })
                        .collect(Collectors.toList());
            }
            @Override public long countMySignups(Long workerId) {
                return store.values().stream().filter(a -> workerId.equals(a.getWorkerId())).count();
            }
            @Override public List<Long> findScheduleIdsByWorkerIdAndJobId(Long workerId, Long jobId) {
                return store.values().stream()
                        .filter(a -> workerId.equals(a.getWorkerId()))
                        .map(ScheduleApplication::getScheduleId)
                        .collect(Collectors.toList());
            }
            @Override public int countByScheduleId(Long scheduleId) {
                return (int) store.values().stream().filter(a -> scheduleId.equals(a.getScheduleId())).count();
            }
            @Override public java.util.Map<Long, Integer> countByScheduleIds(List<Long> scheduleIds) {
                return scheduleIds.stream().collect(Collectors.toMap(
                        id -> id,
                        id -> (int) store.values().stream().filter(a -> id.equals(a.getScheduleId())).count()
                ));
            }
            @Override public List<ScheduleApplication> findByIds(List<Long> ids) {
                return store.values().stream().filter(a -> ids.contains(a.getId())).collect(Collectors.toList());
            }
            @Override public int batchInsert(List<ScheduleApplication> list) {
                int count = 0;
                for (ScheduleApplication app : list) {
                    if (app.getId() == null) app.setId(idGen.getAndIncrement());
                    if (app.getAppliedAt() == null) app.setAppliedAt(LocalDateTime.now());
                    store.put(app.getId(), app);
                    count++;
                }
                return count;
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
            @Override public List<ShiftEntity> findRecentByWorkerId(Long workerId) {
                return findByWorkerId(workerId);
            }
            @Override public List<ShiftEntity> findTodayByWorkerId(Long workerId, LocalDate date) {
                return store.values().stream()
                        .filter(s -> workerId.equals(s.getWorkerId()) && date.equals(s.getShiftDate()))
                        .collect(Collectors.toList());
            }
            @Override public List<ShiftEntity> findFutureByWorkerId(Long workerId, LocalDate date, int size) {
                return store.values().stream()
                        .filter(s -> workerId.equals(s.getWorkerId()))
                        .filter(s -> !s.getShiftDate().isBefore(date))
                        .sorted(Comparator.comparing(ShiftEntity::getShiftDate))
                        .limit(size)
                        .collect(Collectors.toList());
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
            @Override public int batchUpdateStatus(List<Long> ids, String status) {
                int count = 0;
                for (Long id : ids) {
                    ShiftEntity s = store.get(id);
                    if (s != null) { s.setStatus(status); count++; }
                }
                return count;
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
            @Override public List<AttendanceRecordEntity> findByShiftIds(List<Long> shiftIds) {
                return store.values().stream().filter(r -> shiftIds.contains(r.getShiftId())).collect(Collectors.toList());
            }
            @Override public List<AttendanceRecordEntity> findByWorkerId(Long workerId) {
                return store.values().stream().filter(r -> workerId.equals(r.getWorkerId())).collect(Collectors.toList());
            }
            @Override public BigDecimal sumMonthlyHours(Long workerId, LocalDateTime startTime, LocalDateTime endTime) {
                return BigDecimal.ZERO;
            }
            @Override public Integer countMonthlyAttendanceDays(Long workerId, LocalDateTime startTime, LocalDateTime endTime) {
                return 0;
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
            @Override public int updateCompletion(Long id, String status, String thirdPartySerialNo, String thirdPartyPlatform, LocalDateTime completedAt) {
                WithdrawalRecord r = store.get(id);
                if (r != null) {
                    r.setStatus(status);
                    r.setThirdPartySerialNo(thirdPartySerialNo);
                    r.setThirdPartyPlatform(thirdPartyPlatform);
                    r.setCompletedAt(completedAt);
                    return 1;
                }
                return 0;
            }
            @Override public long countTodayWithdrawals(Long workerId, LocalDate date) {
                return store.values().stream()
                        .filter(r -> workerId.equals(r.getWorkerId()))
                        .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().toLocalDate().equals(date))
                        .count();
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
            @Override public List<NotificationVO> findByRecipientId(Long recipientId, String recipientType, int offset, int pageSize) {
                return store.values().stream()
                        .filter(n -> recipientId.equals(n.getRecipientId()) && recipientType.equals(n.getRecipientType()))
                        .sorted(Comparator.comparing(NotificationVO::getSentAt).reversed().thenComparing(Comparator.comparing(NotificationVO::getId).reversed()))
                        .skip(offset)
                        .limit(pageSize)
                        .collect(Collectors.toList());
            }
            @Override public long countByRecipientId(Long recipientId, String recipientType) {
                return store.values().stream()
                        .filter(n -> recipientId.equals(n.getRecipientId()) && recipientType.equals(n.getRecipientType()))
                        .count();
            }
            @Override public int markAsRead(Long id, Long recipientId, String recipientType) {
                NotificationVO notification = store.get(id);
                if (notification != null && recipientId.equals(notification.getRecipientId()) && recipientType.equals(notification.getRecipientType())) {
                    notification.setRead(true);
                    return 1;
                }
                return 0;
            }
        };
    }

    public static JobScheduleMapper createJobScheduleMapper() {
        return new JobScheduleMapper() {
            private final ConcurrentHashMap<Long, JobSchedule> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(JobSchedule schedule) {
                if (schedule.getId() == null) schedule.setId(idGen.getAndIncrement());
                store.put(schedule.getId(), schedule);
                return 1;
            }

            @Override public List<JobSchedule> findByJobId(Long jobId) {
                return store.values().stream()
                        .filter(s -> jobId.equals(s.getJobId()))
                        .sorted(Comparator.comparing(JobSchedule::getScheduleDate)
                                .thenComparing(JobSchedule::getStartTime))
                        .collect(Collectors.toList());
            }

            @Override public List<JobSchedule> findActiveByJobId(Long jobId) {
                return findByJobId(jobId).stream()
                        .filter(s -> s.getStatus() == null || "ACTIVE".equals(s.getStatus()))
                        .collect(Collectors.toList());
            }

            @Override public Optional<JobSchedule> findById(Long id) {
                return Optional.ofNullable(store.get(id));
            }

            @Override public List<JobSchedule> findByIds(List<Long> ids) {
                return ids.stream().map(store::get).filter(Objects::nonNull).collect(Collectors.toList());
            }

            @Override public int batchInsert(List<JobSchedule> list) {
                int count = 0;
                for (JobSchedule js : list) {
                    if (js.getId() == null) js.setId(idGen.getAndIncrement());
                    store.put(js.getId(), js);
                    count++;
                }
                return count;
            }
        };
    }

    public static AttendanceCheckInMapper createAttendanceCheckInMapper() {
        return new AttendanceCheckInMapper() {
            private final ConcurrentHashMap<Long, AttendanceCheckIn> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(AttendanceCheckIn record) {
                if (record.getId() == null) record.setId(idGen.getAndIncrement());
                store.put(record.getId(), record);
                return 1;
            }
            @Override public int update(AttendanceCheckIn record) {
                store.put(record.getId(), record);
                return 1;
            }
            @Override public List<AttendanceCheckIn> findByShiftId(Long shiftId) {
                return store.values().stream().filter(r -> shiftId.equals(r.getShiftId())).collect(Collectors.toList());
            }
            @Override public List<AttendanceCheckIn> findByShiftIdAndWorkerId(Long shiftId, Long workerId) {
                return store.values().stream()
                        .filter(r -> shiftId.equals(r.getShiftId()) && workerId.equals(r.getWorkerId()))
                        .collect(Collectors.toList());
            }
            @Override public AttendanceCheckIn findById(Long id) {
                return store.get(id);
            }
        };
    }

    public static AttendanceCorrectionMapper createAttendanceCorrectionMapper() {
        return new AttendanceCorrectionMapper() {
            private final ConcurrentHashMap<Long, AttendanceCorrectionEntity> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(AttendanceCorrectionEntity c) {
                if (c.getId() == null) c.setId(idGen.getAndIncrement());
                store.put(c.getId(), c);
                return 1;
            }
            @Override public java.util.Optional<AttendanceCorrectionEntity> findById(Long id) {
                return java.util.Optional.ofNullable(store.get(id));
            }
            @Override public java.util.Optional<AttendanceCorrectionEntity> findByShiftId(Long shiftId) {
                return store.values().stream().filter(c -> shiftId.equals(c.getShiftId())).findFirst();
            }
            @Override public List<AttendanceCorrectionEntity> findByShiftIds(List<Long> shiftIds) {
                return store.values().stream().filter(c -> shiftIds.contains(c.getShiftId())).collect(Collectors.toList());
            }
        };
    }

    public static WorkerRealNameAuthMapper createWorkerRealNameAuthMapper() {
        return new WorkerRealNameAuthMapper() {
            private final ConcurrentHashMap<Long, WorkerRealNameAuth> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(WorkerRealNameAuth auth) {
                if (auth.getId() == null) auth.setId(idGen.getAndIncrement());
                store.put(auth.getWorkerId(), auth);
                return 1;
            }
            @Override public int update(WorkerRealNameAuth auth) {
                store.put(auth.getWorkerId(), auth);
                return 1;
            }
            @Override public Optional<WorkerRealNameAuth> findByWorkerId(Long workerId) {
                return Optional.ofNullable(store.get(workerId));
            }
        };
    }

    public static WorkerBankCardMapper createWorkerBankCardMapper() {
        return new WorkerBankCardMapper() {
            private final ConcurrentHashMap<Long, WorkerBankCard> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(WorkerBankCard card) {
                if (card.getId() == null) card.setId(idGen.getAndIncrement());
                store.put(card.getWorkerId(), card);
                return 1;
            }
            @Override public int update(WorkerBankCard card) {
                store.put(card.getWorkerId(), card);
                return 1;
            }
            @Override public Optional<WorkerBankCard> findByWorkerId(Long workerId) {
                return Optional.ofNullable(store.get(workerId));
            }
            @Override public List<WorkerBankCard> findAllByWorkerId(Long workerId) {
                return store.values().stream().filter(c -> workerId.equals(c.getWorkerId())).collect(Collectors.toList());
            }
            @Override public int deleteByWorkerId(Long workerId) {
                return store.remove(workerId) != null ? 1 : 0;
            }
        };
    }

    public static WorkerBalanceMapper createWorkerBalanceMapper() {
        return new WorkerBalanceMapper() {
            private final ConcurrentHashMap<Long, com.parttime.cservice.pojo.entity.WorkerBalance> store = new ConcurrentHashMap<>();

            @Override public int upsert(Long workerId, BigDecimal balance, BigDecimal totalEarned, BigDecimal totalWithdrawn) {
                com.parttime.cservice.pojo.entity.WorkerBalance wb = new com.parttime.cservice.pojo.entity.WorkerBalance();
                wb.setWorkerId(workerId);
                wb.setBalance(balance);
                wb.setTotalEarned(totalEarned);
                wb.setTotalWithdrawn(totalWithdrawn);
                store.put(workerId, wb);
                return 1;
            }
            @Override public com.parttime.cservice.pojo.entity.WorkerBalance findByWorkerId(Long workerId) {
                return store.get(workerId);
            }
        };
    }

    public static BalanceTransactionMapper createBalanceTransactionMapper() {
        return new BalanceTransactionMapper() {
            private final ConcurrentHashMap<Long, com.parttime.cservice.pojo.entity.BalanceTransaction> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(com.parttime.cservice.pojo.entity.BalanceTransaction transaction) {
                if (transaction.getId() == null) transaction.setId(idGen.getAndIncrement());
                store.put(transaction.getId(), transaction);
                return 1;
            }
            @Override public List<com.parttime.cservice.pojo.entity.BalanceTransaction> findByWorkerId(Long workerId) {
                return store.values().stream().filter(t -> workerId.equals(t.getWorkerId())).collect(Collectors.toList());
            }
            @Override public List<com.parttime.cservice.pojo.entity.BalanceTransaction> findByWorkerIdPage(Long workerId, int offset, int pageSize) {
                return store.values().stream()
                        .filter(t -> workerId.equals(t.getWorkerId()))
                        .skip(offset)
                        .limit(pageSize)
                        .collect(Collectors.toList());
            }
            @Override public long countByWorkerId(Long workerId) {
                return store.values().stream().filter(t -> workerId.equals(t.getWorkerId())).count();
            }
            @Override public BigDecimal sumMonthlyEarnings(Long workerId, LocalDateTime startTime, LocalDateTime endTime) {
                return BigDecimal.ZERO;
            }
            @Override public int deleteByRelatedWithdrawalId(Long relatedWithdrawalId) {
                return (int) store.values().stream()
                        .filter(t -> relatedWithdrawalId.equals(t.getRelatedWithdrawalId()))
                        .peek(store::remove)
                        .count();
            }
        };
    }

    public static NotificationService createNotificationService() {
        return new NotificationService() {
            @Override public com.parttime.cservice.pojo.vo.NotificationVO sendNotification(Long workerId, String type, String title, String content) {
                return null;
            }
            @Override public com.parttime.cservice.pojo.vo.NotificationVO createWorkerNotification(Long workerId, String type, String category, String title, String content, String relatedType, Long relatedId) {
                return null;
            }
            @Override public com.parttime.cservice.pojo.vo.PageVO<com.parttime.cservice.pojo.vo.NotificationVO> getMyNotifications(Long workerId, Integer page, Integer pageSize) {
                return null;
            }
            @Override public void markAsRead(Long workerId, Long notificationId) {
                // no-op for tests
            }
        };
    }

    public static WeChatPayService createWeChatPayService() {
        return new WeChatPayService() {
            @Override public com.parttime.cservice.pojo.vo.TransferResult transferToWechat(Long workerId, BigDecimal amount, String openId, String description) {
                com.parttime.cservice.pojo.vo.TransferResult result = new com.parttime.cservice.pojo.vo.TransferResult();
                result.setSuccess(true);
                result.setTransferNo("SIMULATED_" + System.currentTimeMillis());
                return result;
            }
            @Override public com.parttime.cservice.pojo.vo.TransferResult transferToBankCard(Long workerId, BigDecimal amount, String bankAccount, String bankName, String description) {
                com.parttime.cservice.pojo.vo.TransferResult result = new com.parttime.cservice.pojo.vo.TransferResult();
                result.setSuccess(true);
                result.setTransferNo("SIMULATED_" + System.currentTimeMillis());
                return result;
            }
            @Override public com.parttime.cservice.pojo.vo.TransferResult queryTransferStatus(String transferNo) {
                com.parttime.cservice.pojo.vo.TransferResult result = new com.parttime.cservice.pojo.vo.TransferResult();
                result.setSuccess(true);
                return result;
            }
        };
    }

    public static com.parttime.cservice.mapper.ReferralCodeMapper createReferralCodeMapper() {
        return new com.parttime.cservice.mapper.ReferralCodeMapper() {
            private final ConcurrentHashMap<Long, com.parttime.cservice.pojo.entity.ReferralCode> store = new ConcurrentHashMap<>();
            private final ConcurrentHashMap<String, com.parttime.cservice.pojo.entity.ReferralCode> byCode = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public com.parttime.cservice.pojo.entity.ReferralCode findByWorkerId(Long workerId) {
                return store.values().stream().filter(c -> workerId.equals(c.getWorkerId())).findFirst().orElse(null);
            }
            @Override public com.parttime.cservice.pojo.entity.ReferralCode findByCode(String code) {
                return byCode.get(code);
            }
            @Override public void insert(com.parttime.cservice.pojo.entity.ReferralCode referralCode) {
                if (referralCode.getId() == null) referralCode.setId(idGen.getAndIncrement());
                if (referralCode.getCreatedAt() == null) referralCode.setCreatedAt(LocalDateTime.now());
                store.put(referralCode.getId(), referralCode);
                byCode.put(referralCode.getCode(), referralCode);
            }
        };
    }

    public static com.parttime.cservice.mapper.ReferralRecordMapper createReferralRecordMapper() {
        return new com.parttime.cservice.mapper.ReferralRecordMapper() {
            private final ConcurrentHashMap<Long, com.parttime.cservice.pojo.entity.ReferralRecord> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public com.parttime.cservice.pojo.entity.ReferralRecord findByRefereeId(Long refereeId) {
                return store.values().stream().filter(r -> refereeId.equals(r.getRefereeId())).findFirst().orElse(null);
            }
            @Override public List<com.parttime.cservice.pojo.entity.ReferralRecord> findByReferrerId(Long referrerId) {
                return store.values().stream().filter(r -> referrerId.equals(r.getReferrerId())).collect(Collectors.toList());
            }
            @Override public void insert(com.parttime.cservice.pojo.entity.ReferralRecord referralRecord) {
                if (referralRecord.getId() == null) referralRecord.setId(idGen.getAndIncrement());
                if (referralRecord.getBoundAt() == null) referralRecord.setBoundAt(LocalDateTime.now());
                store.put(referralRecord.getId(), referralRecord);
            }
            @Override public int countByReferrerId(Long referrerId) {
                return (int) store.values().stream().filter(r -> referrerId.equals(r.getReferrerId())).count();
            }
        };
    }

    public static com.parttime.cservice.mapper.ReferralRewardMapper createReferralRewardMapper() {
        return new com.parttime.cservice.mapper.ReferralRewardMapper() {
            private final ConcurrentHashMap<Long, com.parttime.cservice.pojo.entity.ReferralReward> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public com.parttime.cservice.pojo.entity.ReferralReward findByReferralRecordId(Long referralRecordId) {
                return store.values().stream().filter(r -> referralRecordId.equals(r.getReferralRecordId())).findFirst().orElse(null);
            }
            @Override public List<com.parttime.cservice.pojo.entity.ReferralReward> findByReferralRecordIds(List<Long> referralRecordIds) {
                return store.values().stream().filter(r -> referralRecordIds.contains(r.getReferralRecordId())).collect(Collectors.toList());
            }
            @Override public List<com.parttime.cservice.pojo.entity.ReferralReward> findByReferrerIdPage(Long referrerId, int offset, int pageSize) {
                return store.values().stream().filter(r -> r.getReferralRecordId() != null).collect(Collectors.toList());
            }
            @Override public long countByReferrerId(Long referrerId) {
                return store.values().stream().count();
            }
            @Override public void insert(com.parttime.cservice.pojo.entity.ReferralReward referralReward) {
                if (referralReward.getId() == null) referralReward.setId(idGen.getAndIncrement());
                if (referralReward.getCreatedAt() == null) referralReward.setCreatedAt(LocalDateTime.now());
                store.put(referralReward.getId(), referralReward);
            }
            @Override public void updateStatus(Long id, String status, String auditRemark) {
                com.parttime.cservice.pojo.entity.ReferralReward r = store.get(id);
                if (r != null) { r.setStatus(status); r.setAuditRemark(auditRemark); }
            }
        };
    }

    public static com.parttime.cservice.mapper.ReferralConfigMapper createReferralConfigMapper() {
        return new com.parttime.cservice.mapper.ReferralConfigMapper() {
            private final ConcurrentHashMap<String, com.parttime.cservice.pojo.entity.ReferralConfig> store = new ConcurrentHashMap<>();

            @Override public List<com.parttime.cservice.pojo.entity.ReferralConfig> findAll() {
                return new ArrayList<>(store.values());
            }
            @Override public com.parttime.cservice.pojo.entity.ReferralConfig findByKey(String configKey) {
                return store.get(configKey);
            }
            @Override public void upsert(String configKey, String configValue, String description) {
                com.parttime.cservice.pojo.entity.ReferralConfig config = new com.parttime.cservice.pojo.entity.ReferralConfig();
                config.setConfigKey(configKey);
                config.setConfigValue(configValue);
                config.setDescription(description);
                config.setUpdatedAt(LocalDateTime.now());
                store.put(configKey, config);
            }
        };
    }
}
