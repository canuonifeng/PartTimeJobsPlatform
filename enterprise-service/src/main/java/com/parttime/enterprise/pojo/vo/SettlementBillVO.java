package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class SettlementBillVO {

    @Schema(description = "账单ID")
    private Long id;
    @Schema(description = "排班日期")
    private LocalDate shiftDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "工人姓名")
    private String workerName;
    @Schema(description = "总工时")
    private BigDecimal totalHours;
    @Schema(description = "薪资规则")
    private String rateType;
    @Schema(description = "薪资规则金额")
    private BigDecimal rateAmount;
    @Schema(description = "排班薪资")
    private BigDecimal scheduledPay;
    @Schema(description = "实付薪资")
    private BigDecimal actualPay;
    @Schema(description = "系统流水号")
    private String serialNumber;
    @Schema(description = "第三方流水号")
    private String thirdPartySerialNo;
    @Schema(description = "第三方支付平台")
    private String thirdPartyPlatform;
    @Schema(description = "状态: PAYING/PAID/FAILED")
    private String status;
    @Schema(description = "支付时间")
    private LocalDateTime paidAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
