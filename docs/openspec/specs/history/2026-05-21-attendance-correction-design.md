# Attendance Correction (补卡) Design

## Overview

Allow workers to apply for attendance correction for missed/late check-in, and enterprises to approve/reject via the schedule management page.

## Eligibility

A shift is eligible for correction when:
1. Worker is assigned to this shift
2. Current time > shift end time
3. No existing approved or pending correction request for this shift
4. One of the following:
   - No attendance record (missed check-in)
   - Has attendance record but check-in is late (checkInTime > shift startTime)
   - Has check-in but no check-out (missed check-out)

## Database

### `attendance_corrections` table (Flyway V14)

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT AUTO_INCREMENT PK | |
| shift_id | BIGINT NOT NULL | FK to schedule_shifts |
| worker_id | BIGINT NOT NULL | Applicant |
| reason | VARCHAR(500) NOT NULL | Correction reason |
| status | VARCHAR(20) NOT NULL DEFAULT 'PENDING' | PENDING / APPROVED / REJECTED |
| reject_reason | VARCHAR(500) NULL | Rejection reason |
| created_at | DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP | |
| processed_at | DATETIME NULL | Processing time |
| processor_id | BIGINT NULL | Approver ID |

Indexes: `idx_shift_id`, `idx_worker_id`, `idx_status`

## Architecture

Both services share the same MySQL database.

- **enterprise-service**: Flyway V14 creates the table. Both services get entity + mapper
- **c-service**: Worker submits correction requests
- **enterprise-service**: Lists and approves/rejects correction requests

## APIs

### C-side (c-service)

**POST /api/attendance/correction**
- Auth: Worker JWT
- Body: `{ shiftId: Long, reason: String }`
- Logic: Validate shift belongs to worker, shift has ended, no duplicate pending request → insert `attendance_corrections` with status=PENDING
- Response: created correction ID

**GET /api/attendance/correction/status?shiftId=xxx**
- Auth: Worker JWT
- Response: `{ eligible: boolean, existingRequest: { id, status } | null }`

### Enterprise-side (enterprise-service)

**GET /api/schedules/corrections**
- Auth: Enterprise JWT
- Query params: `status`, `dateFrom`, `dateTo`, `keyword` (worker name), `page`, `pageSize`
- Response: `{ records: [...], total }`

**PUT /api/schedules/corrections/{id}/approve**
- Auth: Enterprise JWT
- Logic:
  1. Find `schedule_shifts` record by correction.shiftId
  2. Find existing `attendance_records` by shift_id
  3. If exists: update `checkInMethod='CORRECTION'`
  4. If not exists: insert with `checkInTime = shift.startTime`, `checkInMethod='CORRECTION'`
  5. Update correction: status=APPROVED, processed_at=now, processor_id=currentUser
- Response: success

**PUT /api/schedules/corrections/{id}/reject**
- Auth: Enterprise JWT
- Body: `{ rejectReason: String }`
- Logic: Update correction: status=REJECTED, reject_reason, processed_at=now, processor_id=currentUser
- Response: success

## Frontend

### Worker-uniapp

In clockIn.vue or schedule history:
- For shifts that have ended and are eligible: show "补卡" button
- Click → modal with reason textarea (required) → submit to POST /api/attendance/correction
- After submit: show "补卡申请已提交，等待审核"

### Enterprise-pc

In ScheduleShiftList.vue:
- Add a tab bar: "排班列表" | "补卡申请"
- Correction tab shows table:
  - Columns: worker name, job title, shift date, time, reason, status, action
  - Filters: status dropdown, date range
  - Pending rows: "通过" / "拒绝" buttons
  - Approved rows: green tag "已通过"
  - Rejected rows: red tag "已拒绝" + tooltip with reject reason
- "拒绝" opens a dialog: "请输入拒绝原因" → textarea → confirm

## Attendance Record Status

When a correction is approved:
- No existing `attendance_records` → insert with `check_in_time = shift.start_time`, `status = 'CHECKED_IN'`, `remark = '补卡'`
- Existing record → update `remark = '补卡'` (保留原有打卡时间)

Note: `checkInMethod` column does not exist yet. For simplicity, use `remark` field to mark correction records.

## Testing

- enterprise-service: CorrectionServiceTest with 6+ test cases (submit, approve with/without existing record, reject, duplicate submission, invalid shift)
- c-service: AttendanceCorrectionController test coverage for submission and status check
- Manual verification: worker submits correction → enterprise approves → attendance record appears
