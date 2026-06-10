# Attendance Correction (补卡系统) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Allow workers to apply for attendance correction for expired/late shifts and enterprises to approve/reject.

**Architecture:** New `attendance_corrections` table (Flyway V14), shared between both services via entity+mapper. Worker submits via c-service API, enterprise reviews/approves via enterprise-service API. Approval inserts/updates `attendance_records` with remark='补卡'.

**Tech Stack:** Spring Boot, MyBatis, MySQL (Flyway), Vue 3 + Element Plus (enterprise-pc), uni-app (worker-uniapp)

---

### Task 1: Flyway V14 - Create attendance_corrections table

**Files:**
- Create: `enterprise-service/src/main/resources/db/migration/V14__create_attendance_corrections.sql`

- [ ] **Step 1: Create migration file**

```sql
CREATE TABLE IF NOT EXISTS attendance_corrections (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_id      BIGINT       NOT NULL,
    worker_id     BIGINT       NOT NULL,
    reason        VARCHAR(500) NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    reject_reason VARCHAR(500) DEFAULT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at  DATETIME     DEFAULT NULL,
    processor_id  BIGINT       DEFAULT NULL,
    INDEX idx_shift_id (shift_id),
    INDEX idx_worker_id (worker_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: Run Flyway to verify**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && cd /Users/nifeng/workspace/my/demo/enterprise-service && mvn flyway:migrate -Dflyway.baselineOnMigrate=false`
Expected: `Successfully applied 1 migration to schema` (if not already applied), or `Schema is up to date`

---

### Task 2: Enterprise-service entity + mapper for AttendanceCorrection

**Files:**
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/AttendanceCorrection.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/AttendanceCorrectionMapper.java`
- Create: `enterprise-service/src/main/resources/mapper/AttendanceCorrectionMapper.xml`

- [ ] **Step 1: Create entity**

```java
package com.parttime.enterprise.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttendanceCorrection {

    @Schema(description = "补卡申请ID")
    private Long id;
    @Schema(description = "班次ID")
    private Long shiftId;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "补卡原因")
    private String reason;
    @Schema(description = "状态: PENDING/APPROVED/REJECTED")
    private String status;
    @Schema(description = "拒绝原因")
    private String rejectReason;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "处理时间")
    private LocalDateTime processedAt;
    @Schema(description = "处理人ID")
    private Long processorId;
}
```

- [ ] **Step 2: Create mapper interface**

```java
package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.AttendanceCorrection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AttendanceCorrectionMapper {

    int insert(AttendanceCorrection correction);

    Optional<AttendanceCorrection> findById(Long id);

    List<AttendanceCorrection> findByShiftId(Long shiftId);

    List<AttendanceCorrection> findByWorkerId(Long workerId);

    List<AttendanceCorrection> findByStatus(String status);

    List<AttendanceCorrection> search(@Param("status") String status,
                                      @Param("keyword") String keyword,
                                      @Param("dateFrom") String dateFrom,
                                      @Param("dateTo") String dateTo,
                                      @Param("offset") Integer offset,
                                      @Param("limit") Integer limit);

    int countSearch(@Param("status") String status,
                    @Param("keyword") String keyword,
                    @Param("dateFrom") String dateFrom,
                    @Param("dateTo") String dateTo);

    int update(AttendanceCorrection correction);
}
```

- [ ] **Step 3: Create mapper XML**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.enterprise.mapper.AttendanceCorrectionMapper">

    <resultMap id="CorrectionResultMap" type="com.parttime.enterprise.pojo.entity.AttendanceCorrection">
        <id property="id" column="id"/>
        <result property="shiftId" column="shift_id"/>
        <result property="workerId" column="worker_id"/>
        <result property="reason" column="reason"/>
        <result property="status" column="status"/>
        <result property="rejectReason" column="reject_reason"/>
        <result property="createdAt" column="created_at"/>
        <result property="processedAt" column="processed_at"/>
        <result property="processorId" column="processor_id"/>
    </resultMap>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO attendance_corrections (shift_id, worker_id, reason, status, created_at)
        VALUES (#{shiftId}, #{workerId}, #{reason}, #{status}, #{createdAt})
    </insert>

    <select id="findById" resultMap="CorrectionResultMap">
        SELECT * FROM attendance_corrections WHERE id = #{id}
    </select>

    <select id="findByShiftId" resultMap="CorrectionResultMap">
        SELECT * FROM attendance_corrections WHERE shift_id = #{shiftId}
    </select>

    <select id="findByWorkerId" resultMap="CorrectionResultMap">
        SELECT * FROM attendance_corrections WHERE worker_id = #{workerId}
    </select>

    <select id="findByStatus" resultMap="CorrectionResultMap">
        SELECT * FROM attendance_corrections WHERE status = #{status}
    </select>

    <select id="search" resultMap="CorrectionResultMap">
        SELECT ac.* FROM attendance_corrections ac
        LEFT JOIN schedule_shifts s ON ac.shift_id = s.id
        LEFT JOIN jobs j ON s.job_id = j.id
        WHERE 1=1
        <if test="status != null and status != ''">AND ac.status = #{status}</if>
        <if test="keyword != null and keyword != ''">AND j.title LIKE CONCAT('%', #{keyword}, '%')</if>
        <if test="dateFrom != null and dateFrom != ''">AND ac.created_at >= #{dateFrom}</if>
        <if test="dateTo != null and dateTo != ''">AND ac.created_at &lt;= #{dateTo}</if>
        ORDER BY ac.created_at DESC
        LIMIT #{limit} OFFSET #{offset}
    </select>

    <select id="countSearch" resultType="int">
        SELECT COUNT(*) FROM attendance_corrections ac
        LEFT JOIN schedule_shifts s ON ac.shift_id = s.id
        LEFT JOIN jobs j ON s.job_id = j.id
        WHERE 1=1
        <if test="status != null and status != ''">AND ac.status = #{status}</if>
        <if test="keyword != null and keyword != ''">AND j.title LIKE CONCAT('%', #{keyword}, '%')</if>
        <if test="dateFrom != null and dateFrom != ''">AND ac.created_at >= #{dateFrom}</if>
        <if test="dateTo != null and dateTo != ''">AND ac.created_at &lt;= #{dateTo}</if>
    </select>

    <update id="update">
        UPDATE attendance_corrections
        SET status = #{status}, reject_reason = #{rejectReason},
            processed_at = #{processedAt}, processor_id = #{processorId}
        WHERE id = #{id}
    </update>

</mapper>
```

