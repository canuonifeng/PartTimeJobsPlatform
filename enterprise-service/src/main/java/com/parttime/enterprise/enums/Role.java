package com.parttime.enterprise.enums;

import lombok.Getter;

@Getter
public enum Role implements BaseEnum {

    ADMIN("管理员", "ADMIN"),
    HR("人事", "HR"),
    MANAGER("经理", "MANAGER"),
    FINANCE("财务", "FINANCE");

    private final String name;
    private final String code;

    Role(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
