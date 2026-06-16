package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "官网留资请求")
public class LeadCreateCmd {

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "需求说明")
    private String demand;

    @Schema(description = "来源页面")
    private String sourcePage;
}
