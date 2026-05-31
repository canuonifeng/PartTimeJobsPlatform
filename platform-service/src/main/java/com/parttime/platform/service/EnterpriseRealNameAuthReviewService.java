package com.parttime.platform.service;

import com.parttime.platform.pojo.vo.EnterpriseRealNameAuthVO;
import com.parttime.platform.pojo.vo.PageVO;

public interface EnterpriseRealNameAuthReviewService {
    PageVO<EnterpriseRealNameAuthVO> list(String status, int page, int pageSize);
    void approve(Long id, Long reviewerId);
    void reject(Long id, Long reviewerId, String reason);
}
