package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.CompanyWorkerMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.entity.CompanyWorker;
import com.parttime.enterprise.pojo.vo.WorkerListVO;
import com.parttime.enterprise.service.CompanyWorkerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyWorkerServiceImpl implements CompanyWorkerService {

    @Resource
    private CompanyWorkerMapper companyWorkerMapper;
    @Resource
    private WorkerSyncMapper workerSyncMapper;

    @Override
    public List<WorkerListVO> list(Long companyId, String keyword) {
        List<CompanyWorker> list = companyWorkerMapper.findByCompanyId(companyId, keyword);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public WorkerListVO detail(Long id) {
        CompanyWorker cw = companyWorkerMapper.findById(id)
                .orElseThrow(() -> new BusinessException("CompanyWorker not found: " + id));
        return toVO(cw);
    }

    @Override
    public void addWorker(Long companyId, Long workerId) {
        companyWorkerMapper.upsert(companyId, workerId);
    }

    @Override
    public void blacklist(Long id) {
        CompanyWorker cw = companyWorkerMapper.findById(id)
                .orElseThrow(() -> new BusinessException("CompanyWorker not found: " + id));
        companyWorkerMapper.updateStatus(id, "BLACKLISTED");
    }

    @Override
    public void unblacklist(Long id) {
        CompanyWorker cw = companyWorkerMapper.findById(id)
                .orElseThrow(() -> new BusinessException("CompanyWorker not found: " + id));
        companyWorkerMapper.updateStatus(id, "ACTIVE");
    }

    private WorkerListVO toVO(CompanyWorker cw) {
        WorkerListVO vo = new WorkerListVO();
        vo.setId(cw.getId());
        vo.setWorkerId(cw.getWorkerId());
        vo.setStatus(cw.getStatus());
        vo.setFirstContactAt(cw.getFirstContactAt());
        vo.setLastContactAt(cw.getLastContactAt());
        String name = workerSyncMapper.findWorkerNameById(cw.getWorkerId());
        vo.setName(name);
        String phone = workerSyncMapper.findWorkerPhoneById(cw.getWorkerId());
        vo.setPhone(phone);
        return vo;
    }
}
