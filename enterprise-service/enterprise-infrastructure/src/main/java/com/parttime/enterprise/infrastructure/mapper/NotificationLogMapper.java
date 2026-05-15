package com.parttime.enterprise.infrastructure.mapper;

import com.parttime.enterprise.core.domain.NotificationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface NotificationLogMapper {

    int insert(NotificationLog log);

    Optional<NotificationLog> findById(Long id);

    List<NotificationLog> findByRecipientIdAndRecipientType(@Param("recipientId") Long recipientId, @Param("recipientType") String recipientType);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updateStatusWithError(@Param("id") Long id, @Param("status") String status, @Param("errorMessage") String errorMessage);

    int markSent(@Param("id") Long id, @Param("sentAt") LocalDateTime sentAt);
}
