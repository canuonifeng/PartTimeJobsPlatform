package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.EnterpriseRealNameAuthMapper;
import com.parttime.platform.pojo.entity.EnterpriseRealNameAuth;
import com.parttime.platform.pojo.vo.EnterpriseRealNameAuthVO;
import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.service.EnterpriseRealNameAuthReviewService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnterpriseRealNameAuthReviewServiceImpl implements EnterpriseRealNameAuthReviewService {

    @Resource
    private EnterpriseRealNameAuthMapper mapper;

    @Override
    public PageVO<EnterpriseRealNameAuthVO> list(String status, int page, int pageSize) {
        int offset = Math.max(page - 1, 0) * pageSize;
        List<EnterpriseRealNameAuthVO> records = mapper.findPage(status, offset, pageSize).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        long total = mapper.countPage(status);
        return new PageVO<>(records, total);
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

    private EnterpriseRealNameAuthVO toVO(EnterpriseRealNameAuth entity) {
        EnterpriseRealNameAuthVO vo = new EnterpriseRealNameAuthVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
