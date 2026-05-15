package com.parttime.cservice.pojo.entity;

import com.parttime.cservice.pojo.vo.JobRateInfoVO;
import com.parttime.cservice.pojo.vo.JobScheduleInfoVO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Job {
    private Long id;
    private Long jobId;
    private Long companyId;
    private String companyName;
    private String title;
    private String description;
    private String location;
    private Long categoryId;
    private String categoryName;
    private String rateType;
    private BigDecimal rateAmount;
    private String status;
    private LocalDateTime publishedAt;
    private List<JobRateInfoVO> rates;
    private List<JobScheduleInfoVO> schedules;
    private Integer headcount;
    private Integer acceptedCount;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Job() {}

    public Job(Long id, String title, String description, String location, Long categoryId,
               String categoryName, List<JobRateInfoVO> rates, List<JobScheduleInfoVO> schedules,
               Integer headcount, Integer acceptedCount, LocalDateTime deadline, String status) {
        this.id = id;
        this.jobId = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.rates = rates;
        this.schedules = schedules;
        this.headcount = headcount;
        this.acceptedCount = acceptedCount;
        this.deadline = deadline;
        this.status = status;
    }
}
