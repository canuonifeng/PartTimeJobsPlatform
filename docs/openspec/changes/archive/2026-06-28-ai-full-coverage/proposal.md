## Why

当前企业端小程序功能分散在 26 个页面中，用户需要学习导航结构、表单操作、筛选条件等才能完成日常工作。对于低文化水平或年长用户，使用门槛较高。

AI 助手已实现岗位/班次的创建和修改，证明自然语言交互在此业务域可行。将 AI 覆盖范围扩展到报名审核、考勤确认、薪资结算、数据查询等全部核心业务，让任何文化水平的用户都能通过"说句话"完成工作。

## What Changes

- AI 代理层新增 3 个泛化函数（query_data / execute_action / batch_action），覆盖报名审核、考勤结算、岗位管理、班次管理四大域共 32+ 场景
- enterprise_client.py 新增 12 个后端 API 调用方法
- /chat/sync 支持多轮内联 tool_call（query_data 内联执行后 AI 继续生成）
- /chat/execute 支持 8 种新 action 的执行
- 前端确认卡片扩展为 4 种布局（报名操作卡、状态卡、考勤编辑卡、批量操作卡）
- 已有 6 个岗位/班次函数保持不变

## Capabilities

### New Capabilities
- `ai-query`: AI 通过 query_data 函数查询岗位/班次/报名/考勤数据，内联执行后由 AI 总结展示
- `ai-action`: AI 通过 execute_action 执行报名审核、岗位开关、班次取消、考勤修改、结算/撤回等单条操作
- `ai-batch`: AI 通过 batch_action 批量通过报名、批量结算工资
- `ai-confirm-cards`: 前端 4 种确认卡片布局，按操作类型动态渲染

### Modified Capabilities
- `job-management`: AI 能力从创建/修改扩展到查询列表、查看详情、关闭/重新开放
- `schedule-attendance`: AI 能力从创建/修改/复制扩展到查询班次列表、取消班次
- `process`: 报名审核、考勤确认、薪资结算全流程可通过 AI 完成

## Impact

- **后端代理层（ai-proxy）**：prompts/job.py 新增 3 个函数 schema；enterprise_client.py 新增 12 个方法；routers/chat.py 多轮内联扩展 + execute 分发扩展
- **前端（enterprise-uniapp）**：aiChat.vue 新增 4 种确认卡片模板 + 新 action 映射
- **后端服务（enterprise-service）**：无改动，全部通过已有 REST API 调用
- **数据库**：无变动
- **基础设施**：无新增依赖
