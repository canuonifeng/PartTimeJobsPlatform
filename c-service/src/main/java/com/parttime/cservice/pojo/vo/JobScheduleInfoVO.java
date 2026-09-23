package com.parttime.cservice.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
public class JobScheduleInfoVO {
    @Schema(description = "排班ID")
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "排班日期")
    private LocalDate date;
    @Schema(description = "开始时间")
    private String startTime;
    @Schema(description = "结束时间")
    private String endTime;
    @Schema(description = "可报名人数")
    private Integer slotsAvailable;
    @Schema(description = "剩余可报名人数")
    private Integer remainingSlots;
    @Schema(description = "联系人姓名快照")
    private String contactName;
    @Schema(description = "联系人电话快照")
    private String contactPhone;
    @Schema(description = "总数量")
    private Integer totalItems;
    @Schema(description = "外部批次ID")
    private String externalBatchId;
    @Schema(description = "剩余可抢数量")
    private Integer remainingItems;
}
