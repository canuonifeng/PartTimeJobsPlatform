# Design: enterprise-job-module-refinement

## 方案概述

企业端零工/标注模块 UI 字段级区分：标注列表改用标注特有列（薪资标准/任务总量/批次代号），标注表单按 taskType 条件隐藏零工区块（模版/地址/单价/排班/招聘人数/截止日期/图片），标注职位类型独立。改动集中在 enterprise-pc 前端 2 个文件 + enterprise-service 后端校验与 VO 少量调整，零工模块不动。

## 架构与模块

- **前端** `enterprise-pc`：
  - `src/views/jobs/AnnotationJobList.vue`：列表列改造（薪资标准/任务总量/批次代号）。
  - `src/views/jobs/JobForm.vue`：
    - 标注类型（taskType=ANNOTATION）时隐藏：模版选择按钮、地址信息卡（省/市/区/详细地址/坐标/工作地点选择）、单价（计价方式/单价不再渲染）、排班时段卡。
    - 标注类型显示：招聘人数、截止日期、职位图片（保留，与零工共用区块）、薪资标准卡（复用现有 rates 区块，时薪/日薪/按单）、任务总量。
    - 标注职位类型：独立选项列表（图像标注/语音标注/文本标注/视频标注/混合数据标注），提交时写入 categoryId（独立值域 101–105，沿用现有字段，不新增字段）。
  - `src/api/job.js`：listJobs 透传现有参数（无需改）；JobVO 返回 batchCode 后直接展示。
- **后端** `enterprise-service`：
  - `JobVO`：增加 `batchCode`（标注职位返回最新批次代号）。
  - `JobMapper.xml`：列表查询聚合最新批次（job_schedules.batch_code），可用子查询或 LEFT JOIN + 去重。
  - `JobServiceImpl` create/update：taskType=ANNOTATION 校验改为——`rates` 至少一条（薪资标准必填）且 `totalItems > 0`；不再强制 pricingMode/pricePerUnit 必填（保留字段兼容历史数据）。
  - `categoryName` 兼容：标注类型（101–105）不在既有分类表时，后端查询分类名可返回空或前端 fallback 显示选项 label（实现时按现状确认）。

## 关键流程

1. 企业进入"标注任务"列表 → 调 listJobs(taskType=ANNOTATION) → 每行展示薪资标准（首档，多档加"起"）/任务总量/批次代号。
2. 新建标注职位 → JobForm 预置 taskType=ANNOTATION → 隐藏零工区块（模版/地址/单价/排班）→ 填标题/标注类型/招聘人数/截止日期/图片/薪资标准/任务总量/职责/要求/联系人/标签 → 提交（rates 数组 + totalItems + headcount + deadline + imageUrl，无地址/排班）。
3. 后端 create：ANNOTATION 校验 rates 非空 + totalItems>0 → 落库（pricingMode/pricePerUnit 为 null 不报错）。
4. 编辑标注职位 → fetchDetail 回填 → 同样只显示标注区块；保存时 rates/totalItems 更新。

## 接口定义

- 复用 `GET /api/enterprise/jobs`（listJobs），无新增接口。
- `JobVO` 增加 `batchCode`（可选，标注职位返回最新批次代号）。
- `JobCreateCmd`/`UpdateJobCmd`：不变（pricingMode/pricePerUnit 保留，标注不传即可）。
- Mapper：`JobMapper.xml` 列表查询聚合批次代号。

## 对现有代码的影响

- 改动文件：
  - `enterprise-pc/src/views/jobs/AnnotationJobList.vue`（列表列）
  - `enterprise-pc/src/views/jobs/JobForm.vue`（条件渲染 + 标注类型选项 + 薪资标准保留）
  - `enterprise-service`：`JobVO`、`JobMapper.xml`（+batchCode）、`JobServiceImpl`（标注校验调整）
- 兼容性：标注历史数据（地址/图片/单价/排班）保留在库中，仅前端不展示；编辑保存时这些字段以原值回传或后端忽略 null 不置空（需确认 update 逻辑对 null 字段的处理，避免误清空历史数据）。

## 备选方案

- 方案 A（当前）：共用 JobForm + 条件渲染。改动小、零工无回归。
- 方案 B：拆独立 AnnotationJobForm.vue。隔离更彻底但重复量大，且与既有共用表单模式冲突。
- 方案 C：抽公共 JobForm 组件 + 插槽分区。最规范但改动大，超出本 change 范围。
- 选择 A：字段级差异小，条件渲染足够表达差异，风险最低。
