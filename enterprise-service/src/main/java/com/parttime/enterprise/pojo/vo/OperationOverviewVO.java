package com.parttime.enterprise.pojo.vo;

import lombok.Data;

@Data
public class OperationOverviewVO {
    private Long publishedJobCount;
    private Long totalApplicationCount;
    private Long pendingTodoCount;
    private Long todayShiftCount;
    private Long unpaidSalaryCount;
}
