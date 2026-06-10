# C-Job/Shift Unification & Attendance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Merge c_job→jobs, c_shift→schedule_shifts, complete attendance flow (check-in/check-out with auto pay calculation), and update worker-uniapp frontend.

**Architecture:** Three independent subsystems touched in order: (1) enterprise-service gets a V11 migration that adds columns to jobs/schedule_shifts, (2) c-service mappers switch from c_* tables to enterprise schema tables, (3) c-service checkOut adds payAmount, (4) worker-uniapp page adjusted for response shape.

**Tech Stack:** Java 17, Spring Boot, MyBatis XML, Flyway, MySQL (shared instance, separate schemas), uni-app, Vue 3.

---

### Task 1: Inspect schema names and baseline configs

**Files:**
- Read: `enterprise-service/src/main/resources/application.yml`
- Read: `c-service/src/main/resources/application.yml`

- [ ] **Step 1: Read both application.yml files**

Find the exact schema names (spring.datasource.url / spring.flyway.schemas) for enterprise-service and c-service. Also check whether c-service has Flyway enabled and its baseline version.

- [ ] **Step 2: Record findings**

```
Example (adjust after reading):
  enterprise-service schema: parttime_enterprise
  c-service schema: parttime_c_service
  c-service flyway enabled: true/false
  c-service flyway baseline-version: <value or none>
```

---

### Task 2: Add V11 migration (enterprise-service) — new columns + backfill

**Files:**
- Create: `enterprise-service/src/main/resources/db/migration/V11__unify_c_job_c_shift.sql`
- Test: `enterprise-service/src/test/java/com/parttime/enterprise/config/FlywayConfigTest.java`

- [ ] **Step 1: Write the failing test**

No new test needed. Existing `FlywayConfigTest` that checks application context loads will catch a bad migration.

- [ ] **Step 2: Write the V11 migration SQL**

Uses `<enterprise_schema>` as the placeholder for the actual schema name found in Task 1:

```sql
-- Add columns to jobs table for c_job merge
ALTER TABLE jobs
    ADD COLUMN company_name VARCHAR(200) DEFAULT NULL,
    ADD COLUMN company_logo VARCHAR(500) DEFAULT NULL,
    ADD COLUMN category_name VARCHAR(100) DEFAULT NULL,
    ADD COLUMN rate_type VARCHAR(20) DEFAULT NULL,
    ADD COLUMN rate_amount DECIMAL(10,2) DEFAULT NULL,
    ADD COLUMN published_at DATETIME DEFAULT NULL,
    ADD COLUMN accepted_count INT DEFAULT 0;

-- Add company_id to schedule_shifts for c_shift merge
ALTER TABLE schedule_shifts
    ADD COLUMN company_id BIGINT DEFAULT NULL,
    ADD INDEX idx_company_id (company_id);

-- Backfill jobs data from c_job (cross-schema)
INSERT INTO <enterprise_schema>.jobs (id, company_id, title, description, location, province, city, district, address, latitude, longitude, category_id, headcount, status, deadline, created_at, updated_at, company_name, company_logo, category_name, rate_type, rate_amount, published_at, accepted_count)
SELECT cj.id, cj.company_id, cj.title, cj.description, cj.location, cj.province, cj.city, cj.district, cj.address, cj.latitude, cj.longitude, cj.category_id, COALESCE(cj.headcount, 0), cj.status, cj.deadline, cj.created_at, cj.updated_at, cj.company_name, cj.company_logo, cj.category_name, cj.rate_type, cj.rate_amount, cj.published_at, 0
FROM <c_service_schema>.c_job cj
ON DUPLICATE KEY UPDATE
    company_name = VALUES(company_name),
    company_logo = VALUES(company_logo),
    category_name = VALUES(category_name),
    rate_type = VALUES(rate_type),
    rate_amount = VALUES(rate_amount),
    published_at = VALUES(published_at);

-- Backfill schedule_shifts data from c_shift
INSERT INTO <enterprise_schema>.schedule_shifts (job_id, worker_id, shift_date, start_time, end_time, location_name, location_lat, location_lng, location_radius, status, company_id, created_at, updated_at)
SELECT cs.job_id, cs.worker_id, cs.shift_date, cs.start_time, cs.end_time, cs.location_name, cs.location_lat, cs.location_lng, cs.location_radius, cs.status, cs.company_id, cs.created_at, cs.updated_at
FROM <c_service_schema>.c_shift cs;

-- Note: DROP TABLE happens in Task 3 (c-service migration) to keep Flyway ordering clean
```

