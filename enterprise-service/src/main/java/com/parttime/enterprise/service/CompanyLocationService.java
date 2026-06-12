package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.LocationCreateCmd;
import com.parttime.enterprise.pojo.cmd.LocationUpdateCmd;
import com.parttime.enterprise.pojo.vo.CompanyLocationVO;
import com.parttime.enterprise.pojo.vo.PageVO;

public interface CompanyLocationService {
    PageVO<CompanyLocationVO> list(Long companyId, Integer page, Integer pageSize);
    CompanyLocationVO create(LocationCreateCmd cmd, Long companyId);
    CompanyLocationVO update(LocationUpdateCmd cmd);
    void delete(Long id);
    void enable(Long id);
    void disable(Long id);
}
