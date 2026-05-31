package com.parttime.platform.service;

import com.parttime.platform.pojo.vo.PageVO;
import com.parttime.platform.pojo.vo.WorkerRealNameAuthVO;

public interface WorkerRealNameAuthReviewService {
    PageVO<WorkerRealNameAuthVO> list(String status, int page, int pageSize);
    void approve(Long id, Long reviewerId);
    void reject(Long id, Long reviewerId, String reason);
}
