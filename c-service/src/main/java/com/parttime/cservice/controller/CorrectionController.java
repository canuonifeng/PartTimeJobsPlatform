package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.service.CorrectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CorrectionController {

    @Resource
    private CorrectionService correctionService;

    private Long getCurrentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    @Operation(summary = "提交补卡申请", description = "工人对已结束的排班提交补卡申请")
    @PostMapping("/attendance/correction")
    public ApiResponse<?> submitCorrection(@RequestBody Map<String, Object> body) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }

        Long shiftId = body.get("shiftId") instanceof Number
                ? ((Number) body.get("shiftId")).longValue() : null;
        String reason = body.get("reason") instanceof String ? (String) body.get("reason") : null;

        try {
            correctionService.submitCorrection(workerId, shiftId, reason);
            return ApiResponse.success(Map.of("status", "PENDING"));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "查询补卡申请状态", description = "查询指定排班的补卡申请状态")
    @GetMapping("/attendance/correction/status")
    public ApiResponse<?> getCorrectionStatus(
            @Parameter(description = "班次ID") @RequestParam Long shiftId) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }

        return ApiResponse.success(correctionService.getCorrectionStatus(workerId, shiftId));
    }
}
