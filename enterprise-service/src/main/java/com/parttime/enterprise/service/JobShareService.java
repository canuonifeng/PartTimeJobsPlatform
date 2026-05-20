package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.JobShareCodeVO;
import com.parttime.enterprise.pojo.vo.JobShareLinkVO;

public interface JobShareService {

    JobShareCodeVO getShareCode(Long jobId);

    JobShareLinkVO getShareLink(Long jobId);
}
