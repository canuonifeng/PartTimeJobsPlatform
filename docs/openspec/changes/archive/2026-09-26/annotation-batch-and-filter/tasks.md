# Tasks: annotation-batch-and-filter

> 任务按 3 份实现计划拆分执行（对应 `docs/superpowers/plans/2026-09-23-annotation-batch-and-filter-tasks-1.1-3.3.md` / `-4.1-5.4.md` / `-6.1-8.4.md`）

## Plan 1（Task 1.1–3.3）：数据库迁移 + 企业端批次后端 + 企业端急招字段

### 1. 数据库迁移
- [x] 1.1 创建迁移脚本 `scripts/36_annotation_batch_and_filter.sql`：jobs 加 urgent 字段、job_schedules 日期/时段改可空、job_schedules 新增 batch_code 批次代号
- [x] 1.2 本地执行脚本并验证表结构（urgent 默认 0、日期时段可空、batch_code 存在）

### 2. 企业端后端：批次管理
- [x] 2.1 新增 `AnnotationBatchController`（/api/enterprise/annotation-batches）：list/create/update/toggle 四接口，typed cmd
- [x] 2.2 扩展 `JobScheduleMapper`+XML：按 jobId 查全部批次（含 status）、新建标注批次（日期时段 null + batch_code/total_items 写入）、按 id 更新批次代号/数据量/外部ID、更新状态
- [x] 2.3 批次创建/更新校验：batchCode 与 totalItems 必填（为空拒绝保存）
- [x] 2.4 批次列表聚合进度：按 schedule_id 统计 annotation_task_orders 已抢数/已完成数（Mapper 聚合查询）
- [x] 2.5 单元测试：批次 create（日期时段 null、batchCode 落库）/update/toggle/进度统计/必填校验

### 3. 企业端后端：急招字段
- [x] 3.1 `Job` 实体/`JobCreateCmd`/`UpdateJobCmd`/`JobVO` 增加 urgent 字段
- [x] 3.2 `JobController`/`JobServiceImpl` create/update 读写 urgent；Mapper XML 同步
- [x] 3.3 契约检查：既有 /api/enterprise 前缀接口不变更，新增接口无 @PathVariable/@PutMapping 违规

## Plan 2（Task 4.1–5.4）：C端搜索扩展 + 企业端PC模块拆分

### 4. C端后端：搜索扩展
- [x] 4.1 `JobSummaryVO`/`JobDetailVO` 增加 urgent 字段
- [x] 4.2 c-service `JobController` searchJobs 增加 urgent 参数（true 过滤）
- [x] 4.3 c-service searchJobs 增加 sort=distance 参数（有经纬度时按距离升序，无则默认排序）
- [x] 4.4 标注详情批次真实数据：getJobDetail 返回 ACTIVE 批次（schedules 含 total_items/external_batch_id/剩余量）
- [x] 4.5 单元测试：urgent 过滤、距离排序、批次返回

### 5. 企业端 PC：模块拆分
- [x] 5.1 `JobList.vue` 改造为零工招聘（**强制固定 taskType=WORK，只展示零工数据**、移除任务类型下拉、新建默认 WORK）
- [x] 5.2 新增 `AnnotationJobList.vue`：标注职位列表（固定 ANNOTATION，含标题/状态筛选 + 新建/编辑/发布/下线/删除 + 批次管理入口）
- [x] 5.3 `JobForm.vue`：支持 query 预置任务类型；增加"急招"开关（通用）
- [x] 5.4 `router/index.js` + `App.vue`：新增 /jobs/annotation 与 /jobs/annotation/:id/batches 路由；菜单拆"零工招聘"/"标注任务"

## Plan 3（Task 6.1–8.4）：企业端PC批次页 + C端快捷筛选 + 集成验证

### 6. 企业端 PC：批次管理页
- [x] 6.1 新增 `AnnotationBatchList.vue`：批次列表（**批次代号**/数据量/外部ID/状态/已抢/已完成/进度）+ 新建/编辑弹窗（代号、数据量必填）+ 上线/下线操作
- [x] 6.2 api 封装（annotationBatchList/create/update/toggle）；构建通过

### 7. C端：快捷筛选
- [x] 7.1 `jobList.vue` 顶部筛选改为固定入口：全部/零工/标注/附近/急招（替换原任务类型Tab+分类平铺，分类降级或保留折叠）
- [x] 7.2 `api/jobs.js` 搜索参数扩展 urgent/sort
- [x] 7.3 标注详情页批次展示校验（沿用 schedules 真实数据）；构建通过

### 8. 集成验证
- [x] 8.1 enterprise-service `mvn test` 全绿（含新批次测试与既有基线；注意 enterprise 既有 3 个失败与本任务无关）
- [x] 8.2 c-service `mvn test` 全绿（培训/标注相关）
- [x] 8.3 双端前端 `npm run build` 通过
- [x] 8.4 手工回归要点：零工列表只显示零工数据（不混标注）、标注模块批次管理可用（代号/数据量必填）、C端五个快捷入口过滤正确