- [ ] **Step 3: Run Flyway test**

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -Dtest=FlywayConfigTest test
```
Expected: PASS

---

### Task 3: Add c-service Flyway migration — drop old tables, update schema

**Files:**
- Create: `c-service/src/main/resources/db/migration/V6__unify_c_job_c_shift.sql`
- Modify: (config if needed)

- [ ] **Step 1: Drop c_job and c_shift tables in c-service schema**

```sql
DROP TABLE IF EXISTS c_job;
DROP TABLE IF EXISTS c_shift;
```

(The data has been backfilled in Task 2.)

- [ ] **Step 2: Verify c-service Flyway config**

Read `c-service/src/main/resources/application.yml` to confirm Flyway is enabled and baseline is set correctly. Add baseline config if missing:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 5
```

(Use V5 as baseline since the last migration is V5_add_company_logo_to_c_job.)

---

### Task 4: Update c-service JobMapper — switch to jobs table

**Files:**
- Modify: `c-service/src/main/resources/mapper/JobMapper.xml`
- Modify: `c-service/src/main/java/com/parttime/cservice/pojo/entity/Job.java`

- [ ] **Step 1: Update JobMapper.xml**

Replace all `c_job` references with `<enterprise_schema>.jobs` (cross-schema). Remove `job_id` column from INSERT/SELECT:

```xml
<mapper namespace="com.parttime.cservice.mapper.JobMapper">
    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO <enterprise_schema>.jobs (company_id, company_name, company_logo, title, description, location,
                            province, city, district, address, latitude, longitude,
                            category_id, category_name, rate_type, rate_amount, status, published_at,
                            created_at, updated_at)
        VALUES (#{companyId}, #{companyName}, #{companyLogo}, #{title}, #{description}, #{location},
                #{province}, #{city}, #{district}, #{address}, #{latitude}, #{longitude},
                #{categoryId}, #{categoryName}, #{rateType}, #{rateAmount}, #{status}, #{publishedAt},
                #{createdAt}, #{updatedAt})
    </insert>

    <select id="findById" resultType="com.parttime.cservice.pojo.entity.Job">
        SELECT * FROM <enterprise_schema>.jobs WHERE id = #{id}
    </select>

    <select id="findByJobId" resultType="com.parttime.cservice.pojo.entity.Job">
        SELECT * FROM <enterprise_schema>.jobs WHERE id = #{jobId}
    </select>

    <!-- All other queries: c_job → <enterprise_schema>.jobs -->
</mapper>
```

- [ ] **Step 2: Update Job entity**

Remove `jobId` field (or keep as alias for backward compatibility). Remove `scheduleInfo` field if not needed. Add `headcount`, `acceptedCount` (already have them). No structural changes needed — fields already match.

- [ ] **Step 3: Update JobServiceImpl**

Change `findByJobId` calls to `findById` where appropriate.

---

### Task 5: Update c-service ShiftMapper — switch to schedule_shifts

**Files:**
- Modify: `c-service/src/main/resources/mapper/ShiftMapper.xml`
- Modify: `c-service/src/main/java/com/parttime/cservice/pojo/entity/ShiftEntity.java`

- [ ] **Step 1: Rewrite ShiftMapper.xml**

Replace `c_shift` with `<enterprise_schema>.schedule_shifts`. Add JOIN to `jobs` for `job_title` and `job_location`:

```xml
<mapper namespace="com.parttime.cservice.mapper.ShiftMapper">
    <resultMap id="ShiftResultMap" type="com.parttime.cservice.pojo.entity.ShiftEntity">
        <id property="id" column="id"/>
        <result property="jobId" column="job_id"/>
        <result property="jobTitle" column="job_title"/>
        <result property="jobLocation" column="job_location"/>
        <result property="workerId" column="worker_id"/>
        <result property="companyId" column="company_id"/>
        <result property="shiftDate" column="shift_date"/>
        <result property="startTime" column="start_time"/>
        <result property="endTime" column="end_time"/>
        <result property="locationLat" column="location_lat"/>
        <result property="locationLng" column="location_lng"/>
        <result property="locationRadius" column="location_radius"/>
        <result property="locationName" column="location_name"/>
        <result property="status" column="status"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO <enterprise_schema>.schedule_shifts
            (worker_id, job_id, company_id, shift_date, start_time, end_time,
             location_name, location_lat, location_lng, location_radius, status,
             created_at, updated_at)
        VALUES (#{workerId}, #{jobId}, #{companyId}, #{shiftDate}, #{startTime}, #{endTime},
                #{locationName}, #{locationLat}, #{locationLng}, #{locationRadius}, #{status},
                #{createdAt}, #{updatedAt})
    </insert>

    <select id="findById" resultMap="ShiftResultMap">
        SELECT ss.*, j.title AS job_title, j.location AS job_location
        FROM <enterprise_schema>.schedule_shifts ss
        LEFT JOIN <enterprise_schema>.jobs j ON ss.job_id = j.id
        WHERE ss.id = #{id}
    </select>

    <select id="findByWorkerId" resultMap="ShiftResultMap">
        SELECT ss.*, j.title AS job_title, j.location AS job_location
        FROM <enterprise_schema>.schedule_shifts ss
        LEFT JOIN <enterprise_schema>.jobs j ON ss.job_id = j.id
        WHERE ss.worker_id = #{workerId}
    </select>

    <!-- ... similar for all other queries -->
</mapper>
```

