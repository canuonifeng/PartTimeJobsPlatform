# Auto Generate Shifts On Application Acceptance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** When a job application is accepted, generate one frozen shift per job schedule, then support punch-in/punch-out, automatic hours calculation, and payroll calculation from stored attendance results.

**Architecture:** Keep the change inside the existing enterprise-service boundaries. `ApplicationServiceImpl` generates shifts synchronously after acceptance, `ScheduleServiceImpl` owns punch-in/punch-out and attendance calculation, and `PayrollServiceImpl` reads persisted attendance pay results instead of recomputing everything from scratch.

**Tech Stack:** Java 17, Spring Boot, MyBatis XML mappers, JUnit 5, Mockito, AssertJ.

---

### Task 1: Add shift and attendance snapshot fields

**Files:**
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/ScheduleShift.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/AttendanceRecord.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/ScheduleShiftVO.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/AttendanceRecordVO.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/ScheduleShiftMapper.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/AttendanceRecordMapper.java`
- Modify: `enterprise-service/src/main/resources/mapper/ScheduleShiftMapper.xml`
- Modify: `enterprise-service/src/main/resources/mapper/AttendanceRecordMapper.xml`
- Test: `enterprise-service/src/test/java/com/parttime/enterprise/service/ScheduleServiceTest.java`

- [ ] **Step 1: Write the failing test**

Add a service test that captures a generated shift and asserts the snapshot fields are carried through:

```java
@Test
void getShifts_shouldExposeSnapshotFields() {
    ScheduleShift shift = new ScheduleShift();
    shift.setId(1L);
    shift.setJobId(100L);
    shift.setWorkerId(200L);
    shift.setApplicationId(300L);
    shift.setSalaryType("HOURLY");
    shift.setSalaryAmount(new BigDecimal("25.00"));
    shift.setSalaryCurrency("CNY");

    when(shiftMapper.findByJobId(100L)).thenReturn(List.of(shift));

    List<ScheduleShiftVO> responses = scheduleService.getShifts(100L, null, null);

    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).getApplicationId()).isEqualTo(300L);
    assertThat(responses.get(0).getSalaryType()).isEqualTo("HOURLY");
    assertThat(responses.get(0).getSalaryAmount()).isEqualByComparingTo(new BigDecimal("25.00"));
    assertThat(responses.get(0).getSalaryCurrency()).isEqualTo("CNY");
}
```

Also add a mapper-level test or service test assertion that `ScheduleShiftVO` exposes `applicationId`, `salaryType`, `salaryAmount`, and `salaryCurrency`.

- [ ] **Step 2: Run test to verify it fails**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -q -Dtest=ScheduleServiceTest test`

Expected: FAIL because the new snapshot fields and punch methods do not exist yet.

- [ ] **Step 3: Write minimal implementation**

Add the fields and mapper columns only, without business logic changes:

```java
// ScheduleShift.java
private Long applicationId;
private String salaryType;
private BigDecimal salaryAmount;
private String salaryCurrency;

// AttendanceRecord.java
private BigDecimal payAmount;
private LocalDateTime calculatedAt;
```

Update XML mappings and insert/update statements to persist the new columns.

- [ ] **Step 4: Run test to verify it passes**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -q -Dtest=ScheduleServiceTest test`

Expected: PASS for the field exposure assertions.

- [ ] **Step 5: Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/ScheduleShift.java enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/AttendanceRecord.java enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/ScheduleShiftVO.java enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/AttendanceRecordVO.java enterprise-service/src/main/java/com/parttime/enterprise/mapper/ScheduleShiftMapper.java enterprise-service/src/main/java/com/parttime/enterprise/mapper/AttendanceRecordMapper.java enterprise-service/src/main/resources/mapper/ScheduleShiftMapper.xml enterprise-service/src/main/resources/mapper/AttendanceRecordMapper.xml enterprise-service/src/test/java/com/parttime/enterprise/service/ScheduleServiceTest.java
git commit -m "feat: add shift and attendance snapshots"
```

### Task 2: Generate shifts when an application is accepted

