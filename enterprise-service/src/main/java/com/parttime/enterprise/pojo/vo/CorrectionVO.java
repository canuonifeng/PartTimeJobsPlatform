package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
public class CorrectionVO {

    @Schema(description = "补卡申请ID")
    private Long id;
    @Schema(description = "班次ID")
    private Long shiftId;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "工人姓名")
    private String workerName;
    @Schema(description = "年龄")
    private Integer workerAge;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "岗位名称")
    private String jobTitle;
    @Schema(description = "排班日期")
    private LocalDate shiftDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "补卡原因")
    private String reason;
    @Schema(description = "状态: PENDING/APPROVED/REJECTED")
    private String status;
    @Schema(description = "拒绝原因")
    private String rejectReason;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "处理时间")
    private LocalDateTime processedAt;
}
