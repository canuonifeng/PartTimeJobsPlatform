# 报名自动审核 + 自动结算可配置 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make job application approval and post-checkout settlement configurable via platform-level system configs and per-job overrides.

**Architecture:** Add two system_configs keys (`auto_approve_applications`, `auto_settle_attendance`) and a nullable `auto_approve` column on `jobs`. c-service reads these at runtime: auto-approve creates ACCEPTED applications + ScheduleShifts directly in `applyForJob()`; auto-settle executes settlement logic (credit worker, deduct enterprise) in `checkOut()`.

**Tech Stack:** Java 17, Spring Boot 3.x, MyBatis, MySQL

---

### Task 1: SQL Migration — add config items and jobs column

**Files:**
- Create: `scripts/22_auto_approve_and_auto_settle.sql`

- [ ] **Step 1: Create migration script**

```sql
-- scripts/22_auto_approve_and_auto_settle.sql
-- 报名自动审核 + 打卡自动结算

INSERT INTO system_configs (config_key, config_value, name, created_at, updated_at)
VALUES
  ('auto_approve_applications', 'false', '报名自动通过审核', NOW(), NOW()),
  ('auto_settle_attendance', 'false', '打卡签退后自动结算', NOW(), NOW());

ALTER TABLE jobs ADD COLUMN auto_approve TINYINT(1) DEFAULT NULL COMMENT '自动审核: null跟随平台默认, 1开启, 0关闭';
```

- [ ] **Step 2: Execute migration locally**

```bash
mysql --default-character-set=utf8mb4 -h localhost -P 3306 -u part_time_work -ppassword123 part_time_work < scripts/22_auto_approve_and_auto_settle.sql
```

- [ ] **Step 3: Commit**

```bash
git add scripts/22_auto_approve_and_auto_settle.sql
git commit -m "feat: add auto_approve_applications and auto_settle_attendance configs"
```

---

### Task 2: c-service — add autoApprove field to Job entity and mapper

**Files:**
- Modify: `c-service/src/main/java/com/parttime/cservice/pojo/entity/Job.java`
- Modify: `c-service/src/main/resources/mapper/JobMapper.xml`

- [ ] **Step 1: Add autoApprove field to Job entity**

In `c-service/src/main/java/com/parttime/cservice/pojo/entity/Job.java`, add after `closeReason` field (line 78):

```java
    @Schema(description = "自动审核: null跟随平台默认, true开启, false关闭")
    private Boolean autoApprove;
```

- [ ] **Step 2: Add auto_approve to JobMapper.xml resultMap and queries**

In `c-service/src/main/resources/mapper/JobMapper.xml`, add to the resultMap:

```xml
        <result property="autoApprove" column="auto_approve"/>
```

Also add `j.auto_approve` to the SELECT column lists of the main queries (findById equivalent, list queries).

- [ ] **Step 3: Run c-service tests to verify no regression**

Run: `cd c-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test -q`
Expected: All 197 tests pass

- [ ] **Step 4: Commit**

```bash
git add c-service/src/main/java/com/parttime/cservice/pojo/entity/Job.java c-service/src/main/resources/mapper/JobMapper.xml
git commit -m "feat: add autoApprove field to Job entity"
```

---

### Task 3: c-service — auto-approve in applyForJob()

**Files:**
- Modify: `c-service/src/main/java/com/parttime/cservice/service/impl/JobServiceImpl.java`
- Modify: `c-service/src/test/java/com/parttime/cservice/service/JobServiceImplApplyForJobTest.java`

- [ ] **Step 1: Write failing tests for auto-approve**

Add these mocks and tests to `JobServiceImplApplyForJobTest.java`:

Add new mocks:

```java
    @Mock
    private com.parttime.cservice.mapper.SystemConfigMapper systemConfigMapper;

    @Mock
    private com.parttime.cservice.mapper.ShiftMapper shiftMapper;

    @Mock
    private com.parttime.cservice.mapper.NotificationMapper notificationMapper;
```

Add test methods:

