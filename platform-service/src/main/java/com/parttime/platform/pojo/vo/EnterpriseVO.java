package com.parttime.platform.pojo.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnterpriseVO {
    @Schema(description = "企业ID")
    private Long id;
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "企业logoURL")
    private String companyLogo;
    @Schema(description = "联系人")
    private String contactName;
    @Schema(description = "联系电话")
    private String contactPhone;
    @Schema(description = "企业地址")
    private String companyAddress;
    @Schema(description = "营业执照")
    private String businessLicense;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "关联注册申请ID")
    private Long registrationId;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
