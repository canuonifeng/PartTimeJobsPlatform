package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.IdCmd;
import com.parttime.platform.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cs")
public class CustomerServiceController {

    @Operation(summary = "获取会话列表")
    @PostMapping("/sessions")
    public ApiResponse<List<Map<String, Object>>> sessions(@RequestBody(required = false) Map<String, String> body) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] statuses = {"WAITING", "PROCESSING", "CLOSED"};
        String[] types = {"WORKER", "ENTERPRISE"};
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", (long)i);
            item.put("sessionId", "SESSION-" + System.currentTimeMillis() + "-" + i);
            item.put("userType", types[i % 2]);
            item.put("userName", "用户" + i);
            item.put("userPhone", "1380000" + String.format("%02d", i));
            item.put("status", statuses[i % 3]);
            item.put("agentName", i % 3 == 2 ? null : "客服" + ((i % 2) + 1));
            item.put("lastMessage", "最后一条消息内容" + i);
            item.put("messageCount", 5 + (int)(Math.random() * 10));
            item.put("createdAt", LocalDateTime.now().minusMinutes(i * 15).toString());
            item.put("lastMessageAt", LocalDateTime.now().minusMinutes(i * 5).toString());
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "获取会话消息")
    @PostMapping("/messages")
    public ApiResponse<List<Map<String, Object>>> messages(@RequestBody IdCmd body) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] types = {"USER", "AGENT", "SYSTEM"};
        for (int i = 1; i <= 15; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", (long)i);
            item.put("type", types[i % 3]);
            item.put("senderName", types[i % 3].equals("USER") ? "用户" : "客服");
            item.put("content", "消息内容" + i + " - 这是一条测试消息");
            item.put("createdAt", LocalDateTime.now().minusMinutes(15 - i).toString());
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "发送消息")
    @PostMapping("/send-message")
    public ApiResponse<Void> sendMessage(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }

    @Operation(summary = "接起会话")
    @PostMapping("/accept-session")
    public ApiResponse<Void> acceptSession(@RequestBody IdCmd body) {
        return ApiResponse.success();
    }

    @Operation(summary = "关闭会话")
    @PostMapping("/close-session")
    public ApiResponse<Void> closeSession(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }

    @Operation(summary = "获取FAQ列表")
    @PostMapping("/faqs")
    public ApiResponse<List<Map<String, Object>>> faqs(@RequestBody(required = false) Map<String, String> body) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] categories = {"账户相关", "报名相关", "考勤结算", "提现问题", "其他"};
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i);
            item.put("category", categories[i % 5]);
            item.put("question", "常见问题" + i + "：这是一个常见的问题？");
            item.put("answer", "这是问题的答案：请按照以下步骤操作，首先登录账号，然后...");
            item.put("sortOrder", i);
            item.put("enabled", true);
            item.put("viewCount", 100 * i);
            item.put("createdAt", LocalDateTime.now().minusDays(i * 5).toString());
            list.add(item);
        }
        return ApiResponse.success(list);
    }

    @Operation(summary = "创建FAQ")
    @PostMapping("/faqs/create")
    public ApiResponse<Void> createFaq(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }

    @Operation(summary = "更新FAQ")
    @PostMapping("/faqs/update")
    public ApiResponse<Void> updateFaq(@RequestBody Map<String, Object> body) {
        return ApiResponse.success();
    }

    @Operation(summary = "删除FAQ")
    @PostMapping("/faqs/delete")
    public ApiResponse<Void> deleteFaq(@RequestBody IdCmd body) {
        return ApiResponse.success();
    }
}
