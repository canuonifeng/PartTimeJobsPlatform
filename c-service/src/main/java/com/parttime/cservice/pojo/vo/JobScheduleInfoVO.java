package com.parttime.cservice.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class JobScheduleInfoVO {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String startTime;
    private String endTime;
    private Integer slotsAvailable;
}
