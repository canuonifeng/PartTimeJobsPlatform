package com.parttime.enterprise.enums;

public interface BaseEnum {

    String getName();

    String getCode();

    static <E extends Enum<E> & BaseEnum> E fromCode(Class<E> enumClass, String code) {
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.getCode().equals(code)) {
                return constant;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code + " for " + enumClass.getSimpleName());
    }

    static <E extends Enum<E> & BaseEnum> E fromName(Class<E> enumClass, String name) {
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.getName().equals(name)) {
                return constant;
            }
        }
        throw new IllegalArgumentException("Unknown name: " + name + " for " + enumClass.getSimpleName());
    }
}