Make sure every SELECT includes the LEFT JOIN for job_title/job_location.

- [ ] **Step 2: Update ShiftEntity**

No field changes needed — it already has `jobTitle`, `jobLocation`, and all schedule_shifts columns.

---

### Task 6: Update c-service AttendanceRecordMapper — switch to attendance_records

**Files:**
- Modify: `c-service/src/main/resources/mapper/AttendanceRecordMapper.xml`
- Modify: `c-service/src/main/java/com/parttime/cservice/pojo/entity/AttendanceRecordEntity.java`

- [ ] **Step 1: Rewrite AttendanceRecordMapper.xml**

Replace `c_attendance_record` with `<enterprise_schema>.attendance_records`:

```xml
<mapper namespace="com.parttime.cservice.mapper.AttendanceRecordMapper">
    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO <enterprise_schema>.attendance_records
            (shift_id, worker_id, check_in_time, check_out_time, total_hours, pay_amount, calculated_at, status, created_at, updated_at)
        VALUES (#{shiftId}, #{workerId}, #{checkInTime}, #{checkOutTime}, #{totalHours}, #{payAmount}, #{calculatedAt}, #{status}, #{createdAt}, #{updatedAt})
    </insert>

    <select id="findById" ...>
        SELECT * FROM <enterprise_schema>.attendance_records WHERE id = #{id}
    </select>

    <!-- ... similar for all other queries -->
</mapper>
```

- [ ] **Step 2: Update AttendanceRecordEntity**

Add `payAmount` (BigDecimal) and `calculatedAt` (LocalDateTime) fields:

```java
public class AttendanceRecordEntity {
    // ...existing fields
    private BigDecimal payAmount;
    private LocalDateTime calculatedAt;
}
```

---

### Task 7: Update AttendanceServiceImpl.checkOut — calculate pay

**Files:**
- Modify: `c-service/src/main/java/com/parttime/cservice/service/impl/AttendanceServiceImpl.java`
- Test: `c-service/src/test/java/com/parttime/cservice/service/AttendanceServiceTest.java`

- [ ] **Step 1: Write the failing test**

Add test for pay calculation after checkOut:

```java
@Test
void checkOut_shouldCalculatePayAmount() {
    attendanceService.addShift(10L, "Helper", "Shanghai", 1L,
            LocalDate.of(2026, 6, 1), LocalTime.of(9, 0), LocalTime.of(18, 0),
            null, null, null, null);

    List<WorkerShiftVO> shifts = attendanceService.getMyShifts(1L, null, null);
    Long shiftId = shifts.get(0).getShiftId();

    attendanceService.checkIn(1L, shiftId, null, null);
    AttendanceVO response = attendanceService.checkOut(1L, shiftId, null, null);

    assertThat(response.getTotalHours()).isNotNull();
    assertThat(response.getTotalHours().compareTo(BigDecimal.ZERO)).isGreaterThan(0);
    assertThat(response.getPayAmount()).isNotNull(); // new assertion
    assertThat(response.getCalculatedAt()).isNotNull(); // new assertion
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -Dtest=AttendanceServiceTest test
```
Expected: some assertions fail because payAmount/calculatedAt not present.

- [ ] **Step 3: Update AttendanceServiceImpl.checkOut**

After calculating totalHours, look up the shift's salary info and compute payAmount:

