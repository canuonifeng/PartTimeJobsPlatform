package com.parttime.platform.service.impl;

import com.parttime.platform.pojo.cmd.JobQueryCmd;
import com.parttime.platform.pojo.vo.JobScheduleVO;
import com.parttime.platform.service.JobScheduleService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class JobScheduleServiceImpl implements JobScheduleService {

    @Override
    public List<JobScheduleVO> list(JobQueryCmd cmd) {
        List<JobScheduleVO> list = new ArrayList<>();
        String[] statuses = {"PUBLISHED", "FILLED", "COMPLETED", "CANCELLED"};
        for (int i = 1; i <= 20; i++) {
            JobScheduleVO vo = new JobScheduleVO();
            vo.setId((long) i);
            vo.setJobId((long) (i % 10 + 1));
            vo.setJobTitle("职位" + (i % 10 + 1));
            vo.setCompanyId((long) (i % 5 + 1));
            vo.setCompanyName("企业" + (i % 5 + 1));
            vo.setScheduleDate(LocalDate.now().plusDays(i % 7));
            vo.setStartTime(LocalTime.of(9, 0));
            vo.setEndTime(LocalTime.of(18, 0));
            vo.setScheduleName("早班");
            vo.setHeadcount(5);
            vo.setApplicationCount(i % 5);
            vo.setSlotsAvailable(5 - (i % 5));
            vo.setHourlyWage(new BigDecimal("25.00"));
            vo.setStatus(statuses[i % 4]);
            vo.setCreatedAt(LocalDateTime.now().minusDays(i));
            list.add(vo);
        }
        return list;
    }

    @Override
    public List<JobScheduleVO> listByJobId(Long jobId) {
        List<JobScheduleVO> list = new ArrayList<>();
        String[] statuses = {"PUBLISHED", "FILLED", "COMPLETED", "CANCELLED"};
        for (int i = 1; i <= 5; i++) {
            JobScheduleVO vo = new JobScheduleVO();
            vo.setId((long) i);
            vo.setJobId(jobId);
            vo.setJobTitle("职位" + jobId);
            vo.setCompanyId(1L);
            vo.setCompanyName("测试企业");
            vo.setScheduleDate(LocalDate.now().plusDays(i));
            vo.setStartTime(LocalTime.of(9, 0));
            vo.setEndTime(LocalTime.of(18, 0));
            vo.setScheduleName("早班");
            vo.setHeadcount(5);
            vo.setApplicationCount(i % 5);
            vo.setSlotsAvailable(5 - (i % 5));
            vo.setHourlyWage(new BigDecimal("25.00"));
            vo.setStatus(statuses[i % 4]);
            vo.setCreatedAt(LocalDateTime.now().minusDays(i));
            list.add(vo);
        }
        return list;
    }

    @Override
    public JobScheduleVO detail(Long id) {
        JobScheduleVO vo = new JobScheduleVO();
        vo.setId(id);
        vo.setJobId(1L);
        vo.setJobTitle("测试职位");
        vo.setCompanyId(1L);
        vo.setCompanyName("测试企业");
        vo.setScheduleDate(LocalDate.now().plusDays(1));
        vo.setStartTime(LocalTime.of(9, 0));
        vo.setEndTime(LocalTime.of(18, 0));
        vo.setScheduleName("早班");
        vo.setHeadcount(5);
        vo.setApplicationCount(3);
        vo.setSlotsAvailable(2);
        vo.setHourlyWage(new BigDecimal("25.00"));
        vo.setStatus("PUBLISHED");
        vo.setCreatedAt(LocalDateTime.now().minusDays(1));
        return vo;
    }

    @Override
    public void cancel(Long id) {
    }
}
