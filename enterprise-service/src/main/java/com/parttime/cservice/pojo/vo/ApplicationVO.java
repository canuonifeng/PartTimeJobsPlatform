package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationVO {
    @Schema(description = "申请ID")
    private Long applicationId;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "申请状态")
    private String status;
    @Schema(description = "申请时间")
    private LocalDateTime appliedAt;
}
