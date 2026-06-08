package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WithdrawalMethodVO {

    @Schema(description = "提现方式代码")
    private String code;
    
    @Schema(description = "提现方式名称")
    private String name;
    
    @Schema(description = "是否可用")
    private boolean available;
    
    @Schema(description = "描述")
    private String description;
}