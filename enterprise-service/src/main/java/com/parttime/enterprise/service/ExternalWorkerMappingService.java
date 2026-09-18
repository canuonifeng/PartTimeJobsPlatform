package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.entity.ExternalWorkerMapping;

import java.util.List;

public interface ExternalWorkerMappingService {

    void createMapping(Long workerId, String externalSystemType, String externalWorkerId, Long companyId);

    List<ExternalWorkerMapping> getMappingsByWorkerId(Long workerId);

    ExternalWorkerMapping getMappingByExternalId(String externalSystemType, String externalWorkerId);
}
