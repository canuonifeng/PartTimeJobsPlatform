package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkerBankCard {
    private Long id;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "持卡人姓名")
    private String cardHolder;
    @Schema(description = "银行卡号")
    private String cardNumber;
    @Schema(description = "银行名称")
    private String bankName;
    @Schema(description = "开户支行")
    private String bankBranch;
    @Schema(description = "是否默认卡")
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
