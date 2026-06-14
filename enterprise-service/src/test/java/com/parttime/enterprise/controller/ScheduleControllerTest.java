package com.parttime.enterprise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parttime.enterprise.pojo.vo.AttendanceReportVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.pojo.vo.ScheduleShiftVO;
import com.parttime.enterprise.service.CorrectionService;
import com.parttime.enterprise.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private CorrectionService correctionService;

    @InjectMocks
    private ScheduleController scheduleController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduleController, "scheduleService", scheduleService);
        ReflectionTestUtils.setField(scheduleController, "correctionService", correctionService);
        mockMvc = MockMvcBuilders.standaloneSetup(scheduleController).build();
    }

    @Test
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

        mockMvc.perform(post("/api/enterprise/schedule-shifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobId").value(10))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));
    }

    @Test
    void getShifts_shouldReturnList() throws Exception {
        ScheduleShiftVO shift = new ScheduleShiftVO();
        shift.setId(1L);
        shift.setJobId(10L);

        PageVO<ScheduleShiftVO> result = new PageVO<>(List.of(shift), 1);
        when(scheduleService.getShifts(1L, 10L, null, null, null, 1, 20)).thenReturn(result);

        mockMvc.perform(get("/api/enterprise/schedule-shifts")
                        .param("jobId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records.length()").value(1));
    }

    @Test
    void removeShift_shouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/enterprise/schedule-shifts/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":99}"))
                .andExpect(status().isOk());
        verify(scheduleService).removeShift(99L);
    }

    @Test
    void getAttendanceReport_shouldReturnList() throws Exception {
        AttendanceReportVO report = new AttendanceReportVO();
        report.setShiftId(1L);
        report.setWorkerId(20L);
        report.setAttendanceStatus("CHECKED_OUT");

        when(scheduleService.getAttendanceReport(10L, null, null)).thenReturn(List.of(report));

        mockMvc.perform(get("/api/enterprise/attendance/report")
                        .param("jobId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].attendanceStatus").value("CHECKED_OUT"));
    }
}
