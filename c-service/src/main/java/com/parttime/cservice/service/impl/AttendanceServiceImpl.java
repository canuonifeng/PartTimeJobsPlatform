package com.parttime.cservice.service.impl;

import com.parttime.cservice.enums.ShiftStatus;
import com.parttime.cservice.mapper.AttendanceCheckInMapper;
import com.parttime.cservice.mapper.AttendanceCorrectionMapper;
import com.parttime.cservice.mapper.AttendanceRecordMapper;
import com.parttime.cservice.mapper.BalanceTransactionMapper;
import com.parttime.cservice.mapper.JobMapper;
import com.parttime.cservice.mapper.NotificationMapper;
import com.parttime.cservice.mapper.ShiftMapper;
import com.parttime.cservice.mapper.SystemConfigMapper;
import com.parttime.cservice.mapper.WorkerBalanceMapper;
import com.parttime.cservice.pojo.entity.AttendanceCheckIn;
import com.parttime.cservice.pojo.entity.AttendanceCorrectionEntity;
import com.parttime.cservice.pojo.entity.AttendanceRecordEntity;
import com.parttime.cservice.pojo.entity.BalanceTransaction;
import com.parttime.cservice.pojo.entity.Job;
import com.parttime.cservice.pojo.entity.ShiftEntity;
import com.parttime.cservice.pojo.entity.SystemConfig;
import com.parttime.cservice.pojo.entity.WorkerBalance;
import com.parttime.cservice.pojo.vo.AttendanceVO;
import com.parttime.cservice.pojo.vo.NotificationVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.WorkerShiftVO;
import com.parttime.cservice.service.AttendanceService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Resource
    private ShiftMapper shiftMapper;
    @Resource
    private JobMapper jobMapper;
    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;
    @Resource
    private AttendanceCorrectionMapper correctionMapper;
    @Resource
    private WorkerShiftVOConverter workerShiftVOConverter;
    @Resource
    private AttendanceCheckInMapper attendanceCheckInMapper;
    @Resource
    private SystemConfigMapper systemConfigMapper;
    @Resource
    private WorkerBalanceMapper workerBalanceMapper;
    @Resource
    private BalanceTransactionMapper balanceTransactionMapper;
    @Resource
    private NotificationMapper notificationMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public ShiftEntity addShift(Long jobId, Long workerId,
                                  LocalDate shiftDate, LocalTime startTime, LocalTime endTime,
                                  BigDecimal locationLat, BigDecimal locationLng, Integer locationRadius,
                                  String locationName) {
        ShiftEntity shift = new ShiftEntity();
        shift.setJobId(jobId);
        shift.setWorkerId(workerId);
        shift.setShiftDate(shiftDate);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shift.setLocationLat(locationLat);
        shift.setLocationLng(locationLng);
        shift.setLocationRadius(locationRadius);
        shift.setLocationName(locationName);
        shift.setStatus(ShiftStatus.SCHEDULED.name());
        shift.setCreatedAt(LocalDateTime.now());
        shift.setUpdatedAt(LocalDateTime.now());
        shiftMapper.insert(shift);
        return shift;
    }

    @Override
    public List<WorkerShiftVO> getMyShifts(Long workerId, LocalDate startDate, LocalDate endDate, Integer page, Integer pageSize) {
        int p = (page != null && page > 0) ? page : 1;
        int ps = (pageSize != null && pageSize > 0) ? pageSize : 20;
        int offset = (p - 1) * ps;

        List<ShiftEntity> shifts = shiftMapper.findByWorkerIdAndDateRange(workerId, startDate, endDate, p, ps, offset).stream()
                .filter(s -> !"CANCELLED".equals(s.getStatus()))
                .collect(Collectors.toList());

        List<Long> staleIds = shifts.stream()
                .filter(s -> ShiftStatus.SCHEDULED.name().equals(s.getStatus())
                        && s.getShiftDate() != null && s.getEndTime() != null
                        && LocalDateTime.now().isAfter(LocalDateTime.of(s.getShiftDate(), s.getEndTime())))
                .map(ShiftEntity::getId)
                .collect(Collectors.toList());

        if (!staleIds.isEmpty()) {
            shiftMapper.batchUpdateStatus(staleIds, ShiftStatus.ABSENT.name());
            shifts.stream()
                    .filter(s -> staleIds.contains(s.getId()))
                    .forEach(s -> s.setStatus(ShiftStatus.ABSENT.name()));
        }

        List<Long> validShiftIds = shifts.stream()
                .filter(s -> !staleIds.contains(s.getId()))
                .map(ShiftEntity::getId)
                .collect(Collectors.toList());

        Map<Long, AttendanceRecordEntity> recordMap = Map.of();
        if (!validShiftIds.isEmpty()) {
            recordMap = attendanceRecordMapper.findByShiftIds(validShiftIds).stream()
                    .collect(Collectors.toMap(AttendanceRecordEntity::getShiftId, r -> r));
        }

        Map<Long, AttendanceCorrectionEntity> correctionMap = Map.of();
        if (!validShiftIds.isEmpty()) {
            correctionMap = correctionMapper.findByShiftIds(validShiftIds).stream()
                    .collect(Collectors.toMap(AttendanceCorrectionEntity::getShiftId, c -> c));
        }

        Map<Long, AttendanceRecordEntity> finalRecordMap = recordMap;
        Map<Long, AttendanceCorrectionEntity> finalCorrectionMap = correctionMap;

        Map<Long, Job> jobMap = Map.of();
        List<Long> jobIds = shifts.stream().map(ShiftEntity::getJobId).filter(java.util.Objects::nonNull).distinct().toList();
        if (!jobIds.isEmpty()) {
            jobMap = jobMapper.findByJobIds(jobIds).stream()
                    .collect(Collectors.toMap(Job::getId, j -> j));
        }
        Map<Long, Job> finalJobMap = jobMap;

        return shifts.stream()
                .map(s -> workerShiftVOConverter.toWorkerShiftResponseBatch(s, finalRecordMap, finalCorrectionMap, finalJobMap))
                .collect(Collectors.toList());
    }

    @Override
    public Long countMyShifts(Long workerId, LocalDate startDate, LocalDate endDate) {
        return shiftMapper.countByWorkerIdAndDateRange(workerId, startDate, endDate);
    }

    @Override
    public AttendanceVO checkIn(Long workerId, Long shiftId, BigDecimal lat, BigDecimal lng) {
        ShiftEntity shift = shiftMapper.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftId));

        if (!shift.getWorkerId().equals(workerId)) {
            throw new RuntimeException("Shift does not belong to this worker");
        }

        // 已上岗/已签退/缺勤/迟到/早退 都不能再签到
        if (!ShiftStatus.SCHEDULED.name().equals(shift.getStatus())) {
            throw new RuntimeException("Cannot check in: shift status is " + shift.getStatus());
        }

        if (shift.getLocationLat() != null && lat != null) {
            int radius = shift.getLocationRadius() != null ? shift.getLocationRadius() : getDefaultRadius();
            double distance = haversine(
                    shift.getLocationLat().doubleValue(), shift.getLocationLng().doubleValue(),
                    lat.doubleValue(), lng.doubleValue());
            if (distance > radius) {
                throw new RuntimeException("Location out of range: " + (int) distance + "m (max: " + radius + "m)");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledStart = LocalDateTime.of(shift.getShiftDate(), shift.getStartTime());
        long lateSeconds = now.isAfter(scheduledStart) ? Duration.between(scheduledStart, now).getSeconds() : 0;

        ShiftStatus newStatus = lateSeconds > 0 ? ShiftStatus.LATE : ShiftStatus.ON_DUTY;

        shift.setStatus(newStatus.name());
        shift.setUpdatedAt(now);
        shiftMapper.update(shift);

        AttendanceCheckIn checkIn = new AttendanceCheckIn();
        checkIn.setShiftId(shiftId);
        checkIn.setWorkerId(workerId);
        checkIn.setCheckInTime(now);
        checkIn.setCheckInLat(lat);
        checkIn.setCheckInLng(lng);
        checkIn.setLateSeconds((int) lateSeconds);
        checkIn.setEarlyLeaveSeconds(0);
        checkIn.setCreatedAt(now);
        checkIn.setUpdatedAt(now);
        attendanceCheckInMapper.insert(checkIn);

        AttendanceRecordEntity record = new AttendanceRecordEntity();
        record.setShiftId(shiftId);
        record.setJobId(shift.getJobId());
        record.setCompanyId(shift.getCompanyId());
        record.setWorkerId(workerId);
        record.setCheckInTime(now);
        record.setCheckInLat(lat);
        record.setCheckInLng(lng);
        record.setStatus(newStatus.name());
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        attendanceRecordMapper.insert(record);

        return toAttendanceResponse(record, shift);
    }

    @Override
    public AttendanceVO checkOut(Long workerId, Long shiftId, BigDecimal lat, BigDecimal lng) {
        ShiftEntity shift = shiftMapper.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftId));

        if (!shift.getWorkerId().equals(workerId)) {
            throw new RuntimeException("Shift does not belong to this worker");
        }

        // 只能从已上岗/迟到/早退状态签退
        if (!ShiftStatus.ON_DUTY.name().equals(shift.getStatus()) && !ShiftStatus.LATE.name().equals(shift.getStatus()) && !ShiftStatus.EARLY_LEAVE.name().equals(shift.getStatus()) && !ShiftStatus.LATE_EARLY_LEAVE.name().equals(shift.getStatus())) {
            throw new RuntimeException("Cannot check out: shift status is " + shift.getStatus());
        }

        AttendanceRecordEntity record = attendanceRecordMapper.findByShiftId(shiftId)
                .orElseThrow(() -> new RuntimeException("No check-in record found for this shift"));

        LocalDateTime scheduledEnd = LocalDateTime.of(shift.getShiftDate(), shift.getEndTime());
        if (record.getCheckOutTime() != null && !record.getCheckOutTime().isBefore(scheduledEnd)) {
            throw new RuntimeException("Shift already checked out");
        }

        if (shift.getLocationLat() != null && lat != null) {
            int radius = shift.getLocationRadius() != null ? shift.getLocationRadius() : getDefaultRadius();
            double distance = haversine(
                    shift.getLocationLat().doubleValue(), shift.getLocationLng().doubleValue(),
                    lat.doubleValue(), lng.doubleValue());
            if (distance > radius) {
                throw new RuntimeException("Location out of range: " + (int) distance + "m (max: " + radius + "m)");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        long earlySeconds = now.isBefore(scheduledEnd) ? Duration.between(now, scheduledEnd).getSeconds() : 0;

        ShiftStatus newStatus = resolveCheckOutStatus(shift.getStatus(), earlySeconds);

        // 追加签到记录（支持多次签退）
        AttendanceCheckIn checkIn = new AttendanceCheckIn();
        checkIn.setShiftId(shiftId);
        checkIn.setWorkerId(workerId);
        checkIn.setCheckInTime(now);
        checkIn.setCheckInLat(lat);
        checkIn.setCheckInLng(lng);
        checkIn.setLateSeconds(0);
        checkIn.setEarlyLeaveSeconds((int) earlySeconds);
        checkIn.setCreatedAt(now);
        checkIn.setUpdatedAt(now);
        attendanceCheckInMapper.insert(checkIn);

        BigDecimal hours = calculateBillableHours(record.getCheckInTime(), now, shift.getShiftDate(), shift.getStartTime(), shift.getEndTime());

        BigDecimal scheduledPay = BigDecimal.ZERO;
        String rateType = shift.getSalaryType() != null ? shift.getSalaryType() : "HOURLY";
        BigDecimal rateAmount = shift.getSalaryAmount();
        if (rateAmount != null && rateAmount.compareTo(BigDecimal.ZERO) > 0) {
            if ("HOURLY".equals(rateType)) {
                scheduledPay = hours.multiply(rateAmount).setScale(2, RoundingMode.HALF_UP);
            } else if ("DAILY".equals(rateType) || "PER_SHIFT".equals(rateType)) {
                scheduledPay = rateAmount.setScale(2, RoundingMode.HALF_UP);
            }
        }

        record.setCheckOutTime(now);
        record.setCheckOutLat(lat);
        record.setCheckOutLng(lng);
        record.setTotalHours(hours);
        record.setScheduledPay(scheduledPay);
        record.setPayablePay(scheduledPay);
        record.setSettlementStatus("UNPAID");
        record.setCalculatedAt(now);
        record.setStatus(newStatus.name());
        record.setUpdatedAt(now);
        attendanceRecordMapper.update(record);

        shift.setStatus(newStatus.name());
        shift.setUpdatedAt(now);
        shiftMapper.update(shift);

        // Auto-settle if enabled and pay > 0
        boolean settled = false;
        if (scheduledPay.compareTo(BigDecimal.ZERO) > 0) {
            SystemConfig autoSettleConfig = systemConfigMapper.findByKey("auto_settle_attendance").orElse(null);
            if (autoSettleConfig != null && "true".equalsIgnoreCase(autoSettleConfig.getConfigValue())) {
                settled = autoSettle(record, shift, scheduledPay);
            }
        }

        AttendanceVO response = toAttendanceResponse(record, shift);
        response.setAutoSettled(settled);
        return response;
    }

    @Override
    public PageVO<AttendanceVO> getMyAttendance(Long workerId, Integer page, Integer pageSize) {
        int currentPage = page == null || page < 1 ? 1 : page;
        int currentPageSize = pageSize == null || pageSize < 1 ? 20 : pageSize;
        int offset = (currentPage - 1) * currentPageSize;
        long total = attendanceRecordMapper.countByWorkerId(workerId);
        List<AttendanceRecordEntity> records = attendanceRecordMapper.findByWorkerIdPage(workerId, offset, currentPageSize);
        if (records.isEmpty()) {
            return new PageVO<>(List.of(), total);
        }
        List<Long> shiftIds = records.stream().map(AttendanceRecordEntity::getShiftId).filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, ShiftEntity> shiftMap = Map.of();
        if (!shiftIds.isEmpty()) {
            shiftMap = shiftMapper.findByIds(shiftIds).stream()
                    .collect(Collectors.toMap(ShiftEntity::getId, s -> s));
        }
        List<Long> jobIds = records.stream().map(AttendanceRecordEntity::getJobId).filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, Job> jobMap = Map.of();
        if (!jobIds.isEmpty()) {
            jobMap = jobMapper.findByJobIds(jobIds).stream()
                    .collect(Collectors.toMap(Job::getId, j -> j));
        }
        Map<Long, ShiftEntity> finalShiftMap = shiftMap;
        Map<Long, Job> finalJobMap = jobMap;
        List<AttendanceVO> vos = records.stream()
                .map(record -> {
                    ShiftEntity shift = record.getShiftId() != null ? finalShiftMap.get(record.getShiftId()) : null;
                    Job job = record.getJobId() != null ? finalJobMap.get(record.getJobId()) : null;
                    return toAttendanceResponse(record, shift, job, null);
                })
                .collect(Collectors.toList());
        return new PageVO<>(vos, total);
    }

    public static BigDecimal calculateBillableHours(LocalDateTime checkInTime, LocalDateTime checkOutTime, LocalDate shiftDate, LocalTime startTime, LocalTime endTime) {
        LocalDateTime shiftStart = LocalDateTime.of(shiftDate, startTime);
        LocalDateTime shiftEnd = LocalDateTime.of(shiftDate, endTime);
        LocalDateTime effectiveStart = checkInTime.isBefore(shiftStart) ? shiftStart : checkInTime;
        LocalDateTime effectiveEnd = checkOutTime.isAfter(shiftEnd) ? shiftEnd : checkOutTime;
        long minutes = Duration.between(effectiveStart, effectiveEnd).toMinutes();
        if (minutes <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(minutes / 60.0).setScale(2, RoundingMode.HALF_UP);
    }

    private ShiftStatus resolveCheckOutStatus(String currentStatus, long earlySeconds) {
        if (earlySeconds > 0 && ShiftStatus.LATE.name().equals(currentStatus)) {
            return ShiftStatus.LATE_EARLY_LEAVE;
        }
        if (earlySeconds > 0) {
            return ShiftStatus.EARLY_LEAVE;
        }
        if (ShiftStatus.LATE_EARLY_LEAVE.name().equals(currentStatus)) {
            return ShiftStatus.LATE;
        }
        if (ShiftStatus.LATE.name().equals(currentStatus)) {
            return ShiftStatus.LATE;
        }
        return ShiftStatus.COMPLETED;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private int getDefaultRadius() {
        SystemConfig config = systemConfigMapper.findByKey("check_in_radius_meters").orElse(null);
        if (config != null) {
            try {
                return Integer.parseInt(config.getConfigValue());
            } catch (NumberFormatException ignored) {}
        }
        return 100;
    }


    private boolean autoSettle(AttendanceRecordEntity record, ShiftEntity shift, BigDecimal payAmount) {
        Long workerId = shift.getWorkerId();
        Long companyId = shift.getCompanyId();

        BigDecimal enterpriseBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM enterprise_balances WHERE company_id = ?",
                BigDecimal.class, companyId);

        if (enterpriseBalance == null || enterpriseBalance.compareTo(payAmount) < 0) {
            return false;
        }

        WorkerBalance wb = workerBalanceMapper.findByWorkerId(workerId);
        BigDecimal newBalance = payAmount;
        BigDecimal newTotalEarned = payAmount;
        if (wb != null) {
            newBalance = wb.getBalance().add(payAmount);
            newTotalEarned = wb.getTotalEarned().add(payAmount);
        }
        workerBalanceMapper.upsert(workerId, newBalance, newTotalEarned, wb != null ? wb.getTotalWithdrawn() : BigDecimal.ZERO);

        BalanceTransaction bt = new BalanceTransaction();
        bt.setWorkerId(workerId);
        bt.setAmount(payAmount);
        bt.setType("EARNINGS");
        bt.setRelatedAttendanceRecordId(record.getId());
        bt.setDescription("打卡自动结算");
        bt.setCreatedAt(LocalDateTime.now());
        balanceTransactionMapper.insert(bt);

        record.setSettlementStatus("PAID");
        record.setUpdatedAt(LocalDateTime.now());
        attendanceRecordMapper.update(record);

        jdbcTemplate.update(
                "UPDATE enterprise_balances SET balance = balance - ?, total_spent = total_spent + ?, updated_at = NOW() WHERE company_id = ?",
                payAmount, payAmount, companyId);

        jdbcTemplate.update(
                "INSERT INTO enterprise_balance_transactions (company_id, amount, type, description, created_at) VALUES (?, ?, 'SETTLEMENT', '打卡自动结算', NOW())",
                companyId, payAmount.negate());

        NotificationVO notification = new NotificationVO();
        notification.setRecipientId(workerId);
        notification.setRecipientType("WORKER");
        notification.setType("EARNINGS");
        notification.setCategory("income");
        notification.setTitle("收入到账");
        notification.setContent("您打卡的班次已自动结算，收入" + payAmount + "元已到账");
        notification.setStatus("SENT");
        notification.setRead(false);
        notification.setRelatedType("ATTENDANCE");
        notification.setRelatedId(record.getId());
        notification.setSentAt(LocalDateTime.now());
        notificationMapper.insert(notification);
        return true;
    }

    private AttendanceVO toAttendanceResponse(AttendanceRecordEntity record, ShiftEntity shift) {
        Job job = shift.getJobId() != null ? jobMapper.findByJobId(shift.getJobId()).orElse(null) : null;
        com.parttime.cservice.pojo.entity.Enterprise company = shift.getCompanyId() != null ? loadCompany(shift.getCompanyId()) : null;
        return toAttendanceResponse(record, shift, job, company);
    }

    private com.parttime.cservice.pojo.entity.Enterprise loadCompany(Long companyId) {
        // Simple lookup - could be optimized with batch loading if needed
        return null;
    }

    private AttendanceVO toAttendanceResponse(AttendanceRecordEntity record, ShiftEntity shift, Job job, com.parttime.cservice.pojo.entity.Enterprise company) {
        AttendanceVO resp = new AttendanceVO();
        resp.setAttendanceId(record.getId());
        resp.setShiftId(record.getShiftId());
        resp.setCheckInTime(record.getCheckInTime());
        resp.setCheckOutTime(record.getCheckOutTime());
        resp.setTotalHours(record.getTotalHours());
        resp.setScheduledPay(record.getScheduledPay());
        resp.setCalculatedAt(record.getCalculatedAt());
        resp.setStatus(record.getStatus());
        if (job != null) {
            resp.setJobTitle(job.getTitle());
            resp.setLocation(job.getAddress() != null ? job.getAddress() : job.getLocation());
        }
        if (company != null) {
            resp.setCompanyName(company.getCompanyName());
        }
        if (shift != null) {
            resp.setShiftDate(shift.getShiftDate() == null ? null : shift.getShiftDate().toString());
            resp.setStartTime(shift.getStartTime() == null ? null : shift.getStartTime().toString());
            resp.setEndTime(shift.getEndTime() == null ? null : shift.getEndTime().toString());
        }
        resp.setPayablePay(record.getPayablePay());
        resp.setSettlementStatus(record.getSettlementStatus());
        return resp;
    }
}
