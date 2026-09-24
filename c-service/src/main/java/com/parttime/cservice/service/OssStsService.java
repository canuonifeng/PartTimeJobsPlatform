package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.OssStsVO;

public interface OssStsService {

    OssStsVO issueSts(Long workerId, String biz);
}
