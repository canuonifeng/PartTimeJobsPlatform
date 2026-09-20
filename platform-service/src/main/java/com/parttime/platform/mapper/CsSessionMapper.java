package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.CsSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CsSessionMapper {

    Optional<CsSession> findById(@Param("id") Long id);

    List<CsSession> findByFilters(@Param("status") String status,
                                 @Param("userType") String userType,
                                 @Param("keyword") String keyword);

    int insert(CsSession session);

    int accept(@Param("id") Long id, @Param("agentId") Long agentId, @Param("agentName") String agentName);

    int close(@Param("id") Long id);

    int updateLastMessage(@Param("id") Long id, @Param("lastMessage") String lastMessage);

    Integer countMessages(@Param("sessionId") Long sessionId);

    Integer countUnread(@Param("sessionId") Long sessionId);
}
