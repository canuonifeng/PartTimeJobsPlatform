package com.parttime.cservice.service;

import com.parttime.cservice.mapper.AttendanceCorrectionMapper;
import com.parttime.cservice.mapper.AttendanceRecordMapper;
import com.parttime.cservice.mapper.ShiftMapper;
import com.parttime.cservice.service.impl.AttendanceServiceImpl;
import com.parttime.cservice.service.impl.WorkerShiftVOConverter;
import com.parttime.cservice.pojo.vo.AttendanceVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.WorkerShiftVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttendanceServiceTest {

    private AttendanceServiceImpl attendanceService;
    private ShiftMapper shiftMapper;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceServiceImpl();
        AttendanceRecordMapper attendanceRecordMapper = InMemoryMappers.createAttendanceRecordMapper();
        AttendanceCorrectionMapper correctionMapper = InMemoryMappers.createAttendanceCorrectionMapper();
        WorkerShiftVOConverter converter = new WorkerShiftVOConverter();
        ReflectionTestUtils.setField(converter, "attendanceRecordMapper", attendanceRecordMapper);
        ReflectionTestUtils.setField(converter, "correctionMapper", correctionMapper);
        shiftMapper = InMemoryMappers.createShiftMapper();
        ReflectionTestUtils.setField(attendanceService, "shiftMapper", shiftMapper);
        ReflectionTestUtils.setField(attendanceService, "jobMapper", InMemoryMappers.createJobMapper());
        ReflectionTestUtils.setField(attendanceService, "attendanceRecordMapper", attendanceRecordMapper);
        ReflectionTestUtils.setField(attendanceService, "correctionMapper", correctionMapper);
        ReflectionTestUtils.setField(attendanceService, "workerShiftVOConverter", converter);
        ReflectionTestUtils.setField(attendanceService, "attendanceCheckInMapper", InMemoryMappers.createAttendanceCheckInMapper());
        ReflectionTestUtils.setField(attendanceService, "systemConfigMapper", InMemoryMappers.createSystemConfigMapper());
        ReflectionTestUtils.setField(attendanceService, "workerBalanceMapper", InMemoryMappers.createWorkerBalanceMapper());
        ReflectionTestUtils.setField(attendanceService, "balanceTransactionMapper", InMemoryMappers.createBalanceTransactionMapper());
        ReflectionTestUtils.setField(attendanceService, "notificationMapper", InMemoryMappers.createNotificationMapper());
        ReflectionTestUtils.setField(attendanceService, "jdbcTemplate", org.mockito.Mockito.mock(JdbcTemplate.class));
    }

    @Test
    void addShift_shouldStoreShift() {
        attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                new BigDecimal("31.2304"), new BigDecimal("121.4737"), 500, "Office A");

        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L, null, null, null, null);
        assertThat(shifts).hasSize(1);
        assertThat(shifts.get(0).getJobId()).isEqualTo(10L);
    }

    @Test
    void getMyShifts_shouldFilterByWorkerId() {
        attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null);
        attendanceService.addShift(11L, 2L,
                LocalDate.of(2026, 6, 2), LocalTime.of(10, 0), LocalTime.of(17, 0),
                null, null, null, null);

        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L, null, null, null, null);
        assertThat(shifts).hasSize(1);
    }

    @Test
    void getMyShifts_shouldMarkPastUnattendedShiftsAbsent() {
        attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 1, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null);

        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L, null, null, null, null);

        assertThat(shifts).hasSize(1);
        assertThat(shifts.get(0).getStatus()).isEqualTo("ABSENT");
    }

    @Test
    void getMyShifts_shouldFilterByDateRange() {
        attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null);
        attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 7, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null);

        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30), null, null);
        assertThat(shifts).hasSize(1);
        assertThat(shifts.get(0).getDate()).isEqualTo(LocalDate.of(2026, 6, 1));
    }

    @Test
    void checkIn_shouldCreateAttendanceRecord() {
        Long shiftId = attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null).getId();

        AttendanceVO response = attendanceService.checkIn(1L, shiftId, null, null);
        List<WorkerShiftVO> updatedShifts = attendanceService.getMyShifts(1L, null, null, null, null);

        assertThat(response.getStatus()).isEqualTo("LATE");
        assertThat(response.getCheckInTime()).isNotNull();
        assertThat(updatedShifts.get(0).getStatus()).isEqualTo("LATE");
        assertThat(updatedShifts.get(0).getCheckInTime()).isNotNull();
        assertThat(updatedShifts.get(0).getCheckOutTime()).isNull();
    }

    @Test
    void checkIn_shouldThrowWhenShiftNotFound() {
        assertThatThrownBy(() -> attendanceService.checkIn(1L, 999L, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void checkIn_shouldThrowWhenNotOwnShift() {
        Long shiftId = attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null).getId();

        assertThatThrownBy(() -> attendanceService.checkIn(2L, shiftId, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void checkIn_shouldThrowWhenDuplicate() {
        Long shiftId = attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null).getId();

        attendanceService.checkIn(1L, shiftId, null, null);

        assertThatThrownBy(() -> attendanceService.checkIn(1L, shiftId, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot check in");
    }

    @Test
    void checkIn_shouldRejectWhenLocationOutOfRange() {
        Long shiftId = attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                new BigDecimal("31.2304"), new BigDecimal("121.4737"), 10, null).getId();

        assertThatThrownBy(() -> attendanceService.checkIn(1L, shiftId,
                new BigDecimal("31.3000"), new BigDecimal("121.5000")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("out of range");
    }

    @Test
    void calculateBillableHours_shouldClampToShiftWindow() {
        BigDecimal hours = AttendanceServiceImpl.calculateBillableHours(
                LocalDateTime.of(2026, 6, 1, 8, 30),
                LocalDateTime.of(2026, 6, 1, 18, 30),
                LocalDate.of(2026, 6, 1),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0));

        assertThat(hours).isEqualByComparingTo(new BigDecimal("9.00"));
    }

    @Test
    void calculateBillableHours_shouldUseActualTimesInsideShiftWindow() {
        BigDecimal hours = AttendanceServiceImpl.calculateBillableHours(
                LocalDateTime.of(2026, 6, 1, 10, 15),
                LocalDateTime.of(2026, 6, 1, 17, 45),
                LocalDate.of(2026, 6, 1),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0));

        assertThat(hours).isEqualByComparingTo(new BigDecimal("7.50"));
    }

    @Test
    void calculateBillableHours_shouldReturnZeroWhenEffectiveEndBeforeStart() {
        BigDecimal hours = AttendanceServiceImpl.calculateBillableHours(
                LocalDateTime.of(2026, 6, 1, 19, 0),
                LocalDateTime.of(2026, 6, 1, 19, 30),
                LocalDate.of(2026, 6, 1),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0));

        assertThat(hours).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void checkOut_shouldUpdateRecord() {
        Long shiftId = attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null).getId();

        attendanceService.checkIn(1L, shiftId, null, null);
        shiftMapper.findById(shiftId).orElseThrow().setStatus("ON_DUTY");
        AttendanceVO response = attendanceService.checkOut(1L, shiftId, null, null);
        List<WorkerShiftVO> updatedShifts = attendanceService.getMyShifts(1L, null, null, null, null);

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getCheckOutTime()).isNotNull();
        assertThat(response.getTotalHours()).isNotNull();
        assertThat(updatedShifts.get(0).getStatus()).isEqualTo("COMPLETED");
        assertThat(updatedShifts.get(0).getCheckInTime()).isNotNull();
        assertThat(updatedShifts.get(0).getCheckOutTime()).isNotNull();
    }

    @Test
    void checkOut_shouldKeepLateStatusWhenLateShiftCompletes() {
        Long shiftId = attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null).getId();

        attendanceService.checkIn(1L, shiftId, null, null);
        AttendanceVO response = attendanceService.checkOut(1L, shiftId, null, null);
        List<WorkerShiftVO> updatedShifts = attendanceService.getMyShifts(1L, null, null, null, null);

        assertThat(response.getStatus()).isEqualTo("LATE");
        assertThat(response.getCheckOutTime()).isNotNull();
        assertThat(updatedShifts.get(0).getStatus()).isEqualTo("LATE");
        assertThat(updatedShifts.get(0).getCheckOutTime()).isNotNull();
    }

    @Test
    void checkOut_shouldMarkLateEarlyLeaveWhenLateShiftLeavesEarly() {
        LocalDateTime now = LocalDateTime.now();
        Long shiftId = attendanceService.addShift(10L, 1L,
                now.toLocalDate(), now.toLocalTime().minusMinutes(30), now.toLocalTime().plusMinutes(30),
                null, null, null, null).getId();

        attendanceService.checkIn(1L, shiftId, null, null);
        AttendanceVO response = attendanceService.checkOut(1L, shiftId, null, null);
        List<WorkerShiftVO> updatedShifts = attendanceService.getMyShifts(1L, null, null, null, null);

        assertThat(response.getStatus()).isEqualTo("LATE_EARLY_LEAVE");
        assertThat(updatedShifts.get(0).getStatus()).isEqualTo("LATE_EARLY_LEAVE");
    }

    @Test
    void checkOut_shouldThrowWhenLateShiftAlreadyCheckedOutAfterShiftEnd() {
        Long shiftId = attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null).getId();

        attendanceService.checkIn(1L, shiftId, null, null);
        attendanceService.checkOut(1L, shiftId, null, null);

        assertThatThrownBy(() -> attendanceService.checkOut(1L, shiftId, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already checked out");
    }

    @Test
    void checkOut_shouldThrowWhenNotCheckedIn() {
        Long shiftId = attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null).getId();

        assertThatThrownBy(() -> attendanceService.checkOut(1L, shiftId, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot check out");
    }

    @Test
    void getMyAttendance_shouldReturnRecords() {
        Long shiftId = attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null).getId();

        attendanceService.checkIn(1L, shiftId, null, null);

        PageVO<AttendanceVO> page = attendanceService.getMyAttendance(1L, 1, 20);
        assertThat(page.getRecords()).hasSize(1);
        assertThat(page.getRecords().get(0).getStatus()).isEqualTo("LATE");
        assertThat(page.getTotal()).isEqualTo(1);
    }

    @Test
    void checkOut_autoSettleEnabled_shouldSettleAndReturnAutoSettled() {
        com.parttime.cservice.pojo.entity.AttendanceRecordEntity record = new com.parttime.cservice.pojo.entity.AttendanceRecordEntity();
        record.setId(1L);
        record.setSettlementStatus("UNPAID");

        com.parttime.cservice.pojo.entity.ShiftEntity shift = new com.parttime.cservice.pojo.entity.ShiftEntity();
        shift.setId(1L);
        shift.setWorkerId(1L);
        shift.setCompanyId(100L);

        com.parttime.cservice.pojo.entity.SystemConfig config = new com.parttime.cservice.pojo.entity.SystemConfig();
        config.setConfigKey("auto_settle_attendance");
        config.setConfigValue("true");
        com.parttime.cservice.mapper.SystemConfigMapper realMapper = InMemoryMappers.createSystemConfigMapper();
        ((InMemoryMappers.TestSystemConfigMapper) realMapper).put("auto_settle_attendance", config);

        ReflectionTestUtils.setField(attendanceService, "systemConfigMapper", realMapper);
        ReflectionTestUtils.setField(attendanceService, "workerBalanceMapper", InMemoryMappers.createWorkerBalanceMapper());
        ReflectionTestUtils.setField(attendanceService, "balanceTransactionMapper", InMemoryMappers.createBalanceTransactionMapper());
        ReflectionTestUtils.setField(attendanceService, "notificationMapper", InMemoryMappers.createNotificationMapper());

        JdbcTemplate testJdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
        org.mockito.Mockito.lenient().when(testJdbcTemplate.queryForObject(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(BigDecimal.class),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(new BigDecimal("10000.00"));
        ReflectionTestUtils.setField(attendanceService, "jdbcTemplate", testJdbcTemplate);

        boolean result = ReflectionTestUtils.invokeMethod(attendanceService, "autoSettle", record, shift, new BigDecimal("100"));

        assertThat(result).isTrue();
        assertThat(record.getSettlementStatus()).isEqualTo("PAID");
    }

    @Test
    void checkOut_autoSettleDisabled_shouldLeaveUnpaid() {
        Long shiftId = attendanceService.addShift(10L, 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null).getId();

        com.parttime.cservice.mapper.SystemConfigMapper realMapper = InMemoryMappers.createSystemConfigMapper();
        ReflectionTestUtils.setField(attendanceService, "systemConfigMapper", realMapper);

        attendanceService.checkIn(1L, shiftId, null, null);
        AttendanceVO response = attendanceService.checkOut(1L, shiftId, null, null);

        assertThat(response.getAutoSettled()).isFalse();
        assertThat(response.getSettlementStatus()).isEqualTo("UNPAID");
    }

    @Test
    void checkOut_autoSettleInsufficientBalance_shouldFallbackToUnpaid() {
        com.parttime.cservice.pojo.entity.AttendanceRecordEntity record = new com.parttime.cservice.pojo.entity.AttendanceRecordEntity();
        record.setId(1L);
        record.setSettlementStatus("UNPAID");

        com.parttime.cservice.pojo.entity.ShiftEntity shift = new com.parttime.cservice.pojo.entity.ShiftEntity();
        shift.setId(1L);
        shift.setWorkerId(1L);
        shift.setCompanyId(100L);

        com.parttime.cservice.pojo.entity.SystemConfig config = new com.parttime.cservice.pojo.entity.SystemConfig();
        config.setConfigKey("auto_settle_attendance");
        config.setConfigValue("true");
        com.parttime.cservice.mapper.SystemConfigMapper realMapper = InMemoryMappers.createSystemConfigMapper();
        ((InMemoryMappers.TestSystemConfigMapper) realMapper).put("auto_settle_attendance", config);
        ReflectionTestUtils.setField(attendanceService, "systemConfigMapper", realMapper);

        JdbcTemplate testJdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
        org.mockito.Mockito.lenient().when(testJdbcTemplate.queryForObject(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(BigDecimal.class),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(BigDecimal.ZERO);
        ReflectionTestUtils.setField(attendanceService, "jdbcTemplate", testJdbcTemplate);

        boolean result = ReflectionTestUtils.invokeMethod(attendanceService, "autoSettle", record, shift, new BigDecimal("100"));

        assertThat(result).isFalse();
        assertThat(record.getSettlementStatus()).isEqualTo("UNPAID");
    }
}
