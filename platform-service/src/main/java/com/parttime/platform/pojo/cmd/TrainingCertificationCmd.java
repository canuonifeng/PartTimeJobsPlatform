package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TrainingCertificationCmd {

    @Schema(description = "认证ID，更新时必填")
    private Long id;
    @Schema(description = "认证名称")
    private String name;
    @Schema(description = "认证编码")
    private String code;
    @Schema(description = "适用任务类型 WORK/ANNOTATION")
    private String taskType;
    @Schema(description = "认证描述")
    private String description;
    @Schema(description = "有效期天数，NULL=永久")
    private Integer validDays;
}
