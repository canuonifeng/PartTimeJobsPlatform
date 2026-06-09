package com.parttime.enterprise.enums;

import lombok.Getter;

@Getter
public enum Role implements BaseEnum {

    ADMIN("管理员", 0),
    HR("人事", 1),
    MANAGER("经理", 2),
    FINANCE("财务", 3);

    private final String name;
    private final int code;

    Role(String name, int code) {
        this.name = name;
        this.code = code;
    }
}
