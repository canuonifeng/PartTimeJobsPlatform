package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.OperationTodoActionCmd;
import com.parttime.enterprise.pojo.vo.ApiResponse;
import com.parttime.enterprise.pojo.vo.OperationDashboardVO;
import com.parttime.enterprise.pojo.vo.OperationExceptionVO;
import com.parttime.enterprise.pojo.vo.OperationProcessNodeVO;
import com.parttime.enterprise.pojo.vo.OperationTodoItemVO;
import com.parttime.enterprise.pojo.vo.OperationTrendVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.OperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/enterprise/operations")
public class OperationController {

    @Resource
    private OperationService operationService;

    @Operation(summary = "企业运营首页聚合数据")
    @GetMapping("/dashboard")
    public ApiResponse<OperationDashboardVO> dashboard() {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(operationService.getDashboard(companyId));
    }

    @Operation(summary = "企业运营流程节点")
    @GetMapping("/process")
    public ApiResponse<List<OperationProcessNodeVO>> process() {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(operationService.getProcess(companyId));
    }

    @Operation(summary = "企业运营待办分页")
    @GetMapping("/todos")
    public ApiResponse<PageVO<OperationTodoItemVO>> todos(
            @Parameter(description = "待办类型: APPLICATION/SCHEDULE/ATTENDANCE/SALARY") @RequestParam(required = false) String type,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(operationService.getTodos(companyId, type, page, pageSize));
    }

    @Operation(summary = "企业运营异常提醒分页")
    @GetMapping("/exceptions")
    public ApiResponse<PageVO<OperationExceptionVO>> exceptions(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(operationService.getExceptions(companyId, page, pageSize));
    }

    @Operation(summary = "企业运营近7天趋势")
    @GetMapping("/trends")
    public ApiResponse<OperationTrendVO> trends() {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ApiResponse.success(operationService.getTrends(companyId));
    }

    @Operation(summary = "执行企业运营待办动作")
    @PostMapping("/todos/actions")
    public ApiResponse<Void> executeTodoAction(@RequestBody OperationTodoActionCmd cmd) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        operationService.executeTodoAction(companyId, cmd.getTodoId(), cmd);
        return ApiResponse.success();
    }
}
