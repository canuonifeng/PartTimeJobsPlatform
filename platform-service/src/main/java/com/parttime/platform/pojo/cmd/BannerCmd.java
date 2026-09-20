package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "轮播图创建/更新命令")
public class BannerCmd {

    @Schema(description = "轮播图ID(更新时必填)")
    private Long id;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "图片URL")
    private String imageUrl;
    @Schema(description = "跳转链接")
    private String linkUrl;
    @Schema(description = "位置: HOME, WORKER, ENTERPRISE")
    private String position;
    @Schema(description = "排序")
    private Integer sortOrder;
    @Schema(description = "状态: ACTIVE, INACTIVE")
    private String status;
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