```java
    @Test
    void applyForJob_autoApproveEnabled_shouldCreateAcceptedApplicationAndShift() {
        Job job = new Job();
        job.setId(1L);
        job.setJobId(1L);
        job.setCompanyId(88L);
        job.setAutoApprove(true);

        JobSchedule schedule = activeFutureSchedule(10L, 1L);
        when(scheduleApplicationMapper.findScheduleIdsByWorkerIdAndJobId(100L, 1L)).thenReturn(List.of());
        when(jobMapper.findByJobId(1L)).thenReturn(Optional.of(job));
        when(jobScheduleMapper.findByIds(List.of(10L))).thenReturn(List.of(schedule));
        when(systemConfigMapper.findByKey("auto_approve_applications")).thenReturn(
                Optional.of(new com.parttime.cservice.pojo.entity.SystemConfig()));
        doAnswer(invocation -> {
            com.parttime.cservice.pojo.entity.ScheduleApplication sa = invocation.getArgument(0);
            sa.setId(1L);
            return 1;
        }).when(scheduleApplicationMapper).insert(any(com.parttime.cservice.pojo.entity.ScheduleApplication.class));

        boolean result = jobService.applyForJob(100L, 1L, List.of(10L));

        assertThat(result).isTrue();
        verify(scheduleApplicationMapper).insert(argThat(sa -> "ACCEPTED".equals(sa.getStatus())));
        verify(shiftMapper).insert(any(com.parttime.cservice.pojo.entity.ShiftEntity.class));
    }

    @Test
    void applyForJob_autoApproveDisabled_shouldCreatePendingApplication() {
        Job job = new Job();
        job.setId(1L);
        job.setJobId(1L);
        job.setCompanyId(88L);
        job.setAutoApprove(null);

        JobSchedule schedule = activeFutureSchedule(10L, 1L);
        when(scheduleApplicationMapper.findScheduleIdsByWorkerIdAndJobId(100L, 1L)).thenReturn(List.of());
        when(jobMapper.findByJobId(1L)).thenReturn(Optional.of(job));
        when(jobScheduleMapper.findByIds(List.of(10L))).thenReturn(List.of(schedule));
        when(systemConfigMapper.findByKey("auto_approve_applications")).thenReturn(
                Optional.of(new com.parttime.cservice.pojo.entity.SystemConfig()));
        doAnswer(invocation -> 1).when(scheduleApplicationMapper).insert(any(com.parttime.cservice.pojo.entity.ScheduleApplication.class));

        boolean result = jobService.applyForJob(100L, 1L, List.of(10L));

        assertThat(result).isTrue();
        verify(scheduleApplicationMapper).insert(argThat(sa -> "PENDING".equals(sa.getStatus())));
        verify(shiftMapper, never()).insert(any());
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd c-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test -pl . -Dtest=JobServiceImplApplyForJobTest -q`
Expected: FAIL (SystemConfigMapper, ShiftMapper, NotificationMapper not injected)

- [ ] **Step 3: Implement auto-approve in JobServiceImpl**

Add to `JobServiceImpl.java`:

New imports:
```java
import com.parttime.cservice.mapper.SystemConfigMapper;
import com.parttime.cservice.mapper.ShiftMapper;
import com.parttime.cservice.mapper.NotificationMapper;
import com.parttime.cservice.pojo.entity.ShiftEntity;
import com.parttime.cservice.pojo.entity.SystemConfig;
import com.parttime.cservice.pojo.vo.NotificationVO;
```

New field injections (alongside existing `@Resource` fields):
```java
    @Resource
    private SystemConfigMapper systemConfigMapper;
    @Resource
    private ShiftMapper shiftMapper;
    @Resource
    private NotificationMapper notificationMapper;
```

In `applyForJob()`, after the validation loop (line 235) and before the insert loop (line 236), add auto-approve check:

```java
        // Check auto-approve: job-level overrides platform-level
        Boolean jobAutoApprove = job.getAutoApprove();
        boolean autoApprove;
        if (jobAutoApprove != null) {
            autoApprove = jobAutoApprove;
        } else {
            SystemConfig config = systemConfigMapper.findByKey("auto_approve_applications").orElse(null);
            autoApprove = config != null && "true".equalsIgnoreCase(config.getConfigValue());
        }
```

Replace the existing insert loop (lines 236-241):

```java
        for (Long scheduleId : newIds) {
            ScheduleApplication sa = new ScheduleApplication();
            sa.setScheduleId(scheduleId);
            sa.setWorkerId(workerId);
            sa.setStatus(autoApprove ? "ACCEPTED" : "PENDING");
            scheduleApplicationMapper.insert(sa);

            if (autoApprove) {
                createShiftForApplication(sa, job, schedMap.get(scheduleId));
                sendAutoApproveNotification(sa, job);
            }
        }
```

Add new private methods:

