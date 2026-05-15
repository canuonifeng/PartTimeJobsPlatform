package com.parttime.enterprise.infrastructure.mapper;

import com.parttime.enterprise.core.domain.NotificationTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface NotificationTemplateMapper {

    List<NotificationTemplate> findByType(String type);

    List<NotificationTemplate> findByTypeAndChannel(@Param("type") String type, @Param("channel") String channel);

    List<NotificationTemplate> findAll(@Param("type") String type, @Param("channel") String channel);

    Optional<NotificationTemplate> findById(Long id);

    int insert(NotificationTemplate template);

    int update(NotificationTemplate template);

    int deleteById(Long id);
}
