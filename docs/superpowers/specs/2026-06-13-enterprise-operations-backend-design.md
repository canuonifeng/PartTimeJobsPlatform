# 企业端运营中台后端接口设计

## 背景

企业端小程序已完成运营中台方向的 UI 重构，工作台、流程、待办和我的四个入口已经具备基本页面结构。目前工作台、流程页和待办页中的部分运营数字仍是前端静态数据。为了让企业端真正具备运营看板能力，需要在 `enterprise-service` 中新增一组聚合接口，基于现有职位、报名、排班、考勤和薪资数据返回真实统计、流程节点、待办列表、异常提醒和趋势数据。

## 范围

本次采用“运营模块方案”，在 `enterprise-service` 中新增独立运营聚合模块。

纳入范围：

- 新增运营中台 Controller、Service、Mapper 和 VO。
- 新增首页聚合接口。
- 新增流程页汇总接口。
- 新增待办分页接口。
- 新增异常提醒接口。
- 新增近 7 天趋势接口。
- 新增基础待办动作接口，复用现有业务 Service。
- 前端后续可用这些接口替换静态运营数据。

不纳入范围：

- 不新增真实待办表。
- 不新增历史数据迁移脚本。
- 不改变现有职位、报名、排班、考勤、薪资业务规则。
- 不替换现有业务列表接口。
- 不实现复杂筛选器，第一版使用固定混合口径。

## 设计目标

1. 为企业端小程序运营中台提供真实数据来源。
2. 保持聚合模块边界清晰，避免把统计逻辑分散到多个 Controller。
3. 优先复用现有表和 Service，不重复实现核心业务动作。
4. 支持首页一次加载，同时允许流程页、待办页、异常页和趋势页独立刷新。
5. 第一版控制复杂度，不引入任务表和事件一致性机制。

## 时间口径

采用混合口径：

- 核心待办：当前未处理，不限制当天。
- 今日事项：按当前自然日统计。
- 趋势数据：默认近 7 天，包含今天。
- 第一版不开放 `dateFrom/dateTo` 参数，后续可扩展。

## API 设计

所有接口位于 `enterprise-service`，统一前缀：`/api/operations`。

所有接口通过 `SecurityUtil.getCurrentCompanyId()` 获取当前企业 ID，返回 `ApiResponse<T>`。

### 首页聚合

```http
GET /api/operations/dashboard
```

返回首页首屏所需数据：

```json
{
  "overview": {
    "publishedJobCount": 6,
    "totalApplicationCount": 48,
    "pendingTodoCount": 12,
    "todayShiftCount": 9,
    "unpaidSalaryCount": 5
  },
  "today": {
    "newApplicationCount": 18,
    "attendanceExceptionCount": 2,
    "scheduleGapCount": 5
  },
  "process": [
    { "code": "PUBLISH", "name": "发布", "count": 6, "status": "NORMAL", "routePath": "/pages/jobs/jobList" },
    { "code": "APPLICATION", "name": "报名", "count": 48, "status": "NORMAL", "routePath": "/pages/applications/applicationList" },
    { "code": "REVIEW", "name": "审核", "count": 12, "status": "WARNING", "routePath": "/pages/applications/applicationList" },
    { "code": "SCHEDULE", "name": "排班", "count": 5, "status": "WARNING", "routePath": "/pages/schedules/scheduleList" },
    { "code": "ATTENDANCE", "name": "考勤", "count": 2, "status": "WARNING", "routePath": "/pages/schedules/scheduleList" },
    { "code": "SALARY", "name": "薪资", "count": 5, "status": "TODO", "routePath": "/pages/attendance/attendanceList" }
  ],
  "todoSummary": [
    { "type": "APPLICATION", "name": "报名", "count": 12 },
    { "type": "SCHEDULE", "name": "排班", "count": 5 },
    { "type": "ATTENDANCE", "name": "考勤", "count": 2 },
    { "type": "SALARY", "name": "薪资", "count": 5 }
  ],
  "trendSummary": {
    "applicationTotal": 86,
    "shiftTotal": 42,
    "salaryTotalAmount": 12800.00
  }
}
```

### 流程汇总

```http
GET /api/operations/process
```

返回完整流程节点。节点结构：

```json
{
  "code": "REVIEW",
  "name": "审核报名",
  "count": 12,
  "status": "WARNING",
  "description": "12 人等待审核，建议优先处理",
  "tags": ["待审核 12", "今日新增 18"],
  "routePath": "/pages/applications/applicationList"
}
```

### 待办分页

```http
GET /api/operations/todos?type=APPLICATION&page=1&pageSize=20
```

参数：

- `type`：可选。`APPLICATION`、`SCHEDULE`、`ATTENDANCE`、`SALARY`。为空时按优先级混合返回。
- `page`：默认 `1`。
- `pageSize`：默认 `20`。

