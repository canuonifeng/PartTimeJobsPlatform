package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ProfileUpdateCmd {

    @Schema(description = "姓名")
    private String name;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "头像URL")
    private String avatarUrl;
    @Schema(description = "技能特长")
    private List<String> skills;
    @Schema(description = "可工作日期")
    private List<String> availableDays;
}
