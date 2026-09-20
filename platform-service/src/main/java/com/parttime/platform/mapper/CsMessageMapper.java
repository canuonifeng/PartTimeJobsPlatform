package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.CsMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CsMessageMapper {

    List<CsMessage> findBySessionId(@Param("sessionId") Long sessionId);

    int insert(CsMessage message);

    int markReadBySession(@Param("sessionId") Long sessionId);
}
