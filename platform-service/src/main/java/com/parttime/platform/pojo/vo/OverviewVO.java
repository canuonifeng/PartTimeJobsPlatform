package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "运营概览指标")
public class OverviewVO {

    @Schema(description = "累计职位数")
    private Long totalJobs;
    @Schema(description = "累计报名数")
    private Long totalApplications;
    @Schema(description = "累计工人数")
    private Long totalWorkers;
    @Schema(description = "累计企业数")
    private Long totalEnterprises;
    @Schema(description = "今日新增职位")
    private Long todayNewJobs;
    @Schema(description = "今日新增报名")
    private Long todayNewApplications;
    @Schema(description = "今日完成考勤")
    private Long todayCompletedAttendance;
    @Schema(description = "今日结算金额")
    private BigDecimal todaySettlementAmount;
    @Schema(description = "累计结算金额")
    private BigDecimal totalSettlementAmount;
    @Schema(description = "累计服务费收入")
    private BigDecimal totalServiceFee;
}
