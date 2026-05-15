package com.parttime.enterprise.service;

import java.util.Map;

public interface WeChatTemplateService {

    void sendTemplateMessage(String openId, String templateId, Map<String, Object> data);
}
