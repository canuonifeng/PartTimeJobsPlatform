package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "热门推荐VO")
public class HotRecommendationVO {

    @Schema(description = "职位ID")
    private Long jobId;
    @Schema(description = "职位标题")
    private String jobTitle;
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "分类名称")
    private String categoryName;
    @Schema(description = "封面图")
    private String imageUrl;
}
