# 排班考勤模块设计文档

> 基于 `openspec/specs/schedule-attendance/spec.md` 的详细技术设计

## 数据库表

### schedule_template — 排班模板
企业级可复用模板，定义时段、位置、人数上限。

```sql
CREATE TABLE schedule_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_company_id (company_id)
);

CREATE TABLE schedule_template_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    day_of_week TINYINT NOT NULL COMMENT '1=MON, 7=SUN',
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    max_workers INT DEFAULT NULL,
    location_lat DECIMAL(10,7) DEFAULT NULL,
    location_lng DECIMAL(10,7) DEFAULT NULL,
    location_radius INT DEFAULT NULL COMMENT '单位:米',
    location_name VARCHAR(255) DEFAULT NULL,
    FOREIGN KEY (template_id) REFERENCES schedule_templates(id),
    INDEX idx_template_id (template_id)
);
```

### schedule_shift — 排班实例
关联岗位+模板时段+兼职+具体日期，表示某个兼职在某天某时段的值班安排。

```sql
CREATE TABLE schedule_shifts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    template_slot_id BIGINT DEFAULT NULL,
    worker_id BIGINT NOT NULL,
    shift_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location_lat DECIMAL(10,7) DEFAULT NULL,
    location_lng DECIMAL(10,7) DEFAULT NULL,
    location_radius INT DEFAULT NULL,
    location_name VARCHAR(255) DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT 'SCHEDULED/CHECKED_IN/CHECKED_OUT/ABSENT',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_job_id (job_id),
    INDEX idx_worker_id (worker_id),
    INDEX idx_shift_date (shift_date)
);
```

### attendance_record — 考勤记录
每次排班的签到/签退记录，含位置信息和自动计算的工时。

```sql
CREATE TABLE attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_id BIGINT NOT NULL,
    check_in_time DATETIME DEFAULT NULL,
    check_in_lat DECIMAL(10,7) DEFAULT NULL,
    check_in_lng DECIMAL(10,7) DEFAULT NULL,
    check_out_time DATETIME DEFAULT NULL,
    check_out_lat DECIMAL(10,7) DEFAULT NULL,
    check_out_lng DECIMAL(10,7) DEFAULT NULL,
    total_hours DECIMAL(5,2) DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/CHECKED_IN/CHECKED_OUT',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shift_id) REFERENCES schedule_shifts(id),
    INDEX idx_shift_id (shift_id)
);
```

## API 设计

### 企业端 (enterprise-service)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/schedule-templates | 创建排班模板 |
| GET | /api/schedule-templates | 列表（按companyId筛选） |
| GET | /api/schedule-templates/{id} | 详情（含时段） |
| PUT | /api/schedule-templates/{id} | 更新模板 |
| DELETE | /api/schedule-templates/{id} | 删除模板 |
| POST | /api/schedule-shifts | 指派兼职到排班 |
| GET | /api/schedule-shifts | 查询排班（jobId/workerId/date筛选） |
| DELETE | /api/schedule-shifts/{id} | 取消指派 |
| GET | /api/attendance/report | 考勤报表（jobId/scheduleShiftId/date筛选） |

### C端 (c-service)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/schedule-shifts/my | 查看我的排班（按日期筛选） |
| POST | /api/attendance/check-in | 签到（含位置校验） |
| POST | /api/attendance/check-out | 签退 |
| GET | /api/attendance/my | 我的打卡记录 |

## 位置校验逻辑

签到/签退时，如果排班有位置信息，校验：
1. 用 Haversine 公式计算打卡位置与排班位置的距离
2. 距离超出 `location_radius` 则拒绝
3. 无位置信息的排班跳过位置校验

## 工时计算

签退时自动计算 `total_hours = (check_out_time - check_in_time)`，精确到小数点后2位，单位小时。
