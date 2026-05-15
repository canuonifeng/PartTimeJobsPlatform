package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.EvaluationCmd;
import com.parttime.enterprise.pojo.vo.EvaluationVO;
import com.parttime.enterprise.pojo.vo.WorkHistoryVO;
import com.parttime.enterprise.pojo.vo.WorkerProfileVO;

import java.util.List;

public interface WorkerProfileService {

    WorkerProfileVO getWorkerProfile(Long companyId, Long workerId);

    EvaluationVO evaluateWorker(Long companyId, Long jobId, Long workerId, Integer rating, String comment);

    void addToBlacklist(Long companyId, Long workerId, String reason);

    void removeFromBlacklist(Long companyId, Long workerId);

    boolean isBlacklisted(Long companyId, Long workerId);

    List<WorkHistoryVO> getWorkHistory(Long workerId, Long companyId);

    List<EvaluationVO> getEvaluations(Long workerId, Long companyId);
}
