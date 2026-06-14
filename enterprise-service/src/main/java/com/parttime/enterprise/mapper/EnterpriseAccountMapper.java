package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.EnterpriseAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface EnterpriseAccountMapper {

    Optional<EnterpriseAccount> findByUsername(@Param("username") String username);

    List<EnterpriseAccount> findByEnterpriseIdPage(@Param("enterpriseId") Long enterpriseId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    long countByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    Optional<EnterpriseAccount> findById(@Param("id") Long id);

    int insert(EnterpriseAccount account);

    int update(EnterpriseAccount account);

    int updateProfile(@Param("id") Long id, @Param("displayName") String displayName, @Param("phone") String phone);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    int deleteById(@Param("id") Long id);
}
