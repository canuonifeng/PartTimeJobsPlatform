package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationVO {

    @Schema(description = "注册申请ID")
    private Long id;
    @Schema(description = "公司名称")
    private String companyName;
    @Schema(description = "联系人姓名")
    private String contactName;
    @Schema(description = "联系电话")
    private String contactPhone;
    @Schema(description = "公司地址")
    private String companyAddress;
    @Schema(description = "营业执照")
    private String businessLicense;
    @Schema(description = "审核状态")
    private String status;
    @Schema(description = "审核人ID")
    private String reviewerId;
    @Schema(description = "审核备注")
    private String reviewRemark;
    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
