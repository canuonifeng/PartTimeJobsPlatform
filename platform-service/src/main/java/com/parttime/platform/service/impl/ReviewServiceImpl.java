package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.ReviewMapper;
import com.parttime.platform.pojo.cmd.CreditScoreAdjustCmd;
import com.parttime.platform.pojo.cmd.ReviewQueryCmd;
import com.parttime.platform.pojo.cmd.ReviewViolationCmd;
import com.parttime.platform.pojo.entity.Review;
import com.parttime.platform.pojo.vo.ReviewVO;
import com.parttime.platform.service.ReviewService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private ReviewMapper reviewMapper;

    @Override
    public List<ReviewVO> list(ReviewQueryCmd cmd) {
        ReviewQueryCmd q = cmd != null ? cmd : new ReviewQueryCmd();
        List<Review> list = reviewMapper.findByFilters(q.getReviewType(), q.getIsViolation(), q.getRating(), q.getKeyword());
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ReviewVO detail(Long id) {
        Review review = reviewMapper.findById(id)
                .orElseThrow(() -> new BusinessException("评价不存在: " + id));
        return toVO(review);
    }

    @Override
    public void markViolation(ReviewViolationCmd cmd) {
        reviewMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("评价不存在: " + cmd.getId()));
        reviewMapper.markViolation(cmd.getId(), cmd.getViolationReason(), cmd.getOperatorName());
    }

    @Override
    public void delete(Long id) {
        reviewMapper.findById(id)
                .orElseThrow(() -> new BusinessException("评价不存在: " + id));
        reviewMapper.softDelete(id);
    }

    @Override
    @Transactional
    public void adjustCreditScore(CreditScoreAdjustCmd cmd) {
        if (cmd.getDelta() == null || cmd.getDelta() == 0) {
            throw new BusinessException("信用分调整分值不能为空且不能为0");
        }
        int affected;
        if ("WORKER".equalsIgnoreCase(cmd.getTargetType())) {
            affected = reviewMapper.updateWorkerCreditScore(cmd.getTargetId(), cmd.getDelta());
        } else if ("ENTERPRISE".equalsIgnoreCase(cmd.getTargetType())) {
            affected = reviewMapper.updateEnterpriseCreditScore(cmd.getTargetId(), cmd.getDelta());
        } else {
            throw new BusinessException("不支持的目标类型: " + cmd.getTargetType());
        }
        if (affected == 0) {
            throw new BusinessException("目标不存在或未更新: " + cmd.getTargetType() + " " + cmd.getTargetId());
        }
        reviewMapper.insertCreditScoreLog(cmd.getTargetType(), cmd.getTargetId(), cmd.getDelta(),
                cmd.getReason(), cmd.getOperatorName());
    }

    private ReviewVO toVO(Review r) {
        ReviewVO vo = new ReviewVO();
        vo.setId(r.getId());
        vo.setReviewType(r.getReviewType());
        vo.setReviewerId(r.getReviewerId());
        vo.setReviewerName(r.getReviewerName());
        vo.setReviewerType(r.getReviewerType());
        vo.setRevieweeId(r.getRevieweeId());
        vo.setRevieweeName(r.getRevieweeName());
        vo.setRevieweeType(r.getRevieweeType());
        vo.setJobId(r.getJobId());
        vo.setJobTitle(r.getJobTitle());
        vo.setScheduleId(r.getScheduleId());
        vo.setRating(r.getRating());
        vo.setContent(r.getContent());
        vo.setImages(r.getImages());
        vo.setTags(r.getTags());
        vo.setIsAnonymous(r.getIsAnonymous());
        vo.setIsViolation(r.getIsViolation());
        vo.setViolationReason(r.getViolationReason());
        vo.setViolationHandledAt(r.getViolationHandledAt());
        vo.setViolationHandlerName(r.getViolationHandlerName());
        vo.setReplyContent(r.getReplyContent());
        vo.setReplyAt(r.getReplyAt());
        vo.setStatus(r.getStatus());
        vo.setHelpfulCount(r.getHelpfulCount());
        vo.setReportCount(r.getReportCount());
        vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }
}
