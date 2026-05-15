package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.JwtTokenProvider;
import com.parttime.enterprise.config.SecurityConfig;
import com.parttime.enterprise.pojo.cmd.PayrollBatchCmd;
import com.parttime.enterprise.pojo.vo.PayrollBatchVO;
import com.parttime.enterprise.pojo.vo.PayrollItemVO;
import com.parttime.enterprise.service.PayrollService;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayrollController.class)
@Import(SecurityConfig.class)
class PayrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayrollService payrollService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBatch_shouldReturnCreated() throws Exception {
        PayrollBatchCmd request = new PayrollBatchCmd();
        request.setCompanyId(1L);
        request.setName("June 2026 Payroll");
        request.setPeriodStart(LocalDate.of(2026, 6, 1));
        request.setPeriodEnd(LocalDate.of(2026, 6, 30));

        PayrollBatchVO response = new PayrollBatchVO();
        response.setId(1L);
        response.setCompanyId(1L);
        response.setName("June 2026 Payroll");
        response.setStatus("DRAFT");

        when(payrollService.createBatch(any(PayrollBatchCmd.class))).thenReturn(response);

        mockMvc.perform(post("/api/payroll/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("June 2026 Payroll"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(payrollService).createBatch(any(PayrollBatchCmd.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void calculateBatch_shouldReturnOk() throws Exception {
        PayrollBatchVO response = new PayrollBatchVO();
        response.setId(1L);
        response.setStatus("CALCULATED");
        response.setTotalAmount(new BigDecimal("5000.00"));

        when(payrollService.calculateBatch(1L)).thenReturn(response);

        mockMvc.perform(post("/api/payroll/batches/1/calculate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CALCULATED"))
                .andExpect(jsonPath("$.totalAmount").value(5000.00));

        verify(payrollService).calculateBatch(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void confirmBatch_shouldReturnOk() throws Exception {
        PayrollBatchVO response = new PayrollBatchVO();
        response.setId(1L);
        response.setStatus("CONFIRMED");

        when(payrollService.confirmBatch(1L)).thenReturn(response);

        mockMvc.perform(post("/api/payroll/batches/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(payrollService).confirmBatch(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void payBatch_shouldReturnOk() throws Exception {
        PayrollBatchVO response = new PayrollBatchVO();
        response.setId(1L);
        response.setStatus("PAID");

        when(payrollService.payBatch(1L)).thenReturn(response);

        mockMvc.perform(post("/api/payroll/batches/1/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(payrollService).payBatch(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBatchById_shouldReturnBatch() throws Exception {
        PayrollBatchVO response = new PayrollBatchVO();
        response.setId(1L);
        response.setCompanyId(1L);
        response.setName("Test Batch");
        response.setStatus("DRAFT");

        when(payrollService.getBatchById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/payroll/batches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Batch"));

        verify(payrollService).getBatchById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBatchesByCompany_shouldReturnBatches() throws Exception {
        PayrollBatchVO batch = new PayrollBatchVO();
        batch.setId(1L);
        batch.setCompanyId(1L);
        batch.setName("Batch 1");

        when(payrollService.getBatchesByCompany(1L)).thenReturn(List.of(batch));

        mockMvc.perform(get("/api/payroll/batches?companyId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Batch 1"));

        verify(payrollService).getBatchesByCompany(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBatchItems_shouldReturnItems() throws Exception {
        PayrollItemVO item = new PayrollItemVO();
        item.setId(1L);
        item.setWorkerId(100L);
        item.setTotalPay(new BigDecimal("500.00"));

        when(payrollService.getBatchItems(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/payroll/batches/1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].workerId").value(100));

        verify(payrollService).getBatchItems(1L);
    }
}
