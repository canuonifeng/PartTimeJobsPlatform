## 1. 后端代理层 - enterprise_client 新增方法

- [x] 1.1 新增 `list_jobs(status)` → GET /enterprise/jobs
- [x] 1.2 新增 `close_job(id)` → POST /enterprise/jobs/close
- [x] 1.3 新增 `reopen_job(id)` → POST /enterprise/jobs/reopen
- [x] 1.4 新增 `list_applications(params)` → GET /enterprise/applications
- [x] 1.5 新增 `accept_application(id)` → POST /enterprise/applications/accept
- [x] 1.6 新增 `reject_application(id)` → POST /enterprise/applications/reject
- [x] 1.7 新增 `list_attendance(params)` → GET /enterprise/attendance/hours
- [x] 1.8 新增 `update_attendance_hours(id, data)` → POST /enterprise/attendance/hours/update
- [x] 1.9 新增 `batch_pay_attendance(ids)` → POST /enterprise/attendance/hours/pay
- [x] 1.10 新增 `unsettle_attendance(id)` → POST /enterprise/settlement/unsettle

## 2. 后端代理层 - prompts/job.py 函数

- [x] 2.1 系统提示增加 query_data / execute_action / batch_action 使用说明
- [x] 2.2 新增 query_data 函数 schema（含所有 type 枚举 + 筛选参数）
- [x] 2.3 新增 execute_action 函数 schema（含所有 action 枚举）
- [x] 2.4 新增 batch_action 函数 schema（含 action 枚举 + targetIds/filters）

## 3. 后端代理层 - routers/chat.py 多轮内联

- [x] 3.1 新增 `_execute_inline(func_data, token)` 函数，按 type 分发查询
- [x] 3.2 扩展 `_chat_sync_with_tools` 支持 query_data 内联执行（多轮循环）
- [x] 3.3 扩展 `execute_action` 路由处理 8 种新 action
- [x] 3.4 SSE 端点添加 query_data 内联执行支持

## 4. 前端 - aiChat.vue 确认卡片

- [x] 4.1 新增 FUNCTION_ACTION_MAP 映射：execute_action/batch_action
- [x] 4.2 新增报名操作卡模板（accept/reject，含原因输入）
- [x] 4.3 新增状态操作卡模板（close/reopen/cancel）
- [x] 4.4 新增考勤编辑卡模板（update_attendance_hours）
- [x] 4.5 新增批量操作卡模板（含金额汇总+逐条明细）
- [x] 4.6 execute 请求支持新 action 字段

## 5. 验证

- [x] 5.1 curl 测试 query_data 各 type 路径
- [x] 5.2 curl 测试 execute_action 各 action 路径
- [x] 5.3 curl 测试 batch_action 路径
- [x] 5.4 前端重建验证确认卡片渲染
