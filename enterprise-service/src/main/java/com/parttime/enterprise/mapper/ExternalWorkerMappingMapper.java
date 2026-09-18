package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.ExternalWorkerMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExternalWorkerMappingMapper {

    int insert(ExternalWorkerMapping mapping);

    ExternalWorkerMapping selectByExternalId(@Param("externalSystemType") String externalSystemType,
                                             @Param("externalWorkerId") String externalWorkerId);

    List<ExternalWorkerMapping> selectByWorkerId(Long workerId);
}
