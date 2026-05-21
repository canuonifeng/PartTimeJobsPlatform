package com.parttime.cservice.service;

import com.parttime.cservice.service.impl.AttendanceServiceImpl;
import com.parttime.cservice.pojo.vo.AttendanceVO;
import com.parttime.cservice.pojo.vo.WorkerShiftVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttendanceServiceTest {

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(attendanceService, "shiftMapper", InMemoryMappers.createShiftMapper());
        ReflectionTestUtils.setField(attendanceService, "attendanceRecordMapper", InMemoryMappers.createAttendanceRecordMapper());
    }

    @Test
    void addShift_shouldStoreShift() {
        attendanceService.addShift(10L, "Helper", "Shanghai", 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                new BigDecimal("31.2304"), new BigDecimal("121.4737"), 500, "Office A");

        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L, null, null);
        assertThat(shifts).hasSize(1);
        assertThat(shifts.get(0).getJobId()).isEqualTo(10L);
        assertThat(shifts.get(0).getJobTitle()).isEqualTo("Helper");
    }

    @Test
    void getMyShifts_shouldFilterByWorkerId() {
        attendanceService.addShift(10L, "Job1", "Loc1", 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null);
        attendanceService.addShift(11L, "Job2", "Loc2", 2L,
                LocalDate.of(2026, 6, 2), LocalTime.of(10, 0), LocalTime.of(17, 0),
                null, null, null, null);

        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L, null, null);
        assertThat(shifts).hasSize(1);
    }

    @Test
    void getMyShifts_shouldFilterByDateRange() {
        attendanceService.addShift(10L, "Job1", "Loc1", 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null);
        attendanceService.addShift(10L, "Job2", "Loc2", 1L,
                LocalDate.of(2026, 7, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null);

        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30));
        assertThat(shifts).hasSize(1);
        assertThat(shifts.get(0).getDate()).isEqualTo(LocalDate.of(2026, 6, 1));
    }

    @Test
    void checkIn_shouldCreateAttendanceRecord() {
        attendanceService.addShift(10L, "Helper", "Shanghai", 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null);

        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L, null, null);
        Long shiftId = shifts.get(0).getId();

        AttendanceVO response = attendanceService.checkIn(1L, shiftId, null, null);
        assertThat(response.getStatus()).isEqualTo("CHECKED_IN");
        assertThat(response.getCheckInTime()).isNotNull();
    }

    @Test
    void checkIn_shouldThrowWhenShiftNotFound() {
        assertThatThrownBy(() -> attendanceService.checkIn(1L, 999L, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void checkIn_shouldThrowWhenNotOwnShift() {
        attendanceService.addShift(10L, "Helper", "Shanghai", 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null);

        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L, null, null);
        Long shiftId = shifts.get(0).getId();

        assertThatThrownBy(() -> attendanceService.checkIn(2L, shiftId, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void checkIn_shouldThrowWhenDuplicate() {
        attendanceService.addShift(10L, "Helper", "Shanghai", 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null);

        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L, null, null);
        Long shiftId = shifts.get(0).getId();

        attendanceService.checkIn(1L, shiftId, null, null);

        assertThatThrownBy(() -> attendanceService.checkIn(1L, shiftId, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot check in");
    }

    @Test
    void checkIn_shouldRejectWhenLocationOutOfRange() {
        attendanceService.addShift(10L, "Helper", "Shanghai", 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                new BigDecimal("31.2304"), new BigDecimal("121.4737"), 10, null);

        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L, null, null);
        Long shiftId = shifts.get(0).getId();

        assertThatThrownBy(() -> attendanceService.checkIn(1L, shiftId,
                new BigDecimal("31.3000"), new BigDecimal("121.5000")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("out of range");
    }

    @Test
    void checkOut_shouldUpdateRecord() {
        attendanceService.addShift(10L, "Helper", "Shanghai", 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null);

        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L, null, null);
        Long shiftId = shifts.get(0).getId();

        attendanceService.checkIn(1L, shiftId, null, null);
        AttendanceVO response = attendanceService.checkOut(1L, shiftId, null, null);

        assertThat(response.getStatus()).isEqualTo("CHECKED_OUT");
        assertThat(response.getCheckOutTime()).isNotNull();
        assertThat(response.getTotalHours()).isNotNull();
    }

    @Test
    void checkOut_shouldThrowWhenNotCheckedIn() {
        attendanceService.addShift(10L, "Helper", "Shanghai", 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null);

        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L, null, null);
        Long shiftId = shifts.get(0).getId();

        assertThatThrownBy(() -> attendanceService.checkOut(1L, shiftId, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No check-in record");
    }

    @Test
    void getMyAttendance_shouldReturnRecords() {
        attendanceService.addShift(10L, "Helper", "Shanghai", 1L,
                LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
                null, null, null, null);

        List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L, null, null);
        Long shiftId = shifts.get(0).getId();

        attendanceService.checkIn(1L, shiftId, null, null);

        List<AttendanceVO> records = attendanceService.getMyAttendance(1L);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getStatus()).isEqualTo("CHECKED_IN");
    }
}
