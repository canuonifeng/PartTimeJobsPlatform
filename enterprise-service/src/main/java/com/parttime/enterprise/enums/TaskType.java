package com.parttime.enterprise.enums;

import lombok.Getter;

@Getter
public enum TaskType implements BaseEnum {

    WORK("工作", "WORK"),
    ANNOTATION("标注", "ANNOTATION");

    private final String name;
    private final String code;

    TaskType(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
