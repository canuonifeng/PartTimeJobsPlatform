package com.parttime.enterprise.web.controller;

import com.parttime.enterprise.api.dto.*;
import com.parttime.enterprise.core.auth.JwtTokenProvider;
import com.parttime.enterprise.core.service.ScheduleService;
import com.parttime.enterprise.web.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.Test;
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

    // === Schedule Template Controller Tests ===

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTemplate_shouldReturnCreated() throws Exception {
        ScheduleTemplateResponse response = new ScheduleTemplateResponse();
        response.setId(100L);
        response.setCompanyId(1L);
        response.setName("Morning Shift");

        when(scheduleService.createTemplate(any())).thenReturn(response);

        String json = """
                {
                    "companyId": 1,
                    "name": "Morning Shift",
                    "description": "Weekday morning",
                    "slots": [
                        {
                            "dayOfWeek": 1,
                            "startTime": "09:00:00",
                            "endTime": "18:00:00",
                            "maxWorkers": 5
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/schedule-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.name").value("Morning Shift"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTemplates_shouldReturnList() throws Exception {
        ScheduleTemplateResponse t1 = new ScheduleTemplateResponse();
        t1.setId(1L);
        t1.setName("Morning");
        ScheduleTemplateResponse t2 = new ScheduleTemplateResponse();
        t2.setId(2L);
        t2.setName("Evening");

        when(scheduleService.getTemplatesByCompany(1L)).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/schedule-templates")
                        .param("companyId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTemplateById_shouldReturnTemplate() throws Exception {
        ScheduleTemplateResponse response = new ScheduleTemplateResponse();
        response.setId(100L);
        response.setName("Morning Shift");

        when(scheduleService.getTemplateById(100L)).thenReturn(response);

        mockMvc.perform(get("/api/schedule-templates/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTemplate_shouldReturnOk() throws Exception {
        ScheduleTemplateResponse response = new ScheduleTemplateResponse();
        response.setId(100L);
        response.setName("Updated Name");

        when(scheduleService.updateTemplate(eq(100L), any())).thenReturn(response);

        String json = """
                {
                    "companyId": 1,
                    "name": "Updated Name",
                    "description": "Updated"
                }
                """;

        mockMvc.perform(put("/api/schedule-templates/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTemplate_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/schedule-templates/100"))
                .andExpect(status().isNoContent());
        verify(scheduleService).deleteTemplate(100L);
    }

    // === Schedule Shift Controller Tests ===

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignShift_shouldReturnCreated() throws Exception {
        ScheduleShiftResponse response = new ScheduleShiftResponse();
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
        ScheduleShiftResponse shift = new ScheduleShiftResponse();
        shift.setId(1L);
        shift.setJobId(10L);

        when(scheduleService.getShifts(10L, null, null)).thenReturn(List.of(shift));

        mockMvc.perform(get("/api/schedule-shifts")
                        .param("jobId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeShift_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/schedule-shifts/99"))
                .andExpect(status().isNoContent());
        verify(scheduleService).removeShift(99L);
    }

    // === Attendance Report Controller Tests ===

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAttendanceReport_shouldReturnList() throws Exception {
        AttendanceReportResponse report = new AttendanceReportResponse();
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
