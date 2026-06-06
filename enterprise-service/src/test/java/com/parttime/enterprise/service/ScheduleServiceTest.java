package com.parttime.enterprise.service;

import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.CompanyWorkerMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.cmd.ScheduleShiftCmd;
import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.vo.AttendanceReportVO;
import com.parttime.enterprise.pojo.vo.AttendanceRecordVO;
import com.parttime.enterprise.pojo.vo.ScheduleShiftVO;
import com.parttime.enterprise.service.impl.ScheduleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleShiftMapper shiftMapper;

    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;

    @Mock
    private JobMapper jobMapper;

    @Mock
    private CompanyWorkerMapper companyWorkerMapper;

    @Mock
    private WorkerSyncMapper workerSyncMapper;

    @Captor
    private ArgumentCaptor<ScheduleShift> shiftCaptor;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    @Test
    void assignShift_shouldCreateAndReturnResponse() {
        ScheduleShiftCmd request = new ScheduleShiftCmd();
        request.setJobId(10L);
        request.setWorkerId(20L);
        request.setShiftDate(LocalDate.of(2026, 6, 1));
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(18, 0));
        request.setLocationName("Warehouse A");

        doAnswer(invocation -> {
            ScheduleShift s = invocation.getArgument(0);
            s.setId(99L);
            return 1;
        }).when(shiftMapper).insert(any(ScheduleShift.class));
        ScheduleShift stored = new ScheduleShift();
        stored.setId(99L);
        stored.setJobId(10L);
        stored.setApplicationId(88L);
        stored.setWorkerId(20L);
        stored.setStatus("SCHEDULED");
        stored.setSalaryType("HOURLY");
        stored.setSalaryAmount(new BigDecimal("25.50"));
        stored.setSalaryCurrency("CNY");
        stored.setCreatedAt(java.time.LocalDateTime.of(2026, 6, 1, 9, 0));
        stored.setUpdatedAt(java.time.LocalDateTime.of(2026, 6, 1, 9, 0));
        when(shiftMapper.findById(99L)).thenReturn(Optional.of(stored));
        Job job = new Job();
        job.setId(10L);
        job.setCompanyId(30L);
        when(jobMapper.findById(10L)).thenReturn(Optional.of(job));
        when(workerSyncMapper.findWorkerNameById(20L)).thenReturn("TestWorker");

        ScheduleShiftVO response = scheduleService.assignShift(request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getJobId()).isEqualTo(10L);
        assertThat(response.getWorkerId()).isEqualTo(20L);
        assertThat(response.getApplicationId()).isEqualTo(88L);
        assertThat(response.getSalaryType()).isEqualTo("HOURLY");
        assertThat(response.getSalaryAmount()).isEqualByComparingTo(new BigDecimal("25.50"));
        assertThat(response.getSalaryCurrency()).isEqualTo("CNY");
        assertThat(response.getCreatedAt()).isEqualTo(java.time.LocalDateTime.of(2026, 6, 1, 9, 0));
        assertThat(response.getUpdatedAt()).isEqualTo(java.time.LocalDateTime.of(2026, 6, 1, 9, 0));
        assertThat(response.getStatus()).isEqualTo("SCHEDULED");

        verify(shiftMapper).insert(shiftCaptor.capture());
        assertThat(shiftCaptor.getValue().getStatus()).isEqualTo("SCHEDULED");
    }

    @Test
    void attendanceRecordVO_shouldExposePaySnapshotFields() {
        AttendanceRecordVO response = new AttendanceRecordVO();
        response.setScheduledPay(new BigDecimal("123.45"));
        response.setCalculatedAt(LocalDateTime.of(2026, 6, 1, 18, 30));

        assertThat(response.getScheduledPay()).isEqualByComparingTo(new BigDecimal("123.45"));
        assertThat(response.getCalculatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 1, 18, 30));
    }

    @Test
    void getShifts_shouldFilterByJobIdAndDate() {
        ScheduleShift shift = new ScheduleShift();
        shift.setId(1L);
        shift.setJobId(10L);
        shift.setWorkerId(20L);

        when(shiftMapper.findByJobIdAndDate(10L, LocalDate.of(2026, 6, 1)))
                .thenReturn(List.of(shift));
        when(jobMapper.findById(10L)).thenReturn(Optional.empty());
        when(workerSyncMapper.findWorkerNameById(20L)).thenReturn("TestWorker");

        var result = scheduleService.getShifts(10L, null, LocalDate.of(2026, 6, 1), 1, 20);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getJobId()).isEqualTo(10L);
    }

    @Test
    void getShifts_shouldFilterByWorkerId() {
        ScheduleShift shift = new ScheduleShift();
        shift.setId(1L);
        shift.setWorkerId(20L);

        when(shiftMapper.findByWorkerId(20L)).thenReturn(List.of(shift));
        when(workerSyncMapper.findWorkerNameById(20L)).thenReturn("TestWorker");

        var result = scheduleService.getShifts(null, 20L, null, 1, 20);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getWorkerId()).isEqualTo(20L);
    }

    @Test
    void getShifts_shouldReturnEmptyWithoutFilters() {
        when(shiftMapper.findAll()).thenReturn(List.of());

        var result = scheduleService.getShifts(null, null, null, 1, 20);

        assertThat(result.getTotal()).isEqualTo(0);
        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    void removeShift_shouldCallRepository() {
        scheduleService.removeShift(99L);
        verify(shiftMapper).cancelShift(99L);
    }

    @Test
    void getAttendanceReport_shouldReturnReportForShift() {
        ScheduleShift shift = new ScheduleShift();
        shift.setId(1L);
        shift.setJobId(10L);
        shift.setWorkerId(20L);
        shift.setShiftDate(LocalDate.of(2026, 6, 1));
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndTime(LocalTime.of(18, 0));
        shift.setStatus("CHECKED_OUT");

        AttendanceRecord record = new AttendanceRecord();
        record.setShiftId(1L);
        record.setCheckInTime(java.time.LocalDateTime.of(2026, 6, 1, 9, 0));
        record.setCheckOutTime(java.time.LocalDateTime.of(2026, 6, 1, 18, 0));
        record.setTotalHours(new BigDecimal("9.00"));
        record.setStatus("CHECKED_OUT");

        when(shiftMapper.findById(1L)).thenReturn(Optional.of(shift));
        when(attendanceRecordMapper.findByShiftIds(List.of(1L))).thenReturn(List.of(record));

        List<AttendanceReportVO> reports = scheduleService.getAttendanceReport(null, 1L, null);

        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).getShiftId()).isEqualTo(1L);
        assertThat(reports.get(0).getShiftStatus()).isEqualTo("CHECKED_OUT");
        assertThat(reports.get(0).getAttendanceStatus()).isEqualTo("CHECKED_OUT");
        assertThat(reports.get(0).getTotalHours()).isEqualByComparingTo(new BigDecimal("9.00"));
    }

    @Test
    void getAttendanceReport_shouldReturnNoRecordWhenNoAttendance() {
        ScheduleShift shift = new ScheduleShift();
        shift.setId(1L);
        shift.setJobId(10L);
        shift.setWorkerId(20L);
        shift.setShiftDate(LocalDate.of(2026, 6, 1));

        when(shiftMapper.findById(1L)).thenReturn(Optional.of(shift));
        when(attendanceRecordMapper.findByShiftIds(List.of(1L))).thenReturn(List.of());

        List<AttendanceReportVO> reports = scheduleService.getAttendanceReport(null, 1L, null);

        assertThat(reports).hasSize(1);
        assertThat(reports.get(0).getAttendanceStatus()).isEqualTo("NO_RECORD");
    }

    @Test
    void getAttendanceReport_shouldThrowWhenNoShiftIdOrJobId() {
        assertThatThrownBy(() -> scheduleService.getAttendanceReport(null, null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Either jobId or shiftId");
    }
}