- [ ] **Step 4: Compile and verify**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && cd /Users/nifeng/workspace/my/demo/enterprise-service && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 3: Enterprise-service service + controller for corrections

**Files:**
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/CorrectionVO.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/dto/CorrectionRejectCmd.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/service/CorrectionService.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/CorrectionServiceImpl.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/controller/ScheduleController.java`

- [ ] **Step 1: Create CorrectionVO**

```java
package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
public class CorrectionVO {

    @Schema(description = "补卡申请ID")
    private Long id;
    @Schema(description = "班次ID")
    private Long shiftId;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "工人姓名")
    private String workerName;
    @Schema(description = "岗位ID")
    private Long jobId;
    @Schema(description = "岗位名称")
    private String jobTitle;
    @Schema(description = "排班日期")
    private LocalDate shiftDate;
    @Schema(description = "开始时间")
    private LocalTime startTime;
    @Schema(description = "结束时间")
    private LocalTime endTime;
    @Schema(description = "补卡原因")
    private String reason;
    @Schema(description = "状态: PENDING/APPROVED/REJECTED")
    private String status;
    @Schema(description = "拒绝原因")
    private String rejectReason;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "处理时间")
    private LocalDateTime processedAt;
}
```

- [ ] **Step 2: Create CorrectionRejectCmd**

```java
package com.parttime.enterprise.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CorrectionRejectCmd {

    @Schema(description = "拒绝原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String rejectReason;
}
```

- [ ] **Step 3: Create CorrectionService interface**

```java
package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.CorrectionVO;
import java.util.Map;

public interface CorrectionService {

    Map<String, Object> listCorrections(String status, String keyword,
                                         String dateFrom, String dateTo,
                                         Integer page, Integer pageSize);

    void approve(Long id, Long processorId);

    void reject(Long id, Long processorId, String rejectReason);
}
```

- [ ] **Step 4: Create CorrectionServiceImpl**

```java
package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.AttendanceCorrectionMapper;
import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.pojo.dto.CorrectionRejectCmd;
import com.parttime.enterprise.pojo.entity.AttendanceCorrection;
import com.parttime.enterprise.pojo.entity.AttendanceRecord;
import com.parttime.enterprise.pojo.entity.ScheduleShift;
import com.parttime.enterprise.pojo.entity.Job;
import com.parttime.enterprise.pojo.vo.CorrectionVO;
import com.parttime.enterprise.service.CorrectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CorrectionServiceImpl implements CorrectionService {

    @Resource
    private AttendanceCorrectionMapper correctionMapper;
    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;
    @Resource
    private ScheduleShiftMapper shiftMapper;
    @Resource
    private WorkerSyncMapper workerSyncMapper;
    @Resource
    private JobMapper jobMapper;

    @Override
    public Map<String, Object> listCorrections(String status, String keyword,
                                                String dateFrom, String dateTo,
                                                Integer page, Integer pageSize) {
        int offset = (page != null && page > 0) ? (page - 1) * pageSize : 0;
        int limit = pageSize != null ? pageSize : 20;

        List<AttendanceCorrection> list = correctionMapper.search(status, keyword, dateFrom, dateTo, offset, limit);
        int total = correctionMapper.countSearch(status, keyword, dateFrom, dateTo);

        List<Long> shiftIds = list.stream().map(AttendanceCorrection::getShiftId).collect(Collectors.toList());
        Map<Long, ScheduleShift> shiftMap = new HashMap<>();
        if (!shiftIds.isEmpty()) {
            List<ScheduleShift> shifts = shiftMapper.findByIds(shiftIds);
            if (shifts != null) shifts.forEach(s -> shiftMap.put(s.getId(), s));
        }

        List<CorrectionVO> voList = list.stream().map(c -> {
            CorrectionVO vo = new CorrectionVO();
            vo.setId(c.getId());
            vo.setShiftId(c.getShiftId());
            vo.setWorkerId(c.getWorkerId());
            vo.setReason(c.getReason());
            vo.setStatus(c.getStatus());
            vo.setRejectReason(c.getRejectReason());
            vo.setCreatedAt(c.getCreatedAt());
            vo.setProcessedAt(c.getProcessedAt());

            ScheduleShift shift = shiftMap.get(c.getShiftId());
            if (shift != null) {
                vo.setJobId(shift.getJobId());
                vo.setShiftDate(shift.getShiftDate());
                vo.setStartTime(shift.getStartTime());
                vo.setEndTime(shift.getEndTime());
                vo.setWorkerName(workerSyncMapper.findWorkerNameById(shift.getWorkerId()));
                vo.setJobTitle(jobMapper.findById(shift.getJobId()).map(Job::getTitle).orElse(null));
            }
            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", voList);
        result.put("total", total);
        return result;
    }

    @Override
    @Transactional
    public void approve(Long id, Long processorId) {
        AttendanceCorrection correction = correctionMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Correction not found: " + id));

        if (!"PENDING".equals(correction.getStatus())) {
            throw new RuntimeException("Correction is not in PENDING status");
        }

        ScheduleShift shift = shiftMapper.findById(correction.getShiftId())
                .orElseThrow(() -> new RuntimeException("ScheduleShift not found: " + correction.getShiftId()));

        Optional<AttendanceRecord> existing = attendanceRecordMapper.findByShiftId(correction.getShiftId());

        if (existing.isPresent()) {
            AttendanceRecord record = existing.get();
            record.setRemark("补卡");
            attendanceRecordMapper.update(record);
        } else {
            AttendanceRecord record = new AttendanceRecord();
            record.setShiftId(correction.getShiftId());
            record.setCheckInTime(LocalDateTime.of(shift.getShiftDate(), shift.getStartTime()));
            record.setStatus("CHECKED_IN");
            record.setRemark("补卡");
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            attendanceRecordMapper.insert(record);
        }

        correction.setStatus("APPROVED");
        correction.setProcessedAt(LocalDateTime.now());
        correction.setProcessorId(processorId);
        correctionMapper.update(correction);
    }

    @Override
    @Transactional
    public void reject(Long id, Long processorId, String rejectReason) {
        AttendanceCorrection correction = correctionMapper.findById(id)
                .orElseThrow(() -> new RuntimeException("Correction not found: " + id));

        if (!"PENDING".equals(correction.getStatus())) {
            throw new RuntimeException("Correction is not in PENDING status");
        }

        correction.setStatus("REJECTED");
        correction.setRejectReason(rejectReason);
        correction.setProcessedAt(LocalDateTime.now());
        correction.setProcessorId(processorId);
        correctionMapper.update(correction);
    }
}
```

- [ ] **Step 5: Add correction endpoints to ScheduleController**

Add these imports to the existing ScheduleController.java:
```java
import com.parttime.enterprise.pojo.dto.CorrectionRejectCmd;
import com.parttime.enterprise.pojo.vo.CorrectionVO;
import com.parttime.enterprise.service.CorrectionService;
```

Add `@Resource`:
```java
    @Resource
    private CorrectionService correctionService;
```

Add these methods:
```java
    @Operation(summary = "补卡申请列表", description = "查看补卡申请列表，支持分页和筛选")
    @GetMapping("/schedules/corrections")
    public Map<String, Object> listCorrections(
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "岗位关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始日期") @RequestParam(required = false) String dateFrom,
            @Parameter(description = "结束日期") @RequestParam(required = false) String dateTo,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        return correctionService.listCorrections(status, keyword, dateFrom, dateTo, page, pageSize);
    }

    @Operation(summary = "通过补卡申请", description = "通过补卡申请并生成/更新考勤记录")
    @PutMapping("/schedules/corrections/{id}/approve")
    public void approveCorrection(@Parameter(description = "补卡申请ID") @PathVariable Long id) {
        correctionService.approve(id, SecurityUtil.getCurrentUserId());
    }

    @Operation(summary = "拒绝补卡申请", description = "拒绝补卡申请")
    @PutMapping("/schedules/corrections/{id}/reject")
    public void rejectCorrection(@Parameter(description = "补卡申请ID") @PathVariable Long id,
                                  @RequestBody CorrectionRejectCmd cmd) {
        correctionService.reject(id, SecurityUtil.getCurrentUserId(), cmd.getRejectReason());
    }
```

Also need to check if `findByIds` exists in ScheduleShiftMapper. If not, add it.

- [ ] **Step 6: Add findByIds to ScheduleShiftMapper**

Check existing ScheduleShiftMapper interface - if no `findByIds`, add:
```java
List<ScheduleShift> findByIds(@Param("ids") List<Long> ids);
```

And in ScheduleShiftMapper.xml:
```xml
    <select id="findByIds" resultMap="ShiftResultMap">
        SELECT * FROM schedule_shifts WHERE id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
    </select>
```

- [ ] **Step 7: Compile and verify**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && cd /Users/nifeng/workspace/my/demo/enterprise-service && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 4: C-service entity + mapper + API for correction submission

**Files:**
- Create: `c-service/src/main/java/com/parttime/cservice/pojo/entity/AttendanceCorrectionEntity.java`
- Create: `c-service/src/main/java/com/parttime/cservice/mapper/AttendanceCorrectionMapper.java`
- Create: `c-service/src/main/resources/mapper/AttendanceCorrectionMapper.xml`
- Create: `c-service/src/main/java/com/parttime/cservice/controller/CorrectionController.java`

- [ ] **Step 1: Create entity**

```java
package com.parttime.cservice.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttendanceCorrectionEntity {

    @Schema(description = "补卡申请ID")
    private Long id;
    @Schema(description = "班次ID")
    private Long shiftId;
    @Schema(description = "工人ID")
    private Long workerId;
    @Schema(description = "补卡原因")
    private String reason;
    @Schema(description = "状态: PENDING/APPROVED/REJECTED")
    private String status;
    @Schema(description = "拒绝原因")
    private String rejectReason;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "处理时间")
    private LocalDateTime processedAt;
    @Schema(description = "处理人ID")
    private Long processorId;

    public AttendanceCorrectionEntity() {}
}
```

- [ ] **Step 2: Create mapper interface**

```java
package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.AttendanceCorrectionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Optional;

@Mapper
public interface AttendanceCorrectionMapper {

    int insert(AttendanceCorrectionEntity correction);

    Optional<AttendanceCorrectionEntity> findById(Long id);

    Optional<AttendanceCorrectionEntity> findByShiftId(Long shiftId);
}
```

- [ ] **Step 3: Create mapper XML**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.cservice.mapper.AttendanceCorrectionMapper">

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO attendance_corrections (shift_id, worker_id, reason, status, created_at)
        VALUES (#{shiftId}, #{workerId}, #{reason}, #{status}, #{createdAt})
    </insert>

    <select id="findById" resultType="com.parttime.cservice.pojo.entity.AttendanceCorrectionEntity">
        SELECT * FROM attendance_corrections WHERE id = #{id}
    </select>

    <select id="findByShiftId" resultType="com.parttime.cservice.pojo.entity.AttendanceCorrectionEntity">
        SELECT * FROM attendance_corrections WHERE shift_id = #{shiftId}
    </select>

</mapper>
```

- [ ] **Step 4: Create controller**

```java
package com.parttime.cservice.controller;

import com.parttime.cservice.mapper.AttendanceCorrectionMapper;
import com.parttime.cservice.mapper.AttendanceRecordMapper;
import com.parttime.cservice.mapper.ShiftMapper;
import com.parttime.cservice.pojo.entity.AttendanceCorrectionEntity;
import com.parttime.cservice.pojo.entity.AttendanceRecordEntity;
import com.parttime.cservice.pojo.entity.ShiftEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CorrectionController {

    @Resource
    private AttendanceCorrectionMapper correctionMapper;
    @Resource
    private ShiftMapper shiftMapper;
    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;

    private Long getCurrentWorkerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return Long.valueOf(auth.getName());
    }

    @Operation(summary = "提交补卡申请", description = "工人对已结束的排班提交补卡申请")
    @PostMapping("/attendance/correction")
    public ResponseEntity<?> submitCorrection(@RequestBody Map<String, Object> body) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long shiftId = body.get("shiftId") instanceof Number
                ? ((Number) body.get("shiftId")).longValue() : null;
        String reason = body.get("reason") instanceof String ? (String) body.get("reason") : null;

        if (shiftId == null || reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "shiftId and reason are required"));
        }

        ShiftEntity shift = shiftMapper.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found: " + shiftId));

        if (!workerId.equals(shift.getWorkerId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "This shift does not belong to you"));
        }

        LocalDate shiftDate = shift.getShiftDate();
        LocalTime endTime = shift.getEndTime();
        LocalDateTime shiftEnd = LocalDateTime.of(shiftDate, endTime);
        if (LocalDateTime.now().isBefore(shiftEnd)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Shift has not ended yet"));
        }

        java.util.Optional<AttendanceCorrectionEntity> existing = correctionMapper.findByShiftId(shiftId);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Correction already submitted for this shift"));
        }

        AttendanceCorrectionEntity correction = new AttendanceCorrectionEntity();
        correction.setShiftId(shiftId);
        correction.setWorkerId(workerId);
        correction.setReason(reason.trim());
        correction.setStatus("PENDING");
        correction.setCreatedAt(LocalDateTime.now());
        correctionMapper.insert(correction);

        return ResponseEntity.ok(Map.of("id", correction.getId(), "status", "PENDING"));
    }

    @Operation(summary = "查询补卡申请状态", description = "查询指定排班的补卡申请状态")
    @GetMapping("/attendance/correction/status")
    public ResponseEntity<?> getCorrectionStatus(
            @Parameter(description = "班次ID") @RequestParam Long shiftId) {
        Long workerId = getCurrentWorkerId();
        if (workerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        java.util.Optional<AttendanceCorrectionEntity> existing = correctionMapper.findByShiftId(shiftId);

        if (existing.isPresent()) {
            AttendanceCorrectionEntity c = existing.get();
            return ResponseEntity.ok(Map.of(
                    "eligible", false,
                    "existingRequest", Map.of("id", c.getId(), "status", c.getStatus())
            ));
        }

        ShiftEntity shift = shiftMapper.findById(shiftId).orElse(null);
        if (shift == null || !workerId.equals(shift.getWorkerId())) {
            return ResponseEntity.ok(Map.of("eligible", false));
        }

        java.util.Optional<AttendanceRecordEntity> record = attendanceRecordMapper.findByShiftId(shiftId);
        boolean isLate = record.isPresent() && record.get().getCheckInTime() != null
                && !record.get().getCheckInTime().toLocalTime().isAfter(shift.getStartTime());

        LocalDateTime shiftEnd = LocalDateTime.of(shift.getShiftDate(), shift.getEndTime());
        boolean ended = LocalDateTime.now().isAfter(shiftEnd);

        boolean eligible = ended && (record.isEmpty() || !isLate);
        return ResponseEntity.ok(Map.of("eligible", eligible));
    }
}
```

- [ ] **Step 5: Compile and verify**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && cd /Users/nifeng/workspace/my/demo/c-service && mvn compile -q`
Expected: BUILD SUCCESS

---

### Task 5: Enterprise-pc correction management UI

**Files:**
- Modify: `enterprise-pc/src/views/schedules/ScheduleShiftList.vue`
- Modify: `enterprise-pc/src/api/schedule.js`

- [ ] **Step 1: Add correction API functions to schedule.js**

Append to `enterprise-pc/src/api/schedule.js`:
```javascript
export function listCorrections(params) {
  return request.get('/schedules/corrections', { params })
}

export function approveCorrection(id) {
  return request.put(`/schedules/corrections/${id}/approve`)
}

export function rejectCorrection(id, data) {
  return request.put(`/schedules/corrections/${id}/reject`, data)
}
```

- [ ] **Step 2: Add tab bar and correction panel to ScheduleShiftList.vue**

The full file needs restructuring. The script section gets a new tab state, correction data, and new functions. The template gets a tab bar with two panels.

In `<script setup>`, add after the existing state:
```javascript
import { listShifts, createShift, deleteShift, listCorrections, approveCorrection, rejectCorrection } from '../../api/schedule'

const activeTab = ref('shifts')

// Correction state
const corrections = ref([])
const correctionTotal = ref(0)
const correctionPage = ref(1)
const correctionPageSize = 20
const correctionLoading = ref(false)
const correctionFilter = ref({ status: '', keyword: '', dateFrom: '', dateTo: '' })
const rejectDialogVisible = ref(false)
const rejectTarget = ref(null)
const rejectForm = ref({ rejectReason: '' })
```

Add correction functions:
```javascript
async function fetchCorrections() {
  correctionLoading.value = true
  try {
    const params = { ...correctionFilter.value, page: correctionPage.value, pageSize: correctionPageSize }
    const res = await listCorrections(params)
    corrections.value = res.records || []
    correctionTotal.value = res.total || 0
  } finally {
    correctionLoading.value = false
  }
}

function handleCorrectionSearch() {
  correctionPage.value = 1
  fetchCorrections()
}

function handleCorrectionReset() {
  correctionFilter.value = { status: '', keyword: '', dateFrom: '', dateTo: '' }
  correctionPage.value = 1
  fetchCorrections()
}

async function handleApprove(id) {
  try {
    await approveCorrection(id)
    ElMessage.success('已通过')
    fetchCorrections()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function openRejectDialog(row) {
  rejectTarget.value = row
  rejectForm.value = { rejectReason: '' }
  rejectDialogVisible.value = true
}

async function handleReject() {
  if (!rejectForm.value.rejectReason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  try {
    await rejectCorrection(rejectTarget.value.id, rejectForm.value)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    fetchCorrections()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function handleCorrectionPageChange(val) {
  correctionPage.value = val
  fetchCorrections()
}

// Switch tabs
function onTabChange(tab) {
  activeTab.value = tab
  if (tab === 'corrections' && corrections.value.length === 0) {
    fetchCorrections()
  }
}
```

Update the template. The existing shift table gets wrapped in a tab panel alongside the correction table:
```html
<template>
  <div class="schedule-shift-list">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="排班列表" name="shifts">
        <!-- existing search form and table content -->
      </el-tab-pane>
      <el-tab-pane label="补卡申请" name="corrections">
        <!-- correction search form -->
        <el-form :model="correctionFilter" inline class="search-form">
          <el-form-item label="状态">
            <el-select v-model="correctionFilter.status" clearable placeholder="全部">
              <el-option label="待审批" value="PENDING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="correctionFilter.keyword" placeholder="岗位名称" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleCorrectionSearch">搜索</el-button>
            <el-button @click="handleCorrectionReset">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="corrections" v-loading="correctionLoading" stripe style="width: 100%">
          <el-table-column prop="jobTitle" label="职位" min-width="140" />
          <el-table-column prop="workerName" label="人员" width="120" />
          <el-table-column prop="shiftDate" label="排班日期" width="120" />
          <el-table-column label="时段" width="150">
            <template #default="{ row }">
              {{ row.startTime }} - {{ row.endTime }}
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="补卡原因" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'PENDING'" type="warning" size="small">待审批</el-tag>
              <el-tag v-else-if="row.status === 'APPROVED'" type="success" size="small">已通过</el-tag>
              <el-tag v-else type="danger" size="small">已拒绝</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 'PENDING'">
                <el-button size="small" type="primary" @click="handleApprove(row.id)">通过</el-button>
                <el-button size="small" type="warning" @click="openRejectDialog(row)">拒绝</el-button>
              </template>
              <el-tooltip v-else-if="row.status === 'REJECTED' && row.rejectReason" :content="row.rejectReason">
                <span style="color:#999;cursor:pointer">查看原因</span>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-if="correctionTotal > correctionPageSize"
            layout="prev, pager, next"
            :total="correctionTotal"
            :page-size="correctionPageSize"
            :current-page="correctionPage"
            @current-change="handleCorrectionPageChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Reject dialog -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝补卡" width="400px">
      <el-form :model="rejectForm">
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectForm.rejectReason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>
```

- [ ] **Step 3: Verify frontend builds**

Run: check syntax with review. No build step is available for the enterprise-pc.

---

### Task 6: Worker-uniapp correction UI

**Files:**
- Modify: `worker-uniapp/src/pages/attendance/clockIn.vue`
- Modify: `worker-uniapp/src/api/attendance.js`

- [ ] **Step 1: Add correction API to attendance.js**

```javascript
export function submitCorrection(data) {
  return request({
    url: '/api/attendance/correction',
    method: 'POST',
    data
  })
}

export function getCorrectionStatus(params) {
  return request({
    url: '/api/attendance/correction/status',
    method: 'GET',
    data: params
  })
}
```

- [ ] **Step 2: Add correction button to clockIn.vue**

In the shift card, after the check-in/check-out buttons, add for expired shifts:

```html
<button v-if="canApplyCorrection(shift)" class="btn btn-correction" @click="openCorrectionDialog(shift)">
  补卡
</button>
```

Add state to `<script setup>`:
```javascript
import { submitCorrection, getCorrectionStatus } from '@/api/attendance'

const correctionDialogVisible = ref(false)
const correctionShift = ref(null)
const correctionReason = ref('')

function canApplyCorrection(shift) {
  const now = new Date()
  const [h, m] = (shift.endTime || '').split(':')
  const end = new Date(shift.date + 'T' + shift.endTime)
  if (!shift.endTime || now <= end) return false
  if (shift.status === 'CHECKED_OUT' || shift.attendanceStatus === 'CHECKED_OUT') return false
  return true
}

function openCorrectionDialog(shift) {
  correctionShift.value = shift
  correctionReason.value = ''
  correctionDialogVisible.value = true
}

async function handleSubmitCorrection() {
  if (!correctionReason.value.trim()) {
    uni.showToast({ title: '请填写补卡原因', icon: 'none' })
    return
  }
  try {
    await submitCorrection({
      shiftId: correctionShift.value.id,
      reason: correctionReason.value.trim()
    })
    uni.showToast({ title: '补卡申请已提交' })
    correctionDialogVisible.value = false
  } catch (e) {
    uni.showToast({ title: e?.data?.error || '提交失败', icon: 'none' })
  }
}
```

Add correction dialog modal template to clockIn.vue:
```html
<uni-popup v-if="correctionDialogVisible" type="dialog">
  <uni-popup-dialog title="补卡申请" :content="''" @close="correctionDialogVisible = false" @confirm="handleSubmitCorrection">
    <view class="correction-form">
      <text class="label">补卡原因</text>
      <textarea v-model="correctionReason" placeholder="请填写补卡原因" class="correction-textarea" />
    </view>
  </uni-popup-dialog>
</uni-popup>
```

---

### Task 7: Tests

**Files:**
- Create: `enterprise-service/src/test/java/com/parttime/enterprise/service/CorrectionServiceTest.java`
- Modify: `c-service/src/test/java/com/parttime/cservice/service/InMemoryMappers.java`

- [ ] **Step 1: Write enterprise-service CorrectionServiceTest**

```java
package com.parttime.enterprise.service;

import com.parttime.enterprise.mapper.AttendanceCorrectionMapper;
import com.parttime.enterprise.mapper.AttendanceRecordMapper;
import com.parttime.enterprise.mapper.ScheduleShiftMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.mapper.JobMapper;
import com.parttime.enterprise.pojo.entity.*;
import com.parttime.enterprise.service.impl.CorrectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CorrectionServiceTest {

    private CorrectionServiceImpl service;
    private InMemoryAttendanceCorrectionMapper correctionMapper;
    private InMemoryAttendanceRecordMapper recordMapper;
    private InMemoryScheduleShiftMapper shiftMapper;
    private InMemoryWorkerSyncMapper workerMapper;
    private InMemoryJobMapper jobMapper;

    @BeforeEach
    void setUp() {
        correctionMapper = new InMemoryAttendanceCorrectionMapper();
        recordMapper = new InMemoryAttendanceRecordMapper();
        shiftMapper = new InMemoryScheduleShiftMapper();
        workerMapper = new InMemoryWorkerSyncMapper();
        jobMapper = new InMemoryJobMapper();

        service = new CorrectionServiceImpl();
        service.setCorrectionMapper(correctionMapper);
        service.setAttendanceRecordMapper(recordMapper);
        service.setShiftMapper(shiftMapper);
        service.setWorkerSyncMapper(workerMapper);
        service.setJobMapper(jobMapper);

        // Seed a job
        Job job = new Job();
        job.setId(1L);
        job.setTitle("测试岗位");
        jobMapper.store(job);

        // Seed a shift
        ScheduleShift shift = new ScheduleShift();
        shift.setId(1L);
        shift.setJobId(1L);
        shift.setWorkerId(1L);
        shift.setShiftDate(LocalDate.of(2026, 5, 21));
        shift.setStartTime(LocalTime.of(9, 0));
        shift.setEndTime(LocalTime.of(18, 0));
        shiftMapper.store(shift);

        // Seed correction
        AttendanceCorrection c = new AttendanceCorrection();
        c.setId(1L);
        c.setShiftId(1L);
        c.setWorkerId(1L);
        c.setReason("忘记打卡");
        c.setStatus("PENDING");
        c.setCreatedAt(LocalDateTime.now());
        correctionMapper.store(c);
    }

    @Test
    void testListCorrections() {
        Map<String, Object> result = service.listCorrections(null, null, null, null, 1, 20);
        assertEquals(1, result.get("total"));
        List<?> records = (List<?>) result.get("records");
        assertEquals(1, records.size());
    }

    @Test
    void testApproveWithoutExistingRecord() {
        service.approve(1L, 100L);

        AttendanceCorrection c = correctionMapper.findById(1L).orElseThrow();
        assertEquals("APPROVED", c.getStatus());
        assertNotNull(c.getProcessedAt());
        assertEquals(100L, c.getProcessorId());

        assertTrue(recordMapper.findByShiftId(1L).isPresent());
        assertEquals("补卡", recordMapper.findByShiftId(1L).get().getRemark());
    }

    @Test
    void testApproveWithExistingRecord() {
        AttendanceRecord existing = new AttendanceRecord();
        existing.setId(10L);
        existing.setShiftId(1L);
        existing.setCheckInTime(LocalDateTime.of(2026, 5, 21, 9, 5));
        existing.setStatus("CHECKED_IN");
        recordMapper.store(existing);

        service.approve(1L, 100L);

        AttendanceRecord updated = recordMapper.findByShiftId(1L).orElseThrow();
        assertEquals("补卡", updated.getRemark());
    }

    @Test
    void testReject() {
        service.reject(1L, 100L, "理由不充分");

        AttendanceCorrection c = correctionMapper.findById(1L).orElseThrow();
        assertEquals("REJECTED", c.getStatus());
        assertEquals("理由不充分", c.getRejectReason());
        assertNotNull(c.getProcessedAt());
        assertEquals(100L, c.getProcessorId());
    }

    @Test
    void testApproveNonPendingShouldThrow() {
        AttendanceCorrection c = correctionMapper.findById(1L).orElseThrow();
        c.setStatus("APPROVED");

        assertThrows(RuntimeException.class, () -> service.approve(1L, 100L));
    }

    // In-memory mapper implementations
    static class InMemoryAttendanceCorrectionMapper implements AttendanceCorrectionMapper {
        final Map<Long, AttendanceCorrection> store = new HashMap<>();
        final AtomicLong idGen = new AtomicLong(1);
        void store(AttendanceCorrection c) { store.put(c.getId(), c); }
        @Override public int insert(AttendanceCorrection c) {
            if (c.getId() == null) c.setId(idGen.getAndIncrement()); store.put(c.getId(), c); return 1;
        }
        @Override public Optional<AttendanceCorrection> findById(Long id) {
            AttendanceCorrection c = store.get(id);
            if (c != null) {
                AttendanceCorrection copy = new AttendanceCorrection();
                copy.setId(c.getId());
                copy.setShiftId(c.getShiftId());
                copy.setWorkerId(c.getWorkerId());
                copy.setReason(c.getReason());
                copy.setStatus(c.getStatus());
                copy.setRejectReason(c.getRejectReason());
                copy.setCreatedAt(c.getCreatedAt());
                copy.setProcessedAt(c.getProcessedAt());
                copy.setProcessorId(c.getProcessorId());
                return Optional.of(copy);
            }
            return Optional.empty();
        }
        @Override public List<AttendanceCorrection> findByShiftId(Long shiftId) { return List.of(); }
        @Override public List<AttendanceCorrection> findByWorkerId(Long workerId) { return List.of(); }
        @Override public List<AttendanceCorrection> findByStatus(String status) { return List.of(); }
        @Override public List<AttendanceCorrection> search(String status, String keyword, String dateFrom, String dateTo, Integer offset, Integer limit) {
            return new ArrayList<>(store.values());
        }
        @Override public int countSearch(String status, String keyword, String dateFrom, String dateTo) { return store.size(); }
        @Override public int update(AttendanceCorrection c) {
            store.put(c.getId(), c); return 1;
        }
    }

    static class InMemoryAttendanceRecordMapper implements AttendanceRecordMapper {
        final Map<Long, AttendanceRecord> store = new HashMap<>();
        void store(AttendanceRecord r) { store.put(r.getId(), r); }
        @Override public int insert(AttendanceRecord r) { store.put(r.getId(), r); return 1; }
        @Override public Optional<AttendanceRecord> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public Optional<AttendanceRecord> findByShiftId(Long shiftId) {
            return store.values().stream().filter(r -> shiftId.equals(r.getShiftId())).findFirst();
        }
        @Override public List<AttendanceRecord> findByShiftIds(List<Long> shiftIds) { return List.of(); }
        @Override public int update(AttendanceRecord r) { store.put(r.getId(), r); return 1; }
    }

    static class InMemoryScheduleShiftMapper implements ScheduleShiftMapper {
        final Map<Long, ScheduleShift> store = new HashMap<>();
        void store(ScheduleShift s) { store.put(s.getId(), s); }
        @Override public int insert(ScheduleShift s) { store.put(s.getId(), s); return 1; }
        @Override public Optional<ScheduleShift> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public List<ScheduleShift> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<ScheduleShift> findByJobId(Long jobId) { return List.of(); }
        @Override public List<ScheduleShift> findByWorkerId(Long workerId) { return List.of(); }
        @Override public List<ScheduleShift> findByJobIdAndDate(Long jobId, LocalDate date) { return List.of(); }
        @Override public List<ScheduleShift> findByIds(List<Long> ids) { return List.of(); }
        @Override public int update(ScheduleShift s) { store.put(s.getId(), s); return 1; }
        @Override public int delete(Long id) { store.remove(id); return 1; }
        @Override public int updateStatus(Long id, String status) { return 0; }
    }

    static class InMemoryWorkerSyncMapper implements WorkerSyncMapper {
        @Override public String findWorkerNameById(Long workerId) { return "测试工人"; }
        @Override public int upsert(Long companyId, Long workerId) { return 1; }
    }

    static class InMemoryJobMapper implements JobMapper {
        final Map<Long, Job> store = new HashMap<>();
        void store(Job j) { store.put(j.getId(), j); }
        @Override public int insert(Job job) { return 0; }
        @Override public Optional<Job> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public Optional<Job> findByJobId(Long jobId) { return Optional.empty(); }
        @Override public List<Job> findAll() { return List.of(); }
        @Override public List<Job> search(String keyword, String location, Long categoryId) { return List.of(); }
        @Override public List<Job> findByCompanyId(Long companyId) { return List.of(); }
        @Override public int update(Job job) { return 0; }
    }
}
```

The CorrectionServiceImpl needs setter methods for the mappers (since we use `@Resource`, we add setter methods for test injection):
```java
    void setCorrectionMapper(AttendanceCorrectionMapper m) { this.correctionMapper = m; }
    void setAttendanceRecordMapper(AttendanceRecordMapper m) { this.attendanceRecordMapper = m; }
    void setShiftMapper(ScheduleShiftMapper m) { this.shiftMapper = m; }
    void setWorkerSyncMapper(WorkerSyncMapper m) { this.workerSyncMapper = m; }
    void setJobMapper(JobMapper m) { this.jobMapper = m; }
```

- [ ] **Step 2: Run enterprise-service tests**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && cd /Users/nifeng/workspace/my/demo/enterprise-service && mvn test -Dtest=CorrectionServiceTest`
Expected: 5+ tests passing

- [ ] **Step 3: Add InMemoryAttendanceCorrectionMapper to c-service InMemoryMappers**

Add to `c-service/src/test/java/com/parttime/cservice/service/InMemoryMappers.java`:
```java
    public static AttendanceCorrectionMapper createAttendanceCorrectionMapper() {
        return new AttendanceCorrectionMapper() {
            private final ConcurrentHashMap<Long, AttendanceCorrectionEntity> store = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override public int insert(AttendanceCorrectionEntity c) {
                if (c.getId() == null) c.setId(idGen.getAndIncrement());
                store.put(c.getId(), c);
                return 1;
            }
            @Override public Optional<AttendanceCorrectionEntity> findById(Long id) {
                return Optional.ofNullable(store.get(id));
            }
            @Override public Optional<AttendanceCorrectionEntity> findByShiftId(Long shiftId) {
                return store.values().stream().filter(c -> shiftId.equals(c.getShiftId())).findFirst();
            }
        };
    }
```

- [ ] **Step 4: Run all existing C-service tests to verify no regression**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && cd /Users/nifeng/workspace/my/demo/c-service && mvn test`
Expected: 105 tests passing

---

### Task 8: Add findByIds to ScheduleShiftMapper (if needed)

Check if `findByIds` exists. If not, add to the enterprise-service ScheduleShiftMapper.

- [ ] **Step 1: Check and add if needed**

In `enterprise-service/src/main/java/com/parttime/enterprise/mapper/ScheduleShiftMapper.java`, add:
```java
    List<ScheduleShift> findByIds(@Param("ids") List<Long> ids);
```

In `enterprise-service/src/main/resources/mapper/ScheduleShiftMapper.xml`, add:
```xml
    <select id="findByIds" resultMap="ShiftResultMap">
        SELECT * FROM schedule_shifts WHERE id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
    </select>
```

---

### Task 9: Run all tests and verify

- [ ] **Step 1: Run all enterprise-service tests**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && cd /Users/nifeng/workspace/my/demo/enterprise-service && mvn test`
Expected: All existing + new tests passing

- [ ] **Step 2: Run all c-service tests**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && cd /Users/nifeng/workspace/my/demo/c-service && mvn test`
Expected: All 105 tests + new tests passing

- [ ] **Step 3: Verify frontend files syntax**

Manually review the modified Vue files for syntax errors.
