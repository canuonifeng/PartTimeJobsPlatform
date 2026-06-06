package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.JobScheduleInfoVO;
import com.parttime.cservice.pojo.vo.JobRateInfoVO;
import com.parttime.cservice.service.JobService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TestDataFactory {

    public static void addSampleJobs(JobService jobService) {
        JobRateInfoVO hourlyRate = new JobRateInfoVO();
        hourlyRate.setId(1L);
        hourlyRate.setType("HOURLY");
        hourlyRate.setAmount(new BigDecimal("50.00"));
        hourlyRate.setCurrency("CNY");

        JobRateInfoVO dailyRate = new JobRateInfoVO();
        dailyRate.setId(2L);
        dailyRate.setType("DAILY");
        dailyRate.setAmount(new BigDecimal("400.00"));
        dailyRate.setCurrency("CNY");

        JobScheduleInfoVO schedule1 = new JobScheduleInfoVO();
        schedule1.setId(1L);
        schedule1.setDate(LocalDate.now().plusDays(5));
        schedule1.setStartTime("09:00");
        schedule1.setEndTime("18:00");
        schedule1.setSlotsAvailable(5);

        JobScheduleInfoVO schedule2 = new JobScheduleInfoVO();
        schedule2.setId(2L);
        schedule2.setDate(LocalDate.now().plusDays(6));
        schedule2.setStartTime("09:00");
        schedule2.setEndTime("18:00");
        schedule2.setSlotsAvailable(3);

        JobRateInfoVO job2Hourly = new JobRateInfoVO();
        job2Hourly.setId(3L);
        job2Hourly.setType("HOURLY");
        job2Hourly.setAmount(new BigDecimal("80.00"));
        job2Hourly.setCurrency("CNY");

        JobScheduleInfoVO job2Schedule = new JobScheduleInfoVO();
        job2Schedule.setId(3L);
        job2Schedule.setDate(LocalDate.now().plusDays(7));
        job2Schedule.setStartTime("10:00");
        job2Schedule.setEndTime("19:00");
        job2Schedule.setSlotsAvailable(2);

        // Job 1: Software Engineer, Beijing, Category 1
        jobService.addJob(
                1L, "Software Engineer", "负责后端系统开发与维护", "Beijing", 1L, "Technology",
                List.of(hourlyRate, dailyRate), List.of(schedule1, schedule2),
                10, 3, LocalDateTime.now().plusDays(30), "PUBLISHED"
        );

        // Job 2: Senior Designer, Shanghai, Category 1
        jobService.addJob(
                2L, "Senior Designer", "负责UI/UX设计", "Shanghai", 1L, "Technology",
                List.of(job2Hourly), List.of(job2Schedule),
                5, 1, LocalDateTime.now().plusDays(40), "PUBLISHED"
        );

        // Job 3: Marketing Manager, Beijing, Category 2
        JobRateInfoVO job3Daily = new JobRateInfoVO();
        job3Daily.setId(4L);
        job3Daily.setType("DAILY");
        job3Daily.setAmount(new BigDecimal("600.00"));
        job3Daily.setCurrency("CNY");

        JobScheduleInfoVO job3Schedule = new JobScheduleInfoVO();
        job3Schedule.setId(4L);
        job3Schedule.setDate(LocalDate.now().plusDays(8));
        job3Schedule.setStartTime("09:00");
        job3Schedule.setEndTime("18:00");
        job3Schedule.setSlotsAvailable(4);

        jobService.addJob(
                3L, "Marketing Manager", "负责市场推广策略", "Beijing", 2L, "Marketing",
                List.of(job3Daily), List.of(job3Schedule),
                3, 0, LocalDateTime.now().plusDays(20), "PUBLISHED"
        );
    }
}
