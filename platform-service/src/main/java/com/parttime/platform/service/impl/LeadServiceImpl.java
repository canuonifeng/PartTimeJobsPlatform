package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.LeadMapper;
import com.parttime.platform.pojo.cmd.LeadCreateCmd;
import com.parttime.platform.pojo.entity.Lead;
import com.parttime.platform.pojo.vo.LeadVO;
import com.parttime.platform.service.LeadService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class LeadServiceImpl implements LeadService {

    private static final String NEW_STATUS = "NEW";
    private static final String DEFAULT_SOURCE_PAGE = "website";

    @Resource
    private LeadMapper leadMapper;

    @Override
    public LeadVO createLead(LeadCreateCmd cmd) {
        Lead lead = new Lead();
        lead.setContactName(required(cmd.getContactName(), "联系人不能为空"));
        lead.setCompanyName(required(cmd.getCompanyName(), "公司名称不能为空"));
        lead.setPhone(required(cmd.getPhone(), "手机号不能为空"));
        lead.setDemand(trimToNull(cmd.getDemand()));
        lead.setSourcePage(defaultSourcePage(cmd.getSourcePage()));
        lead.setStatus(NEW_STATUS);
        leadMapper.insert(lead);
        return toVO(lead);
    }

    private String required(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new BusinessException(message);
        }
        return normalized;
    }

    private String defaultSourcePage(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? DEFAULT_SOURCE_PAGE : normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LeadVO toVO(Lead lead) {
        LeadVO vo = new LeadVO();
        vo.setId(lead.getId());
        vo.setContactName(lead.getContactName());
        vo.setCompanyName(lead.getCompanyName());
        vo.setPhone(lead.getPhone());
        vo.setDemand(lead.getDemand());
        vo.setSourcePage(lead.getSourcePage());
        vo.setStatus(lead.getStatus());
        vo.setCreatedAt(lead.getCreatedAt());
        return vo;
    }
}
