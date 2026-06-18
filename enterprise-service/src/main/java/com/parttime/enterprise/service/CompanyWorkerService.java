package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.pojo.vo.WorkerListVO;

public interface CompanyWorkerService {
    PageVO<WorkerListVO> list(Long companyId, String keyword, int page, int pageSize);
    WorkerListVO detail(Long id);
    void addWorker(Long companyId, Long workerId);
    void blacklist(Long id);
    void unblacklist(Long id);
}
