# Design: worker-training-certification（模型升级 v2）

## 方案概述

将培训模块从"课程=内容+考试"升级为"课程→多课时→题库/考试规则"三层模型：

- **课程**（training_courses）只保留标题/摘要/认证关联/状态/排序，废弃 content/exam_json/pass_score。
- **课时**（training_lessons）承载内容与考试配置：学习型课时存正文或媒体 URL + 时长；考试课时存 exam_config_json（题库ID、时长、及格分、题型规则）。
- **题库**（question_banks）全局共享，题目（question_bank_questions）在平台端 CRUD + 发布。
- **学习记录**（worker_lesson_records）按课时记录进度/得分/试卷快照/考试次数。
- 课程全部课时完成（考试课时通过）→ 自动发放认证（worker_certifications）。

## 架构与模块

```
platform-service                     c-service
├── TrainingQuestionBankController   ├── TrainingController（改造）
├── TrainingQuestionController       │   ├── 课程列表/详情（课时+进度）
├── TrainingLessonController         │   ├── 课时开始/进度/完成
└── TrainingCourseController(调整)   │   ├── 考试抽题/提交判分
                                     │   └── 认证发放（课程完成触发）
                                     抢单拦截 CertificationGateService（不变）
```

- platform-service 新增：QuestionBank/Question/TrainingLesson 的 Entity/Mapper/Service/Controller
- c-service 新增：QuestionBank 只读访问（抽题）、TrainingLesson/WorkerLessonRecord 实体与流程

## 数据库设计（scripts/35_training_model_v2.sql）

### 新表

```sql
CREATE TABLE training_lessons (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  course_id BIGINT NOT NULL,
  lesson_type VARCHAR(20) NOT NULL COMMENT 'VIDEO/AUDIO/DOCUMENT/IMAGE_TEXT/EXAM',
  title VARCHAR(200) NOT NULL,
  content MEDIUMTEXT COMMENT '文档/图文正文（Markdown/HTML）',
  media_url VARCHAR(500) DEFAULT NULL COMMENT '视频/音频地址',
  duration_minutes INT DEFAULT NULL COMMENT '视频/音频时长（分钟）',
  exam_config_json TEXT COMMENT '考试规则 JSON',
  sort_order INT DEFAULT 0,
  status VARCHAR(20) DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/OFFLINE',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_lesson_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='培训课时';

CREATE TABLE question_banks (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(500) DEFAULT NULL,
  status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_bank_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题库';

CREATE TABLE question_bank_questions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  bank_id BIGINT NOT NULL,
  question_type VARCHAR(20) NOT NULL COMMENT 'SINGLE_CHOICE/MULTIPLE_CHOICE/JUDGE',
  stem TEXT NOT NULL,
  options_json TEXT COMMENT '选项 JSON [{key,label}]',
  answer TEXT NOT NULL COMMENT '正确答案（多选为选项 key 数组）',
  analysis TEXT COMMENT '解析',
  status VARCHAR(20) DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/OFFLINE',
  sort_order INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_q_bank (bank_id),
  KEY idx_q_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题库题目';

CREATE TABLE worker_lesson_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  worker_id BIGINT NOT NULL,
  lesson_id BIGINT NOT NULL,
  status VARCHAR(20) DEFAULT 'IN_PROGRESS' COMMENT 'IN_PROGRESS/COMPLETED/FAILED',
  progress INT DEFAULT 0 COMMENT '视频/音频观看进度 0-100',
  score INT DEFAULT NULL COMMENT '考试得分',
  exam_snapshot_json MEDIUMTEXT COMMENT '通过考试的试卷快照',
  exam_attempts INT DEFAULT 0 COMMENT '考试次数',
  started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME DEFAULT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_worker_lesson (worker_id, lesson_id),
  KEY idx_record_worker (worker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工人课时学习记录';
```

### 表调整
- `training_courses`：DROP COLUMN content、exam_json、pass_score（废弃）
- `worker_training_records`：DROP TABLE（旧学习记录，不保留兼容）
- `training_certifications`、`worker_certifications`：结构不变（认证发放继续使用）

### 考试规则 JSON 示例（training_lessons.exam_config_json）

```json
{
  "bankId": 1,
  "durationMinutes": 30,
  "passScore": 60,
  "rules": [
    { "questionType": "SINGLE_CHOICE", "count": 10, "scorePer": 2 },
    { "questionType": "JUDGE", "count": 5, "scorePer": 1 }
  ]
}
```

总分 = Σ(count × scorePer)，只读自动汇总，不落库。

## 关键流程

### 学习流程（C端）
```
课程详情（按序课时 + 完成/锁定态）
  → 开始课时（校验前置课时完成；考试课时 → 抽题生成试卷）
  → 学习型：上报进度 / 标记已读
  → 考试型：提交答案 → 判分
       ├─ 通过 → 课时 COMPLETED + 试卷快照
       └─ 不通过 → FAILED，可重考
  → 全部课时完成 → 发放认证（幂等）
```

