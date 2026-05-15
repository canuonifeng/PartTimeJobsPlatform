package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkerBlacklist {

    @Schema(description = "黑名单ID")
    private Long id;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "拉黑原因")
    private String reason;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
