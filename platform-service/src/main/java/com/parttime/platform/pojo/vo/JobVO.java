package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JobVO {
    @Schema(description = "岗位ID")
    private Long id;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "岗位标题")
    private String title;
    @Schema(description = "岗位职责")
    private String description;
    @Schema(description = "任职要求")
    private String requirements;
    @Schema(description = "联系人姓名")
    private String contactName;
    @Schema(description = "联系电话")
    private String contactPhone;
    @Schema(description = "工作地点")
    private String location;
    @Schema(description = "分类名称")
    private String categoryName;
    @Schema(description = "招聘人数")
    private Integer headcount;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "是否置顶")
    private Boolean isTop;
    @Schema(description = "是否推荐")
    private Boolean isRecommended;
    @Schema(description = "报名截止时间")
    private LocalDateTime deadline;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
