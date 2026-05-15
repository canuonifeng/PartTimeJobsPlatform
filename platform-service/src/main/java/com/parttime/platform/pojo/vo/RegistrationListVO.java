package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class RegistrationListVO {

    @Schema(description = "注册申请列表")
    private List<RegistrationVO> items;
    @Schema(description = "总数")
    private int total;
}
