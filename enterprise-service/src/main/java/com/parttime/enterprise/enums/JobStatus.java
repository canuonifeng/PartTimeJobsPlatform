package com.parttime.enterprise.enums;

import lombok.Getter;

@Getter
public enum JobStatus implements BaseEnum {

    DRAFT("草稿", "DRAFT"),
    PUBLISHED("已发布", "PUBLISHED"),
    CLOSED("已关闭", "CLOSED"),
    EXPIRED("已过期", "EXPIRED");

    private final String name;
    private final String code;

    JobStatus(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