```java
    private void createShiftForApplication(ScheduleApplication sa, Job job, JobSchedule sched) {
        ShiftEntity shift = new ShiftEntity();
        shift.setJobId(job.getId());
        shift.setCompanyId(job.getCompanyId());
        shift.setWorkerId(sa.getWorkerId());
        shift.setShiftDate(sched.getScheduleDate());
        shift.setStartTime(sched.getStartTime());
        shift.setEndTime(sched.getEndTime());
        shift.setLocationLat(job.getLatitude());
        shift.setLocationLng(job.getLongitude());
        shift.setLocationName(job.getAddress());
        shift.setSalaryType(job.getRateType());
        shift.setSalaryAmount(job.getRateAmount());
        shift.setStatus("SCHEDULED");
        shift.setCreatedAt(LocalDateTime.now());
        shift.setUpdatedAt(LocalDateTime.now());
        try {
            shiftMapper.insert(shift);
        } catch (org.springframework.dao.DuplicateKeyException ignored) {
        }
    }

    private void sendAutoApproveNotification(ScheduleApplication sa, Job job) {
        NotificationVO notification = new NotificationVO();
        notification.setRecipientId(sa.getWorkerId());
        notification.setRecipientType("WORKER");
        notification.setType("APPLICATION_ACCEPTED");
        notification.setCategory("application");
        notification.setTitle("报名已通过");
        notification.setContent("您报名的" + job.getTitle() + "已通过审核");
        notification.setStatus("SENT");
        notification.setRead(false);
        notification.setRelatedType("APPLICATION");
        notification.setRelatedId(sa.getId());
        notification.setSentAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd c-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test -q`
Expected: All tests pass (197 original + new auto-approve tests)

- [ ] **Step 5: Commit**

```bash
git add c-service/src/main/java/com/parttime/cservice/service/impl/JobServiceImpl.java c-service/src/test/java/com/parttime/cservice/service/JobServiceImplApplyForJobTest.java
git commit -m "feat: auto-approve applications based on config"
```

---

### Task 4: c-service — auto-settle in checkOut()

**Files:**
- Modify: `c-service/src/main/java/com/parttime/cservice/service/impl/AttendanceServiceImpl.java`
- Modify: `c-service/src/test/java/com/parttime/cservice/service/AttendanceServiceTest.java`

- [ ] **Step 1: Write failing tests for auto-settle**

Add new mocks to `AttendanceServiceTest.java`:

```java
    @Mock
    private com.parttime.cservice.mapper.SystemConfigMapper systemConfigMapper;

    @Mock
    private com.parttime.cservice.mapper.BalanceTransactionMapper balanceTransactionMapper;

    @Mock
    private com.parttime.cservice.mapper.NotificationMapper notificationMapper;
```

Add test methods:

```java
    @Test
    void checkOut_autoSettleEnabled_shouldSettleAndNotify() {
        // Setup: shift ON_DUTY, auto_settle_attendance = true, enterprise balance sufficient
        // Verify: settlementStatus = "PAID", worker balance credited, notification sent
    }

    @Test
    void checkOut_autoSettleDisabled_shouldLeaveUnpaid() {
        // Setup: shift ON_DUTY, auto_settle_attendance = false
        // Verify: settlementStatus = "UNPAID", no balance changes
    }

    @Test
    void checkOut_autoSettleInsufficientBalance_shouldFallbackToUnpaid() {
        // Setup: shift ON_DUTY, auto_settle_attendance = true, enterprise balance insufficient
        // Verify: settlementStatus = "UNPAID", no balance changes, no error thrown
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd c-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test -pl . -Dtest=AttendanceServiceTest -q`
Expected: FAIL

- [ ] **Step 3: Implement auto-settle in AttendanceServiceImpl**

Add to `AttendanceServiceImpl.java`:

New imports:
```java
import com.parttime.cservice.mapper.SystemConfigMapper;
import com.parttime.cservice.mapper.BalanceTransactionMapper;
import com.parttime.cservice.mapper.NotificationMapper;
import com.parttime.cservice.pojo.entity.BalanceTransaction;
import com.parttime.cservice.pojo.entity.SystemConfig;
import com.parttime.cservice.pojo.entity.WorkerBalance;
import com.parttime.cservice.pojo.vo.NotificationVO;
import org.springframework.jdbc.core.JdbcTemplate;
```

New field injections:
```java
    @Resource
    private SystemConfigMapper systemConfigMapper;
    @Resource
    private BalanceTransactionMapper balanceTransactionMapper;
    @Resource
    private NotificationMapper notificationMapper;
    @Resource
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
```

After the existing checkOut logic (after line 243 `shiftMapper.update(shift);`), add:

```java
        // Auto-settle if enabled
        SystemConfig autoSettleConfig = systemConfigMapper.findByKey("auto_settle_attendance").orElse(null);
        if (autoSettleConfig != null && "true".equalsIgnoreCase(autoSettleConfig.getConfigValue())) {
            autoSettle(record, shift, scheduledPay);
        }
```

Add new private method:

