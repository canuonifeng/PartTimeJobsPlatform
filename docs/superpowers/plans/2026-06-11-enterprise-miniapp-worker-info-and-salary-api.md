# Enterprise Miniapp Worker Info and Salary API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show worker name, phone, gender, and age in enterprise miniapp application and schedule cards, and ensure salary management requests `/attendance/hours` when the page opens.

**Architecture:** Backend VOs will expose missing worker fields. Existing enterprise miniapp pages will consume these fields in the approved “basic info at top” card layout. Salary management will use the same page-level loading pattern as other list pages and call the existing attendance API wrapper.

**Tech Stack:** Spring Boot 3, MyBatis XML, Vue 3 script setup, UniApp, WeChat Mini Program build.

---

## File Structure

- Modify `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/ScheduleApplicationVO.java`: add `workerGender`.
- Modify `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/ScheduleShiftVO.java`: add `workerPhone`, `workerGender`.
- Modify `enterprise-service/src/main/resources/mapper/ScheduleApplicationMapper.xml`: map and select `worker_gender` from `worker_profiles.gender`.
- Modify `enterprise-service/src/main/java/com/parttime/enterprise/mapper/WorkerSyncMapper.java`: add batch worker phone/gender profile methods.
- Modify `enterprise-service/src/main/resources/mapper/WorkerSyncMapper.xml`: add `<foreach>` queries for phones and genders.
- Modify `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/ScheduleServiceImpl.java`: batch-load phone/gender and set fields on `ScheduleShiftVO`.
- Modify `enterprise-service/src/test/java/com/parttime/enterprise/service/ScheduleServiceTest.java`: assert schedule VO exposes phone/gender.
- Modify `enterprise-service/src/test/java/com/parttime/enterprise/controller/ScheduleControllerTest.java`: keep controller contract passing.
- Modify `enterprise-uniapp/src/pages/applications/applicationList.vue`: render `姓名 · 性别 · 年龄 · 手机号` at top.
- Modify `enterprise-uniapp/src/pages/schedules/scheduleList.vue`: render `姓名 · 性别 · 年龄 · 手机号` at top.
- Modify `enterprise-uniapp/src/pages/attendance/attendanceList.vue`: fix initialization so entering page calls `listAttendance()`.

---

### Task 1: Backend application VO exposes worker gender

**Files:**
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/ScheduleApplicationVO.java`
- Modify: `enterprise-service/src/main/resources/mapper/ScheduleApplicationMapper.xml`

- [ ] **Step 1: Add failing expectation manually via mapper XML review**

Expected API item should include:

```json
{
  "workerName": "张三",
  "workerPhone": "13800000000",
  "workerGender": "MALE",
  "workerAge": 28
}
```

- [ ] **Step 2: Add field to VO**

In `ScheduleApplicationVO.java`, after `workerPhone` add:

```java
@Schema(description = "工人性别")
private String workerGender;
```

- [ ] **Step 3: Map gender in resultMap**

In `ScheduleApplicationMapper.xml`, add:

```xml
<result property="workerGender" column="worker_gender"/>
```

- [ ] **Step 4: Select gender in all VO queries**

Update the three VO `SELECT` clauses to include:

```sql
wp.gender AS worker_gender,
```

The selected worker fields should be:

```sql
sa.worker_id, w.name as worker_name, w.phone as worker_phone, wp.gender AS worker_gender,
```

- [ ] **Step 5: Run enterprise tests**

Run:

```bash
cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test
```

Expected: `BUILD SUCCESS`.

---

### Task 2: Backend schedule VO exposes worker phone and gender

**Files:**
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/ScheduleShiftVO.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/WorkerSyncMapper.java`
- Modify: `enterprise-service/src/main/resources/mapper/WorkerSyncMapper.xml`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/ScheduleServiceImpl.java`
- Modify: `enterprise-service/src/test/java/com/parttime/enterprise/service/ScheduleServiceTest.java`

- [ ] **Step 1: Extend ScheduleShiftVO**

After `workerName`, add:

```java
@Schema(description = "工人手机号")
private String workerPhone;
@Schema(description = "工人性别")
private String workerGender;
```

- [ ] **Step 2: Add mapper methods**

In `WorkerSyncMapper.java`, add:

```java
List<Map<String, Object>> findWorkerPhonesByIds(@Param("ids") List<Long> ids);

