package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationActivity {

    @Schema(description = "活动ID")
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
    @Schema(description = "参与人数")
    private Integer participantCount;
    @Schema(description = "浏览次数")
    private Integer viewCount;
    @Schema(description = "排序")
    private Integer sortOrder;
    @Schema(description = "操作人ID")
    private Long operatorId;
    @Schema(description = "操作人姓名")
    private String operatorName;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
