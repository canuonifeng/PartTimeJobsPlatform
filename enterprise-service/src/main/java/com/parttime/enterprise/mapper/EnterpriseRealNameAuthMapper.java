package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.EnterpriseRealNameAuth;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface EnterpriseRealNameAuthMapper {
    int insert(EnterpriseRealNameAuth auth);
    int update(EnterpriseRealNameAuth auth);
    Optional<EnterpriseRealNameAuth> findByEnterpriseId(Long enterpriseId);
}
