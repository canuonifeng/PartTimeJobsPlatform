package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GrabTaskOrderCmd {

    @Schema(description = "标注任务ID")
    private Long jobId;

    @Schema(description = "可选：指定批次ID列表，不传则抢该任务全部可用批次")
    private java.util.List<Long> scheduleIds;
}
