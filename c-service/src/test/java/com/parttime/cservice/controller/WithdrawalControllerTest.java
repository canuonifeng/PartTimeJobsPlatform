package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.BankCardVO;
import com.parttime.cservice.pojo.vo.EarningsSummaryVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.TransactionVO;
import com.parttime.cservice.pojo.vo.WithdrawalMethodVO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        when(withdrawalService.requestWithdrawal(eq(1L), any(), any(), any())).thenReturn(response);

        Map<String, Object> request = new HashMap<>();
        request.put("amount", "500.00");
        request.put("withdrawalMethod", "WECHAT");

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

        when(withdrawalService.requestWithdrawal(eq(1L), any(), any(), any()))
                .thenThrow(new RuntimeException("Insufficient balance"));

        Map<String, Object> request = new HashMap<>();
        request.put("amount", "999999.00");
        request.put("withdrawalMethod", "WECHAT");

        mockMvc.perform(post("/api/withdrawals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient balance"));
    }

    @Test
    void requestWithdrawal_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        Map<String, Object> request = new HashMap<>();
        request.put("amount", "100.00");

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
    void getEarningsTransactions_shouldReturnShiftDisplayFields() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        TransactionVO transaction = new TransactionVO();
        transaction.setId(1L);
        transaction.setAmount(new BigDecimal("120.00"));
        transaction.setType("EARNINGS");
        transaction.setDescription("结算收入：张三 2026-06-06 10:00:00");
        transaction.setCreatedAt("2026-06-06 10:00:00");
        transaction.setJobTitle("仓库分拣员");
        transaction.setCompanyName("绿地物流");
        transaction.setLocation("绿地物流园3号仓");
        transaction.setShiftDate("2026-06-05");
        transaction.setStartTime("09:00");
        transaction.setEndTime("18:00");
        transaction.setTotalHours(new BigDecimal("8.00"));
        transaction.setSettlementStatus("PAID");

        when(withdrawalService.getTransactions(1L, 1, 20)).thenReturn(new PageVO<>(List.of(transaction), 1));

        mockMvc.perform(get("/api/earnings/transactions?page=1&pageSize=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].jobTitle").value("仓库分拣员"))
                .andExpect(jsonPath("$.records[0].companyName").value("绿地物流"))
                .andExpect(jsonPath("$.records[0].location").value("绿地物流园3号仓"))
                .andExpect(jsonPath("$.records[0].shiftDate").value("2026-06-05"))
                .andExpect(jsonPath("$.records[0].startTime").value("09:00"))
                .andExpect(jsonPath("$.records[0].endTime").value("18:00"))
                .andExpect(jsonPath("$.records[0].settlementStatus").value("PAID"));
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

    @Test
    void getAvailableMethods_shouldReturnMethods() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        WithdrawalMethodVO method = new WithdrawalMethodVO();
        method.setCode("WECHAT");
        method.setName("微信零钱");

        when(withdrawalService.getAvailableMethods(1L)).thenReturn(List.of(method));

        mockMvc.perform(get("/api/withdrawal-methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("WECHAT"));
    }

    @Test
    void getAvailableMethods_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/withdrawal-methods"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getBankCards_shouldReturnCards() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null, List.of()));

        BankCardVO card = new BankCardVO();
        card.setId(1L);
        card.setBankName("中国银行");
        card.setCardNumber("1234567890123456");

        when(withdrawalService.getBankCards(1L)).thenReturn(List.of(card));

        mockMvc.perform(get("/api/bank-cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bankName").value("中国银行"));
    }

    @Test
    void getBankCards_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/bank-cards"))
                .andExpect(status().isUnauthorized());
    }
}
