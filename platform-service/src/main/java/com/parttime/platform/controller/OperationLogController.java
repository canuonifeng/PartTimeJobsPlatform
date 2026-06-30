package com.parttime.platform.controller;

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
@RequestMapping("/api/admin/operation-logs")
public class OperationLogController {

    @Operation(summary = "获取操作日志列表")
    @PostMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestBody(required = false) Map<String, String> body) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] modules = {"职位管理", "报名审核", "考勤管理", "提现审核", "企业管理", "工人管理"};
        String[] actions = {"创建", "更新", "删除", "审核通过", "审核拒绝", "状态变更"};
        for (int i = 1; i <= 20; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i);
            item.put("operator", "admin");
            item.put("module", modules[i % modules.length]);
            item.put("action", actions[i % actions.length]);
            item.put("targetId", (long)(1000 + i));
            item.put("detail", "执行了" + actions[i % actions.length] + "操作，目标ID: " + (1000 + i));
            item.put("ipAddress", "192.168.1." + (i % 255));
            item.put("createdAt", LocalDateTime.now().minusMinutes(i * 30).toString());
            list.add(item);
        }
        return ApiResponse.success(list);
    }
}
