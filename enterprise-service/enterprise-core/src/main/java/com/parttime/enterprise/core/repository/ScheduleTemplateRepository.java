package com.parttime.enterprise.core.repository;

import com.parttime.enterprise.core.domain.ScheduleTemplate;
import com.parttime.enterprise.core.domain.ScheduleTemplateSlot;

import java.util.List;
import java.util.Optional;

public interface ScheduleTemplateRepository {

    void save(ScheduleTemplate template);

    Optional<ScheduleTemplate> findById(Long id);

    List<ScheduleTemplate> findByCompanyId(Long companyId);

    void update(ScheduleTemplate template);

    void delete(Long id);

    void saveSlot(ScheduleTemplateSlot slot);

    List<ScheduleTemplateSlot> findSlotsByTemplateId(Long templateId);

    void deleteSlotsByTemplateId(Long templateId);
}
