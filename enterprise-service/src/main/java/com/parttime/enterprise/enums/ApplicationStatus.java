package com.parttime.enterprise.enums;

import lombok.Getter;

@Getter
public enum ApplicationStatus implements BaseEnum {

    PENDING("待审核", "PENDING"),
    ACCEPTED("已通过", "ACCEPTED"),
    REJECTED("已拒绝", "REJECTED");

    private final String name;
    private final String code;

    ApplicationStatus(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
