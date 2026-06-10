# Worker Homepage APIs Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add dedicated worker homepage stats and schedules APIs, connect the miniapp homepage to them, and remove obsolete `/api/schedule-shifts/my-top` code.

**Architecture:** Add `HomeController` + `HomeService` as a focused backend boundary over attendance, balance transaction, and shift mappers. Add `worker-uniapp/src/api/home.js` and update the homepage to use split stats/schedule responses instead of mock values and legacy top-shift data.

**Tech Stack:** Spring Boot, MyBatis XML, Lombok, UniApp 3, Vue 3 script setup.

---

## Files

- Create `c-service/src/main/java/com/parttime/cservice/controller/HomeController.java`
- Create `c-service/src/main/java/com/parttime/cservice/service/HomeService.java`
- Create `c-service/src/main/java/com/parttime/cservice/service/impl/HomeServiceImpl.java`
- Create `c-service/src/main/java/com/parttime/cservice/pojo/vo/HomeStatsVO.java`
- Create `c-service/src/main/java/com/parttime/cservice/pojo/vo/HomeSchedulesVO.java`
- Modify `c-service/src/main/java/com/parttime/cservice/mapper/AttendanceRecordMapper.java`
- Modify `c-service/src/main/resources/mapper/AttendanceRecordMapper.xml`
- Modify `c-service/src/main/java/com/parttime/cservice/mapper/BalanceTransactionMapper.java`
- Modify `c-service/src/main/resources/mapper/BalanceTransactionMapper.xml`
- Modify `c-service/src/main/java/com/parttime/cservice/mapper/ShiftMapper.java`
- Modify `c-service/src/main/resources/mapper/ShiftMapper.xml`
- Modify `c-service/src/main/java/com/parttime/cservice/controller/AttendanceController.java`
- Modify `c-service/src/main/java/com/parttime/cservice/service/AttendanceService.java`
- Modify `c-service/src/main/java/com/parttime/cservice/service/impl/AttendanceServiceImpl.java`
- Delete `c-service/src/main/java/com/parttime/cservice/pojo/vo/MyTopShiftsVO.java`
- Create `worker-uniapp/src/api/home.js`
- Modify `worker-uniapp/src/api/schedule.js`
- Modify `worker-uniapp/src/pages/index/index.vue`

---

### Task 1: Backend VO Classes

- [ ] Create `HomeStatsVO.java` with fields `BigDecimal monthHours`, `BigDecimal monthIncome`, `Integer attendanceDays` using Lombok `@Data`.
- [ ] Create `HomeSchedulesVO.java` with fields `List<WorkerShiftVO> todayShifts`, `List<WorkerShiftVO> futureShifts` using Lombok `@Data`.
- [ ] Compile backend: `mvn -pl c-service -am compile`. Expected: success or unrelated pre-existing compile issue.

### Task 2: Backend Mapper Additions

- [ ] Add to `AttendanceRecordMapper.java`:
  - `BigDecimal sumMonthlyHours(Long workerId, LocalDateTime startTime, LocalDateTime endTime)` with `@Param`.
  - `Integer countMonthlyAttendanceDays(Long workerId, LocalDateTime startTime, LocalDateTime endTime)` with `@Param`.
- [ ] Add to `AttendanceRecordMapper.xml`:
  - `sumMonthlyHours`: `SELECT COALESCE(SUM(total_hours), 0) FROM attendance_records WHERE worker_id = #{workerId} AND check_in_time >= #{startTime} AND check_in_time < #{endTime}`.
  - `countMonthlyAttendanceDays`: `SELECT COUNT(DISTINCT DATE(check_in_time)) FROM attendance_records WHERE worker_id = #{workerId} AND check_in_time >= #{startTime} AND check_in_time < #{endTime}`.
- [ ] Add to `BalanceTransactionMapper.java`: `BigDecimal sumMonthlyEarnings(Long workerId, LocalDateTime startTime, LocalDateTime endTime)` with `@Param`.
- [ ] Add to `BalanceTransactionMapper.xml`: sum positive `amount` for `worker_id`, `type = 'EARNINGS'`, and monthly `created_at` range.
- [ ] Add to `ShiftMapper.java`:
  - `List<ShiftEntity> findTodayByWorkerId(Long workerId, LocalDate date)`.
  - `List<ShiftEntity> findFutureByWorkerId(Long workerId, LocalDate date, int size)`.
- [ ] Add to `ShiftMapper.xml`:
  - today query: current worker, `shift_date = #{date}`, `status != 'CANCELLED'`, order by `start_time`.
  - future query: current worker, `shift_date > #{date}`, `status != 'CANCELLED'`, order by `shift_date,start_time`, `LIMIT #{size}`.
