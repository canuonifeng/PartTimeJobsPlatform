# 企业端小程序和 PC 操作规则优化设计

## 范围

同时修改企业端小程序、企业 PC 和企业端后端服务。

纳入规则：

1. 排班考勤中 `ABSENT` 显示为“缺勤”。
2. 只有 `shiftDate + startTime` 晚于当前时间，且排班未取消时，才能取消排班。
3. 薪资管理中已结算记录不能再次结算，也不能删除。
4. 职位管理中职位不能删除，只能关闭。
5. 账号管理中账号不能删除，只能禁用。

## 前端设计

### 企业小程序

- `schedules/scheduleList.vue`：补充 `ABSENT` 中文映射；新增未来排班判断；非未来排班不显示取消入口或显示不可取消状态。
- `attendance/attendanceList.vue`：已结算记录不可选中；批量结算和批量删除只作用于未结算记录；已结算记录显示“已结算”。
- `jobs/jobList.vue`：移除删除职位入口，关闭状态也不显示删除。
- `accounts/accountList.vue`：删除入口改为禁用入口，启用账号显示“禁用”，禁用账号显示“已禁用”。

### 企业 PC

- `schedules/ScheduleShiftList.vue`：补充 `ABSENT` 中文显示；取消按钮只对未来未取消排班展示。
- `attendance/AttendanceHoursList.vue`：已结算记录不可结算和删除；批量操作过滤已结算。
- `jobs/JobList.vue`：移除删除按钮和删除调用，只保留关闭。
- `accounts/AccountList.vue`：删除按钮改为禁用按钮。

## 后端设计

### 排班取消

`ScheduleServiceImpl.cancelShift` 在取消前读取排班，组合 `shiftDate + startTime` 与当前时间比较。若排班已取消、已开始或已过期，则抛出业务异常；否则更新为 `CANCELLED`。

### 薪资结算与删除

`AttendanceHoursServiceImpl` 在结算和删除前批量读取考勤记录，若任一记录 `settlementStatus = PAID`，拒绝操作。

### 职位删除

`JobServiceImpl.deleteJob` 或对应删除方法直接拒绝删除，提示“职位不能删除，请关闭职位”。保留关闭接口。

### 账号删除/禁用

`AccountServiceImpl.deleteAccount` 拒绝删除账号，提示“账号不能删除，请禁用账号”。禁用通过更新账号状态为 `DISABLED` 完成；如现有更新接口支持 status，则复用。

## 验证

- `cd enterprise-uniapp && npm run build:mp-weixin`
- `cd enterprise-pc && npm run build`
- `cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test`
- `git diff --check`
