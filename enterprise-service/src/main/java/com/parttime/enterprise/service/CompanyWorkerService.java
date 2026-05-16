package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.WorkerListVO;
import java.util.List;

public interface CompanyWorkerService {
    List<WorkerListVO> list(Long companyId, String keyword);
    WorkerListVO detail(Long id);
    void addWorker(Long companyId, Long workerId);
    void blacklist(Long id);
    void unblacklist(Long id);
}
