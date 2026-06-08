package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransferResult {

    @Schema(description = "是否成功")
    private boolean success;
    
    @Schema(description = "转账流水号")
    private String transferNo;
    
    @Schema(description = "错误代码")
    private String errorCode;
    
    @Schema(description = "错误信息")
    private String errorMessage;
    
    @Schema(description = "转账时间")
    private LocalDateTime transferTime;
}