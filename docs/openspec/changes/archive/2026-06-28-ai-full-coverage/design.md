## Architecture

```
┌──────────────────────────────────────────────────────────┐
│                    微信小程序页面                        │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │ jobList.vue │  │ manageList   │  │ 其他传统页面    │  │
│  │ jobForm.vue │  │ scheduleList │  │ (回退方案)      │  │
│  └─────────────┘  └──────────────┘  └────────────────┘  │
│                        ┌─────────────┐                   │
│                        │  aiChat.vue  │  ← 主入口        │
│                        └──────┬──────┘                   │
└───────────────────────────────┼──────────────────────────┘
                                │ POST /chat/sync
                                │ POST /chat/execute
                                ▼
┌──────────────────────────────────────────────────────────┐
│                   AI Proxy (ai-proxy)                     │
│  ┌─────────────┐  ┌────────────────┐  ┌──────────────┐  │
│  │ prompts/    │  │  routers/      │  │  services/   │  │
│  │ job.py      │  │  chat.py       │  │  enterprise  │  │
│  │ (9 functions)│  │  sync/execute │◀─┤  _client.py  │──┤
│  └─────────────┘  │  inline/confirm│  │  (12 methods)│  │
│                    └───────┬────────┘  └──────────────┘  │
└────────────────────────────┼─────────────────────────────┘
                             │ HTTP
                             ▼
┌──────────────────────────────────────────────────────────┐
│              enterprise-service (localhost:8081)          │
│  GET/POST /jobs, /schedules, /applications, /attendance  │
│  /settlement, /schedule-shifts, /operations, /balance    │
└──────────────────────────────────────────────────────────┘
```

## Data Flow

### Query Flow (query_data, inline)
```
User: "保安岗位有多少人报名了"
  → AI calls query_data({type:"applications", jobTitle:"保安"})
  → proxy executes GET /applications?jobTitle=保安
  → results returned as tool response
  → AI summarizes: "保安岗位有5人报名，其中3人待审核"
  → text only (no function_call to frontend)
```

### Action Flow (execute_action, confirmed)
```
User: "通过张三的报名"
  → AI calls execute_action({action:"accept_application", targetId:1})
  → proxy returns function_call to frontend
  → frontend shows confirm card (报名操作卡)
  → user clicks "确认通过"
  → POST /chat/execute {action:"accept_application", data:{targetId:1}}
  → proxy executes POST /applications/accept
  → returns success
```

### Batch Flow (batch_action, confirmed)
```
User: "结算保安岗位上周的工资"
  → AI calls query_data({type:"attendance", jobTitle:"保安", settlementStatus:"UNPAID"})
  → proxy returns records inline
  → AI calls batch_action({action:"batch_pay", filters:{jobId:1, settlementStatus:"UNPAID"}})
  → proxy returns function_call to frontend
  → frontend shows 批量操作卡 with totals
  → user confirms → execute
```

## Design Decisions

| Decision | Rationale |
|----------|-----------|
| 3 个泛化函数而非 20+ 独立函数 | 减少 schema 膨胀，降低 AI 选错概率 |
| query_data 内联执行 | 只读操作无需用户确认，AI 直接总结 |
| execute/batch 确认后执行 | 写操作安全护栏，防止误操作 |
| 同步接口为主 | SSE 多轮 tool_call 复杂度高，当前场景优先保证功能完整性 |
| 后端 0 改动 | 全部复用已有 REST API，仅代理层增强 |

## Risk / Mitigation

| Risk | Mitigation |
|------|------------|
| AI 调用错误函数/传错参数 | execute_action 通过 action enum 约束，前端确认卡片展示数据供用户核验 |
| 批量操作耗时较长 | HTTP timeout 30s，后端事务逐条执行 |
| query_data 返回大量数据 | AI 自动摘要，前端不超过 100 条 |
| 用户误点确认 | execute_action 支持幂等判断，已处理的操作会返回明确错误 |
