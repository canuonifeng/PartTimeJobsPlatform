package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SettlementVO {
    @Schema(description = "结算流水ID")
    private Long id;
    @Schema(description = "结算编号")
    private String settlementNo;
    @Schema(description = "企业ID")
    private Long companyId;
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "结算金额")
    private BigDecimal amount;
    @Schema(description = "流水类型")
    private String type;
    @Schema(description = "说明")
    private String description;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
