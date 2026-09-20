package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class DashboardTrendVO {
    @Schema(description = "日期序列 yyyy-MM-dd")
    private List<String> dates;
    @Schema(description = "每日新增岗位数")
    private List<Integer> newJobs;
    @Schema(description = "每日新增工人数")
    private List<Integer> newWorkers;
    @Schema(description = "每日完成班次数")
    private List<Integer> completedShifts;
}
