package com.parttime.cservice.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BankCardVO {

    @Schema(description = "银行卡ID")
    private Long id;
    
    @Schema(description = "银行名称")
    private String bankName;
    
    @Schema(description = "卡号后四位")
    private String cardNumber;
    
    @Schema(description = "持卡人姓名")
    private String cardHolder;
    
    @Schema(description = "是否默认卡")
    private boolean isDefault;
}