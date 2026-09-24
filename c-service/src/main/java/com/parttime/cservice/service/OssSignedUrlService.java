package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.SignedUrlVO;

public interface OssSignedUrlService {

    SignedUrlVO getSignedUrl(Long workerId, String key);
}
