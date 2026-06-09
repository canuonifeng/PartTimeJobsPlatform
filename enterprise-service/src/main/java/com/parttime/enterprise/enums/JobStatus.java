package com.parttime.enterprise.enums;

import lombok.Getter;

@Getter
public enum JobStatus implements BaseEnum {

    DRAFT("草稿", 0),
    PUBLISHED("已发布", 1),
    CLOSED("已关闭", 2),
    EXPIRED("已过期", 3);

    private final String name;
    private final int code;

    JobStatus(String name, int code) {
        this.name = name;
        this.code = code;
    }
}
