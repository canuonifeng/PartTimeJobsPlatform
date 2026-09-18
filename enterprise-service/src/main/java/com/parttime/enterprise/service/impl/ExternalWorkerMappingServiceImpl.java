package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.ExternalWorkerMappingMapper;
import com.parttime.enterprise.pojo.entity.ExternalWorkerMapping;
import com.parttime.enterprise.service.ExternalWorkerMappingService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExternalWorkerMappingServiceImpl implements ExternalWorkerMappingService {

    @Resource
    private ExternalWorkerMappingMapper externalWorkerMappingMapper;

    @Override
    public void createMapping(Long workerId, String externalSystemType, String externalWorkerId, Long companyId) {
        if (workerId == null || externalSystemType == null || externalWorkerId == null) {
            throw new BusinessException("workerId、externalSystemType、externalWorkerId不能为空");
        }
        ExternalWorkerMapping existing = externalWorkerMappingMapper.selectByExternalId(externalSystemType, externalWorkerId);
        if (existing != null) {
            throw new BusinessException("该外部系统工人ID已存在映射");
        }
        ExternalWorkerMapping mapping = new ExternalWorkerMapping();
        mapping.setWorkerId(workerId);
        mapping.setExternalSystemType(externalSystemType);
        mapping.setExternalWorkerId(externalWorkerId);
        mapping.setCompanyId(companyId);
        externalWorkerMappingMapper.insert(mapping);
    }

    @Override
    public List<ExternalWorkerMapping> getMappingsByWorkerId(Long workerId) {
        if (workerId == null) {
            throw new BusinessException("workerId不能为空");
        }
        return externalWorkerMappingMapper.selectByWorkerId(workerId);
    }

    @Override
    public ExternalWorkerMapping getMappingByExternalId(String externalSystemType, String externalWorkerId) {
        if (externalSystemType == null || externalWorkerId == null) {
            throw new BusinessException("externalSystemType、externalWorkerId不能为空");
        }
        return externalWorkerMappingMapper.selectByExternalId(externalSystemType, externalWorkerId);
    }
}
