package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class NotificationTemplateCmd {

    @Schema(description = "模板ID")
    private Long id;
    @Schema(description = "通知类型")
    private String type;
    @Schema(description = "发送渠道: SMS-短信, EMAIL-邮件, APP_PUSH-应用推送")
    private String channel;
    @Schema(description = "标题模板")
    private String titleTemplate;
    @Schema(description = "内容模板")
    private String contentTemplate;
}
