package com.parttime.cservice.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
public class JobScheduleInfoVO {
    @Schema(description = "排班ID")
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "排班日期")
    private LocalDate date;
    @Schema(description = "开始时间")
    private String startTime;
    @Schema(description = "结束时间")
    private String endTime;
    @Schema(description = "可报名人数")
    private Integer slotsAvailable;
}
