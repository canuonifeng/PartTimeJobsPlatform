# Tasks: worker-training-certification（模型升级 v2）

## 1. 数据库迁移
- [x] 1.1 新建 scripts/35_training_model_v2.sql：training_lessons、question_banks、question_bank_questions、worker_lesson_records 四表；ALTER training_courses 删 content/exam_json/pass_score；DROP worker_training_records
- [x] 1.2 执行脚本验证表结构

## 2. 平台端：题库与题目
- [x] 2.1 题库 Entity/Mapper/Service/Controller（list/create/update/toggle，名称唯一）
- [x] 2.2 题目 Entity/Mapper/Service/Controller（list/create/update/delete/publish/offline，按题库筛选）
- [x] 2.3 题目数据校验（题型枚举、选项数、答案合法性，见 REQ-QB-3）
- [x] 2.4 题库/题目单元测试

## 3. 平台端：课时管理
- [x] 3.1 课时 Entity/Mapper/Service/Controller（list/create/update/delete/publish/offline）
- [x] 3.2 考试课时配置校验（题库存在且 ACTIVE、规则非空、题量/分值合法、总分自动汇总）
- [x] 3.3 TrainingCourseController/Service 移除 content/exam_json/pass_score 读写
- [x] 3.4 课时管理单元测试

## 4. C端：课时学习与考试
- [x] 4.1 课程列表/详情改造（课时列表按序 + 完成态/锁定态 + 课程进度）
- [x] 4.2 开始课时：校验前置课时完成、课时 PUBLISHED；学习型返回内容/媒体，考试型触发抽题
- [x] 4.3 进度上报（VIDEO/AUDIO 单调递增）与标记已读（DOCUMENT/IMAGE_TEXT）
- [x] 4.4 考试抽题：按规则从题库 PUBLISHED 随机抽题，题量不足拒绝；返回不含答案
- [x] 4.5 考试判分与重考：按题型判分（多选严格全对）、通过记录试卷快照、不及格可重考
- [x] 4.6 课程完成自动发放认证（幂等，含有效期）
- [x] 4.7 抽题/判分/重考/顺序/进度/认证发放单元测试

## 5. 前端：platform-pc
- [x] 5.1 题库管理页（CRUD + 启停）
- [x] 5.2 题目管理页（按题库筛选、CRUD、发布/下线）
- [x] 5.3 课程详情增加课时管理（课时 CRUD；考试课时配置题库/时长/题型题量/分值/及格分，展示自动总分）
- [x] 5.4 路由/菜单/api 封装（question-banks、questions、lessons）

## 6. 前端：worker-uniapp
- [x] 6.1 课程详情改为课时列表（按序、锁定态、进度）
- [x] 6.2 视频/音频课时学习页（播放 + 进度上报）
- [x] 6.3 文档/图文课时阅读页（标记已读）
- [x] 6.4 考试课时答题页（倒计时、提交、结果展示、重考入口）
- [x] 6.5 我的认证页与标注详情引导适配（保持可用）

## 7. 集成验证
- [x] 7.1 platform-service mvn test 通过（含既有契约标准）
- [x] 7.2 c-service mvn test 通过
- [x] 7.3 platform-pc / worker-uniapp 构建通过
- [x] 7.4 抢单拦截回归：已认证可抢、未认证被拦
