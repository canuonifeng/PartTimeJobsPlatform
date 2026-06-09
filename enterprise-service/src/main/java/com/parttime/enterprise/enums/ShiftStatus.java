package com.parttime.enterprise.enums;

import lombok.Getter;

@Getter
public enum ShiftStatus implements BaseEnum {

    SCHEDULED("待上岗", 0),
    ON_DUTY("工作中", 1),
    COMPLETED("已完成", 2),
    ABSENT("缺勤", 3),
    LATE("迟到", 4),
    EARLY_LEAVE("早退", 5);

    private final String name;
    private final int code;

    ShiftStatus(String name, int code) {
        this.name = name;
        this.code = code;
    }
}
