package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.SignedUrlVO;

public interface OssSignedUrlService {

    SignedUrlVO getSignedUrl(Long companyId, String key);
}
