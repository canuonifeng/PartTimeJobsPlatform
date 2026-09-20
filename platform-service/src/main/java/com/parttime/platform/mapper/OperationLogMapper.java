package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OperationLogMapper {

    List<OperationLog> findByFilters(@Param("module") String module,
                                     @Param("operatorName") String operatorName,
                                     @Param("operationType") String operationType);

    int insert(OperationLog log);
}
