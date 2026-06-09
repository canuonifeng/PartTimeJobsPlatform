package com.parttime.enterprise.enums;

import lombok.Getter;

@Getter
public enum JobRateType implements BaseEnum {

    HOURLY("时薪", "HOURLY"),
    DAILY("日薪", "DAILY"),
    PIECEWORK("计件", "PIECEWORK");

    private final String name;
    private final String code;

    JobRateType(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
