package com.parttime.enterprise.enums;

import lombok.Getter;

@Getter
public enum PricingMode implements BaseEnum {

    PER_ITEM("按件计价", "PER_ITEM"),
    PER_PACKAGE("按包计价", "PER_PACKAGE");

    private final String name;
    private final String code;

    PricingMode(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
