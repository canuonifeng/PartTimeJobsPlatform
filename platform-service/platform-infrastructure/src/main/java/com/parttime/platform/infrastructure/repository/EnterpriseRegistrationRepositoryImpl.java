package com.parttime.platform.infrastructure.repository;

import com.parttime.platform.core.domain.EnterpriseRegistration;
import com.parttime.platform.core.repository.EnterpriseRegistrationRepository;
import com.parttime.platform.infrastructure.mapper.EnterpriseRegistrationMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EnterpriseRegistrationRepositoryImpl implements EnterpriseRegistrationRepository {

    private final EnterpriseRegistrationMapper mapper;

    public EnterpriseRegistrationRepositoryImpl(EnterpriseRegistrationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<EnterpriseRegistration> findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public List<EnterpriseRegistration> findByStatus(String status) {
        return mapper.findByStatus(status);
    }

    @Override
    public List<EnterpriseRegistration> findAll() {
        return mapper.findAll();
    }

    @Override
    public void update(EnterpriseRegistration registration) {
        mapper.update(registration);
    }
}
