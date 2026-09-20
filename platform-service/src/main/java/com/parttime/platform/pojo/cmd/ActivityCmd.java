package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "活动创建/更新命令")
public class ActivityCmd {

    @Schema(description = "活动ID(更新时必填)")
    private Long id;
    @Schema(description = "活动标题")
    private String title;
    @Schema(description = "活动描述")
    private String description;
    @Schema(description = "活动类型: NEW_USER, REFERRAL, HOLIDAY, GENERAL")
    private String activityType;
    @Schema(description = "活动横幅")
    private String bannerImage;
    @Schema(description = "活动内容图")
    private String contentImage;
    @Schema(description = "活动链接")
    private String linkUrl;
    @Schema(description = "状态: DRAFT, PUBLISHED, ENDED")
    private String status;
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
    @Schema(description = "排序")
    private Integer sortOrder;
}
