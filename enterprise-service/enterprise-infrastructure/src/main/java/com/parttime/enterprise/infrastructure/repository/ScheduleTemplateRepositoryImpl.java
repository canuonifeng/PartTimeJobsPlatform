package com.parttime.enterprise.infrastructure.repository;

import com.parttime.enterprise.core.domain.ScheduleTemplate;
import com.parttime.enterprise.core.domain.ScheduleTemplateSlot;
import com.parttime.enterprise.core.repository.ScheduleTemplateRepository;
import com.parttime.enterprise.infrastructure.mapper.ScheduleTemplateMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ScheduleTemplateRepositoryImpl implements ScheduleTemplateRepository {

    private final ScheduleTemplateMapper mapper;

    public ScheduleTemplateRepositoryImpl(ScheduleTemplateMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(ScheduleTemplate template) {
        mapper.insert(template);
    }

    @Override
    public Optional<ScheduleTemplate> findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public List<ScheduleTemplate> findByCompanyId(Long companyId) {
        return mapper.findByCompanyId(companyId);
    }

    @Override
    public void update(ScheduleTemplate template) {
        mapper.update(template);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteSlotsByTemplateId(id);
        mapper.delete(id);
    }

    @Override
    public void saveSlot(ScheduleTemplateSlot slot) {
        mapper.insertSlot(slot);
    }

    @Override
    public List<ScheduleTemplateSlot> findSlotsByTemplateId(Long templateId) {
        return mapper.findSlotsByTemplateId(templateId);
    }

    @Override
    public void deleteSlotsByTemplateId(Long templateId) {
        mapper.deleteSlotsByTemplateId(templateId);
    }
}
