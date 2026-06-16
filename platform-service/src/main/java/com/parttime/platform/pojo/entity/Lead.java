package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Lead {

    @Schema(description = "留资ID")
    private Long id;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "需求说明")
    private String demand;

    @Schema(description = "来源页面")
    private String sourcePage;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
