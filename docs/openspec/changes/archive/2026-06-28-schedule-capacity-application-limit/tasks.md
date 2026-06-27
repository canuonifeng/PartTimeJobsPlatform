## 1. Backend Capacity Rules

- [x] 1.1 Add C-service mapper methods to count `ACCEPTED` applications by `scheduleId` and by selected schedule IDs.
- [x] 1.2 Update C-service `applyForJob` to validate each selected班次 against `job_schedules.slots_available` before inserting applications.
- [x] 1.3 Ensure C-service auto-approve path uses the same per-schedule capacity validation before creating shifts.
- [x] 1.4 Add Enterprise-service mapper methods to count `ACCEPTED` applications by `scheduleId`.
- [x] 1.5 Update Enterprise-service `acceptApplication` to validate the target schedule capacity before setting `ACCEPTED`.

## 2. Frontend And API Consistency

- [x] 2.1 Verify C端岗位详情 and 报名确认 pages consume per-schedule `remainingSlots` consistently.
- [x] 2.2 Remove or bypass backend business decisions based on `jobs.accepted_count` while keeping response compatibility.
- [x] 2.3 Remove `slotsAvailable` from enterprise PC and enterprise mini-program job form payloads; backend remains responsible for deriving schedule capacity from `headcount`.
- [x] 2.4 Add `contactName` to enterprise PC and enterprise mini-program job forms and payloads.
- [x] 2.5 Expose schedule/shift contact snapshots to C端岗位详情、报名确认、我的排班 and 打卡相关页面.

## 3. Contact Snapshot Persistence

- [x] 3.1 Add database migration for `jobs.contact_name`, `job_schedules.contact_name/contact_phone`, and `schedule_shifts.contact_name/contact_phone`.
- [x] 3.2 Update Enterprise-service Job entity/VO/CMD/mapper to read and write `contact_name`.
- [x] 3.3 Update Enterprise-service schedule creation/update paths to copy job contact info into `job_schedules`.
- [x] 3.4 Update shift creation paths in C-service and Enterprise-service to copy schedule contact snapshots into `schedule_shifts`.

## 4. Enterprise Shift Management Backend

- [x] 4.1 Add migration fields for班次名称 and any remark/status metadata needed by班次管理.
- [x] 4.2 Add Enterprise-service VO/CMD classes for班次列表、班次报名人明细、班次修改、复制班次、批量创建班次 and export requests.
- [x] 4.3 Add Enterprise-service mapper queries to aggregate班次报名统计、剩余名额 and 考勤概览 by `job_schedule.id`.
- [x] 4.4 Add Enterprise-service API endpoints for班次列表、报名人列表、修改班次、复制班次、批量创建班次 and导出.
- [x] 4.5 Enforce edit rules: changes only affect future applications, capacity cannot be lower than accepted count, started/ended shifts cannot change date/time/capacity.

## 5. Enterprise Shift Management Frontend

- [x] 5.1 Add Enterprise PC班次管理 page with board/list filters, statistics columns, status badges, and operation buttons.
- [x] 5.2 Add Enterprise PC报名人 drawer/table with status capsules and attendance/correction/settlement columns.
- [x] 5.3 Add Enterprise PC dialogs for editing, copying, batch creating, and exporting shifts.
- [x] 5.4 Add Enterprise mini-program班次管理 page with the same core list, filters,报名人 drawer, edit/copy/batch-create actions adapted for mobile.
- [x] 5.5 Add navigation entries for班次管理 in enterprise PC and enterprise mini-program.

## 6. Tests And Validation

- [x] 6.1 Add C-service tests for full schedule rejection, available schedule acceptance, auto-approve capacity enforcement, and shift contact snapshot creation.
- [x] 6.2 Add Enterprise-service tests for accepting within schedule capacity, rejecting when that schedule is full, contact snapshot sync, shift aggregation, edit constraints, copy, batch create, and export.
- [x] 6.3 Run targeted Maven tests for C-service and Enterprise-service.
- [x] 6.4 Run Enterprise PC build and relevant UniApp builds.