List<Map<String, Object>> findWorkerGendersByIds(@Param("ids") List<Long> ids);
```

- [ ] **Step 3: Add XML queries**

In `WorkerSyncMapper.xml`, add:

```xml
<select id="findWorkerPhonesByIds" resultType="java.util.Map">
    SELECT id, phone FROM c_worker WHERE id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</select>

<select id="findWorkerGendersByIds" resultType="java.util.Map">
    SELECT worker_id, gender FROM worker_profiles WHERE worker_id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</select>
```

- [ ] **Step 4: Batch load maps in ScheduleServiceImpl**

In `getShifts`, add maps beside `workerNameMap`/`workerAgeMap`:

```java
Map<Long, String> workerPhoneMap = new HashMap<>();
Map<Long, String> workerGenderMap = new HashMap<>();
```

Inside `if (!workerIds.isEmpty())`, add:

```java
workerSyncMapper.findWorkerPhonesByIds(workerIds).forEach(m -> workerPhoneMap.put((Long) m.get("id"), (String) m.get("phone")));
workerSyncMapper.findWorkerGendersByIds(workerIds).forEach(m -> workerGenderMap.put((Long) m.get("worker_id"), (String) m.get("gender")));
```

Pass maps into `toShiftResponse`.

- [ ] **Step 5: Set fields in toShiftResponse**

Change method signature to accept phone/gender maps and set:

```java
response.setWorkerPhone(workerPhoneMap.get(shift.getWorkerId()));
response.setWorkerGender(workerGenderMap.get(shift.getWorkerId()));
```

For overloads used by single-shift operations, pass empty maps.

- [ ] **Step 6: Update service test**

In `ScheduleServiceTest.getShifts_shouldFilterByWorkerId`, stub:

```java
when(workerSyncMapper.findWorkerNamesByIds(List.of(20L))).thenReturn(List.of(Map.of("id", 20L, "name", "TestWorker")));
when(workerSyncMapper.findWorkerPhonesByIds(List.of(20L))).thenReturn(List.of(Map.of("id", 20L, "phone", "13800000000")));
when(workerSyncMapper.findWorkerGendersByIds(List.of(20L))).thenReturn(List.of(Map.of("worker_id", 20L, "gender", "MALE")));
when(workerSyncMapper.findWorkerBirthdaysByIds(List.of(20L))).thenReturn(List.of());
```

Assert:

```java
assertThat(result.getRecords().get(0).getWorkerPhone()).isEqualTo("13800000000");
assertThat(result.getRecords().get(0).getWorkerGender()).isEqualTo("MALE");
```

- [ ] **Step 7: Run enterprise tests**

Run:

```bash
cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test
```

Expected: `BUILD SUCCESS`.

---

### Task 3: Frontend application card displays basic info at top

**Files:**
- Modify: `enterprise-uniapp/src/pages/applications/applicationList.vue`

- [ ] **Step 1: Add helpers**

Add:

```js
function genderLabel(gender) {
  const map = { MALE: '男', FEMALE: '女', OTHER: '其他' }
  return map[gender] || gender || '未知'
}

function ageLabel(age) {
  return age == null ? '年龄未知' : `${age}岁`
}

