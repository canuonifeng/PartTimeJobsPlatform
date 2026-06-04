package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class HomeSchedulesVO {

    @Schema(description = "今日班次列表")
    private List<WorkerShiftVO> todayShifts;
    @Schema(description = "未来班次列表")
    private List<WorkerShiftVO> futureShifts;
}
