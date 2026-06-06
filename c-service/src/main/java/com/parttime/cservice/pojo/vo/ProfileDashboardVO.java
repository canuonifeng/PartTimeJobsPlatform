package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProfileDashboardVO {

    @Schema(description = "个人资料")
    private ProfileVO profile;

    @Schema(description = "首页统计")
    private HomeStatsVO stats;

    @Schema(description = "收入汇总")
    private EarningsSummaryVO earningsSummary;

    @Schema(description = "实名认证状态")
    private WorkerRealNameAuthVO realNameAuth;
}
