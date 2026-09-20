package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.OperationLogMapper;
import com.parttime.platform.pojo.cmd.OperationLogQueryCmd;
import com.parttime.platform.pojo.entity.OperationLog;
import com.parttime.platform.pojo.vo.OperationLogVO;
import com.parttime.platform.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public List<OperationLogVO> list(OperationLogQueryCmd cmd) {
        String module = cmd == null ? null : cmd.getModule();
        String operatorName = cmd == null ? null : cmd.getOperatorName();
        String operationType = cmd == null ? null : cmd.getOperationType();
        return operationLogMapper.findByFilters(module, operatorName, operationType)
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    private OperationLogVO toVO(OperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(log.getId());
        vo.setOperatorName(log.getOperatorName());
        vo.setModule(log.getModule());
        vo.setOperationType(log.getOperationType());
        vo.setTargetType(log.getTargetType());
        vo.setTargetId(log.getTargetId());
        vo.setTargetName(log.getTargetName());
        vo.setIpAddress(log.getIpAddress());
        vo.setRequestMethod(log.getRequestMethod());
        vo.setRequestUrl(log.getRequestUrl());
        vo.setResult(log.getResult());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }
}
