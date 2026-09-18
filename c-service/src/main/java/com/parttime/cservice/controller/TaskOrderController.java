package com.parttime.cservice.controller;

import com.parttime.cservice.pojo.vo.ApiResponse;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.TaskOrderVO;
import com.parttime.cservice.service.TaskOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/worker/jobs/task-orders")
public class TaskOrderController {

    @Resource
    private TaskOrderService taskOrderService;

    @Operation(summary = "获取我的任务单", description = "分页获取当前工人的标注任务单列表")
    @GetMapping("/my")
    public ApiResponse<PageVO<TaskOrderVO>> getMyTaskOrders(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ApiResponse.error(401, "未登录");
        }
        Long workerId = Long.valueOf(auth.getName());
        PageVO<TaskOrderVO> result = taskOrderService.getMyTaskOrders(workerId, page, pageSize);
        return ApiResponse.success(result);
    }
}
