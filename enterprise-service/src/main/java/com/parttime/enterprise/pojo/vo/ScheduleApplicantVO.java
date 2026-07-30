package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ScheduleApplicantVO {
    @Schema(description = "报名ID")
    private Long applicationId;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "姓名")
    private String workerName;
    @Schema(description = "手机号")
    private String workerPhone;
    @Schema(description = "是否实名")
    private Boolean realNamed;
    @Schema(description = "报名状态")
    private String applicationStatus;
    @Schema(description = "报名时间")
    private LocalDateTime appliedAt;
    @Schema(description = "排班明细")
    private List<ShiftItem> shifts;

    @Data
    public static class ShiftItem {
        @Schema(description = "排班ID")
        private Long shiftId;
        @Schema(description = "排班状态")
        private String shiftStatus;
        @Schema(description = "签到时间")
        private LocalDateTime checkInTime;
        @Schema(description = "签退时间")
        private LocalDateTime checkOutTime;
        @Schema(description = "考勤状态")
        private String attendanceStatus;
        @Schema(description = "补卡状态")
        private String correctionStatus;
        @Schema(description = "结算状态")
        private String settlementStatus;
    }
}
