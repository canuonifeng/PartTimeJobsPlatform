package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.BankCardVO;
import com.parttime.cservice.pojo.vo.EarningsSummaryVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.TransactionVO;
import com.parttime.cservice.pojo.vo.WithdrawalMethodVO;
import com.parttime.cservice.pojo.vo.WithdrawalVO;
import com.parttime.cservice.service.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
public class WithdrawalController {

    @Resource
    private WithdrawalService withdrawalService;

    private Long getCurrentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    @Operation(summary = "申请提现", description = "工人申请提现账户余额")
    @PostMapping("/api/withdrawals")
    public ResponseEntity<?> requestWithdrawal(@RequestBody Map<String, Object> request) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            java.math.BigDecimal amount = new java.math.BigDecimal(request.get("amount").toString());
            String withdrawalMethod = (String) request.get("withdrawalMethod");
            Long bankAccountId = request.get("bankAccountId") != null ? 
                Long.valueOf(request.get("bankAccountId").toString()) : null;
            
            WithdrawalVO response = withdrawalService.requestWithdrawal(workerId, amount, withdrawalMethod, bankAccountId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "获取可用提现方式", description = "获取当前工人可用的提现方式")
    @GetMapping("/api/withdrawal-methods")
    public ResponseEntity<?> getAvailableMethods() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<WithdrawalMethodVO> methods = withdrawalService.getAvailableMethods(workerId);
        return ResponseEntity.ok(methods);
    }

    @Operation(summary = "获取银行卡列表", description = "获取当前工人的银行卡列表")
    @GetMapping("/api/bank-cards")
    public ResponseEntity<?> getBankCards() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<BankCardVO> cards = withdrawalService.getBankCards(workerId);
        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "获取收益汇总", description = "获取当前工人的收益汇总信息")
    @GetMapping("/api/earnings/summary")
    public ResponseEntity<?> getEarningsSummary() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        EarningsSummaryVO summary = withdrawalService.getEarningsSummary(workerId);
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "获取账户流水", description = "获取当前工人的余额变动记录，分页返回")
    @GetMapping("/api/earnings/transactions")
    public ResponseEntity<?> getTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(withdrawalService.getTransactions(workerId, page, pageSize));
    }

    @Operation(summary = "获取提现记录", description = "获取当前工人的提现历史记录")
    @GetMapping("/api/withdrawals/my")
    public ResponseEntity<?> getWithdrawalHistory() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<WithdrawalVO> records = withdrawalService.getWithdrawalHistory(workerId);
        return ResponseEntity.ok(records);
    }
}
