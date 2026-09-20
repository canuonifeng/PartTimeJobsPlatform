package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AttendanceReviewCmd {
    @Schema(description = "考勤记录ID")
    private Long id;
    @Schema(description = "考勤状态")
    private String status;
    @Schema(description = "备注")
    private String remark;
}
