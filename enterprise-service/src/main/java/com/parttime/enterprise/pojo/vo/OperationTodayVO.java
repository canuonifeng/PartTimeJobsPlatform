package com.parttime.enterprise.pojo.vo;

import lombok.Data;

@Data
public class OperationTodayVO {
    private Long newApplicationCount;
    private Long attendanceExceptionCount;
    private Long scheduleGapCount;
}
