package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "活动效果统计VO")
public class ActivityEffectVO {

    @Schema(description = "活动ID")
    private Long activityId;
    @Schema(description = "参与人数")
    private Integer participantCount;
    @Schema(description = "浏览次数")
    private Integer viewCount;
    @Schema(description = "活动带来的报名数")
    private Long incrementalApplications;
    @Schema(description = "转化率")
    private BigDecimal conversionRate;
}
