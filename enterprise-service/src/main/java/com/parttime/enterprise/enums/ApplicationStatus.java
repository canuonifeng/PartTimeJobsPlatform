package com.parttime.enterprise.enums;

import lombok.Getter;

@Getter
public enum ApplicationStatus implements BaseEnum {

    PENDING("待审核", 0),
    ACCEPTED("已通过", 1),
    REJECTED("已拒绝", 2);

    private final String name;
    private final int code;

    ApplicationStatus(String name, int code) {
        this.name = name;
        this.code = code;
    }
}
