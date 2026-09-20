package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.CsCloseSessionCmd;
import com.parttime.platform.pojo.cmd.CsSendMessageCmd;
import com.parttime.platform.pojo.cmd.CsSessionQueryCmd;
import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import com.parttime.platform.pojo.vo.CsMessageVO;
import com.parttime.platform.pojo.vo.CsSessionVO;
import com.parttime.platform.service.CsSessionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cs")
public class CustomerServiceController {

    private static final String DEFAULT_AGENT_NAME = "在线客服";

    @Resource
    private CsSessionService csSessionService;

    @Operation(summary = "获取会话列表")
    @PostMapping("/sessions")
    public ApiResponse<List<CsSessionVO>> sessions(@RequestBody(required = false) CsSessionQueryCmd body) {
        return ApiResponse.success(csSessionService.list(body));
    }

    @Operation(summary = "获取会话消息")
    @PostMapping("/messages")
    public ApiResponse<List<CsMessageVO>> messages(@RequestBody IdCmd body) {
        return ApiResponse.success(csSessionService.messages(body.getId()));
    }

    @Operation(summary = "发送消息")
    @PostMapping("/send-message")
    public ApiResponse<Void> sendMessage(@RequestBody CsSendMessageCmd body) {
        csSessionService.sendMessage(body, currentAgentId(), currentAgentName());
        return ApiResponse.success();
    }

    @Operation(summary = "接起会话")
    @PostMapping("/accept-session")
    public ApiResponse<Void> acceptSession(@RequestBody IdCmd body) {
        csSessionService.accept(body.getId(), currentAgentId(), currentAgentName());
        return ApiResponse.success();
    }

    @Operation(summary = "关闭会话")
    @PostMapping("/close-session")
    public ApiResponse<Void> closeSession(@RequestBody CsCloseSessionCmd body) {
        csSessionService.close(body);
        return ApiResponse.success();
    }

    private Long currentAgentId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return 0L;
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String currentAgentName() {
        return DEFAULT_AGENT_NAME;
    }
}
