## Why

企业端小程序目前页面分散，业务流（创建岗位 → 创建班次 → 审核报名 → 考勤确认 → 薪资结算）缺乏连贯引导，企业用户容易在各页面间迷失，操作效率低。需要围绕核心业务流重新梳理页面结构和交互路径。

## What Changes

- **首页工作台改造**：将 5 步业务流以可视化进度引导展示，当前步骤高亮，点击直接跳转到对应操作页
- **招聘流程页（Tab 2）升级**：改为业务流驱动的操作中心，每步展示待处理数量和快捷入口，取代现有纯 timeline 展示
- **创建岗位 → 创建班次连贯操作**：岗位创建成功后，直接引导进入班次创建页，而非返回列表
- **报名审核页优化**：按班次分组展示报名列表，支持批量操作，审核完成后引导到下一步
- **考勤确认页优化**：清晰展示待确认/已确认，与结算状态联动
- **薪资结算页优化**：展示结算进度，与企业余额联动，余额不足时引导充值
- **Tabbar 改为 5 项**：工作台 · 招聘 · **AI 创建** · 消息 · 我的，中间 "+" 按钮凸起突出
- **新增 AI 对话页**：点击 "+" 进入，支持文字、语音、图片输入，通义千问识别意图后自动创建岗位和班次
- **新增 Python 代理层**：FastAPI SSE 服务，桥接小程序和通义千问，调 enterprise-service 执行

## Capabilities

### New Capabilities
- `enterprise-flow-navigation`: 业务流导航组件，在首页和流程页展示 5 步进度，支持跳转和状态标记
- `post-job-with-schedules`: 岗位创建后无缝衔接班次创建，支持批量生成班次
- `enterprise-ai-chat`: AI 对话创建岗位/班次，含语音/图片/文字输入、流式 SSE 对话、结构化意图识别、确认后自动创建

### Modified Capabilities
- `schedule-attendance`: 考勤确认页增加待确认/已确认筛选，与结算状态联动
- `payroll`: 薪资结算页增加结算进度展示和企业余额联动

## Impact

- **Frontend (enterprise-uniapp)**: 改造 `pages/home/index.vue`（工作台）、`pages/process/process.vue`（招聘流程）、`pages/jobs/jobForm.vue`（岗位发布后跳转）、`pages/applications/applicationList.vue`（报名审核）、`pages/schedules/scheduleList.vue`（考勤确认）、`pages/attendance/attendanceList.vue`（薪资结算）；新增流引导组件；`EnterpriseTabBar` 支持 5 项和中间凸起按钮；新增 `pages/aiChat/aiChat.vue` AI 对话页
- **New Service (ai-proxy/)**: 新增 Python FastAPI 服务，提供 SSE 对话端点和图片上传端点，对接通义千问和 enterprise-service
- **Backend (enterprise-service)**: 少量 API 调整，如报名按班次分组、结算进度统计等
- **Infrastructure**: 新增 Python 服务部署配置、阿里云 AK/SK、通义千问 API Key
- **No database changes**
