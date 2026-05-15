package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import com.parttime.enterprise.enums.JobStatus;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobVO {

    private Long id;
    private Long companyId;
    private String title;
    private String description;
    private String location;
    private Long categoryId;
    private Integer headcount;
    private JobStatus status;
    private LocalDateTime deadline;
    private List<JobRateVO> rates;
    private List<JobScheduleVO> schedules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
