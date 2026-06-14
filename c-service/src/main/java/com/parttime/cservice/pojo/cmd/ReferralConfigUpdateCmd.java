package com.parttime.cservice.pojo.cmd;

import com.parttime.cservice.pojo.entity.ReferralConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ReferralConfigUpdateCmd {
    @Schema(description = "邀请奖励配置列表")
    private List<ReferralConfig> configs;
}