function phoneLabel(phone) {
  return phone || '暂无手机号'
}
```

- [ ] **Step 2: Update card header subtitle**

Render:

```vue
<text class="worker-meta">{{ genderLabel(app.workerGender) }} · {{ ageLabel(app.workerAge) }} · {{ phoneLabel(app.workerPhone) }}</text>
```

- [ ] **Step 3: Remove duplicated phone info block**

Replace the first `info-item` with job info so the card shows:

```vue
<text class="info-label">报名岗位</text>
<text class="info-value">{{ app.jobTitle || '-' }}</text>
```

- [ ] **Step 4: Ensure styling truncates**

Add:

```css
.worker-meta {
  font-size: 24rpx;
  color: #98a3b3;
  display: block;
  margin-top: 4rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
```

- [ ] **Step 5: Build miniapp**

Run:

```bash
cd enterprise-uniapp && npm run build:mp-weixin
```

Expected: `DONE Build complete.`

---

### Task 4: Frontend schedule card displays basic info at top

**Files:**
- Modify: `enterprise-uniapp/src/pages/schedules/scheduleList.vue`

- [ ] **Step 1: Add helpers**

Add the same helpers:

```js
function genderLabel(gender) {
  const map = { MALE: '男', FEMALE: '女', OTHER: '其他' }
  return map[gender] || gender || '未知'
}

function ageLabel(age) {
  return age == null ? '年龄未知' : `${age}岁`
}

function phoneLabel(phone) {
  return phone || '暂无手机号'
}
```

- [ ] **Step 2: Update subtitle**

Replace the subtitle under worker name with:

```vue
<text class="worker-meta">{{ genderLabel(s.workerGender) }} · {{ ageLabel(s.workerAge) }} · {{ phoneLabel(s.workerPhone) }}</text>
```

- [ ] **Step 3: Remove age pill from meta row**

Keep job information and remove the duplicate age pill.

- [ ] **Step 4: Build miniapp**

Run:

```bash
cd enterprise-uniapp && npm run build:mp-weixin
```

Expected: `DONE Build complete.`

---

### Task 5: Fix salary management page request initialization

**Files:**
- Modify: `enterprise-uniapp/src/pages/attendance/attendanceList.vue`
- Test manually via WeChat devtools network panel.

- [ ] **Step 1: Replace onMounted with onShow**

Change imports:

```js
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
```

Remove Vue `onMounted` import if unused.

- [ ] **Step 2: Add refresh function**

Add:

```js
function refreshRecords() {
  page.value = 1
  records.value = []
  hasMore.value = true
  selectedIds.value = []
  return loadRecords()
}
```

- [ ] **Step 3: Call refresh on page show**

Replace:

```js
onMounted(loadRecords)
```

with:

```js
onShow(refreshRecords)
```

- [ ] **Step 4: Use refresh in pull-down and after mutations**

Replace duplicated reset blocks with:

```js
refreshRecords()
```

- [ ] **Step 5: Build miniapp**

Run:

```bash
cd enterprise-uniapp && npm run build:mp-weixin
```

Expected: `DONE Build complete.`

---

### Task 6: Final verification and commit

**Files:**
- All modified backend and frontend files.

- [ ] **Step 1: Run backend tests**

```bash
cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 2: Run miniapp build**

```bash
cd enterprise-uniapp && npm run build:mp-weixin
```

Expected: `DONE Build complete.`

- [ ] **Step 3: Inspect git diff**

```bash
git status --short
git diff --check
git diff
```

Expected: only files listed in this plan are changed; no whitespace errors.

- [ ] **Step 4: Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/ScheduleApplicationVO.java \
  enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/ScheduleShiftVO.java \
  enterprise-service/src/main/java/com/parttime/enterprise/mapper/WorkerSyncMapper.java \
  enterprise-service/src/main/resources/mapper/WorkerSyncMapper.xml \
  enterprise-service/src/main/resources/mapper/ScheduleApplicationMapper.xml \
  enterprise-service/src/main/java/com/parttime/enterprise/service/impl/ScheduleServiceImpl.java \
  enterprise-service/src/test/java/com/parttime/enterprise/service/ScheduleServiceTest.java \
  enterprise-uniapp/src/pages/applications/applicationList.vue \
  enterprise-uniapp/src/pages/schedules/scheduleList.vue \
  enterprise-uniapp/src/pages/attendance/attendanceList.vue

git commit -m "feat(enterprise-miniapp): 展示零工基本信息并修复薪资加载"
```

- [ ] **Step 5: Push**

```bash
git push
```

Expected: push succeeds.
