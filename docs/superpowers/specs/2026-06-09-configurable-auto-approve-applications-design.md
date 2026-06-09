# 报名自动通过审核系统可配置

## 背景

当前所有工人报名都必须由企业手动审核（通过/拒绝）。平台希望支持自动审核模式，开启后工人报名直接通过，无需企业手动操作。

## 需求

- 平台级开关：`system_configs` 表新增 `auto_approve_applications` 配置，默认 `false`
- 职位级覆盖：`jobs` 表新增 `auto_approve` 字段（nullable BOOLEAN），null 表示跟随平台默认，true/false 表示覆盖
- 优先级：职位级 > 平台级
- 自动通过时：报名状态直接为 `ACCEPTED`，同时自动生成排班（ScheduleShift），发送通知

## 数据库变更

### 1. system_configs 新增配置项

```sql
INSERT INTO system_configs (config_key, config_value, name, created_at, updated_at)
VALUES ('auto_approve_applications', 'false', '报名自动通过审核', NOW(), NOW());
```

### 2. jobs 表新增字段

```sql
ALTER TABLE jobs ADD COLUMN auto_approve TINYINT(1) DEFAULT NULL COMMENT '自动审核: null跟随平台默认, 1开启, 0关闭';
```

## 后端变更

### c-service

#### Job 实体

`c-service/src/main/java/com/parttime/cservice/pojo/entity/Job.java` 新增字段：

```java
private Boolean autoApprove;
```

#### JobMapper.xml

查询语句新增 `auto_approve` 字段映射。

#### JobServiceImpl.java — applyForJob()

核心逻辑变更：

```
1. 查询 job（已有）
2. 判断是否自动审核：
   autoApprove != null ? autoApprove : platformAutoApprove
3. 如果自动审核：
   a. 创建 ScheduleApplication，status = "ACCEPTED"
   b. 查询 JobSchedule（排班时间表）
   c. 创建 ScheduleShift（班次），status = "SCHEDULED"
   d. 发送通知："报名已通过"
4. 如果不自动审核：
   a. 创建 ScheduleApplication，status = "PENDING"（现有逻辑）
```

需要新增：
- `SystemConfigMapper.findByKey("auto_approve_applications")` 读取平台配置
- `JobScheduleMapper.findByJobId()` 查询排班时间
- `ShiftMapper.insert()` 创建班次
- `WorkerNotificationMapper` 发送通知

### enterprise-service

无变更。企业端的 accept/reject 手动审核流程保持不变，仅处理 PENDING 状态的报名。

### platform-service

无变更。SystemConfig CRUD 已有，配置项通过现有管理界面操作。

## 前端变更

### platform-pc — 系统配置管理

`SystemConfigList.vue` 中配置列表自动包含新配置项，无需额外修改（已有通用渲染逻辑）。

### enterprise-pc — 职位管理

职位创建/编辑表单新增"自动审核"开关：
- 选项：跟随平台默认 / 开启 / 关闭
- 对应值：null / true / false

## 通知模板

自动通过时发送两条通知：

1. **报名已通过** — type: `APPLICATION_ACCEPTED`, category: `application`
2. **排班已生成** — type: `SCHEDULE_ASSIGNED`, category: `schedule`

与企业手动审核通过时的通知保持一致。

## 测试计划

1. c-service 单元测试：
   - 平台开启 + 职位 null → 自动通过
   - 平台关闭 + 职位 null → 不自动通过
   - 平台关闭 + 职位 true → 自动通过
   - 平台开启 + 职位 false → 不自动通过
   - 自动通过时 ScheduleShift 正确创建
   - 自动通过时通知正确发送
2. 验证：`cd c-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test`
