# Worker Homepage APIs Design

## Goal

Replace the worker miniapp homepage mock stats and legacy top-shift API usage with two dedicated homepage APIs:

- `GET /api/home/stats`
- `GET /api/home/schedules`

Remove the obsolete `/api/schedule-shifts/my-top` frontend call and backend endpoint/service code.

## Scope

In scope:

- Add homepage stats endpoint for the authenticated worker.
- Add homepage schedules endpoint for the authenticated worker.
- Wire `worker-uniapp/src/pages/index/index.vue` to the new APIs.
- Remove fallback demo shifts from the logged-in homepage path.
- Remove `getMyTopShifts` from frontend API code.
- Remove `/api/schedule-shifts/my-top`, `getMyTopShifts`, and `MyTopShiftsVO` backend usage/code if no longer referenced.

Out of scope:

- Changing the full schedule page API `/api/schedule-shifts/my`.
- Changing check-in/check-out behavior.
- Changing settlement generation rules.

## API Design

### `GET /api/home/stats`

Returns monthly homepage statistics for the current authenticated worker.

Response fields:

```json
{
  "monthHours": 156.50,
  "monthIncome": 4680.00,
  "attendanceDays": 28
}
```

Data sources:

- `monthHours`: sum of `attendance_records.total_hours` for records whose `worker_id` is the current worker and `check_in_time` falls within the current calendar month.
- `attendanceDays`: count of distinct dates from `attendance_records.check_in_time` in the current calendar month for the current worker.
- `monthIncome`: sum of positive earning transactions from `balance_transactions` where `worker_id` is the current worker, `type = 'EARNINGS'`, and `created_at` falls within the current calendar month.

Null aggregate results return zero values.

### `GET /api/home/schedules`

Returns homepage schedule sections for the current authenticated worker.

Response fields:

```json
{
  "todayShifts": [],
  "futureShifts": []
}
```

Rules:

- `todayShifts`: all non-cancelled shifts for the current worker where `shift_date = current date`, ordered by `start_time` ascending.
- `futureShifts`: upcoming non-cancelled shifts where `shift_date > current date`, ordered by `shift_date` and `start_time` ascending, limited to the homepage display size.
- Reuse `WorkerShiftVO` shape so the existing homepage UI and schedule page conventions stay consistent.

## Backend Design

Add a small homepage API boundary under `c-service`:

- `HomeController`
- `HomeService`
- `HomeServiceImpl`
- `HomeStatsVO`
- `HomeSchedulesVO`

Mapper additions:

- `AttendanceRecordMapper.sumMonthlyHours(workerId, monthStart, nextMonthStart)`
- `AttendanceRecordMapper.countMonthlyAttendanceDays(workerId, monthStart, nextMonthStart)`
- `BalanceTransactionMapper.sumMonthlyEarnings(workerId, monthStart, nextMonthStart)`
- `ShiftMapper.findTodayByWorkerId(workerId, date)`
- `ShiftMapper.findFutureByWorkerId(workerId, date, size)`

Authentication follows the existing controller pattern using `SecurityContextHolder`, returning `401` when no authenticated worker is present.

## Frontend Design

Add `worker-uniapp/src/api/home.js`:

- `getHomeStats()` -> `/api/home/stats`
- `getHomeSchedules()` -> `/api/home/schedules`

Update `worker-uniapp/src/pages/index/index.vue`:

- Replace hardcoded stats with reactive values from `getHomeStats`.
- Replace `getMyTopShifts` usage with `getHomeSchedules`.
- Normalize `todayShifts` and `futureShifts` from separate response arrays instead of deriving them from one mixed list.
- When logged out, keep the current login card behavior.
- When logged in and APIs return empty lists, show empty schedule sections instead of demo fallback shifts.

Remove from `worker-uniapp/src/api/schedule.js`:

- `getMyTopShifts()`

## Cleanup Design

Remove backend code for the obsolete top-shift endpoint:

- Delete `AttendanceController.getMyTopShifts()`.
- Delete `AttendanceService.getMyTopShifts(...)`.
- Delete `AttendanceServiceImpl.getMyTopShifts(...)`.
- Delete `MyTopShiftsVO` if no references remain.
- Delete unused `ShiftMapper.findLtStartTimeByWorkerId(...)` and `ShiftMapper.getGtEndTimeByWorkerId(...)` if only used by the removed service method.

Keep `/api/schedule-shifts/my` unchanged for the full schedule page.

## Error Handling

- Unauthorized users receive HTTP 401, consistent with existing controllers.
- Aggregates return zero instead of null.
- Frontend failures show empty/zero homepage data and avoid showing stale demo shift data for logged-in users.

## Testing

Backend verification:

- Compile/test `c-service` with the repo’s Maven command.
- Confirm removed `/api/schedule-shifts/my-top` references no longer exist.

Frontend verification:

- Run `npm run build:mp-weixin` in `worker-uniapp`.
- Confirm no import references to `getMyTopShifts` remain.

## Self Review

- No placeholder requirements remain.
- The stats and schedule endpoints are split as required.
- The income source is explicitly `balance_transactions` with `type = 'EARNINGS'`.
- The obsolete my-top API removal is included across frontend and backend.
