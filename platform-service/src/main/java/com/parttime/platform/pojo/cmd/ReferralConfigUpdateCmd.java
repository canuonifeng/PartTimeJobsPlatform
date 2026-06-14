package com.parttime.platform.pojo.cmd;

import com.parttime.platform.pojo.entity.ReferralConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ReferralConfigUpdateCmd {
    @Schema(description = "邀请配置列表")
    private List<ReferralConfig> configs;
}
