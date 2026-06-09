package com.parttime.enterprise.enums;

import lombok.Getter;

@Getter
public enum JobRateType implements BaseEnum {

    HOURLY("时薪", 0),
    DAILY("日薪", 1),
    PIECEWORK("计件", 2);

    private final String name;
    private final int code;

    JobRateType(String name, int code) {
        this.name = name;
        this.code = code;
    }
}
