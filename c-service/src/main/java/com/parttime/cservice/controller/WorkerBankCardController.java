package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.cmd.WorkerBankCardCmd;
import com.parttime.cservice.pojo.entity.WorkerBankCard;
import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.service.WorkerBankCardService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/worker/bank-card")
public class WorkerBankCardController {

    @Resource
    private WorkerBankCardService workerBankCardService;

    private Long getCurrentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    @Operation(summary = "获取银行卡")
    @GetMapping
    public ApiResponse<WorkerBankCard> get() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) return ApiResponse.error(401, "未登录");
        WorkerBankCard card = workerBankCardService.get(workerId);
        return ApiResponse.success(card);
    }

    @Operation(summary = "绑定/更新银行卡")
    @PutMapping
    public ApiResponse<WorkerBankCard> upsert(@RequestBody WorkerBankCardCmd cmd) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) return ApiResponse.error(401, "未登录");
        try {
            return ApiResponse.success(workerBankCardService.upsert(workerId, cmd));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "解绑银行卡")
    @DeleteMapping
    public ApiResponse<Void> delete() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) return ApiResponse.error(401, "未登录");
        workerBankCardService.delete(workerId);
        return ApiResponse.success();
    }
}
