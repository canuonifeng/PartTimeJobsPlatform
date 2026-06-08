package com.parttime.cservice.controller;

import com.parttime.cservice.enums.CorrectionStatus;
import com.parttime.cservice.mapper.AttendanceCorrectionMapper;
import com.parttime.cservice.mapper.ShiftMapper;
import com.parttime.cservice.pojo.entity.AttendanceCorrectionEntity;
import com.parttime.cservice.pojo.entity.ShiftEntity;
import com.parttime.cservice.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CorrectionController {

    @Resource
    private AttendanceCorrectionMapper correctionMapper;
    @Resource
    private ShiftMapper shiftMapper;

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

        if (shiftId == null || reason == null || reason.trim().isEmpty()) {
            return ApiResponse.error("shiftId and reason are required");
        }

        ShiftEntity shift = shiftMapper.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftId));

        if (!workerId.equals(shift.getWorkerId())) {
            return ApiResponse.error("This shift does not belong to you");
        }

        LocalDateTime shiftEnd = LocalDateTime.of(shift.getShiftDate(), shift.getEndTime());
        if (LocalDateTime.now().isBefore(shiftEnd)) {
            return ApiResponse.error("Shift has not ended yet");
        }

        java.util.Optional<AttendanceCorrectionEntity> existing = correctionMapper.findByShiftId(shiftId);
        if (existing.isPresent()) {
            return ApiResponse.error("Correction already submitted for this shift");
        }

        AttendanceCorrectionEntity correction = new AttendanceCorrectionEntity();
        correction.setShiftId(shiftId);
        correction.setWorkerId(workerId);
        correction.setReason(reason.trim());
        correction.setStatus(CorrectionStatus.PENDING.name());
        correction.setCreatedAt(LocalDateTime.now());
        correctionMapper.insert(correction);

        return ApiResponse.success(Map.of("id", correction.getId(), "status", CorrectionStatus.PENDING.name()));
    }

    @Operation(summary = "查询补卡申请状态", description = "查询指定排班的补卡申请状态")
    @GetMapping("/attendance/correction/status")
    public ApiResponse<?> getCorrectionStatus(
            @Parameter(description = "班次ID") @RequestParam Long shiftId) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ApiResponse.error(401, "未登录");
        }

        java.util.Optional<AttendanceCorrectionEntity> existing = correctionMapper.findByShiftId(shiftId);

        if (existing.isPresent()) {
            AttendanceCorrectionEntity c = existing.get();
            return ApiResponse.success(Map.of(
                    "eligible", false,
                    "existingRequest", Map.of("id", c.getId(), "status", c.getStatus())
            ));
        }

        ShiftEntity shift = shiftMapper.findById(shiftId).orElse(null);
        if (shift == null || !workerId.equals(shift.getWorkerId())) {
            return ApiResponse.success(Map.of("eligible", false));
        }

        LocalDateTime shiftEnd = LocalDateTime.of(shift.getShiftDate(), shift.getEndTime());
        boolean ended = LocalDateTime.now().isAfter(shiftEnd);

        boolean eligible = ended;
        return ApiResponse.success(Map.of("eligible", eligible));
    }
}
