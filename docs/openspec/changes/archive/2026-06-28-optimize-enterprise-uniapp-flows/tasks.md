## 1. 首页工作台改造（今日待办模式）

- [x] 1.1 改造 `pages/home/index.vue`：顶部保留统计卡片，下方展示今日待办卡片列表（报名审核/今日班次/待结算）
- [x] 1.2 每张待办卡片显示事项标题、描述、操作链接，点击跳转到对应操作页
- [x] 1.3 从现有 `getOperationDashboard`、`getOperationTodos` API 获取待办数据

## 2. 招聘流程页升级（流程概览模式）

- [x] 2.1 改造 `pages/process/process.vue`：展示 5 步流程卡片列表（创建岗位→创建班次→审核报名→考勤确认→薪资结算）
- [x] 2.2 每步卡片显示步骤编号、标题、汇总数据、状态标记（已完成/当前待办/未开始）
- [x] 2.3 从 `getOperationProcess` API 获取流程数据，过滤无数据步骤

## 4. 岗位→班次连贯操作

- [x] 4.1 `pages/jobs/jobForm.vue`：发布成功后判断，若成功则 `uni.redirectTo` 到班次管理页（带 jobId）
- [x] 4.2 班次管理页接收 jobId 参数，自动筛选该岗位的班次，并显示"岗位已创建，请添加班次"提示

## 5. 报名审核页优化

- [x] 5.1 `pages/applications/applicationList.vue`：报名列表按班次时间排序分组展示
- [x] 5.2 每组默认折叠，点击展开该班次的报名列表
- [x] 5.3 审核完成后显示"下一步：考勤确认"引导

## 6. 考勤确认页优化

- [x] 6.1 `pages/schedules/scheduleList.vue`：增加"待确认""工作中""已完成"状态筛选标签
- [x] 6.2 对接已有 API 的 settlementStatus 参数，已结算记录显示标记

## 7. 薪资结算页优化

- [x] 7.1 `pages/attendance/attendanceList.vue`：顶部增加企业余额卡片，调用 `getBalance` API
- [x] 7.2 余额不足时结算按钮变为"余额不足，去充值"，跳转到充值页

## 8. Tabbar 改造

- [x] 8.1 `EnterpriseTabBar` 组件新增 `type: 'fab'` 配置，支持 5 项 tab 和中间凸起绿色 "+" 按钮
- [x] 8.2 `pages.json` tabbar 配置改为 5 项，新增 AI 创建页路径
- [x] 8.3 更新各 tab 页引用，隐藏原生 tabBar 使用自定义组件

## 9. AI 对话页

- [x] 9.1 创建 `pages/aiChat/aiChat.vue`，聊天气泡布局 + 输入区（文字/🎤/📷）
- [x] 9.2 实现逐字打字机渲染效果
- [x] 9.3 实现语音录音（`wx.getRecorderManager`）→ 上传 → ASR → 发送
- [x] 9.4 实现图片选择（`uni.chooseImage`）→ 上传
- [x] 9.5 实现结构化确认卡片组件，用户确认/修改交互
- [x] 9.6 确认后调 Python 代理层接口，调用 enterprise-service 执行创建

## 10. Python 代理层 (ai-proxy/)

- [x] 10.1 初始化 FastAPI 项目骨架（main.py, routers/, services/, prompts/）
- [x] 10.2 实现 `/api/chat` SSE 端点和 `/api/chat/sync` 同步端点
- [x] 10.3 实现 `/api/upload` 文件上传端点（图片/音频临时存储）
- [x] 10.4 实现阿里云 ASR 对接服务
- [x] 10.5 实现 Prompt 模板 + Function Calling（岗位/班次 JSON Schema）
- [x] 10.6 实现 enterprise-service REST 客户端（创建岗位、创建班次）
- [x] 10.7 JWT token 校验中间件
- [x] 10.8 部署配置、环境变量、启动脚本
