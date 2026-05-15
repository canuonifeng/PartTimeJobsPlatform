package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.EnterpriseRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface EnterpriseRegistrationMapper {

    Optional<EnterpriseRegistration> findById(Long id);

    List<EnterpriseRegistration> findByStatus(String status);

    List<EnterpriseRegistration> findAll();

    int update(EnterpriseRegistration registration);
}
