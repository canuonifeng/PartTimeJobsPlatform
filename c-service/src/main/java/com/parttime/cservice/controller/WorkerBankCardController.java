package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.cmd.WorkerBankCardCmd;
import com.parttime.cservice.pojo.entity.WorkerBankCard;
import com.parttime.cservice.service.WorkerBankCardService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<?> get() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        WorkerBankCard card = workerBankCardService.get(workerId);
        return ResponseEntity.ok(card);
    }

    @Operation(summary = "绑定/更新银行卡")
    @PutMapping
    public ResponseEntity<?> upsert(@RequestBody WorkerBankCardCmd cmd) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            return ResponseEntity.ok(workerBankCardService.upsert(workerId, cmd));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "解绑银行卡")
    @DeleteMapping
    public ResponseEntity<?> delete() {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        workerBankCardService.delete(workerId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
