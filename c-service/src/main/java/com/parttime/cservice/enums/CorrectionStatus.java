package com.parttime.cservice.enums;

import lombok.Getter;

@Getter
public enum CorrectionStatus implements BaseEnum {

    PENDING("待审核", "PENDING"),
    APPROVED("已通过", "APPROVED"),
    REJECTED("已驳回", "REJECTED");

    private final String name;
    private final String code;

    CorrectionStatus(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