### 抽题（考试开始时）
1. 读取课时 exam_config_json，校验题库 ACTIVE
2. 按 rules 逐题型查题库中 PUBLISHED 题目，题量不足则拒绝开考
3. 随机选取 count 道，组装试卷（含题面/选项，不含答案），返回前端
4. 试卷不持久化到 DB（提交时以题目 ID + 我的答案判分；通过时快照落库）

### 判分
- SINGLE_CHOICE / JUDGE：答案字符串完全一致 → 得 scorePer，否则 0
- MULTIPLE_CHOICE：所选 key 集合与正确答案集合完全一致 → 得 scorePer，否则 0（严格全对）
- 总分 = Σ答对题分；>= passScore 通过

### 认证发放（课程完成）
- 任一课时状态非 COMPLETED → 未完成
- 全部 COMPLETED → 检查 worker_certifications 是否已有该 certification_id 的 ACTIVE 未过期记录；无则插入（granted_at=now，expires_at=now+valid_days 或 NULL）

## 接口设计

### 平台端（platform-service，前缀 /api/admin/training，全部 POST + typed cmd）
| 接口 | 说明 |
|---|---|
| /question-banks/list | 题库列表 |
| /question-banks/create | 创建题库 |
| /question-banks/update | 更新题库 |
| /question-banks/toggle | 启停题库 |
| /question-banks/questions/list | 按题库列题目（body: bankId） |
| /question-banks/questions/create | 创建题目 |
| /question-banks/questions/update | 更新题目 |
| /question-banks/questions/delete | 删除题目（仅 DRAFT） |
| /question-banks/questions/publish | 发布题目 |
| /question-banks/questions/offline | 下线题目 |
| /lessons/list | 按课程列课时（body: courseId） |
| /lessons/create | 创建课时 |
| /lessons/update | 更新课时（含考试规则） |
| /lessons/delete | 删除课时 |
| /lessons/publish | 发布课时 |
| /lessons/offline | 下线课时 |
| /courses/create|update（调整） | 课程不再接收 content/exam_json/pass_score |

### C端（c-service，前缀 /api/worker/training，全部 POST + typed cmd）
| 接口 | 说明 |
|---|---|
| /courses | 课程列表（含我的进度：已完成课时数/总数、认证状态） |
| /courses/detail | 课程详情（课时列表：类型/标题/排序/完成态/锁定态） |
| /lessons/start | 开始课时（body: lessonId；返回内容/媒体信息；考试课时返回试卷） |
| /lessons/progress | 上报观看进度（body: lessonId, progress；仅 VIDEO/AUDIO，单调递增） |
| /lessons/complete | 标记已读完成（body: lessonId；仅 DOCUMENT/IMAGE_TEXT） |
| /lessons/exam/submit | 提交考试答案（body: lessonId, answers[{questionId, answer}]） |
| /certifications/my | 我的认证（不变） |

### 试卷/快照数据结构
- 试卷返回：{ lessonId, durationMinutes, totalScore, passScore, questions: [{questionId, questionType, stem, options, score}] }
- 快照存储（worker_lesson_records.exam_snapshot_json）：{ submittedAt, score, passScore, totalScore, questions: [{questionId, stem, options, myAnswer, correctAnswer, score, correct}] }

## 对现有代码的影响

- `platform-service`：
  - 新增 QuestionBank/Question/Lesson 三套 Entity/Mapper/Service/Controller
  - TrainingCourseServiceImpl 移除 content/exam_json/pass_score 读写
  - TrainingCourseController 相应调整
- `c-service`：
  - TrainingController/TrainingServiceImpl 重写课程详情、学习、考试流程
  - 新增 TrainingLesson/WorkerLessonRecord/QuestionBank 相关实体与 Mapper
  - CertificationGateService 不变
  - 新增迁移脚本执行验证
- 前端：
  - platform-pc：课程管理加"课时管理"Tab；新增题库管理页、题目管理页；api 封装
  - worker-uniapp：课程详情改为课时列表；新增视频/音频学习、文档/图文阅读、考试答题页
- 兼容性：旧字段/旧表废弃，旧数据不迁移；worker_certifications 既有数据保留但不保证与新认证并发冲突（认证发放幂等）

## 备选方案

- 方案 A：考试规则拆独立表（exam_configs）而非 JSON → 更规范但过度建模，题型规则结构固定，JSON 足够且便于前端直接渲染；选 JSON。
- 方案 B：试卷持久化到库（每次开考存试卷表）→ 可审计但表膨胀、复杂度高；仅通过时存快照，开考试卷不落库；选后者。
- 方案 C：多选部分得分 → 提升通过率但规则复杂、争议多；用户确认严格全对；选严格全对。
