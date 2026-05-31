package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WorkerProfile {

    @Schema(description = "档案ID")
    private Long id;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "头像URL")
    private String avatarUrl;
    @Schema(description = "技能特长")
    private String skills;
    @Schema(description = "可工作日期")
    private String availableDays;
    @Schema(description = "性别: MALE/FEMALE/OTHER")
    private String gender;
    @Schema(description = "出生日期")
    private LocalDate birthday;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public WorkerProfile() {}

    public WorkerProfile(Long workerId, String name, String phone, String avatarUrl) {
        this.workerId = workerId;
        this.name = name;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
    }
}
