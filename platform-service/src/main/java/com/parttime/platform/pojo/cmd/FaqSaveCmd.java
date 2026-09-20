package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FaqSaveCmd {
    @Schema(description = "FAQ ID，编辑时传")
    private Long id;
    @Schema(description = "问题")
    private String question;
    @Schema(description = "答案")
    private String answer;
    @Schema(description = "分类")
    private String category;
    @Schema(description = "排序")
    private Integer sortOrder;
    @Schema(description = "状态: ACTIVE, INACTIVE")
    private String status;
}
