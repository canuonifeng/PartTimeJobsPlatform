package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.cmd.ApplyJobCmd;
import com.parttime.cservice.pojo.vo.ApplicationVO;
import com.parttime.cservice.pojo.vo.JobDetailVO;
import com.parttime.cservice.pojo.vo.JobSummaryVO;
import com.parttime.cservice.pojo.vo.ProfileCompletenessVO;
import com.parttime.cservice.service.JobService;
import com.parttime.cservice.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Resource
    private JobService jobService;

    @Resource
    private ProfileService profileService;

    @Operation(summary = "搜索岗位", description = "根据关键词、分类、地点和薪资范围搜索岗位")
    @GetMapping
    public ResponseEntity<List<JobSummaryVO>> searchJobs(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "岗位分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "工作地点") @RequestParam(required = false) String location,
            @Parameter(description = "最低薪资") @RequestParam(required = false) BigDecimal minRate,
            @Parameter(description = "最高薪资") @RequestParam(required = false) BigDecimal maxRate,
            @Parameter(description = "当前位置纬度") @RequestParam(required = false) BigDecimal latitude,
            @Parameter(description = "当前位置经度") @RequestParam(required = false) BigDecimal longitude) {
        log.info("job");
        List<JobSummaryVO> results = jobService.searchJobs(keyword, categoryId, location, minRate, maxRate, latitude, longitude);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "获取岗位详情", description = "根据ID获取岗位详细信息，包括薪资规则和排班")
    @GetMapping("/detail")
    public ResponseEntity<JobDetailVO> getJobDetail(@Parameter(description = "岗位ID") @RequestParam Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long workerId = null;
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null && !"anonymousUser".equals(auth.getPrincipal())) {
            workerId = Long.valueOf(auth.getName());
        }
        JobDetailVO detail = jobService.getJobDetail(id, workerId);
        return ResponseEntity.ok(detail);
    }

    @Operation(summary = "申请岗位", description = "工人申请岗位并选择排班")
    @PostMapping("/apply")
    public ResponseEntity<?> applyForJob(@Parameter(description = "岗位ID") @RequestParam Long id, @RequestBody ApplyJobCmd request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long workerId = Long.valueOf(auth.getName());
        ProfileCompletenessVO completeness = profileService.getCompleteness(workerId);
        if (!completeness.isComplete()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "PROFILE_INCOMPLETE",
                    "message", "请先完善个人信息",
                    "missing", completeness.getMissing()
            ));
        }
        boolean success = jobService.applyForJob(workerId, id, request.scheduleIds());
        return ResponseEntity.ok(Map.of("success", success));
    }

    @Operation(summary = "获取申请状态", description = "获取工人在指定岗位的申请状态")
    @GetMapping("/application")
    public ResponseEntity<?> getApplicationStatus(@Parameter(description = "岗位ID") @RequestParam Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long workerId = Long.valueOf(auth.getName());
        List<ApplicationVO> statuses = jobService.getApplicationStatus(workerId, id);
        return ResponseEntity.ok(statuses);
    }
}
