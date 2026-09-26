package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.EnterpriseRealNameAuthMapper;
import com.parttime.enterprise.pojo.cmd.EnterpriseRealNameSubmitCmd;
import com.parttime.enterprise.pojo.entity.EnterpriseRealNameAuth;
import com.parttime.enterprise.pojo.vo.EnterpriseRealNameAuthVO;
import com.parttime.enterprise.pojo.vo.SignedUrlVO;
import com.parttime.enterprise.service.EnterpriseRealNameAuthService;
import com.parttime.enterprise.service.OssSignedUrlService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EnterpriseRealNameAuthServiceImpl implements EnterpriseRealNameAuthService {

    private static final String LICENSE_PREFIX = "license/";

    @Resource
    private EnterpriseRealNameAuthMapper enterpriseRealNameAuthMapper;

    @Resource
    private OssSignedUrlService ossSignedUrlService;

    @Override
    public EnterpriseRealNameAuthVO submit(Long enterpriseId, EnterpriseRealNameSubmitCmd cmd) {
        if (cmd == null
                || isBlank(cmd.getLegalPersonName())
                || isBlank(cmd.getLegalPersonIdCard())
                || isBlank(cmd.getUnifiedSocialCreditCode())
                || isBlank(cmd.getBusinessLicenseUrl())) {
            throw new RuntimeException("法人姓名、身份证号、统一社会信用代码、营业执照必填");
        }
        Optional<EnterpriseRealNameAuth> existing = enterpriseRealNameAuthMapper.findByEnterpriseId(enterpriseId);
        if (existing.isPresent()) {
            EnterpriseRealNameAuth current = existing.get();
            if ("PENDING".equals(current.getStatus())) {
                throw new RuntimeException("已提交，请等待审核");
            }
            if ("APPROVED".equals(current.getStatus())) {
                throw new RuntimeException("已通过认证");
            }
            current.setLegalPersonName(cmd.getLegalPersonName());
            current.setLegalPersonIdCard(cmd.getLegalPersonIdCard());
            current.setUnifiedSocialCreditCode(cmd.getUnifiedSocialCreditCode());
            current.setBusinessLicenseUrl(cmd.getBusinessLicenseUrl());
            current.setStatus("PENDING");
            current.setRejectReason(null);
            current.setSubmittedAt(LocalDateTime.now());
            current.setReviewedAt(null);
            current.setReviewerId(null);
            enterpriseRealNameAuthMapper.update(current);
            return toVO(current);
        }
        EnterpriseRealNameAuth auth = new EnterpriseRealNameAuth();
        auth.setEnterpriseId(enterpriseId);
        auth.setLegalPersonName(cmd.getLegalPersonName());
        auth.setLegalPersonIdCard(cmd.getLegalPersonIdCard());
        auth.setUnifiedSocialCreditCode(cmd.getUnifiedSocialCreditCode());
        auth.setBusinessLicenseUrl(cmd.getBusinessLicenseUrl());
        auth.setStatus("PENDING");
        auth.setSubmittedAt(LocalDateTime.now());
        enterpriseRealNameAuthMapper.insert(auth);
        return toVO(auth);
    }

    @Override
    public EnterpriseRealNameAuthVO getStatus(Long enterpriseId) {
        Optional<EnterpriseRealNameAuth> existing = enterpriseRealNameAuthMapper.findByEnterpriseId(enterpriseId);
        if (existing.isEmpty()) {
            EnterpriseRealNameAuthVO vo = new EnterpriseRealNameAuthVO();
            vo.setStatus("NONE");
            return vo;
        }
        return toVO(existing.get());
    }

    private EnterpriseRealNameAuthVO toVO(EnterpriseRealNameAuth auth) {
        EnterpriseRealNameAuthVO vo = new EnterpriseRealNameAuthVO();
        vo.setStatus(auth.getStatus());
        vo.setLegalPersonName(auth.getLegalPersonName());
        vo.setLegalPersonIdCardMasked(maskIdCard(auth.getLegalPersonIdCard()));
        vo.setUnifiedSocialCreditCode(auth.getUnifiedSocialCreditCode());
        vo.setBusinessLicenseUrl(resolvePrivateUrl(auth.getEnterpriseId(), auth.getBusinessLicenseUrl()));
        vo.setRejectReason(auth.getRejectReason());
        vo.setSubmittedAt(auth.getSubmittedAt());
        vo.setReviewedAt(auth.getReviewedAt());
        return vo;
    }

    private String resolvePrivateUrl(Long enterpriseId, String urlOrKey) {
        if (urlOrKey == null || !urlOrKey.startsWith(LICENSE_PREFIX)) {
            return urlOrKey;
        }
        try {
            SignedUrlVO signed = ossSignedUrlService.getSignedUrl(enterpriseId, urlOrKey);
            return signed.getUrl();
        } catch (RuntimeException e) {
            return urlOrKey;
        }
    }

    private String maskIdCard(String s) {
        if (s == null || s.length() < 8) return s;
        return s.substring(0, 4) + "********" + s.substring(s.length() - 4);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
