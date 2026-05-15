package com.parttime.enterprise.service;

import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.ScheduleTemplateMapper;
import com.parttime.enterprise.pojo.cmd.ScheduleShiftCmd;
import com.parttime.enterprise.pojo.cmd.ScheduleTemplateCmd;
import com.parttime.enterprise.pojo.cmd.ScheduleTemplateSlotCmd;
import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.entity.ScheduleTemplate;
import com.parttime.enterprise.pojo.entity.ScheduleTemplateSlot;
import com.parttime.enterprise.pojo.vo.AttendanceReportVO;
import com.parttime.enterprise.pojo.vo.ScheduleShiftVO;
import com.parttime.enterprise.pojo.vo.ScheduleTemplateVO;
import com.parttime.enterprise.service.impl.ScheduleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private ScheduleTemplateMapper templateMapper;

    @Mock
    private ScheduleShiftMapper shiftMapper;

    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;

    @Captor
    private ArgumentCaptor<ScheduleTemplate> templateCaptor;

    @Captor
    private ArgumentCaptor<ScheduleTemplateSlot> slotCaptor;

    @Captor
    private ArgumentCaptor<ScheduleShift> shiftCaptor;

    private ScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        scheduleService = new ScheduleServiceImpl(templateMapper, shiftMapper, attendanceRecordMapper);
    }

    @Test
    void createTemplate_shouldCreateAndReturnResponse() {
        ScheduleTemplateSlotCmd slotReq = new ScheduleTemplateSlotCmd();
        slotReq.setDayOfWeek(1);
        slotReq.setStartTime(LocalTime.of(9, 0));
        slotReq.setEndTime(LocalTime.of(18, 0));
        slotReq.setMaxWorkers(5);
        slotReq.setLocationName("Office A");

        ScheduleTemplateCmd request = new ScheduleTemplateCmd();
        request.setCompanyId(1L);
        request.setName("Morning Shift");
        request.setDescription("Weekday morning shift");
        request.setSlots(List.of(slotReq));

        doAnswer(invocation -> {
            ScheduleTemplate t = invocation.getArgument(0);
            t.setId(100L);
            return 1;
        }).when(templateMapper).insert(any(ScheduleTemplate.class));

        ScheduleTemplateVO response = scheduleService.createTemplate(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo("Morning Shift");
        assertThat(response.getCompanyId()).isEqualTo(1L);

        verify(templateMapper).insert(templateCaptor.capture());
        assertThat(templateCaptor.getValue().getName()).isEqualTo("Morning Shift");
        assertThat(templateCaptor.getValue().getCompanyId()).isEqualTo(1L);

        verify(templateMapper).insertSlot(slotCaptor.capture());
        assertThat(slotCaptor.getValue().getDayOfWeek()).isEqualTo(1);
        assertThat(slotCaptor.getValue().getStartTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void getTemplateById_shouldReturnFullResponseWithSlots() {
        ScheduleTemplate template = new ScheduleTemplate();
        template.setId(100L);
        template.setCompanyId(1L);
        template.setName("Morning Shift");

        ScheduleTemplateSlot slot = new ScheduleTemplateSlot();
        slot.setId(1L);
        slot.setTemplateId(100L);
        slot.setDayOfWeek(1);
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(18, 0));

        when(templateMapper.findById(100L)).thenReturn(Optional.of(template));
        when(templateMapper.findSlotsByTemplateId(100L)).thenReturn(List.of(slot));

        ScheduleTemplateVO response = scheduleService.getTemplateById(100L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getName()).isEqualTo("Morning Shift");
        assertThat(response.getSlots()).hasSize(1);
        assertThat(response.getSlots().get(0).getDayOfWeek()).isEqualTo(1);
    }

    @Test
    void getTemplateById_shouldThrowWhenNotFound() {
        when(templateMapper.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.getTemplateById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getTemplatesByCompany_shouldReturnList() {
        ScheduleTemplate t1 = new ScheduleTemplate();
        t1.setId(1L);
        t1.setCompanyId(1L);
        t1.setName("Morning");

        ScheduleTemplate t2 = new ScheduleTemplate();
        t2.setId(2L);
        t2.setCompanyId(1L);
        t2.setName("Evening");

        when(templateMapper.findByCompanyId(1L)).thenReturn(List.of(t1, t2));

        List<ScheduleTemplateVO> responses = scheduleService.getTemplatesByCompany(1L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Morning");
        assertThat(responses.get(1).getName()).isEqualTo("Evening");
    }

    @Test
    void updateTemplate_shouldModifyFields() {
        ScheduleTemplate existing = new ScheduleTemplate();
        existing.setId(100L);
        existing.setCompanyId(1L);
        existing.setName("Old Name");

        ScheduleTemplateSlotCmd slotReq = new ScheduleTemplateSlotCmd();
        slotReq.setDayOfWeek(2);
        slotReq.setStartTime(LocalTime.of(10, 0));
        slotReq.setEndTime(LocalTime.of(19, 0));

        ScheduleTemplateCmd request = new ScheduleTemplateCmd();
        request.setName("New Name");
        request.setDescription("Updated description");
        request.setSlots(List.of(slotReq));

        when(templateMapper.findById(100L)).thenReturn(Optional.of(existing));

        ScheduleTemplateVO response = scheduleService.updateTemplate(100L, request);

        assertThat(response.getName()).isEqualTo("New Name");
        verify(templateMapper).update(existing);
        verify(templateMapper).deleteSlotsByTemplateId(100L);
        verify(templateMapper).insertSlot(any(ScheduleTemplateSlot.class));
    }

    @Test
    void deleteTemplate_shouldCallRepository() {
        scheduleService.deleteTemplate(100L);
        verify(templateMapper).delete(100L);
    }

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

        ScheduleShiftVO response = scheduleService.assignShift(request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getJobId()).isEqualTo(10L);
        assertThat(response.getWorkerId()).isEqualTo(20L);
        assertThat(response.getStatus()).isEqualTo("SCHEDULED");

        verify(shiftMapper).insert(shiftCaptor.capture());
        assertThat(shiftCaptor.getValue().getStatus()).isEqualTo("SCHEDULED");
    }

    @Test
    void getShifts_shouldFilterByJobIdAndDate() {
        ScheduleShift shift = new ScheduleShift();
        shift.setId(1L);
        shift.setJobId(10L);
        shift.setWorkerId(20L);

        when(shiftMapper.findByJobIdAndDate(10L, LocalDate.of(2026, 6, 1)))
                .thenReturn(List.of(shift));

        List<ScheduleShiftVO> responses = scheduleService.getShifts(10L, null, LocalDate.of(2026, 6, 1));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getJobId()).isEqualTo(10L);
    }

    @Test
    void getShifts_shouldFilterByWorkerId() {
        ScheduleShift shift = new ScheduleShift();
        shift.setId(1L);
        shift.setWorkerId(20L);

        when(shiftMapper.findByWorkerId(20L)).thenReturn(List.of(shift));

        List<ScheduleShiftVO> responses = scheduleService.getShifts(null, 20L, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getWorkerId()).isEqualTo(20L);
    }

    @Test
    void getShifts_shouldReturnEmptyWithoutFilters() {
        List<ScheduleShiftVO> responses = scheduleService.getShifts(null, null, null);
        assertThat(responses).isEmpty();
    }

    @Test
    void removeShift_shouldCallRepository() {
        scheduleService.removeShift(99L);
        verify(shiftMapper).delete(99L);
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
