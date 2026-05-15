package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class JobRate {

    @Schema(description = "薪资规则ID")
    private Long id;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "薪资类型: HOURLY-时薪, DAILY-日薪, WEEKLY-周薪, MONTHLY-月薪")
    private String type;
    @Schema(description = "金额")
    private BigDecimal amount;
    @Schema(description = "币种: CNY-人民币")
    private String currency;
    @Schema(description = "薪资规则描述")
    private String rules;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
