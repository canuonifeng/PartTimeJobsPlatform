package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.WorkerRealNameAuthMapper;
import com.parttime.platform.pojo.entity.WorkerRealNameAuth;
import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.WorkerRealNameAuthVO;
import com.parttime.platform.service.WorkerRealNameAuthReviewService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkerRealNameAuthReviewServiceImpl implements WorkerRealNameAuthReviewService {

    @Resource
    private WorkerRealNameAuthMapper mapper;

    @Override
    public PageVO<WorkerRealNameAuthVO> list(String status, int page, int pageSize) {
        int offset = Math.max(page - 1, 0) * pageSize;
        List<WorkerRealNameAuthVO> records = mapper.findPage(status, offset, pageSize).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        long total = mapper.countPage(status);
        return new PageVO<>(records, total);
    }

    @Override
    public void approve(Long id, Long reviewerId) {
        WorkerRealNameAuth auth = mapper.findById(id)
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
        WorkerRealNameAuth auth = mapper.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!"PENDING".equals(auth.getStatus())) {
            throw new RuntimeException("状态不允许操作");
        }
        mapper.updateReview(id, "REJECTED", reason, reviewerId, LocalDateTime.now());
    }

    private WorkerRealNameAuthVO toVO(WorkerRealNameAuth entity) {
        WorkerRealNameAuthVO vo = new WorkerRealNameAuthVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
