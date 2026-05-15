package com.parttime.platform.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class RegistrationListVO {

    private List<RegistrationVO> items;
    private int total;
}
