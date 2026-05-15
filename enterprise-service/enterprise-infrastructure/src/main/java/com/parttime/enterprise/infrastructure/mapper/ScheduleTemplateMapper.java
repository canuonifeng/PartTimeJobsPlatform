package com.parttime.enterprise.infrastructure.mapper;

import com.parttime.enterprise.core.domain.ScheduleTemplate;
import com.parttime.enterprise.core.domain.ScheduleTemplateSlot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ScheduleTemplateMapper {

    int insert(ScheduleTemplate template);

    Optional<ScheduleTemplate> findById(Long id);

    List<ScheduleTemplate> findByCompanyId(Long companyId);

    int update(ScheduleTemplate template);

    int delete(Long id);

    int insertSlot(ScheduleTemplateSlot slot);

    List<ScheduleTemplateSlot> findSlotsByTemplateId(Long templateId);

    int deleteSlotsByTemplateId(Long templateId);
}
