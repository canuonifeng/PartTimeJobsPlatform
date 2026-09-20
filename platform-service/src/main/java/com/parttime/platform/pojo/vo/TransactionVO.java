package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "交易流水")
public class TransactionVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "交易单号")
    private String transactionNo;
    @Schema(description = "交易类型编码")
    private String type;
    @Schema(description = "交易类型名称")
    private String typeName;
    @Schema(description = "关联方")
    private String relatedName;
    @Schema(description = "联系方式")
    private String relatedPhone;
    @Schema(description = "金额")
    private BigDecimal amount;
    @Schema(description = "变动后余额")
    private BigDecimal balanceAfter;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "操作人")
    private String operator;
    @Schema(description = "交易时间")
    private LocalDateTime createdAt;
}
