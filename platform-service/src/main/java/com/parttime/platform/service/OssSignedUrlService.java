package com.parttime.platform.service;

import com.parttime.platform.pojo.vo.SignedUrlVO;

public interface OssSignedUrlService {

    SignedUrlVO getSignedUrl(Long adminId, String key);
}
