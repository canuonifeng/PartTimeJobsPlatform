package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class MyTopShiftsVO {
    @Schema(description = "当前排班（今天的排班）")
    private WorkerShiftVO currentShift;
    @Schema(description = "未来排班列表（当前时间小于排班开始时间）")
    private List<WorkerShiftVO> futureShifts;
}
