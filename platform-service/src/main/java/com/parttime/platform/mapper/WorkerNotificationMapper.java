package com.parttime.platform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorkerNotificationMapper {
    int insertWorkerNotification(@Param("workerId") Long workerId,
                                 @Param("type") String type,
                                 @Param("category") String category,
                                 @Param("title") String title,
                                 @Param("content") String content,
                                 @Param("relatedType") String relatedType,
                                 @Param("relatedId") Long relatedId);
}
