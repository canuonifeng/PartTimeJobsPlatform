package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.service.WeChatTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WeChatTemplateServiceImpl implements WeChatTemplateService {

    private static final Logger log = LoggerFactory.getLogger(WeChatTemplateServiceImpl.class);

    @Override
    public void sendTemplateMessage(String openId, String templateId, Map<String, Object> data) {
        log.info("Sending WeChat template message: openId={}, templateId={}, data={}", openId, templateId, data);
    }
}
