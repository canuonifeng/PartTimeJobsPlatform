package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.DashboardTrendCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.DashboardTrendVO;
import com.parttime.platform.pojo.vo.DashboardVO;
import com.parttime.platform.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @Operation(summary = "获取仪表盘统计", description = "获取平台运营数据统计，包括企业数、岗位数、工人数等")
    @GetMapping("/stats")
    public ApiResponse<DashboardVO> getStats() {
        return ApiResponse.success(dashboardService.getDashboardStats());
    }

    @Operation(summary = "获取核心指标趋势", description = "返回近 N 天每日新增岗位、新增工人、完成班次序列")
    @PostMapping("/trend")
    public ApiResponse<DashboardTrendVO> trend(@RequestBody(required = false) DashboardTrendCmd body) {
        Integer days = body == null ? null : body.getDays();
        return ApiResponse.success(dashboardService.getTrend(days));
    }
}
