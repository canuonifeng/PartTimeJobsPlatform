package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@Sql(statements = {
        "DROP TABLE IF EXISTS attendance_records",
        "DROP TABLE IF EXISTS schedule_shifts",
        "DROP TABLE IF EXISTS schedule_template_slots",
        "CREATE TABLE schedule_template_slots (id BIGINT PRIMARY KEY AUTO_INCREMENT)",
        "CREATE TABLE schedule_shifts (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "job_id BIGINT NOT NULL, " +
                "template_slot_id BIGINT, " +
                "application_id BIGINT, " +
                "worker_id BIGINT NOT NULL, " +
                "shift_date DATE NOT NULL, " +
                "start_time TIME NOT NULL, " +
                "end_time TIME NOT NULL, " +
                "salary_type VARCHAR(20), " +
                "salary_amount DECIMAL(10,2), " +
                "salary_currency VARCHAR(10), " +
                "location_lat DECIMAL(10,7), " +
                "location_lng DECIMAL(10,7), " +
                "location_radius INT, " +
                "location_name VARCHAR(255), " +
                "status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED', " +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (template_slot_id) REFERENCES schedule_template_slots(id))" ,
        "CREATE TABLE attendance_records (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "shift_id BIGINT NOT NULL, " +
                "check_in_time DATETIME, " +
                "check_in_lat DECIMAL(10,7), " +
                "check_in_lng DECIMAL(10,7), " +
                "check_out_time DATETIME, " +
                "check_out_lat DECIMAL(10,7), " +
                "check_out_lng DECIMAL(10,7), " +
                "total_hours DECIMAL(5,2), " +
                "pay_amount DECIMAL(10,2), " +
                "calculated_at DATETIME, " +
                "status VARCHAR(20) NOT NULL DEFAULT 'PENDING', " +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (shift_id) REFERENCES schedule_shifts(id))"
})
class SnapshotMapperTest {

    @Autowired
    private ScheduleShiftMapper shiftMapper;

    @Autowired
    private AttendanceRecordMapper attendanceRecordMapper;

    @Test
    void scheduleShiftMapper_shouldPersistSnapshotFields() {
        ScheduleShift shift = new ScheduleShift();
        shift.setJobId(100L);
        shift.setApplicationId(200L);
        shift.setWorkerId(300L);
        shift.setShiftDate(LocalDate.of(2026, 6, 1));
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndTime(LocalTime.of(18, 0));
        shift.setSalaryType("HOURLY");
        shift.setSalaryAmount(new BigDecimal("25.00"));
        shift.setSalaryCurrency("CNY");
        shift.setStatus("SCHEDULED");

        shiftMapper.insert(shift);

        ScheduleShift loaded = shiftMapper.findById(shift.getId()).orElseThrow();
        assertThat(loaded.getApplicationId()).isEqualTo(200L);
        assertThat(loaded.getSalaryType()).isEqualTo("HOURLY");
        assertThat(loaded.getSalaryAmount()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(loaded.getSalaryCurrency()).isEqualTo("CNY");
    }

    @Test
    void attendanceRecordMapper_shouldPersistPaySnapshotFields() {
        ScheduleShift shift = new ScheduleShift();
        shift.setJobId(100L);
        shift.setWorkerId(200L);
        shift.setShiftDate(LocalDate.of(2026, 6, 1));
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndTime(LocalTime.of(18, 0));
        shift.setStatus("SCHEDULED");
        shiftMapper.insert(shift);

        AttendanceRecord record = new AttendanceRecord();
        record.setShiftId(shift.getId());
        record.setCheckInTime(LocalDateTime.of(2026, 6, 1, 9, 0));
        record.setCheckOutTime(LocalDateTime.of(2026, 6, 1, 18, 0));
        record.setTotalHours(new BigDecimal("9.00"));
        record.setScheduledPay(new BigDecimal("225.00"));
        record.setCalculatedAt(LocalDateTime.of(2026, 6, 1, 18, 10));
        record.setStatus("CHECKED_OUT");

        attendanceRecordMapper.insert(record);

        AttendanceRecord loaded = attendanceRecordMapper.findByShiftId(shift.getId()).orElseThrow();
        assertThat(loaded.getScheduledPay()).isEqualByComparingTo(new BigDecimal("225.00"));
        assertThat(loaded.getCalculatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 1, 18, 10));
    }
}
