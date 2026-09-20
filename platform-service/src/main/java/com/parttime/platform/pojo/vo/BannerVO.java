package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "轮播图VO")
public class BannerVO {

    @Schema(description = "轮播图ID")
    private Long id;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "图片URL")
    private String imageUrl;
    @Schema(description = "跳转链接")
    private String linkUrl;
    @Schema(description = "位置")
    private String position;
    @Schema(description = "排序")
    private Integer sortOrder;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
    @Schema(description = "点击次数")
    private Integer clickCount;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
