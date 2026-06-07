package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.vo.NotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    int insert(NotificationVO notification);
    List<NotificationVO> findByRecipientId(@Param("recipientId") Long recipientId, @Param("recipientType") String recipientType,
                                           @Param("offset") int offset, @Param("pageSize") int pageSize);
    long countByRecipientId(@Param("recipientId") Long recipientId, @Param("recipientType") String recipientType);
    int markAsRead(@Param("id") Long id, @Param("recipientId") Long recipientId, @Param("recipientType") String recipientType);
}
