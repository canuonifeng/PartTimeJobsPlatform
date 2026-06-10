# Job Schedules Unification

## Overview

Unify schedule storage across B-side and C-side: replace `jobs.schedule_info` JSON column with `job_schedules` relational table for C-side reading; change B-side PC schedule picker from day-of-week to concrete dates.

## Changes

### 1. Database (enterprise-service Flyway)

- **V13 migration**: `ALTER TABLE jobs DROP COLUMN schedule_info;`
- **Demo data**: Seed `job_schedules` table for all 10 jobs with time slots matching their job types

### 2. c-service (C-side backend)

- **New `JobScheduleMapper`**: Query `job_schedules` table by `job_id`
- **`JobServiceImpl.toDetail`**: Replace `JsonConverter.parse(job.getScheduleInfo())` with `jobScheduleMapper.findByJobId(job.getId())` → convert to `List<JobScheduleInfoVO>`
- **`Job.java`**: Remove `scheduleInfo` field (no longer used)
- **`JobMapper.xml`**: No change needed; `j.*` no longer includes `schedule_info` after column drop

### 3. enterprise-pc (B-side PC)

- **`JobForm.vue`**: Replace day-of-week `<el-select>` with concrete date `<el-date-picker>`; remove `getScheduleDate()`/`dateToDayOfWeek()` helpers

### 4. Unchanged

- enterprise-service backend: `JobScheduleCmd`/`JobScheduleVO`/`JobScheduleMapper` stay as-is
- enterprise-uniapp: already uses date picker
- worker-uniapp: `jobDetail.vue` template unchanged (still displays `job.schedules`)

## Files Modified

| File | Change |
|------|--------|
| `enterprise-service/.../V13__drop_schedule_info.sql` | DROP COLUMN schedule_info |
| `c-service/.../JobScheduleMapper.java` | New interface |
| `c-service/.../JobScheduleMapper.xml` | New mapper SQL |
| `c-service/.../JobServiceImpl.java` | Use jobScheduleMapper instead of scheduleInfo |
| `c-service/.../Job.java` | Remove scheduleInfo field |
| `enterprise-pc/.../JobForm.vue` | Date picker instead of dayOfWeek |
