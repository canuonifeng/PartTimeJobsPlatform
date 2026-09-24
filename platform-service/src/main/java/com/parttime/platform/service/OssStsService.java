package com.parttime.platform.service;

import com.parttime.platform.pojo.vo.OssStsVO;

public interface OssStsService {

    OssStsVO issueSts(String adminName, String biz);
}
