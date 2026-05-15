package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
public class WorkerProfileVO {

    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "平均评分")
    private Double avgRating;
    @Schema(description = "评价总数")
    private Integer totalEvaluations;
    @Schema(description = "是否在黑名单中: true-已拉黑, false-正常")
    private Boolean isBlacklisted;
    @Schema(description = "拉黑原因")
    private String blacklistReason;
    @Schema(description = "工作经历列表")
    private List<WorkHistoryVO> workHistory;
}
