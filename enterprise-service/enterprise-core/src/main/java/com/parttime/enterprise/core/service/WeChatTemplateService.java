package com.parttime.enterprise.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class WeChatTemplateService {

    private static final Logger log = LoggerFactory.getLogger(WeChatTemplateService.class);

    public void sendTemplateMessage(String openId, String templateId, Map<String, Object> data) {
        log.info("Sending WeChat template message: openId={}, templateId={}, data={}", openId, templateId, data);
    }
}
