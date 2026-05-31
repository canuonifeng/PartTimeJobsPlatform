package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ProfileCompletenessVO {
    @Schema(description = "信息是否完整")
    private boolean complete;
    @Schema(description = "缺失字段列表")
    private List<String> missing;

    public ProfileCompletenessVO() {}

    public ProfileCompletenessVO(boolean complete, List<String> missing) {
        this.complete = complete;
        this.missing = missing;
    }
}
