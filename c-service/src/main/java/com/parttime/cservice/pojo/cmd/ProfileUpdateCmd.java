package com.parttime.cservice.pojo.cmd;

import lombok.Data;

@Data
public class ProfileUpdateCmd {

    private String name;
    private String phone;
    private String avatarUrl;
    private String skills;
    private String availableDays;
}