**Files:**
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/ApplicationServiceImpl.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/ApplicationService.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/JobScheduleMapper.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/JobRateMapper.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/ScheduleShiftMapper.java`
- Modify: `enterprise-service/src/main/resources/mapper/ScheduleShiftMapper.xml`
- Modify: `enterprise-service/src/test/java/com/parttime/enterprise/service/ApplicationServiceTest.java`

- [ ] **Step 1: Write the failing test**

Add a test that accepts a pending application and verifies one shift per job schedule is inserted with a frozen snapshot:

```java
@Test
void acceptApplication_shouldGenerateOneShiftPerJobSchedule() {
    JobApplication app = new JobApplication();
    app.setId(1L);
    app.setJobId(100L);
    app.setWorkerId(200L);
    app.setStatus("PENDING");

    Job job = new Job();
    job.setId(100L);
    job.setCompanyId(300L);
    job.setHeadcount(5);

    JobSchedule s1 = new JobSchedule();
    s1.setId(11L);
    s1.setJobId(100L);
    s1.setScheduleDate(LocalDate.of(2026, 6, 1));
    s1.setStartTime(LocalTime.of(9, 0));
    s1.setEndTime(LocalTime.of(18, 0));

    JobSchedule s2 = new JobSchedule();
    s2.setId(12L);
    s2.setJobId(100L);
    s2.setScheduleDate(LocalDate.of(2026, 6, 2));
    s2.setStartTime(LocalTime.of(10, 0));
    s2.setEndTime(LocalTime.of(19, 0));

    JobRate rate = new JobRate();
    rate.setType("HOURLY");
    rate.setAmount(new BigDecimal("25.00"));
    rate.setCurrency("CNY");

    when(applicationMapper.findById(1L)).thenReturn(Optional.of(app));
    when(jobMapper.findById(100L)).thenReturn(Optional.of(job));
    when(applicationMapper.countByJobIdAndStatus(100L, "ACCEPTED")).thenReturn(0);
    when(jobScheduleMapper.findByJobId(100L)).thenReturn(List.of(s1, s2));
    when(jobRateMapper.findByJobId(100L)).thenReturn(List.of(rate));

    applicationService.acceptApplication(1L);

    verify(shiftMapper, times(2)).insert(any(ScheduleShift.class));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -q -Dtest=ApplicationServiceTest test`

Expected: FAIL because `acceptApplication` still only updates the application and does not create shifts.

- [ ] **Step 3: Write minimal implementation**

In `ApplicationServiceImpl.acceptApplication`, after setting the application to `ACCEPTED`, load all `job_schedules` and the first `job_rates` record, then insert one `ScheduleShift` per schedule. Copy the snapshot values from the schedule and rate:

```java
ScheduleShift shift = new ScheduleShift();
shift.setApplicationId(app.getId());
shift.setJobId(app.getJobId());
shift.setWorkerId(app.getWorkerId());
shift.setShiftDate(schedule.getScheduleDate());
shift.setStartTime(schedule.getStartTime());
shift.setEndTime(schedule.getEndTime());
shift.setStatus("SCHEDULED");
shift.setSalaryType(rate.getType());
shift.setSalaryAmount(rate.getAmount());
shift.setSalaryCurrency(rate.getCurrency());
shiftMapper.insert(shift);
```

Keep the method transactional so the application update and shift generation either both succeed or both roll back.

- [ ] **Step 4: Run test to verify it passes**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -q -Dtest=ApplicationServiceTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/service/impl/ApplicationServiceImpl.java enterprise-service/src/main/java/com/parttime/enterprise/service/ApplicationService.java enterprise-service/src/main/java/com/parttime/enterprise/mapper/JobScheduleMapper.java enterprise-service/src/main/java/com/parttime/enterprise/mapper/JobRateMapper.java enterprise-service/src/main/java/com/parttime/enterprise/mapper/ScheduleShiftMapper.java enterprise-service/src/main/resources/mapper/ScheduleShiftMapper.xml enterprise-service/src/test/java/com/parttime/enterprise/service/ApplicationServiceTest.java
git commit -m "feat: generate shifts on application acceptance"
```

### Task 3: Add punch-in and punch-out APIs

**Files:**
- Add: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/cmd/AttendancePunchCmd.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/ScheduleService.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/ScheduleServiceImpl.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/controller/ScheduleController.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/AttendanceRecordMapper.java`
- Modify: `enterprise-service/src/main/resources/mapper/AttendanceRecordMapper.xml`
- Modify: `enterprise-service/src/test/java/com/parttime/enterprise/controller/ScheduleControllerTest.java`
- Modify: `enterprise-service/src/test/java/com/parttime/enterprise/service/ScheduleServiceTest.java`

- [ ] **Step 1: Write the failing test**

Add controller tests for the new punch endpoints and service tests for the status transitions:

```java
@Test
void checkIn_shouldCreateAttendanceRecord() throws Exception {
    mockMvc.perform(post("/api/attendance/check-in")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"shiftId\":10,\"checkLat\":31.22,\"checkLng\":121.48}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shiftId").value(10L))
        .andExpect(jsonPath("$.checkInTime").exists());
}
```

Add a service test that punches out and asserts the record status changes to completed and `checkOutTime` is populated.

- [ ] **Step 2: Run test to verify it fails**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -q -Dtest=ScheduleControllerTest,ScheduleServiceTest test`

Expected: FAIL because the endpoints and service methods do not exist yet.

- [ ] **Step 3: Write minimal implementation**

Add `AttendancePunchCmd` with `shiftId`, `checkLat`, and `checkLng`. Implement:

