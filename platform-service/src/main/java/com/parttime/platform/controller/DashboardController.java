package com.parttime.platform.controller;

import com.parttime.platform.pojo.vo.DashboardVO;
import com.parttime.platform.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public DashboardVO getStats() {
        return dashboardService.getDashboardStats();
    }
}
