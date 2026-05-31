package com.parttime.cservice.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WorkerRealNameSubmitCmd {
    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "身份证号")
    private String idCardNo;
    @Schema(description = "身份证正面URL")
    private String idCardFrontUrl;
    @Schema(description = "身份证反面URL")
    private String idCardBackUrl;
}
