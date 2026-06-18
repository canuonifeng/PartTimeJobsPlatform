package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.cmd.WithdrawalCmd;
import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.pojo.vo.BankCardVO;
import com.parttime.cservice.pojo.vo.EarningsSummaryVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.TransactionVO;
import com.parttime.cservice.pojo.vo.WithdrawalMethodVO;
import com.parttime.cservice.pojo.vo.WithdrawalVO;
import com.parttime.cservice.service.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

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
    @PostMapping("/api/worker/withdrawals")
    public ApiResponse<?> requestWithdrawal(@RequestBody WithdrawalCmd request) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        try {
            WithdrawalVO response = withdrawalService.requestWithdrawal(workerId,
                    request.getAmount(), request.getWithdrawalMethod(), request.getBankAccountId());
            return ApiResponse.success(response);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "获取可用提现方式", description = "获取当前工人可用的提现方式")
    @GetMapping("/api/worker/withdrawal-methods")
    public ApiResponse<List<WithdrawalMethodVO>> getAvailableMethods() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        List<WithdrawalMethodVO> methods = withdrawalService.getAvailableMethods(workerId);
        return ApiResponse.success(methods);
    }

    @Operation(summary = "获取银行卡列表", description = "获取当前工人的银行卡列表")
    @GetMapping("/api/worker/bank-cards")
    public ApiResponse<List<BankCardVO>> getBankCards() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        List<BankCardVO> cards = withdrawalService.getBankCards(workerId);
        return ApiResponse.success(cards);
    }

    @Operation(summary = "获取收益汇总", description = "获取当前工人的收益汇总信息")
    @GetMapping("/api/worker/earnings/summary")
    public ApiResponse<EarningsSummaryVO> getEarningsSummary() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        EarningsSummaryVO summary = withdrawalService.getEarningsSummary(workerId);
        return ApiResponse.success(summary);
    }

    @Operation(summary = "获取账户流水", description = "获取当前工人的余额变动记录，分页返回")
    @GetMapping("/api/worker/earnings/transactions")
    public ApiResponse<PageVO<TransactionVO>> getTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        return ApiResponse.success(withdrawalService.getTransactions(workerId, page, pageSize));
    }

    @Operation(summary = "获取提现记录", description = "获取当前工人的提现历史记录，分页返回")
    @GetMapping("/api/worker/withdrawals/my")
    public ApiResponse<PageVO<WithdrawalVO>> getWithdrawalHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }
        return ApiResponse.success(withdrawalService.getWithdrawalHistory(workerId, page, pageSize));
    }
}
