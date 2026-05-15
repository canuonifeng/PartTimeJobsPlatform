package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.WorkerBlacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface WorkerBlacklistMapper {

    int insert(WorkerBlacklist blacklist);

    Optional<WorkerBlacklist> findByCompanyIdAndWorkerId(@Param("companyId") Long companyId, @Param("workerId") Long workerId);

    int deleteByCompanyIdAndWorkerId(@Param("companyId") Long companyId, @Param("workerId") Long workerId);
}
