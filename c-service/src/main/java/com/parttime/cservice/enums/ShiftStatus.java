package com.parttime.cservice.enums;

import lombok.Getter;

@Getter
public enum ShiftStatus implements BaseEnum {

    SCHEDULED("待上岗", "SCHEDULED"),
    ON_DUTY("工作中", "ON_DUTY"),
    COMPLETED("已完成", "COMPLETED"),
    ABSENT("缺勤", "ABSENT"),
    LATE("迟到", "LATE"),
    EARLY_LEAVE("早退", "EARLY_LEAVE");

    private final String name;
    private final String code;

    ShiftStatus(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
