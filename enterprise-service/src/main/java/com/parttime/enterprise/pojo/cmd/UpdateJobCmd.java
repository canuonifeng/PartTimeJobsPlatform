package com.parttime.enterprise.pojo.cmd;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateJobCmd {

    private Long companyId;
    private String title;
    private String description;
    private String location;
    private Long categoryId;
    private Integer headcount;
    private LocalDateTime deadline;
    private List<JobRateCmd> rates;
    private List<JobScheduleCmd> schedules;
}
