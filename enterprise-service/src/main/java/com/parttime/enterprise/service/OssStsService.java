package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.OssStsVO;

public interface OssStsService {

    OssStsVO issueSts(Long companyId, String biz);
}
