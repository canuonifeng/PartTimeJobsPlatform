package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.EnterpriseMapper;
import com.parttime.platform.mapper.EnterpriseRealNameAuthMapper;
import com.parttime.platform.pojo.entity.Enterprise;
import com.parttime.platform.pojo.entity.EnterpriseRealNameAuth;
import com.parttime.platform.pojo.vo.EnterpriseRealNameAuthVO;
import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.SignedUrlVO;
import com.parttime.platform.service.EnterpriseRealNameAuthReviewService;
import com.parttime.platform.service.OssSignedUrlService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnterpriseRealNameAuthReviewServiceImpl implements EnterpriseRealNameAuthReviewService {

    private static final String LICENSE_PREFIX = "license/";

    @Resource
    private EnterpriseRealNameAuthMapper mapper;

    @Resource
    private OssSignedUrlService ossSignedUrlService;

    @Resource
    private EnterpriseMapper enterpriseMapper;

    @Override
    public PageVO<EnterpriseRealNameAuthVO> list(String status, int page, int pageSize) {
        int offset = Math.max(page - 1, 0) * pageSize;
        List<EnterpriseRealNameAuth> records = mapper.findPage(status, offset, pageSize);
        Map<Long, String> companyNameMap = loadCompanyNameMap(records);
        List<EnterpriseRealNameAuthVO> vos = records.stream()
                .map(r -> {
                    EnterpriseRealNameAuthVO vo = toVO(r);
                    vo.setCompanyName(companyNameMap.getOrDefault(r.getEnterpriseId(), ""));
                    return vo;
                })
                .collect(Collectors.toList());
        long total = mapper.countPage(status);
        return new PageVO<>(vos, total);
    }

    @Override
    public void approve(Long id, Long reviewerId) {
        EnterpriseRealNameAuth auth = mapper.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!"PENDING".equals(auth.getStatus())) {
            throw new RuntimeException("状态不允许操作");
        }
        mapper.updateReview(id, "APPROVED", null, reviewerId, LocalDateTime.now());
    }

    @Override
    public void reject(Long id, Long reviewerId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("拒绝原因必填");
        }
        EnterpriseRealNameAuth auth = mapper.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!"PENDING".equals(auth.getStatus())) {
            throw new RuntimeException("状态不允许操作");
        }
        mapper.updateReview(id, "REJECTED", reason, reviewerId, LocalDateTime.now());
    }

    private Map<Long, String> loadCompanyNameMap(List<EnterpriseRealNameAuth> records) {
        List<Long> enterpriseIds = records.stream()
                .map(EnterpriseRealNameAuth::getEnterpriseId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (enterpriseIds.isEmpty()) return Map.of();
        return enterpriseMapper.findByIds(enterpriseIds).stream()
                .collect(Collectors.toMap(Enterprise::getId, Enterprise::getCompanyName, (a, b) -> a));
    }

    private EnterpriseRealNameAuthVO toVO(EnterpriseRealNameAuth entity) {
        EnterpriseRealNameAuthVO vo = new EnterpriseRealNameAuthVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setBusinessLicenseUrl(resolvePrivateUrl(entity.getBusinessLicenseUrl()));
        return vo;
    }

    private String resolvePrivateUrl(String urlOrKey) {
        if (urlOrKey == null || !urlOrKey.startsWith(LICENSE_PREFIX)) {
            return urlOrKey;
        }
        String adminName = resolveAdminName();
        if (adminName == null) {
            return urlOrKey;
        }
        try {
            SignedUrlVO signed = ossSignedUrlService.getSignedUrl(adminName, urlOrKey);
            return signed.getUrl();
        } catch (RuntimeException e) {
            return urlOrKey;
        }
    }

    private String resolveAdminName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String name = auth.getName();
        return (name == null || name.isBlank()) ? null : name;
    }
}