返回 `PageVO<OperationTodoItemVO>`。

待办项结构：

```json
{
  "id": "APPLICATION-123",
  "type": "APPLICATION",
  "title": "张小雨报名周末促销员",
  "description": "23 岁 · 女 · 138****9527",
  "status": "PENDING",
  "priority": "HIGH",
  "bizId": 123,
  "bizType": "SCHEDULE_APPLICATION",
  "actionText": "通过报名",
  "routePath": "/pages/applications/applicationList",
  "createdAt": "2026-06-13 10:24:00",
  "deadlineAt": null,
  "actions": ["ACCEPT_APPLICATION", "REJECT_APPLICATION"]
}
```

### 异常提醒

```http
GET /api/operations/exceptions?page=1&pageSize=20
```

返回当前企业的异常事项分页列表，第一版包括：

- 排班缺口：班次人数不足或未排满。
- 考勤异常：缺签到、缺签退、工时异常。
- 薪资异常：结算失败或待结算堆积。

### 趋势数据

```http
GET /api/operations/trends
```

返回近 7 天趋势：

```json
{
  "days": [
    { "date": "2026-06-07", "applicationCount": 8, "shiftCount": 5, "salaryAmount": 1200.00 },
    { "date": "2026-06-08", "applicationCount": 12, "shiftCount": 6, "salaryAmount": 1800.00 }
  ]
}
```

### 待办动作

```http
POST /api/operations/todos/{todoId}/actions
Content-Type: application/json

{ "action": "ACCEPT_APPLICATION" }
```

支持动作：

- `ACCEPT_APPLICATION`：通过报名。
- `REJECT_APPLICATION`：拒绝报名。
- `CANCEL_SHIFT`：取消排班。
- `PAY_SALARY`：结算薪资。

动作接口只做路由和权限校验，实际业务调用现有 `JobService`、`ScheduleService`、`AttendanceHoursService` 或结算相关 Service。

## 数据口径

### 首页概览

- `publishedJobCount`：`jobs.status = 'PUBLISHED'`。
- `totalApplicationCount`：当前企业职位下的全部报名数。
- `pendingTodoCount`：报名待审核、排班缺口、考勤异常、待结算薪资总和。
- `todayShiftCount`：`schedule_shifts.shift_date = CURDATE()`。
- `unpaidSalaryCount`：`attendance_records.settlement_status = 'UNPAID'`。

### 待办

- 报名待办：`schedule_applications.status = 'PENDING'`。
- 排班待办：当前或未来班次中，需要处理的未满员或未开始排班事项。第一版优先返回未来未开始班次。
- 考勤待办：缺签到、缺签退或状态异常的考勤记录。
- 薪资待办：`attendance_records.settlement_status = 'UNPAID'`。

### 趋势

- 报名趋势：按 `schedule_applications.applied_at` 分组统计。
- 排班趋势：按 `schedule_shifts.shift_date` 分组统计。
- 薪资趋势：按 `attendance_records.calculated_at` 或班次日期统计应付金额。

## 后端文件结构

新增或修改文件：

```text
enterprise-service/src/main/java/com/parttime/enterprise/controller/OperationController.java
enterprise-service/src/main/java/com/parttime/enterprise/service/OperationService.java
enterprise-service/src/main/java/com/parttime/enterprise/service/impl/OperationServiceImpl.java
enterprise-service/src/main/java/com/parttime/enterprise/mapper/OperationMapper.java
enterprise-service/src/main/resources/mapper/OperationMapper.xml
enterprise-service/src/main/java/com/parttime/enterprise/pojo/cmd/OperationTodoActionCmd.java
enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationDashboardVO.java
enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationOverviewVO.java
enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationTodayVO.java
enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationProcessNodeVO.java
enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationTodoSummaryVO.java
enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationTodoItemVO.java
enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationExceptionVO.java
enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationTrendVO.java
enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationTrendPointVO.java
```

## 实施策略

1. 先实现只读接口：dashboard、process、todos、exceptions、trends。
2. 再实现基础动作接口，复用现有业务 Service。
3. 最后前端替换静态数据。

## 验证

后端完成后执行：

```bash
cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test
```

前端接入后执行：

```bash
cd enterprise-uniapp && npm run build:mp-weixin
```

## 风险与约束

- 现有表中部分“排班缺口”口径可能缺少明确目标人数，第一版应采用可从现有字段推导的简化口径。
- 统一动作接口必须复用现有 Service，避免绕过业务校验。
- 待办 ID 使用 `TYPE-bizId` 字符串，避免引入新表前发生 ID 冲突。
- 趋势金额应使用 `payable_pay` 优先，缺失时回退 `scheduled_pay`。
