package com.parttime.platform.core.repository;

import com.parttime.platform.core.domain.EnterpriseRegistration;

import java.util.List;
import java.util.Optional;

public interface EnterpriseRegistrationRepository {

    Optional<EnterpriseRegistration> findById(Long id);

    List<EnterpriseRegistration> findByStatus(String status);

    List<EnterpriseRegistration> findAll();

    void update(EnterpriseRegistration registration);
}
