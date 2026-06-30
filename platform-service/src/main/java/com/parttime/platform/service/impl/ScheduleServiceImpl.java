package com.parttime.platform.service.impl;

import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.vo.ScheduleVO;
import com.parttime.platform.service.ScheduleService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Override
    public List<ScheduleVO> list(JobQueryCmd cmd) {
        List<ScheduleVO> list = new ArrayList<>();
        String[] statuses = {"PUBLISHED", "FILLED", "COMPLETED", "CANCELLED"};
        for (int i = 1; i <= 20; i++) {
            ScheduleVO vo = new ScheduleVO();
            vo.setId((long) i);
            vo.setJobId((long) (i % 10 + 1));
            vo.setJobTitle("职位" + (i % 10 + 1));
            vo.setCompanyId((long) (i % 5 + 1));
            vo.setCompanyName("企业" + (i % 5 + 1));
            vo.setShiftDate(LocalDate.now().plusDays(i % 7));
            vo.setStartTime(LocalTime.of(9, 0));
            vo.setEndTime(LocalTime.of(18, 0));
            vo.setHourlyWage(new BigDecimal("25.00"));
            vo.setTotalHours(new BigDecimal("8.0"));
            vo.setEstimatedWage(new BigDecimal("200.00"));
            vo.setApplicantCount(i % 5);
            vo.setHeadcount(5);
            vo.setStatus(statuses[i % 4]);
            vo.setCreatedAt(LocalDateTime.now().minusDays(i));
            list.add(vo);
        }
        return list;
    }

    @Override
    public ScheduleVO detail(Long id) {
        ScheduleVO vo = new ScheduleVO();
        vo.setId(id);
        vo.setJobId(1L);
        vo.setJobTitle("测试职位");
        vo.setCompanyId(1L);
        vo.setCompanyName("测试企业");
        vo.setShiftDate(LocalDate.now().plusDays(1));
        vo.setStartTime(LocalTime.of(9, 0));
        vo.setEndTime(LocalTime.of(18, 0));
        vo.setHourlyWage(new BigDecimal("25.00"));
        vo.setTotalHours(new BigDecimal("8.0"));
        vo.setEstimatedWage(new BigDecimal("200.00"));
        vo.setApplicantCount(3);
        vo.setHeadcount(5);
        vo.setStatus("PUBLISHED");
        vo.setCreatedAt(LocalDateTime.now().minusDays(1));
        return vo;
    }

    @Override
    public void cancel(Long id) {
    }
}
