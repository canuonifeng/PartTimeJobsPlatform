package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.WorkerUpdateCmd;
import com.parttime.platform.pojo.vo.WorkerVO;
import com.parttime.platform.service.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/workers")
public class WorkerController {

    @Resource
    private WorkerService workerService;

    @Operation(summary = "获取兼职列表")
    @PostMapping("/list")
    public List<WorkerVO> list(@RequestBody(required = false) Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        String keyword = body != null ? body.get("keyword") : null;
        return workerService.list(status, keyword);
    }

    @Operation(summary = "获取兼职详情")
    @PostMapping("/detail")
    public WorkerVO detail(@RequestBody Map<String, Long> body) {
        return workerService.detail(body.get("id"));
    }

    @Operation(summary = "编辑兼职信息")
    @PostMapping("/update")
    public WorkerVO update(@RequestBody WorkerUpdateCmd cmd) {
        return workerService.update(cmd);
    }

    @Operation(summary = "封禁兼职")
    @PostMapping("/ban")
    public void ban(@RequestBody Map<String, Long> body) {
        workerService.ban(body.get("id"));
    }

    @Operation(summary = "解封兼职")
    @PostMapping("/unban")
    public void unban(@RequestBody Map<String, Long> body) {
        workerService.unban(body.get("id"));
    }
}