```java
@Override
public AttendanceVO checkOut(Long workerId, Long shiftId, BigDecimal lat, BigDecimal lng) {
    ShiftEntity shift = shiftMapper.findById(shiftId)
            .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftId));

    if (!shift.getWorkerId().equals(workerId)) {
        throw new RuntimeException("Shift does not belong to this worker");
    }

    AttendanceRecordEntity record = attendanceRecordMapper.findByShiftId(shiftId)
            .orElseThrow(() -> new RuntimeException("No check-in record found for this shift"));

    if (!"CHECKED_IN".equals(record.getStatus())) {
        throw new RuntimeException("Cannot check out: status is " + record.getStatus());
    }

    LocalDateTime checkOutTime = LocalDateTime.now();
    Duration duration = Duration.between(record.getCheckInTime(), checkOutTime);
    BigDecimal hours = BigDecimal.valueOf(duration.toMinutes() / 60.0)
            .setScale(2, RoundingMode.HALF_UP);

    // Calculate pay from shift's salary snapshot (or job's rate if no snapshot)
    BigDecimal payAmount = BigDecimal.ZERO;
    String rateType = shift.getSalaryType() != null ? shift.getSalaryType() : "HOURLY";
    BigDecimal rateAmount = shift.getSalaryAmount();
    if (rateAmount == null || rateAmount.compareTo(BigDecimal.ZERO) == 0) {
        // Fallback: look up from job_rates
        // For now, skip if no rate available
    } else if ("HOURLY".equals(rateType)) {
        payAmount = hours.multiply(rateAmount).setScale(2, RoundingMode.HALF_UP);
    } else if ("DAILY".equals(rateType)) {
        payAmount = rateAmount.setScale(2, RoundingMode.HALF_UP);
    }

    record.setCheckOutTime(checkOutTime);
    record.setTotalHours(hours);
    record.setPayAmount(payAmount);
    record.setCalculatedAt(LocalDateTime.now());
    record.setStatus("CHECKED_OUT");
    record.setUpdatedAt(LocalDateTime.now());
    attendanceRecordMapper.update(record);

    shift.setStatus("CHECKED_OUT");
    shift.setUpdatedAt(LocalDateTime.now());
    shiftMapper.update(shift);

    return toAttendanceResponse(record);
}
```

Also need to add `salaryType` and `salaryAmount` fields to `ShiftEntity`:

```java
// ShiftEntity.java additions
private String salaryType;
private BigDecimal salaryAmount;
```

And update ShiftMapper.xml `<select>` resultMap to include `ss.salary_type AS salaryType, ss.salary_amount AS salaryAmount`.

- [ ] **Step 4: Update AttendanceVO**

Add `payAmount` and `calculatedAt` fields:

```java
// AttendanceVO.java
private BigDecimal payAmount;
private LocalDateTime calculatedAt;
```

And populate them in `toAttendanceResponse`:

```java
resp.setPayAmount(record.getPayAmount());
resp.setCalculatedAt(record.getCalculatedAt());
```

- [ ] **Step 5: Run test to verify it passes**

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -Dtest=AttendanceServiceTest test
```
Expected: PASS.

---

### Task 8: Update InMemoryMappers for c-service tests

**Files:**
- Modify: `c-service/src/test/java/com/parttime/cservice/service/InMemoryMappers.java`

- [ ] **Step 1: Update ShiftMapper in-memory implementation**

Add `salaryType` and `salaryAmount` fields to ShiftEntity when creating instances:

No change needed to InMemoryMappers — it stores whatever fields the entity has. Just verify the store operations work.

---

### Task 9: Fix worker-uniapp clockIn.vue field mapping

**Files:**
- Modify: `worker-uniapp/src/pages/attendance/clockIn.vue`

- [ ] **Step 1: Fix request payload field names**

Change `latitude`/`longitude` to `lat`/`lng`:

```typescript
// clockIn.vue - handleCheckIn
await checkIn({
  shiftId: shift.id,
  lat: location.latitude,
  lng: location.longitude
})

// handleCheckOut
await checkOut({
  shiftId: shift.id,
  lat: location.latitude,
  lng: location.longitude
})
```

- [ ] **Step 2: Fix response mapping for today's shifts**

The `getMyShifts` API returns `shiftId` (not `id`). Update the mapping:

```typescript
shifts.value = list.map((s: any) => ({
  id: s.shiftId, // already correct
  jobTitle: s.jobTitle,
  location: s.jobLocation || s.locationName,
  startTime: s.startTime,
  endTime: s.endTime,
  date: s.shiftDate,
  status: s.status || '',
  checkedIn: s.status === 'CHECKED_IN' || s.status === 'CHECKED_OUT',
  checkedOut: s.status === 'CHECKED_OUT'
}))
```

---

### Task 10: Run all affected tests

- [ ] **Step 1: Run c-service tests**

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn test
```
Expected: All tests pass (pre-existing failures unrelated to this change are acceptable).

- [ ] **Step 2: Run enterprise-service tests**

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn test
```
Expected: FlywayConfigTest and ApplicationServiceTest pass.
