package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.CsCloseSessionCmd;
import com.parttime.platform.pojo.cmd.CsSendMessageCmd;
import com.parttime.platform.pojo.cmd.CsSessionQueryCmd;
import com.parttime.platform.pojo.vo.CsMessageVO;
import com.parttime.platform.pojo.vo.CsSessionVO;

import java.util.List;

public interface CsSessionService {

    List<CsSessionVO> list(CsSessionQueryCmd cmd);

    List<CsMessageVO> messages(Long sessionId);

    void sendMessage(CsSendMessageCmd cmd, Long agentId, String agentName);

    void accept(Long sessionId, Long agentId, String agentName);

    void close(CsCloseSessionCmd cmd);
}
