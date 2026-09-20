package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "企业活跃度趋势项")
public class EnterpriseTrendVO {

    @Schema(description = "日期")
    private String date;
    @Schema(description = "新增企业数")
    private Long newEnterprises;
    @Schema(description = "活跃企业数")
    private Long activeEnterprises;
    @Schema(description = "发布职位数")
    private Long publishedJobs;
}
