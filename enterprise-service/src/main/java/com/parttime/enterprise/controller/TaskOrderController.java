package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.entity.AnnotationTaskOrder;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.TaskOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/enterprise/task-orders")
public class TaskOrderController {

    @Resource
    private TaskOrderService taskOrderService;

    @Operation(summary = "获取任务单列表", description = "分页查询标注任务订单")
    @GetMapping
    public ApiResponse<PageVO<AnnotationTaskOrder>> getTaskOrders(
            @Parameter(description = "岗位ID") @RequestParam(required = false) Long jobId,
            @Parameter(description = "工人ID") @RequestParam(required = false) Long workerId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(taskOrderService.getTaskOrders(companyId, jobId, workerId, status, page, pageSize));
    }

    @Operation(summary = "获取任务单详情", description = "根据ID获取任务单详情")
    @GetMapping("/detail")
    public ApiResponse<AnnotationTaskOrder> getTaskOrderById(@RequestParam Long id) {
        return ApiResponse.success(taskOrderService.getTaskOrderById(id));
    }
}
