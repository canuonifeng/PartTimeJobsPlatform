package com.parttime.platform.controller;

import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.WorkerRealNameAuthVO;
import com.parttime.platform.service.WorkerRealNameAuthReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/worker-real-name")
public class WorkerRealNameAuthReviewController {

    @Resource
    private WorkerRealNameAuthReviewService service;

    @Operation(summary = "兼职实名认证列表")
    @GetMapping
    public PageVO<WorkerRealNameAuthVO> list(@RequestParam(required = false) String status,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int pageSize) {
        return service.list(status, page, pageSize);
    }

    @Operation(summary = "通过兼职实名认证")
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        try {
            service.approve(id, null);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "拒绝兼职实名认证")
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            service.reject(id, null, body.get("reason"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
