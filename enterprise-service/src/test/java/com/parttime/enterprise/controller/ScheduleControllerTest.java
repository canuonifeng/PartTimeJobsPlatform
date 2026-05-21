package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.JwtTokenProvider;
import com.parttime.enterprise.config.SecurityConfig;
import com.parttime.enterprise.pojo.vo.AttendanceReportVO;
import com.parttime.enterprise.pojo.vo.ScheduleShiftVO;
import com.parttime.enterprise.service.ScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScheduleController.class)
@Import(SecurityConfig.class)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignShift_shouldReturnCreated() throws Exception {
        ScheduleShiftVO response = new ScheduleShiftVO();
        response.setId(99L);
        response.setJobId(10L);
        response.setWorkerId(20L);
        response.setStatus("SCHEDULED");

        when(scheduleService.assignShift(any())).thenReturn(response);

        String json = """
                {
                    "jobId": 10,
                    "workerId": 20,
                    "shiftDate": "2026-06-01",
                    "startTime": "09:00:00",
                    "endTime": "18:00:00"
                }
                """;

        mockMvc.perform(post("/api/schedule-shifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").value(10))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getShifts_shouldReturnList() throws Exception {
        ScheduleShiftVO shift = new ScheduleShiftVO();
        shift.setId(1L);
        shift.setJobId(10L);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("records", List.of(shift));
        result.put("total", 1);
        when(scheduleService.getShifts(10L, null, null, 1, 20)).thenReturn(result);

        mockMvc.perform(get("/api/schedule-shifts")
                        .param("jobId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeShift_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/schedule-shifts").param("id", "99"))
                .andExpect(status().isNoContent());
        verify(scheduleService).removeShift(99L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAttendanceReport_shouldReturnList() throws Exception {
        AttendanceReportVO report = new AttendanceReportVO();
        report.setShiftId(1L);
        report.setWorkerId(20L);
        report.setAttendanceStatus("CHECKED_OUT");

        when(scheduleService.getAttendanceReport(10L, null, null)).thenReturn(List.of(report));

        mockMvc.perform(get("/api/attendance/report")
                        .param("jobId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].attendanceStatus").value("CHECKED_OUT"));
    }
}
