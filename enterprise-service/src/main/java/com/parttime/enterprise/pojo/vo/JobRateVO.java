package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.parttime.enterprise.enums.JobRateType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class JobRateVO {

    @Schema(description = "薪资规则ID")
    private Long id;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "薪资类型")
    private JobRateType type;
    @Schema(description = "金额")
    private BigDecimal amount;
    @Schema(description = "币种")
    private String currency;
    @Schema(description = "薪资规则描述")
    private String rules;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
