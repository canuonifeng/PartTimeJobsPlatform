package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.EnterpriseAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface EnterpriseAccountMapper {

    List<EnterpriseAccount> findByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    Optional<EnterpriseAccount> findById(@Param("id") Long id);

    Optional<EnterpriseAccount> findByUsername(@Param("username") String username);

    int insert(EnterpriseAccount account);

    int update(EnterpriseAccount account);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    int deleteById(@Param("id") Long id);
}
