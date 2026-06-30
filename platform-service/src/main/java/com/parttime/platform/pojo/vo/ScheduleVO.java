package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ScheduleVO {
    @Schema(description = "排班ID")
    private Long id;
    @Schema(description = "职位ID")
    private Long jobId;
    @Schema(description = "职位标题")
    private String jobTitle;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "班次日期")
    private LocalDate shiftDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "时薪")
    private BigDecimal hourlyWage;
    @Schema(description = "总工时")
    private BigDecimal totalHours;
    @Schema(description = "预计薪资")
    private BigDecimal estimatedWage;
    @Schema(description = "已报名人数")
    private Integer applicantCount;
    @Schema(description = "需求人数")
    private Integer headcount;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
