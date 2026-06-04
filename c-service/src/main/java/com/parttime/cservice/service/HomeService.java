package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.HomeSchedulesVO;
import com.parttime.cservice.pojo.vo.HomeStatsVO;

public interface HomeService {
    HomeStatsVO getStats(Long workerId);
    HomeSchedulesVO getSchedules(Long workerId);
}