```java
    private void autoSettle(AttendanceRecordEntity record, ShiftEntity shift, BigDecimal payAmount) {
        Long workerId = shift.getWorkerId();
        Long companyId = shift.getCompanyId();

        // Check enterprise balance
        BigDecimal enterpriseBalance = jdbcTemplate.queryForObject(
                "SELECT balance FROM enterprise_balances WHERE company_id = ?",
                BigDecimal.class, companyId);

        if (enterpriseBalance == null || enterpriseBalance.compareTo(payAmount) < 0) {
            // Insufficient balance — fallback to UNPAID, don't settle
            return;
        }

        // Credit worker balance
        WorkerBalance wb = workerBalanceMapper.findByWorkerId(workerId);
        BigDecimal newBalance = payAmount;
        BigDecimal newTotalEarned = payAmount;
        if (wb != null) {
            newBalance = wb.getBalance().add(payAmount);
            newTotalEarned = wb.getTotalEarned().add(payAmount);
        }
        workerBalanceMapper.upsert(workerId, newBalance, newTotalEarned, wb != null ? wb.getTotalWithdrawn() : BigDecimal.ZERO);

        // Insert worker balance transaction
        BalanceTransaction bt = new BalanceTransaction();
        bt.setWorkerId(workerId);
        bt.setAmount(payAmount);
        bt.setType("EARNINGS");
        bt.setRelatedAttendanceRecordId(record.getId());
        bt.setDescription("打卡自动结算");
        bt.setCreatedAt(LocalDateTime.now());
        balanceTransactionMapper.insert(bt);

        // Update attendance settlement status
        record.setSettlementStatus("PAID");
        record.setUpdatedAt(LocalDateTime.now());
        attendanceRecordMapper.update(record);

        // Deduct enterprise balance
        jdbcTemplate.update(
                "UPDATE enterprise_balances SET balance = balance - ?, total_spent = total_spent + ?, updated_at = NOW() WHERE company_id = ?",
                payAmount, payAmount, companyId);

        // Insert enterprise balance transaction
        jdbcTemplate.update(
                "INSERT INTO enterprise_balance_transactions (company_id, amount, type, description, created_at) VALUES (?, ?, 'SETTLEMENT', '打卡自动结算', NOW())",
                companyId, payAmount.negate());

        // Send notification
        NotificationVO notification = new NotificationVO();
        notification.setRecipientId(workerId);
        notification.setRecipientType("WORKER");
        notification.setType("EARNINGS");
        notification.setCategory("income");
        notification.setTitle("收入到账");
        notification.setContent("您打卡的班次已自动结算，收入" + payAmount + "元已到账");
        notification.setStatus("SENT");
        notification.setRead(false);
        notification.setRelatedType("ATTENDANCE");
        notification.setRelatedId(record.getId());
        notification.setSentAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd c-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test -q`
Expected: All tests pass

- [ ] **Step 5: Commit**

```bash
git add c-service/src/main/java/com/parttime/cservice/service/impl/AttendanceServiceImpl.java c-service/src/test/java/com/parttime/cservice/service/AttendanceServiceTest.java
git commit -m "feat: auto-settle attendance on check-out based on config"
```

---

### Task 5: enterprise-pc — add auto-approve toggle to job form

**Files:**
- Modify: `enterprise-pc/src/views/jobs/JobForm.vue` (or equivalent job create/edit form)

- [ ] **Step 1: Add auto-approve field to job form**

In the job creation/edit form, add a select field:

```vue
<el-form-item label="自动审核">
  <el-select v-model="form.autoApprove" placeholder="跟随平台默认" clearable>
    <el-option label="跟随平台默认" :value="null" />
    <el-option label="开启" :value="true" />
    <el-option label="关闭" :value="false" />
  </el-select>
</el-form-item>
```

- [ ] **Step 2: Build to verify**

Run: `cd enterprise-pc && npm run build`
Expected: Build passes

- [ ] **Step 3: Commit**

```bash
git add enterprise-pc/src/views/jobs/JobForm.vue
git commit -m "feat: add auto-approve toggle to job form"
```

---

### Task 6: platform-pc — verify system config list displays new items

**Files:**
- Verify: `platform-pc/src/views/configs/SystemConfigList.vue`

- [ ] **Step 1: Verify config list shows new items**

The SystemConfigList.vue already has generic rendering for all config items. The two new configs (`auto_approve_applications`, `auto_settle_attendance`) will appear automatically. Verify by building.

Run: `cd platform-pc && npm run build`
Expected: Build passes, new configs visible in list

- [ ] **Step 2: Commit (if any changes needed)**

```bash
git add platform-pc/
git commit -m "chore: verify new config items in platform admin"
```

---

### Task 7: Final verification — all services and builds

- [ ] **Step 1: Run all backend tests**

```bash
cd c-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test -q
cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test -q
cd platform-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test -q
```

- [ ] **Step 2: Build all frontends**

```bash
cd worker-uniapp && npm run build:h5
cd enterprise-pc && npm run build
cd platform-pc && npm run build
```

- [ ] **Step 3: Final commit if needed**

```bash
git status --short --branch
git diff
```
