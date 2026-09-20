package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GrabTaskOrderVO {

    @Schema(description = "成功抢到的批次数量")
    private Integer grabbed;
}
