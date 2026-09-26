# Tasks: enterprise-job-module-refinement

## 1. 标注列表字段改造
- [x] 1.1 `AnnotationJobList.vue` 移除零工列（工作地点/招聘人数/报名情况），新增标注列（薪资标准首档/任务总量/批次代号）
- [x] 1.2 后端 `JobVO` 增加 batchCode 并聚合返回最新批次代号；列表薪资标准展示（rates 首档格式化）

## 2. 标注表单条件渲染
- [x] 2.1 `JobForm.vue` 标注类型隐藏：模版选择/地址信息/单价/排班时段
- [x] 2.2 标注类型独立选项：图像标注/语音标注/文本标注/视频标注/混合数据标注（写入 categoryId 独立值域）
- [x] 2.3 标注类型保留：招聘人数/截止日期/职位图片/薪资标准区块（时薪/日薪/按单，可多档）/任务总量；提交 payload 标注时地址与排班不参与

## 3. 后端校验与兼容
- [x] 3.1 `JobServiceImpl` create/update：taskType=ANNOTATION 校验改为 rates 至少一条 + totalItems>0，不再强制 pricingMode/pricePerUnit
- [x] 3.2 标注类型 categoryName 兼容（分类查询返回空或前端 fallback）；标注列表/详情不报错

## 4. 验证
- [x] 4.1 enterprise-service `mvn test` 通过（标注校验相关测试更新/新增）
- [x] 4.2 enterprise-pc `npm run build` 通过
- [x] 4.3 手工回归：标注列表字段正确、标注表单隐藏零工区块且可保存、零工模块无回归