- [ ] Compile backend: `mvn -pl c-service -am compile`.

### Task 3: Backend Homepage Service

- [ ] Create `HomeService.java` with `HomeStatsVO getStats(Long workerId)` and `HomeSchedulesVO getSchedules(Long workerId)`.
- [ ] Create `HomeServiceImpl.java`:
  - Inject `AttendanceRecordMapper`, `BalanceTransactionMapper`, `ShiftMapper`, `AttendanceCorrectionMapper`.
  - Compute `monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay()` and `nextMonthStart = ...plusMonths(1)`.
  - Return zero for null aggregates.
  - Use `ShiftMapper.findTodayByWorkerId(workerId, LocalDate.now())` and `findFutureByWorkerId(workerId, LocalDate.now(), 5)`.
  - Convert `ShiftEntity` to `WorkerShiftVO` with the same mapping currently used in `AttendanceServiceImpl.toWorkerShiftResponse`.
- [ ] Compile backend: `mvn -pl c-service -am compile`.

### Task 4: Backend Homepage Controller

- [ ] Create `HomeController.java` with `@RestController` and `@RequestMapping("/api/home")`.
- [ ] Add the same `getCurrentWorkerId()` pattern used by `AttendanceController`.
- [ ] Add `GET /stats`: return 401 if unauthenticated, else `homeService.getStats(workerId)`.
- [ ] Add `GET /schedules`: return 401 if unauthenticated, else `homeService.getSchedules(workerId)`.
- [ ] Compile backend: `mvn -pl c-service -am compile`.

### Task 5: Remove Obsolete Backend Top-Shift API

- [ ] In `AttendanceController.java`, remove `MyTopShiftsVO` import and the `/schedule-shifts/my-top` method.
- [ ] In `AttendanceService.java`, remove `MyTopShiftsVO` import and `getMyTopShifts` declaration.
- [ ] In `AttendanceServiceImpl.java`, remove `MyTopShiftsVO` import and `getMyTopShifts` method.
- [ ] In `ShiftMapper.java`, remove `findLtStartTimeByWorkerId` and `getGtEndTimeByWorkerId`.
- [ ] In `ShiftMapper.xml`, remove the matching SQL blocks.
- [ ] Delete `MyTopShiftsVO.java`.
- [ ] Search: `getMyTopShifts|MyTopShiftsVO|findLtStartTimeByWorkerId|getGtEndTimeByWorkerId`. Expected: no matches.
- [ ] Compile backend: `mvn -pl c-service -am compile`.

### Task 6: Frontend API Module

- [ ] Create `worker-uniapp/src/api/home.js` exporting `getHomeStats()` -> `GET /api/home/stats` and `getHomeSchedules()` -> `GET /api/home/schedules`.
- [ ] Remove `getMyTopShifts()` from `worker-uniapp/src/api/schedule.js`.
- [ ] Search: `getMyTopShifts`. Expected: no matches after homepage update.

### Task 7: Frontend Homepage Wiring

- [ ] In `worker-uniapp/src/pages/index/index.vue`, replace `import { getMyTopShifts } from '@/api/schedule'` with `import { getHomeStats, getHomeSchedules } from '@/api/home'`.
- [ ] Add `stats = ref({ monthHours: 0, monthIncome: 0, attendanceDays: 0 })`.
- [ ] Replace hardcoded `156`, `4,680`, `28` with formatted `stats.monthHours`, `stats.monthIncome`, `stats.attendanceDays`.
- [ ] Remove demo fallback shift generation from logged-in loading path.
- [ ] Change `loadShifts()` to call `getHomeSchedules()` and set `shifts.value = [...todayShifts, ...futureShifts].map(normalizeShift)`.
- [ ] Add `loadStats()` that calls `getHomeStats()` and resets zeros on failure.
- [ ] Update `refreshHome()` to call both `loadStats()` and `loadShifts()` after `authStore.loadSession()`.
- [ ] Ensure empty logged-in schedules render without demo cards.

### Task 8: Verification

- [ ] Run backend compile: `mvn -pl c-service -am compile`. Expected: success.
- [ ] Run frontend build from `worker-uniapp`: `npm run build:mp-weixin`. Expected: `DONE  Build complete.`
- [ ] Search removed references: `getMyTopShifts|MyTopShiftsVO|findLtStartTimeByWorkerId|getGtEndTimeByWorkerId|/schedule-shifts/my-top`. Expected: no matches.
- [ ] Check git diff and ensure only intended files changed.

## Self Review

- Covers split stats and schedules APIs.
- Uses `balance_transactions` with `type = 'EARNINGS'` for month income.
- Removes obsolete top-shift frontend and backend code.
- Keeps `/api/schedule-shifts/my` unchanged.
