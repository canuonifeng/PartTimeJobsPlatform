package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionVO {
    @Schema(description = "交易ID")
    private Long id;
    @Schema(description = "变动金额")
    private BigDecimal amount;
    @Schema(description = "类型: EARNINGS-收入, WITHDRAWAL-支出")
    private String type;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "交易时间(北京时间)")
    private String createdAt;
    @Schema(description = "岗位标题")
    private String jobTitle;
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "工作地点")
    private String location;
    @Schema(description = "班次日期")
    private String shiftDate;
    @Schema(description = "班次开始时间")
    private String startTime;
    @Schema(description = "班次结束时间")
    private String endTime;
    @Schema(description = "工时")
    private BigDecimal totalHours;
    @Schema(description = "结算状态")
    private String settlementStatus;
}
