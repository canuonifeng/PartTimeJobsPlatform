package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.CreditScoreAdjustCmd;
import com.parttime.platform.pojo.cmd.ReviewQueryCmd;
import com.parttime.platform.pojo.cmd.ReviewViolationCmd;
import com.parttime.platform.pojo.vo.ReviewVO;

import java.util.List;

public interface ReviewService {
    List<ReviewVO> list(ReviewQueryCmd cmd);
    ReviewVO detail(Long id);
    void markViolation(ReviewViolationCmd cmd);
    void delete(Long id);
    void adjustCreditScore(CreditScoreAdjustCmd cmd);
}
