package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.OperationMapper;
import com.parttime.enterprise.pojo.cmd.OperationTodoActionCmd;
import com.parttime.enterprise.pojo.vo.OperationDashboardVO;
import com.parttime.enterprise.pojo.vo.OperationExceptionVO;
import com.parttime.enterprise.pojo.vo.OperationOverviewVO;
import com.parttime.enterprise.pojo.vo.OperationProcessNodeVO;
import com.parttime.enterprise.pojo.vo.OperationTodayVO;
import com.parttime.enterprise.pojo.vo.OperationTodoItemVO;
import com.parttime.enterprise.pojo.vo.OperationTodoSummaryVO;
import com.parttime.enterprise.pojo.vo.OperationTrendPointVO;
import com.parttime.enterprise.pojo.vo.OperationTrendVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.ApplicationService;
import com.parttime.enterprise.service.OperationService;
import com.parttime.enterprise.service.ScheduleService;
import com.parttime.enterprise.service.SettlementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class OperationServiceImpl implements OperationService {

    @Resource
    private OperationMapper operationMapper;

    @Resource
    private ApplicationService applicationService;

    @Resource
    private ScheduleService scheduleService;

    @Resource
    private SettlementService settlementService;

    @Override
    public OperationDashboardVO getDashboard(Long companyId) {
        OperationDashboardVO dashboard = new OperationDashboardVO();
        dashboard.setOverview(buildOverview(companyId));
        dashboard.setToday(buildToday(companyId));
        dashboard.setProcess(getProcess(companyId));
        dashboard.setTodoSummary(buildTodoSummary(companyId));
        dashboard.setTrendSummary(buildTrendSummary(companyId));
        return dashboard;
    }

    @Override
    public List<OperationProcessNodeVO> getProcess(Long companyId) {
        LocalDate today = LocalDate.now();
        long publishedJobs = operationMapper.countPublishedJobs(companyId);
        long pendingApplications = operationMapper.countPendingApplications(companyId);
        long attendanceTodos = operationMapper.countScheduleTodos(companyId, today);
        long unpaidSalary = operationMapper.countSalaryTodos(companyId);
        List<OperationProcessNodeVO> nodes = new ArrayList<>();
        nodes.add(node("PUBLISH", "发布职位", publishedJobs, "NORMAL", publishedJobs + " 个岗位正在招聘中", Collections.singletonList("发布中 " + publishedJobs), "/pages/jobs/jobList"));
        nodes.add(node("REVIEW", "审核报名", pendingApplications, pendingApplications > 0 ? "WARNING" : "NORMAL", pendingApplications + " 人等待审核", Collections.singletonList("待审核 " + pendingApplications), "/pages/applications/applicationList?status=PENDING"));
        nodes.add(node("ATTENDANCE", "考勤确认", attendanceTodos, attendanceTodos > 0 ? "WARNING" : "NORMAL", attendanceTodos + " 条考勤待确认", Collections.singletonList("待确认 " + attendanceTodos), "/pages/schedules/scheduleList?status=SCHEDULED"));
        nodes.add(node("SALARY", "薪资结算", unpaidSalary, unpaidSalary > 0 ? "TODO" : "NORMAL", unpaidSalary + " 条薪资待结算", Collections.singletonList("待结算 " + unpaidSalary), "/pages/attendance/attendanceList?settlementStatus=UNPAID"));
        return nodes;
    }

    @Override
    public PageVO<OperationTodoItemVO> getTodos(Long companyId, String type, Integer page, Integer pageSize) {
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);
        int offset = (safePage - 1) * safePageSize;
        String normalizedType = type == null ? "" : type.trim().toUpperCase();
        List<OperationTodoItemVO> records;
        long total;
        LocalDate today = LocalDate.now();
        switch (normalizedType) {
            case "APPLICATION" -> {
                records = operationMapper.findApplicationTodos(companyId, offset, safePageSize);
                total = operationMapper.countApplicationTodos(companyId);
            }
            case "SCHEDULE" -> {
                records = operationMapper.findScheduleTodos(companyId, today, offset, safePageSize);
                total = operationMapper.countScheduleTodos(companyId, today);
            }
            case "ATTENDANCE" -> {
                records = operationMapper.findAttendanceConfirmTodos(companyId, today, offset, safePageSize);
                total = operationMapper.countScheduleTodos(companyId, today);
            }
            case "SALARY" -> {
                records = operationMapper.findSalaryTodos(companyId, offset, safePageSize);
                total = operationMapper.countSalaryTodos(companyId);
            }
            default -> {
                records = new ArrayList<>();
                int remaining = safePageSize;
                List<OperationTodoItemVO> applicationTodos = operationMapper.findApplicationTodos(companyId, 0, remaining);
                records.addAll(applicationTodos);
                remaining -= applicationTodos.size();
                if (remaining > 0) {
                    List<OperationTodoItemVO> attendanceTodos = operationMapper.findAttendanceConfirmTodos(companyId, today, 0, remaining);
                    records.addAll(attendanceTodos);
                    remaining -= attendanceTodos.size();
                }
                if (remaining > 0) {
                    records.addAll(operationMapper.findSalaryTodos(companyId, 0, remaining));
                }
                total = operationMapper.countApplicationTodos(companyId)
                        + operationMapper.countScheduleTodos(companyId, today)
                        + operationMapper.countSalaryTodos(companyId);
            }
        }
        fillActions(records);
        return new PageVO<>(records, total);
    }

    @Override
    public PageVO<OperationExceptionVO> getExceptions(Long companyId, Integer page, Integer pageSize) {
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);
        int offset = (safePage - 1) * safePageSize;
        return new PageVO<>(operationMapper.findExceptions(companyId, offset, safePageSize), operationMapper.countExceptions(companyId));
    }

    @Override
    public OperationTrendVO getTrends(Long companyId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        OperationTrendVO vo = new OperationTrendVO();
        vo.setDays(operationMapper.findTrendPoints(companyId, startDate, endDate));
        return vo;
    }

    @Override
    @Transactional
    public void executeTodoAction(Long companyId, String todoId, OperationTodoActionCmd cmd) {
        if (todoId == null || cmd == null || cmd.getAction() == null) {
            throw new IllegalArgumentException("待办动作不能为空");
        }
        Long bizId = parseBizId(todoId);
        switch (cmd.getAction()) {
            case "ACCEPT_APPLICATION" -> applicationService.acceptApplication(bizId);
            case "REJECT_APPLICATION" -> applicationService.rejectApplication(bizId);
            case "CANCEL_SHIFT" -> scheduleService.removeShift(bizId);
            case "PAY_SALARY" -> settlementService.payFromAttendanceRecords(Collections.singletonList(bizId), companyId);
            default -> throw new IllegalArgumentException("不支持的待办动作: " + cmd.getAction());
        }
    }

    private OperationOverviewVO buildOverview(Long companyId) {
        LocalDate today = LocalDate.now();
        long applicationTodos = operationMapper.countApplicationTodos(companyId);
        long attendanceTodos = operationMapper.countScheduleTodos(companyId, today);
        long salaryTodos = operationMapper.countSalaryTodos(companyId);
        OperationOverviewVO overview = new OperationOverviewVO();
        overview.setPublishedJobCount(operationMapper.countPublishedJobs(companyId));
        overview.setTotalApplicationCount(operationMapper.countApplications(companyId));
        overview.setPendingTodoCount(applicationTodos + attendanceTodos + salaryTodos);
        overview.setTodayShiftCount(operationMapper.countTodayShifts(companyId, today));
        overview.setUnpaidSalaryCount(salaryTodos);
        return overview;
    }

    private OperationTodayVO buildToday(Long companyId) {
        LocalDate today = LocalDate.now();
        OperationTodayVO todayVO = new OperationTodayVO();
        todayVO.setNewApplicationCount(operationMapper.countTodayApplications(companyId, today));
        todayVO.setAttendanceExceptionCount(operationMapper.countAttendanceExceptions(companyId, today));
        todayVO.setScheduleGapCount(operationMapper.countScheduleTodos(companyId, today));
        return todayVO;
    }

    private List<OperationTodoSummaryVO> buildTodoSummary(Long companyId) {
        LocalDate today = LocalDate.now();
        List<OperationTodoSummaryVO> summaries = new ArrayList<>();
        summaries.add(summary("APPLICATION", "报名", operationMapper.countApplicationTodos(companyId)));
        summaries.add(summary("ATTENDANCE", "考勤", operationMapper.countScheduleTodos(companyId, today)));
        summaries.add(summary("SALARY", "薪资", operationMapper.countSalaryTodos(companyId)));
        return summaries;
    }

    private OperationDashboardVO.TrendSummary buildTrendSummary(Long companyId) {
        OperationTrendVO trend = getTrends(companyId);
        OperationDashboardVO.TrendSummary summary = new OperationDashboardVO.TrendSummary();
        long applicationTotal = 0;
        long shiftTotal = 0;
        BigDecimal salaryTotal = BigDecimal.ZERO;
        if (trend.getDays() != null) {
            for (OperationTrendPointVO point : trend.getDays()) {
                applicationTotal += point.getApplicationCount() == null ? 0 : point.getApplicationCount();
                shiftTotal += point.getShiftCount() == null ? 0 : point.getShiftCount();
                salaryTotal = salaryTotal.add(point.getSalaryAmount() == null ? BigDecimal.ZERO : point.getSalaryAmount());
            }
        }
        summary.setApplicationTotal(applicationTotal);
        summary.setShiftTotal(shiftTotal);
        summary.setSalaryTotalAmount(salaryTotal);
        return summary;
    }

    private OperationProcessNodeVO node(String code, String name, long count, String status, String description, List<String> tags, String routePath) {
        OperationProcessNodeVO node = new OperationProcessNodeVO();
        node.setCode(code);
        node.setName(name);
        node.setCount(count);
        node.setStatus(status);
        node.setDescription(description);
        node.setTags(tags);
        node.setRoutePath(routePath);
        return node;
    }

    private OperationTodoSummaryVO summary(String type, String name, long count) {
        OperationTodoSummaryVO summary = new OperationTodoSummaryVO();
        summary.setType(type);
        summary.setName(name);
        summary.setCount(count);
        return summary;
    }

    private void fillActions(List<OperationTodoItemVO> records) {
        for (OperationTodoItemVO item : records) {
            switch (item.getType()) {
                case "APPLICATION" -> item.setActions(List.of("ACCEPT_APPLICATION", "REJECT_APPLICATION"));
                case "SCHEDULE" -> item.setActions(List.of("CANCEL_SHIFT"));
                case "ATTENDANCE" -> item.setActions(Collections.emptyList());
                case "SALARY" -> item.setActions(List.of("PAY_SALARY"));
                default -> item.setActions(Collections.emptyList());
            }
        }
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) return 20;
        return Math.min(pageSize, 100);
    }

    private Long parseBizId(String todoId) {
        int idx = todoId.lastIndexOf('-');
        if (idx < 0 || idx == todoId.length() - 1) {
            throw new IllegalArgumentException("待办ID格式错误: " + todoId);
        }
        return Long.parseLong(todoId.substring(idx + 1));
    }
}
