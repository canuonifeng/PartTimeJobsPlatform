package com.parttime.cservice.enums;

import lombok.Getter;

@Getter
public enum CorrectionStatus implements BaseEnum {

    PENDING("待审核", 0),
    APPROVED("已通过", 1),
    REJECTED("已驳回", 2);

    private final String name;
    private final int code;

    CorrectionStatus(String name, int code) {
        this.name = name;
        this.code = code;
    }
}
