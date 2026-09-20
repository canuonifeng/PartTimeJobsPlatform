package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class BatchIdCmd {
    @Schema(description = "ID列表")
    private List<Long> ids;
}
