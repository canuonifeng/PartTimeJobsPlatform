package com.parttime.enterprise.pojo.cmd;

import lombok.Data;

@Data
public class NotificationTemplateCmd {

    private String type;
    private String channel;
    private String titleTemplate;
    private String contentTemplate;
}