```java
public AttendanceRecordVO checkIn(AttendancePunchCmd request)
public AttendanceRecordVO checkOut(AttendancePunchCmd request)
```

Rules:
- `checkIn` creates a record if one does not exist, sets `checkInTime = LocalDateTime.now()`, and updates the shift to `CHECKED_IN`.
- `checkOut` loads the record, sets `checkOutTime = LocalDateTime.now()`, computes `totalHours`, computes `payAmount = totalHours * salaryAmount` for hourly shifts, sets `calculatedAt`, and updates the shift to `COMPLETED`.
- If the shift is missing, if check-out happens before check-in, or if the record is already closed, throw `BusinessException`.

- [ ] **Step 4: Run test to verify it passes**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -q -Dtest=ScheduleControllerTest,ScheduleServiceTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/pojo/cmd/AttendancePunchCmd.java enterprise-service/src/main/java/com/parttime/enterprise/service/ScheduleService.java enterprise-service/src/main/java/com/parttime/enterprise/service/impl/ScheduleServiceImpl.java enterprise-service/src/main/java/com/parttime/enterprise/controller/ScheduleController.java enterprise-service/src/main/java/com/parttime/enterprise/mapper/AttendanceRecordMapper.java enterprise-service/src/main/resources/mapper/AttendanceRecordMapper.xml enterprise-service/src/test/java/com/parttime/enterprise/controller/ScheduleControllerTest.java enterprise-service/src/test/java/com/parttime/enterprise/service/ScheduleServiceTest.java
git commit -m "feat: add attendance punch endpoints"
```

### Task 4: Use persisted attendance results in payroll calculation

**Files:**
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/PayrollServiceImpl.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/PayrollItem.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/PayrollItemVO.java`
- Modify: `enterprise-service/src/test/java/com/parttime/enterprise/service/PayrollServiceTest.java`

- [ ] **Step 1: Write the failing test**

Add a payroll test that expects the batch to use `AttendanceRecord.payAmount` when it exists:

```java
@Test
void calculateBatch_shouldUsePersistedPayAmountWhenPresent() {
    PayrollBatch batch = new PayrollBatch();
    batch.setId(1L);
    batch.setCompanyId(1L);
    batch.setPeriodStart(LocalDate.of(2026, 6, 1));
    batch.setPeriodEnd(LocalDate.of(2026, 6, 30));
    batch.setStatus("DRAFT");

    ScheduleShift shift = new ScheduleShift();
    shift.setId(10L);
    shift.setWorkerId(100L);
    shift.setJobId(200L);

    AttendanceRecord record = new AttendanceRecord();
    record.setShiftId(10L);
    record.setTotalHours(new BigDecimal("8.00"));
    record.setPayAmount(new BigDecimal("200.00"));

    when(payrollBatchMapper.findById(1L)).thenReturn(Optional.of(batch));
    when(scheduleShiftMapper.findByDateRange(batch.getPeriodStart(), batch.getPeriodEnd())).thenReturn(List.of(shift));
    when(attendanceRecordMapper.findByShiftIds(List.of(10L))).thenReturn(List.of(record));
    when(jobRateMapper.findByJobId(200L)).thenReturn(List.of());

    PayrollBatchVO response = payrollService.calculateBatch(1L);

    assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("200.00"));

    verify(payrollItemMapper).insertBatch(itemsCaptor.capture());
    List<PayrollItem> items = itemsCaptor.getValue();
    assertThat(items).hasSize(1);
    assertThat(items.get(0).getTotalPay()).isEqualByComparingTo(new BigDecimal("200.00"));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -q -Dtest=PayrollServiceTest test`

Expected: FAIL because payroll still recomputes from job rate only.

- [ ] **Step 3: Write minimal implementation**

Update `calculateBatch` so each grouped worker/job bucket sums `payAmount` from attendance records when available, and only falls back to the current rate-based calculation when a record has no stored pay result.

```java
BigDecimal totalPay = attendanceRecords.stream()
    .map(AttendanceRecord::getPayAmount)
    .filter(Objects::nonNull)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```

Keep the current hourly/daily fallback path for older records.

- [ ] **Step 4: Run test to verify it passes**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -q -Dtest=PayrollServiceTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/service/impl/PayrollServiceImpl.java enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/PayrollItem.java enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/PayrollItemVO.java enterprise-service/src/test/java/com/parttime/enterprise/service/PayrollServiceTest.java
git commit -m "feat: calculate payroll from attendance results"
```

## Spec Coverage Check
- Accept application generates shifts: Task 2.
- Shifts are per schedule and frozen snapshots: Task 1 + Task 2.
- Punch-in and punch-out with stored timestamps: Task 3.
- Automatic hours calculation after punch-out: Task 3.
- Automatic wage calculation: Task 3 and Task 4.
- Existing payroll batch continues to work with persisted results: Task 4.
