package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.EarningsSummaryVO;
import com.parttime.cservice.pojo.cmd.WithdrawalCmd;
import com.parttime.cservice.pojo.vo.WithdrawalVO;
import com.parttime.cservice.service.impl.WithdrawalServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WithdrawalControllerTest {

    private MockMvc mockMvc;
    @Mock
    private WithdrawalServiceImpl withdrawalService;
    @InjectMocks
    private WithdrawalController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void requestWithdrawal_shouldReturnOk() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        WithdrawalVO response = new WithdrawalVO();
        response.setId(1L);
        response.setWorkerId(1L);
        response.setAmount(new BigDecimal("500.00"));
        response.setStatus("PENDING");

        when(withdrawalService.requestWithdrawal(eq(1L), any())).thenReturn(response);

        WithdrawalCmd request = new WithdrawalCmd();
        request.setAmount(new BigDecimal("500.00"));

        mockMvc.perform(post("/api/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(500.00));
    }

    @Test
    void requestWithdrawal_shouldReturnBadRequestOnError() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        when(withdrawalService.requestWithdrawal(eq(1L), any()))
                .thenThrow(new RuntimeException("Insufficient balance"));

        WithdrawalCmd request = new WithdrawalCmd();
        request.setAmount(new BigDecimal("999999.00"));

        mockMvc.perform(post("/api/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient balance"));
    }

    @Test
    void requestWithdrawal_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        WithdrawalCmd request = new WithdrawalCmd();
        request.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getWithdrawalHistory_shouldReturnRecords() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        WithdrawalVO record = new WithdrawalVO();
        record.setId(1L);
        record.setWorkerId(1L);
        record.setAmount(new BigDecimal("100.00"));
        record.setStatus("PENDING");

        when(withdrawalService.getWithdrawalHistory(1L)).thenReturn(List.of(record));

        mockMvc.perform(get("/api/withdrawals/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getWithdrawalHistory_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/withdrawals/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getEarningsSummary_shouldReturnSummary() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        EarningsSummaryVO summary = new EarningsSummaryVO();
        summary.setTotalEarned(new BigDecimal("1000.00"));
        summary.setTotalWithdrawn(new BigDecimal("400.00"));
        summary.setPendingWithdrawal(new BigDecimal("600.00"));

        when(withdrawalService.getEarningsSummary(1L)).thenReturn(summary);

        mockMvc.perform(get("/api/earnings/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEarned").value(1000.00))
                .andExpect(jsonPath("$.totalWithdrawn").value(400.00))
                .andExpect(jsonPath("$.pendingWithdrawal").value(600.00));
    }

    @Test
    void getEarningsSummary_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/earnings/summary"))
                .andExpect(status().isUnauthorized());
    }
}
