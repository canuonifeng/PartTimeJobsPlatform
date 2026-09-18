package com.parttime.enterprise.enums;

import lombok.Getter;

@Getter
public enum AnnotationTaskOrderStatus implements BaseEnum {

    PENDING("待处理", "PENDING"),
    IN_PROGRESS("进行中", "IN_PROGRESS"),
    SUBMITTED("已提交", "SUBMITTED"),
    COMPLETED("已完成", "COMPLETED"),
    REJECTED("已拒绝", "REJECTED");

    private final String name;
    private final String code;

    AnnotationTaskOrderStatus(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
