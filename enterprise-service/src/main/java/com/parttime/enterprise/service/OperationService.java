package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.OperationTodoActionCmd;
import com.parttime.enterprise.pojo.vo.OperationDashboardVO;
import com.parttime.enterprise.pojo.vo.OperationExceptionVO;
import com.parttime.enterprise.pojo.vo.OperationProcessNodeVO;
import com.parttime.enterprise.pojo.vo.OperationTodoItemVO;
import com.parttime.enterprise.pojo.vo.OperationTrendVO;
import com.parttime.enterprise.pojo.vo.PageVO;

import java.util.List;

public interface OperationService {
    OperationDashboardVO getDashboard(Long companyId);
    List<OperationProcessNodeVO> getProcess(Long companyId);
    PageVO<OperationTodoItemVO> getTodos(Long companyId, String type, Integer page, Integer pageSize);
    PageVO<OperationExceptionVO> getExceptions(Long companyId, Integer page, Integer pageSize);
    OperationTrendVO getTrends(Long companyId);
    void executeTodoAction(Long companyId, String todoId, OperationTodoActionCmd cmd);
}
