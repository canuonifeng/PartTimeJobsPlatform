package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分类供需项")
public class CategorySupplyVO {

    @Schema(description = "分类名称")
    private String category;
    @Schema(description = "招聘需求(职位数)")
    private Long demand;
    @Schema(description = "供给(报名人数)")
    private Long supply;
}
