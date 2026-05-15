package com.parttime.cservice.web.controller;

import com.parttime.cservice.core.dto.AttendanceResponse;
import com.parttime.cservice.core.dto.CheckInRequest;
import com.parttime.cservice.core.dto.WorkerShiftResponse;
import com.parttime.cservice.core.service.AttendanceService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AttendanceControllerTest {

    private MockMvc mockMvc;
    private AttendanceService attendanceService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        attendanceService = mock(AttendanceService.class);
        objectMapper = new ObjectMapper();

        AttendanceController controller = new AttendanceController(attendanceService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getMyShifts_shouldReturnShifts() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        WorkerShiftResponse shift = new WorkerShiftResponse();
        shift.setShiftId(100L);
        shift.setJobId(10L);
        shift.setJobTitle("Helper");
        shift.setStatus("SCHEDULED");

        when(attendanceService.getMyShifts(eq(1L), any(), any())).thenReturn(List.of(shift));

        mockMvc.perform(get("/api/schedule-shifts/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].jobTitle").value("Helper"));
    }

    @Test
    void getMyShifts_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/schedule-shifts/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkIn_shouldReturnOk() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        AttendanceResponse response = new AttendanceResponse();
        response.setAttendanceId(1L);
        response.setShiftId(100L);
        response.setStatus("CHECKED_IN");
        response.setCheckInTime(LocalDateTime.now());

        when(attendanceService.checkIn(eq(1L), eq(100L), any(), any())).thenReturn(response);

        CheckInRequest request = new CheckInRequest();
        request.setShiftId(100L);
        request.setLat(new java.math.BigDecimal("31.2304"));
        request.setLng(new java.math.BigDecimal("121.4737"));

        mockMvc.perform(post("/api/attendance/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"));
    }

    @Test
    void checkIn_shouldReturnBadRequestOnError() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        when(attendanceService.checkIn(eq(1L), eq(999L), any(), any()))
                .thenThrow(new RuntimeException("Shift not found"));

        CheckInRequest request = new CheckInRequest();
        request.setShiftId(999L);

        mockMvc.perform(post("/api/attendance/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Shift not found"));
    }

    @Test
    void checkOut_shouldReturnOk() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        AttendanceResponse response = new AttendanceResponse();
        response.setAttendanceId(1L);
        response.setShiftId(100L);
        response.setStatus("CHECKED_OUT");

        when(attendanceService.checkOut(eq(1L), eq(100L), any(), any())).thenReturn(response);

        CheckInRequest request = new CheckInRequest();
        request.setShiftId(100L);

        mockMvc.perform(post("/api/attendance/check-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"));
    }

    @Test
    void getMyAttendance_shouldReturnRecords() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        AttendanceResponse record = new AttendanceResponse();
        record.setAttendanceId(1L);
        record.setShiftId(100L);
        record.setStatus("CHECKED_IN");

        when(attendanceService.getMyAttendance(1L)).thenReturn(List.of(record));

        mockMvc.perform(get("/api/attendance/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("CHECKED_IN"));
    }
}
