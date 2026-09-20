package com.parttime.platform.service.impl;

import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.CsMessageMapper;
import com.parttime.platform.mapper.CsSessionMapper;
import com.parttime.platform.pojo.cmd.CsCloseSessionCmd;
import com.parttime.platform.pojo.cmd.CsSendMessageCmd;
import com.parttime.platform.pojo.cmd.CsSessionQueryCmd;
import com.parttime.platform.pojo.entity.CsMessage;
import com.parttime.platform.pojo.entity.CsSession;
import com.parttime.platform.pojo.vo.CsMessageVO;
import com.parttime.platform.pojo.vo.CsSessionVO;
import com.parttime.platform.service.CsSessionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CsSessionServiceImpl implements CsSessionService {

    @Resource
    private CsSessionMapper csSessionMapper;

    @Resource
    private CsMessageMapper csMessageMapper;

    @Override
    public List<CsSessionVO> list(CsSessionQueryCmd cmd) {
        String status = blankToNull(cmd == null ? null : cmd.getStatus());
        String userType = blankToNull(cmd == null ? null : cmd.getUserType());
        String keyword = blankToNull(cmd == null ? null : cmd.getKeyword());
        return csSessionMapper.findByFilters(status, userType, keyword).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CsMessageVO> messages(Long sessionId) {
        require(sessionId);
        csMessageMapper.markReadBySession(sessionId);
        return csMessageMapper.findBySessionId(sessionId).stream()
                .map(this::toMessageVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void sendMessage(CsSendMessageCmd cmd, Long agentId, String agentName) {
        CsSession session = require(cmd.getSessionId());
        if ("CLOSED".equals(session.getStatus())) {
            throw new BusinessException("会话已关闭，无法发送消息");
        }
        CsMessage message = new CsMessage();
        message.setSessionId(cmd.getSessionId());
        message.setSenderType("AGENT");
        message.setSenderName(agentName);
        message.setContent(cmd.getContent());
        message.setIsRead(1);
        csMessageMapper.insert(message);
        csSessionMapper.updateLastMessage(cmd.getSessionId(), cmd.getContent());
    }

    @Override
    public void accept(Long sessionId, Long agentId, String agentName) {
        require(sessionId);
        csSessionMapper.accept(sessionId, agentId, agentName);
    }

    @Override
    public void close(CsCloseSessionCmd cmd) {
        require(cmd.getSessionId());
        csSessionMapper.close(cmd.getSessionId());
    }

    private CsSession require(Long id) {
        return csSessionMapper.findById(id)
                .orElseThrow(() -> new BusinessException("会话不存在: " + id));
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private CsSessionVO toVO(CsSession s) {
        CsSessionVO vo = new CsSessionVO();
        vo.setId(s.getId());
        vo.setSessionNo(s.getSessionNo());
        vo.setUserType(s.getUserType());
        vo.setUserId(s.getUserId());
        vo.setUserName(s.getUserName());
        vo.setUserPhone(s.getUserPhone());
        vo.setAgentName(s.getAgentName());
        vo.setStatus(s.getStatus());
        vo.setLastMessage(s.getLastMessage());
        Integer total = csSessionMapper.countMessages(s.getId());
        Integer unread = csSessionMapper.countUnread(s.getId());
        vo.setMessageCount(total == null ? 0 : total);
        vo.setUnreadCount(unread == null ? 0 : unread);
        vo.setCreatedAt(s.getCreatedAt());
        vo.setLastMessageAt(s.getLastMessageAt());
        return vo;
    }

    private CsMessageVO toMessageVO(CsMessage m) {
        CsMessageVO vo = new CsMessageVO();
        vo.setId(m.getId());
        vo.setSessionId(m.getSessionId());
        vo.setSenderType(m.getSenderType());
        vo.setSenderName(m.getSenderName());
        vo.setContent(m.getContent());
        vo.setCreatedAt(m.getCreatedAt());
        return vo;
    }
}
