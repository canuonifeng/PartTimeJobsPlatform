package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkerListVO {
    @Schema(description = "人才库记录ID")
    private Long id;
    @Schema(description = "兼职ID")
    private Long workerId;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "电话")
    private String phone;
    @Schema(description = "年龄")
    private Integer workerAge;
    @Schema(description = "头像URL")
    private String avatarUrl;
    @Schema(description = "状态: ACTIVE/BLACKLISTED")
    private String status;
    @Schema(description = "首次联系时间")
    private LocalDateTime firstContactAt;
    @Schema(description = "最近联系时间")
    private LocalDateTime lastContactAt;
}
