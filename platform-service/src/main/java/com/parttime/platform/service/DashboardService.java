package com.parttime.platform.service;

import com.parttime.platform.pojo.vo.DashboardTrendVO;
import com.parttime.platform.pojo.vo.DashboardVO;

public interface DashboardService {

    DashboardVO getDashboardStats();

    DashboardTrendVO getTrend(Integer days);
}
