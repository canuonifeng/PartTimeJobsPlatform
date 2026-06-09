# 报名自动审核 + 自动结算可配置

## 背景

当前所有工人报名都必须由企业手动审核（通过/拒绝），打卡签退后也需企业手动点击"批量结算"。平台希望这两个环节都支持自动化。

## 需求一：报名自动通过审核

- 平台级开关：`system_configs` 表新增 `auto_approve_applications` 配置，默认 `false`
- 职位级覆盖：`jobs` 表新增 `auto_approve` 字段（nullable BOOLEAN），null 表示跟随平台默认，true/false 表示覆盖
- 优先级：职位级 > 平台级
- 自动通过时：报名状态直接为 `ACCEPTED`，同时自动生成排班（ScheduleShift），发送通知

## 需求二：打卡签退后自动结算

- 平台级开关：`system_configs` 表新增 `auto_settle_attendance` 配置，默认 `false`
- 无职位级覆盖（全平台统一）
- 自动结算时：工人打卡签退后，系统自动完成结算（计算薪资 → 入账工人余额 → 扣减企业余额 → 发送通知），无需企业手动操作

## 数据库变更

### 1. system_configs 新增配置项

```sql
INSERT INTO system_configs (config_key, config_value, name, created_at, updated_at)
VALUES
  ('auto_approve_applications', 'false', '报名自动通过审核', NOW(), NOW()),
  ('auto_settle_attendance', 'false', '打卡签退后自动结算', NOW(), NOW());
```

### 2. jobs 表新增字段

```sql
ALTER TABLE jobs ADD COLUMN auto_approve TINYINT(1) DEFAULT NULL COMMENT '自动审核: null跟随平台默认, 1开启, 0关闭';
```

## 后端变更 — 需求一（报名自动审核）

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

## 后端变更 — 需求二（自动结算）

### c-service

#### AttendanceServiceImpl.java — checkOut()

核心逻辑变更：

```
现有逻辑（lines 172-246）：
1. 验证签退条件
2. 计算工时和薪资
3. 更新考勤记录，settlementStatus = "UNPAID"
4. 更新班次状态

新增逻辑（在步骤 4 之后）：
5. 读取 auto_settle_attendance 配置
6. 如果开启自动结算：
   a. 信用工人余额（WorkerBalanceMapper.upsert）
   b. 插入余额交易记录（BalanceTransactionMapper，type=EARNINGS）
   c. 更新考勤记录 settlementStatus = "PAID"
   d. 扣减企业余额（enterprise_balances 表直接 SQL）
   e. 插入企业余额交易记录（enterprise_balance_transactions 表）
   f. 发送通知："收入到账"
```

需要新增：
- `SystemConfigMapper.findByKey("auto_settle_attendance")` 读取平台配置
- `WorkerBalanceMapper.upsert()` 信用工人余额
- `BalanceTransactionMapper.insert()` 插入交易记录
- 直接操作 `enterprise_balances` 和 `enterprise_balance_transactions` 表（同数据库）
- `WorkerNotificationMapper` 发送通知

#### WorkerBalanceMapper

新增方法：

```java
void upsert(@Param("workerId") Long workerId, @Param("amount") BigDecimal amount);
```

对应 XML：

```sql
INSERT INTO worker_balances (worker_id, balance, total_earned, created_at, updated_at)
VALUES (#{workerId}, #{amount}, #{amount}, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  balance = balance + #{amount},
  total_earned = total_earned + #{amount},
  updated_at = NOW()
```

#### BalanceTransactionMapper

新增方法：

```java
void insert(BalanceTransaction transaction);
```

对应 XML：标准 INSERT。

#### 直接 SQL 操作企业余额

```sql
-- 扣减企业余额
UPDATE enterprise_balances
SET balance = balance - #{amount}, total_spent = total_spent + #{amount}, updated_at = NOW()
WHERE company_id = #{companyId} AND balance >= #{amount}

-- 插入企业交易记录
INSERT INTO enterprise_balance_transactions (company_id, amount, type, description, created_at)
VALUES (#{companyId}, -#{amount}, 'SETTLEMENT', '打卡自动结算', NOW())
```

### enterprise-service

无变更。企业端手动"批量结算"流程保持不变，作为自动结算的备选方案。

### platform-service

无变更。SystemConfig CRUD 已有，配置项通过现有管理界面操作。

## 前端变更

### platform-pc — 系统配置管理

`SystemConfigList.vue` 中配置列表自动包含新配置项，无需额外修改。

### enterprise-pc — 职位管理

职位创建/编辑表单新增"自动审核"开关：
- 选项：跟随平台默认 / 开启 / 关闭
- 对应值：null / true / false

### worker-uniapp — 签退自动结算弹窗

签退接口返回 `autoSettled` 字段（boolean）。前端处理：

1. `index.vue`（首页签退）和 `clockIn.vue`（打卡页签退）均需处理
2. 如果 `res.autoSettled === true`：
   - 弹出 `uni.showModal`，标题"薪资已到账"
   - 内容："已收到 ¥X.XX 薪资，去提现？"
   - 确认按钮"去提现" → 跳转 `/pages/earnings/earnings`
   - 取消按钮"不了" → 关闭弹窗
3. 如果 `autoSettled === false`：保持现有逻辑（toast/早退提示）

## 通知模板

### 报名自动通过时

1. **报名已通过** — type: `APPLICATION_ACCEPTED`, category: `application`
2. **排班已生成** — type: `SCHEDULE_ASSIGNED`, category: `schedule`

### 自动结算时

1. **收入到账** — type: `EARNINGS`, category: `income`

与企业手动操作时的通知保持一致。

## 测试计划

### 需求一（c-service）

- 平台开启 + 职位 null → 自动通过
- 平台关闭 + 职位 null → 不自动通过
- 平台关闭 + 职位 true → 自动通过
- 平台开启 + 职位 false → 不自动通过
- 自动通过时 ScheduleShift 正确创建
- 自动通过时通知正确发送

### 需求二（c-service）

- 自动结算开启 → 签退后 settlementStatus = "PAID"，工人余额增加，企业余额扣减
- 自动结算关闭 → 签退后 settlementStatus = "UNPAID"（现有行为）
- 企业余额不足时自动结算降级为 UNPAID（签退成功但不自动结算，等企业手动处理或充值后结算）

### 验证命令

```bash
cd c-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test
```
