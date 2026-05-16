package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnterpriseCreateCmd {
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "联系人")
    private String contactName;
    @Schema(description = "联系电话")
    private String contactPhone;
    @Schema(description = "企业地址")
    private String companyAddress;
    @Schema(description = "营业执照")
    private String businessLicense;
}
