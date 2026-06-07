# Enterprise Account Balance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add enterprise account balance system — each enterprise has a balance + credit limit, can top up (self-service simulated pay or platform-admin direct), and settlement deducts from enterprise balance to worker balance.

**Architecture:** Follow existing worker_balances pattern: `enterprise_balances` (balance + credit_limit), `enterprise_balance_transactions` (ledger), `enterprise_top_up_records` (self-service payment records). Settlement flow modified to check `balance + credit_limit >= total` before deducting. Platform-service gets a direct top-up endpoint.

**Tech Stack:** Spring Boot + MyBatis + MySQL (enterprise-service, c-service, platform-service share DB `part_time_work`); enterprise-service (port 8081); platform-service; Element Plus (enterprise-pc); uni-app (enterprise-uniapp)

---

### Task 1: SQL Script

**Files:**
- Create: `scripts/enterprise_balance.sql`

- [ ] **Create the SQL file with 3 tables**

```sql
-- enterprises balance table
CREATE TABLE IF NOT EXISTS enterprise_balances (
    company_id    BIGINT PRIMARY KEY,
    balance       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_top_up  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_spent   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    credit_limit  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at    DATETIME,
    updated_at    DATETIME
);

-- enterprise balance transactions ledger
CREATE TABLE IF NOT EXISTS enterprise_balance_transactions (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id       BIGINT NOT NULL,
    amount           DECIMAL(12,2) NOT NULL COMMENT 'positive=income, negative=expense',
    type             VARCHAR(20) NOT NULL COMMENT 'TOP_UP / SETTLEMENT / SETTLEMENT_REFUND',
    related_bill_id  BIGINT,
    related_top_up_id BIGINT,
    description      VARCHAR(255),
    created_at       DATETIME,
    INDEX idx_company_id (company_id),
    INDEX idx_created_at (created_at)
);

-- enterprise top-up records (self-service simulated payment)
CREATE TABLE IF NOT EXISTS enterprise_top_up_records (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id           BIGINT NOT NULL,
    amount               DECIMAL(12,2) NOT NULL,
    status               VARCHAR(20) DEFAULT 'PROCESSING' COMMENT 'PROCESSING/COMPLETED/FAILED',
    serial_number        VARCHAR(64),
    third_party_serial_no VARCHAR(128),
    third_party_platform  VARCHAR(50),
    completed_at         DATETIME,
    created_at           DATETIME,
    updated_at           DATETIME,
    INDEX idx_company_id (company_id)
);
```

- [ ] **Commit**

```bash
git add scripts/enterprise_balance.sql
git commit -m "feat: add enterprise_balance tables DDL"
```

---

### Task 2: enterprise-service Entities + VOs

**Files:**
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/EnterpriseBalance.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/EnterpriseBalanceTransaction.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/EnterpriseTopUpRecord.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/EnterpriseBalanceVO.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/EnterpriseTransactionVO.java`

- [ ] **Create EnterpriseBalance entity**

```java
package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnterpriseBalance {
    private Long companyId;
    private BigDecimal balance;
    private BigDecimal totalTopUp;
    private BigDecimal totalSpent;
    private BigDecimal creditLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Create EnterpriseBalanceTransaction entity**

```java
package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnterpriseBalanceTransaction {
    private Long id;
    private Long companyId;
    private BigDecimal amount;
    private String type;         // TOP_UP / SETTLEMENT / SETTLEMENT_REFUND
    private Long relatedBillId;
    private Long relatedTopUpId;
    private String description;
    private LocalDateTime createdAt;
}
```

- [ ] **Create EnterpriseTopUpRecord entity**

```java
package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnterpriseTopUpRecord {
    private Long id;
    private Long companyId;
    private BigDecimal amount;
    private String status;           // PROCESSING / COMPLETED / FAILED
    private String serialNumber;
    private String thirdPartySerialNo;
    private String thirdPartyPlatform;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Create EnterpriseBalanceVO**

```java
package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EnterpriseBalanceVO {
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private BigDecimal totalTopUp;
    private BigDecimal totalSpent;
    private BigDecimal usableBalance;   // balance + creditLimit
}
```

- [ ] **Create EnterpriseTransactionVO**

```java
package com.parttime.enterprise.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnterpriseTransactionVO {
    private Long id;
    private BigDecimal amount;
    private String type;           // TOP_UP / SETTLEMENT / SETTLEMENT_REFUND
    private Long relatedBillId;
    private String description;
    private String createdAt;      // Beijing time string
}
```

- [ ] **Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/EnterpriseBalance.java enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/EnterpriseBalanceTransaction.java enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/EnterpriseTopUpRecord.java enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/EnterpriseBalanceVO.java enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/EnterpriseTransactionVO.java
git commit -m "feat(enterprise-service): add enterprise balance entities and VOs"
```

---

### Task 3: enterprise-service Mappers + XML

**Files:**
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/EnterpriseBalanceMapper.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/EnterpriseBalanceTransactionMapper.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/EnterpriseTopUpRecordMapper.java`
- Create: `enterprise-service/src/main/resources/mapper/EnterpriseBalanceMapper.xml`
- Create: `enterprise-service/src/main/resources/mapper/EnterpriseBalanceTransactionMapper.xml`
- Create: `enterprise-service/src/main/resources/mapper/EnterpriseTopUpRecordMapper.xml`

- [ ] **Create EnterpriseBalanceMapper interface**

```java
package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.EnterpriseBalance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EnterpriseBalanceMapper {

    EnterpriseBalance findByCompanyId(@Param("companyId") Long companyId);

    void upsert(@Param("companyId") Long companyId,
                @Param("balance") java.math.BigDecimal balance,
                @Param("totalTopUp") java.math.BigDecimal totalTopUp,
                @Param("totalSpent") java.math.BigDecimal totalSpent);
}
```

- [ ] **Create EnterpriseBalanceMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.enterprise.mapper.EnterpriseBalanceMapper">

    <resultMap id="EnterpriseBalanceResultMap" type="com.parttime.enterprise.pojo.entity.EnterpriseBalance">
        <id property="companyId" column="company_id"/>
        <result property="balance" column="balance"/>
        <result property="totalTopUp" column="total_top_up"/>
        <result property="totalSpent" column="total_spent"/>
        <result property="creditLimit" column="credit_limit"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>

    <select id="findByCompanyId" resultMap="EnterpriseBalanceResultMap">
        SELECT * FROM enterprise_balances WHERE company_id = #{companyId}
    </select>

    <insert id="upsert">
        INSERT INTO enterprise_balances (company_id, balance, total_top_up, total_spent, credit_limit)
        VALUES (#{companyId}, #{balance}, #{totalTopUp}, #{totalSpent}, 0)
        ON DUPLICATE KEY UPDATE
            balance = #{balance},
            total_top_up = #{totalTopUp},
            total_spent = #{totalSpent}
    </insert>

</mapper>
```

- [ ] **Create EnterpriseBalanceTransactionMapper interface**

```java
package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.EnterpriseBalanceTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EnterpriseBalanceTransactionMapper {

    void insert(EnterpriseBalanceTransaction transaction);

    List<EnterpriseBalanceTransaction> findByCompanyIdPage(@Param("companyId") Long companyId,
                                                            @Param("offset") int offset,
                                                            @Param("pageSize") int pageSize);

    long countByCompanyId(@Param("companyId") Long companyId);
}
```

- [ ] **Create EnterpriseBalanceTransactionMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.enterprise.mapper.EnterpriseBalanceTransactionMapper">

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO enterprise_balance_transactions (company_id, amount, type, related_bill_id, related_top_up_id, description)
        VALUES (#{companyId}, #{amount}, #{type}, #{relatedBillId}, #{relatedTopUpId}, #{description})
    </insert>

    <select id="findByCompanyIdPage" resultType="com.parttime.enterprise.pojo.entity.EnterpriseBalanceTransaction">
        SELECT * FROM enterprise_balance_transactions
        WHERE company_id = #{companyId}
        ORDER BY created_at DESC
        LIMIT #{pageSize} OFFSET #{offset}
    </select>

    <select id="countByCompanyId" resultType="long">
        SELECT COUNT(*) FROM enterprise_balance_transactions
        WHERE company_id = #{companyId}
    </select>

</mapper>
```

- [ ] **Create EnterpriseTopUpRecordMapper interface**

```java
package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.EnterpriseTopUpRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EnterpriseTopUpRecordMapper {

    void insert(EnterpriseTopUpRecord record);

    void updateStatus(@Param("id") Long id,
                      @Param("status") String status,
                      @Param("thirdPartySerialNo") String thirdPartySerialNo,
                      @Param("thirdPartyPlatform") String thirdPartyPlatform,
                      @Param("completedAt") java.time.LocalDateTime completedAt);
}
```

- [ ] **Create EnterpriseTopUpRecordMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.enterprise.mapper.EnterpriseTopUpRecordMapper">

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO enterprise_top_up_records (company_id, amount, status, serial_number)
        VALUES (#{companyId}, #{amount}, #{status}, #{serialNumber})
    </insert>

    <update id="updateStatus">
        UPDATE enterprise_top_up_records
        SET status = #{status},
            third_party_serial_no = #{thirdPartySerialNo},
            third_party_platform = #{thirdPartyPlatform},
            completed_at = #{completedAt}
        WHERE id = #{id}
    </update>

</mapper>
```

- [ ] **Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/mapper/EnterpriseBalance*.java enterprise-service/src/main/java/com/parttime/enterprise/mapper/EnterpriseTopUpRecordMapper.java enterprise-service/src/main/resources/mapper/EnterpriseBalance*.xml enterprise-service/src/main/resources/mapper/EnterpriseTopUpRecordMapper.xml
git commit -m "feat(enterprise-service): add enterprise balance mappers and XML"
```

---

### Task 4: enterprise-service Service + Controller

**Files:**
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/service/EnterpriseBalanceService.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/EnterpriseBalanceServiceImpl.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/controller/EnterpriseBalanceController.java`

- [ ] **Create EnterpriseBalanceService interface**

```java
package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.vo.EnterpriseBalanceVO;
import com.parttime.enterprise.pojo.vo.EnterpriseTransactionVO;
import com.parttime.enterprise.pojo.vo.PageVO;

public interface EnterpriseBalanceService {

    EnterpriseBalanceVO getBalance(Long companyId);

    void topUp(Long companyId, java.math.BigDecimal amount);

    PageVO<EnterpriseTransactionVO> getTransactions(Long companyId, int page, int pageSize);

    void deduct(Long companyId, java.math.BigDecimal amount, Long relatedBillId, String description);

    void refund(Long companyId, java.math.BigDecimal amount, Long relatedBillId, String description);
}
```

- [ ] **Create EnterpriseBalanceServiceImpl**

```java
package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.mapper.EnterpriseBalanceMapper;
import com.parttime.enterprise.mapper.EnterpriseBalanceTransactionMapper;
import com.parttime.enterprise.mapper.EnterpriseTopUpRecordMapper;
import com.parttime.enterprise.pojo.entity.EnterpriseBalance;
import com.parttime.enterprise.pojo.entity.EnterpriseBalanceTransaction;
import com.parttime.enterprise.pojo.entity.EnterpriseTopUpRecord;
import com.parttime.enterprise.pojo.vo.EnterpriseBalanceVO;
import com.parttime.enterprise.pojo.vo.EnterpriseTransactionVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.EnterpriseBalanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class EnterpriseBalanceServiceImpl implements EnterpriseBalanceService {

    @Resource
    private EnterpriseBalanceMapper enterpriseBalanceMapper;

    @Resource
    private EnterpriseBalanceTransactionMapper transactionMapper;

    @Resource
    private EnterpriseTopUpRecordMapper topUpRecordMapper;

    private static final DateTimeFormatter SERIAL_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter BEIJING_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId BEIJING = ZoneId.of("Asia/Shanghai");

    @Override
    public EnterpriseBalanceVO getBalance(Long companyId) {
        EnterpriseBalance eb = enterpriseBalanceMapper.findByCompanyId(companyId);
        if (eb == null) {
            EnterpriseBalanceVO vo = new EnterpriseBalanceVO();
            vo.setBalance(BigDecimal.ZERO);
            vo.setCreditLimit(BigDecimal.ZERO);
            vo.setTotalTopUp(BigDecimal.ZERO);
            vo.setTotalSpent(BigDecimal.ZERO);
            vo.setUsableBalance(BigDecimal.ZERO);
            return vo;
        }
        EnterpriseBalanceVO vo = new EnterpriseBalanceVO();
        vo.setBalance(eb.getBalance());
        vo.setCreditLimit(eb.getCreditLimit());
        vo.setTotalTopUp(eb.getTotalTopUp());
        vo.setTotalSpent(eb.getTotalSpent());
        vo.setUsableBalance(eb.getBalance().add(eb.getCreditLimit()));
        return vo;
    }

    @Override
    @Transactional
    public void topUp(Long companyId, BigDecimal amount) {
        // 1. Create top-up record (PROCESSING)
        EnterpriseTopUpRecord record = new EnterpriseTopUpRecord();
        record.setCompanyId(companyId);
        record.setAmount(amount);
        record.setStatus("PROCESSING");
        record.setSerialNumber(generateTopUpSerial());
        topUpRecordMapper.insert(record);

        // 2. Simulate payment completion
        String serialNo = "SIM_" + System.currentTimeMillis();
        topUpRecordMapper.updateStatus(record.getId(), "COMPLETED", serialNo, "SIMULATED_PAY", LocalDateTime.now());

        // 3. Upsert balance
        EnterpriseBalance eb = enterpriseBalanceMapper.findByCompanyId(companyId);
        BigDecimal newBalance = (eb == null ? BigDecimal.ZERO : eb.getBalance()).add(amount);
        BigDecimal newTotalTopUp = (eb == null ? BigDecimal.ZERO : eb.getTotalTopUp()).add(amount);
        BigDecimal totalSpent = (eb == null ? BigDecimal.ZERO : eb.getTotalSpent());
        enterpriseBalanceMapper.upsert(companyId, newBalance, newTotalTopUp, totalSpent);

        // 4. Insert transaction
        EnterpriseBalanceTransaction txn = new EnterpriseBalanceTransaction();
        txn.setCompanyId(companyId);
        txn.setAmount(amount);
        txn.setType("TOP_UP");
        txn.setRelatedTopUpId(record.getId());
        txn.setDescription("企业充值: +" + amount + "元");
        transactionMapper.insert(txn);
    }

    @Override
    public PageVO<EnterpriseTransactionVO> getTransactions(Long companyId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<EnterpriseBalanceTransaction> list = transactionMapper.findByCompanyIdPage(companyId, offset, pageSize);
        long total = transactionMapper.countByCompanyId(companyId);
        List<EnterpriseTransactionVO> voList = list.stream().map(txn -> {
            EnterpriseTransactionVO vo = new EnterpriseTransactionVO();
            vo.setId(txn.getId());
            vo.setAmount(txn.getAmount());
            vo.setType(txn.getType());
            vo.setRelatedBillId(txn.getRelatedBillId());
            vo.setDescription(txn.getDescription());
            if (txn.getCreatedAt() != null) {
                ZonedDateTime beijing = txn.getCreatedAt().atZone(ZoneId.systemDefault()).withZoneSameInstant(BEIJING);
                vo.setCreatedAt(beijing.format(BEIJING_FMT));
            }
            return vo;
        }).collect(Collectors.toList());
        return new PageVO<>(voList, total);
    }

    @Override
    @Transactional
    public void deduct(Long companyId, BigDecimal amount, Long relatedBillId, String description) {
        // amount is positive (actual_pay), stored as negative in transaction
        EnterpriseBalance eb = enterpriseBalanceMapper.findByCompanyId(companyId);
        BigDecimal balance = (eb == null ? BigDecimal.ZERO : eb.getBalance());
        BigDecimal creditLimit = (eb == null ? BigDecimal.ZERO : eb.getCreditLimit());
        BigDecimal totalSpent = (eb == null ? BigDecimal.ZERO : eb.getTotalSpent());

        if (balance.add(creditLimit).compareTo(amount) < 0) {
            throw new RuntimeException("企业余额不足");
        }

        BigDecimal newBalance = balance.subtract(amount);
        BigDecimal newTotalSpent = totalSpent.add(amount);
        BigDecimal totalTopUp = (eb == null ? BigDecimal.ZERO : eb.getTotalTopUp());
        enterpriseBalanceMapper.upsert(companyId, newBalance, totalTopUp, newTotalSpent);

        EnterpriseBalanceTransaction txn = new EnterpriseBalanceTransaction();
        txn.setCompanyId(companyId);
        txn.setAmount(amount.negate());
        txn.setType("SETTLEMENT");
        txn.setRelatedBillId(relatedBillId);
        txn.setDescription(description);
        transactionMapper.insert(txn);
    }

    @Override
    @Transactional
    public void refund(Long companyId, BigDecimal amount, Long relatedBillId, String description) {
        EnterpriseBalance eb = enterpriseBalanceMapper.findByCompanyId(companyId);
        BigDecimal balance = (eb == null ? BigDecimal.ZERO : eb.getBalance());
        BigDecimal totalSpent = (eb == null ? BigDecimal.ZERO : eb.getTotalSpent());
        BigDecimal totalTopUp = (eb == null ? BigDecimal.ZERO : eb.getTotalTopUp());

        BigDecimal newBalance = balance.add(amount);
        BigDecimal newTotalSpent = totalSpent.subtract(amount);
        enterpriseBalanceMapper.upsert(companyId, newBalance, totalTopUp, newTotalSpent);

        EnterpriseBalanceTransaction txn = new EnterpriseBalanceTransaction();
        txn.setCompanyId(companyId);
        txn.setAmount(amount);
        txn.setType("SETTLEMENT_REFUND");
        txn.setRelatedBillId(relatedBillId);
        txn.setDescription(description);
        transactionMapper.insert(txn);
    }

    private synchronized String generateTopUpSerial() {
        String ts = LocalDateTime.now().format(SERIAL_FMT);
        int seq = RANDOM.nextInt(10000);
        return "TOP" + ts + String.format("%04d", seq);
    }
}
```

- [ ] **Create EnterpriseBalanceController**

```java
package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.vo.EnterpriseBalanceVO;
import com.parttime.enterprise.pojo.vo.EnterpriseTransactionVO;
import com.parttime.enterprise.pojo.vo.PageVO;
import com.parttime.enterprise.service.EnterpriseBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/enterprise/balance")
public class EnterpriseBalanceController {

    @Resource
    private EnterpriseBalanceService enterpriseBalanceService;

    @Operation(summary = "查询企业余额")
    @GetMapping("")
    public EnterpriseBalanceVO getBalance() {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return enterpriseBalanceService.getBalance(companyId);
    }

    @Operation(summary = "企业充值（模拟支付）")
    @PostMapping("/top-up")
    public void topUp(@RequestBody Map<String, BigDecimal> body) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        enterpriseBalanceService.topUp(companyId, body.get("amount"));
    }

    @Operation(summary = "企业流水列表")
    @GetMapping("/transactions")
    public PageVO<EnterpriseTransactionVO> getTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return enterpriseBalanceService.getTransactions(companyId, page, pageSize);
    }
}
```

- [ ] **Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/service/EnterpriseBalanceService.java enterprise-service/src/main/java/com/parttime/enterprise/service/impl/EnterpriseBalanceServiceImpl.java enterprise-service/src/main/java/com/parttime/enterprise/controller/EnterpriseBalanceController.java
git commit -m "feat(enterprise-service): add enterprise balance service and controller"
```

---

### Task 5: Modify SettlementServiceImpl — Balance Check + Deduction

**Files:**
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/SettlementServiceImpl.java`

- [ ] **Add `EnterpriseBalanceMapper` and `EnterpriseBalanceService` injection + modify `payFromAttendanceRecords` and `unsettle`**

In `SettlementServiceImpl.java`:
1. Add `@Resource private EnterpriseBalanceService enterpriseBalanceService;`
2. Add imports for `EnterpriseBalanceService`
3. In `payFromAttendanceRecords`:
   - Before the for loop, calculate total `actualPay` across all records
   - Call `enterpriseBalanceService.deduct(companyId, totalPay, ...)` after all bills are created
4. In `unsettle`:
   - After existing refund logic, call `enterpriseBalanceService.refund(companyId, ...)`

```java
// Add as new fields:
    @Resource
    private EnterpriseBalanceService enterpriseBalanceService;

// Modify payFromAttendanceRecords — add total deduction after the for loop:
    @Override
    @Transactional
    public void payFromAttendanceRecords(List<Long> attendanceRecordIds, Long companyId) {
        BigDecimal totalPay = BigDecimal.ZERO;
        for (Long arId : attendanceRecordIds) {
            // ... existing logic ...
            totalPay = totalPay.add(actualPay);
        }
        // Deduct enterprise balance after all bills created
        if (totalPay.compareTo(BigDecimal.ZERO) > 0) {
            enterpriseBalanceService.deduct(companyId, totalPay, null, "批量结算: " + totalPay + "元");
        }
    }

// Modify unsettle — add refund at the end:
    @Override
    @Transactional
    public void unsettle(Long attendanceRecordId, Long companyId) {
        // ... existing logic up to ar.setSettlementStatus("UNPAID") ...

        // Refund enterprise balance
        enterpriseBalanceService.refund(companyId, actualPay, bill.getId(), "撤回结算: " + bill.getWorkerName() + " " + bill.getShiftDate());

        // ... rest ...
        ar.setSettlementStatus("UNPAID");
        attendanceRecordMapper.update(ar);
    }
```

Full modified methods:

For `payFromAttendanceRecords`, the method should sum up totalPay across all records and deduct once:

```java
    @Override
    @Transactional
    public void payFromAttendanceRecords(List<Long> attendanceRecordIds, Long companyId) {
        BigDecimal totalPay = BigDecimal.ZERO;
        for (Long arId : attendanceRecordIds) {
            AttendanceRecord ar = attendanceRecordMapper.findById(arId)
                    .orElseThrow(() -> new RuntimeException("Attendance record not found: " + arId));
            if ("PAID".equals(ar.getSettlementStatus())) continue;

            ScheduleShift shift = scheduleShiftMapper.findById(ar.getShiftId())
                    .orElseThrow(() -> new RuntimeException("Schedule shift not found: " + ar.getShiftId()));

            Long workerId = ar.getWorkerId() != null ? ar.getWorkerId() : shift.getWorkerId();
            String workerName = workerSyncMapper.findWorkerNameById(workerId);
            BigDecimal actualPay = ar.getPayablePay() != null ? ar.getPayablePay() : ar.getScheduledPay();

            String serialNo = generateSerialNumber();

            SettlementBill bill = new SettlementBill();
            bill.setCompanyId(companyId);
            bill.setJobId(ar.getJobId() != null ? ar.getJobId() : shift.getJobId());
            bill.setShiftId(ar.getShiftId());
            bill.setWorkerId(workerId);
            bill.setWorkerName(workerName);
            bill.setShiftDate(shift.getShiftDate());
            bill.setStartTime(shift.getStartTime());
            bill.setEndTime(shift.getEndTime());
            bill.setTotalHours(ar.getTotalHours());
            bill.setRateType(shift.getSalaryType());
            bill.setRateAmount(shift.getSalaryAmount());
            bill.setScheduledPay(ar.getScheduledPay());
            bill.setActualPay(actualPay);
            bill.setSerialNumber(serialNo);
            bill.setStatus("PAID");
            bill.setPaidAt(LocalDateTime.now());
            settlementBillMapper.insert(bill);

            WorkerBalance wb = workerBalanceMapper.findByWorkerId(workerId);
            if (wb == null) {
                workerBalanceMapper.upsert(workerId, actualPay, actualPay, BigDecimal.ZERO);
            } else {
                workerBalanceMapper.upsert(workerId,
                        wb.getBalance().add(actualPay),
                        wb.getTotalEarned().add(actualPay),
                        wb.getTotalWithdrawn());
            }

            BalanceTransaction bt = new BalanceTransaction();
            bt.setWorkerId(workerId);
            bt.setAmount(actualPay);
            bt.setType("EARNINGS");
            bt.setRelatedBillId(bill.getId());
            bt.setDescription("结算收入: " + workerName + " " + shift.getShiftDate());
            balanceTransactionMapper.insert(bt);

            ar.setSettlementStatus("PAID");
            attendanceRecordMapper.update(ar);

            totalPay = totalPay.add(actualPay);
        }

        // Deduct enterprise balance
        if (totalPay.compareTo(BigDecimal.ZERO) > 0) {
            enterpriseBalanceService.deduct(companyId, totalPay, null, "批量结算: " + totalPay + "元");
        }
    }
```

For `unsettle`, add refund before the final update:

```java
    @Override
    @Transactional
    public void unsettle(Long attendanceRecordId, Long companyId) {
        AttendanceRecord ar = attendanceRecordMapper.findById(attendanceRecordId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found: " + attendanceRecordId));
        if (!"PAID".equals(ar.getSettlementStatus())) {
            throw new RuntimeException("Record is not settled");
        }

        SettlementBill bill = settlementBillMapper.findByShiftId(ar.getShiftId())
                .orElseThrow(() -> new RuntimeException("Settlement bill not found for this record"));

        Long workerId = ar.getWorkerId() != null ? ar.getWorkerId() : bill.getWorkerId();
        BigDecimal actualPay = bill.getActualPay();

        WorkerBalance wb = workerBalanceMapper.findByWorkerId(workerId);
        if (wb == null || wb.getBalance().compareTo(actualPay) < 0) {
            throw new RuntimeException("Cannot unsettle: worker has withdrawn the settled amount");
        }

        bill.setStatus("REFUNDED");
        settlementBillMapper.updateBillStatus(bill.getId(), "REFUNDED");

        workerBalanceMapper.upsert(workerId,
                wb.getBalance().subtract(actualPay),
                wb.getTotalEarned().subtract(actualPay),
                wb.getTotalWithdrawn());

        BalanceTransaction bt = new BalanceTransaction();
        bt.setWorkerId(workerId);
        bt.setAmount(actualPay.negate());
        bt.setType("REFUND");
        bt.setRelatedBillId(bill.getId());
        bt.setDescription("撤回结算: " + bill.getWorkerName() + " " + bill.getShiftDate());
        balanceTransactionMapper.insert(bt);

        // Refund enterprise balance
        enterpriseBalanceService.refund(companyId, actualPay, bill.getId(), "撤回结算退款: " + bill.getWorkerName() + " " + bill.getShiftDate());

        ar.setSettlementStatus("UNPAID");
        attendanceRecordMapper.update(ar);
    }
```

- [ ] **Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/service/impl/SettlementServiceImpl.java
git commit -m "feat(enterprise-service): add enterprise balance check/deduction in settlement flow"
```

---

### Task 6: platform-service Direct Top-Up

**Files:**
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/cmd/EnterpriseBalanceAdjustCmd.java`
- Create: `platform-service/src/main/java/com/parttime/platform/service/EnterpriseBalanceService.java`
- Create: `platform-service/src/main/java/com/parttime/platform/service/impl/EnterpriseBalanceServiceImpl.java`
- Create: `platform-service/src/main/java/com/parttime/platform/controller/EnterpriseBalanceController.java`
- Create: `platform-service/src/main/java/com/parttime/platform/mapper/EnterpriseBalanceMapper.java`
- Create: `platform-service/src/main/resources/mapper/EnterpriseBalanceMapper.xml`

- [ ] **Create EnterpriseBalanceAdjustCmd**

```java
package com.parttime.platform.pojo.cmd;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EnterpriseBalanceAdjustCmd {
    private Long companyId;
    private BigDecimal amount;
    private String description;
}
```

- [ ] **Create EnterpriseBalanceMapper for platform-service**

```java
package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.EnterpriseBalance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;

@Mapper
public interface EnterpriseBalanceMapper {

    EnterpriseBalance findByCompanyId(@Param("companyId") Long companyId);

    void upsert(@Param("companyId") Long companyId,
                @Param("balance") BigDecimal balance,
                @Param("totalTopUp") BigDecimal totalTopUp,
                @Param("totalSpent") BigDecimal totalSpent);
}
```

- [ ] **Create platform-service mapper XML**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.platform.mapper.EnterpriseBalanceMapper">

    <resultMap id="EnterpriseBalanceResultMap" type="com.parttime.platform.pojo.entity.EnterpriseBalance">
        <id property="companyId" column="company_id"/>
        <result property="balance" column="balance"/>
        <result property="totalTopUp" column="total_top_up"/>
        <result property="totalSpent" column="total_spent"/>
        <result property="creditLimit" column="credit_limit"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>

    <select id="findByCompanyId" resultMap="EnterpriseBalanceResultMap">
        SELECT * FROM enterprise_balances WHERE company_id = #{companyId}
    </select>

    <insert id="upsert">
        INSERT INTO enterprise_balances (company_id, balance, total_top_up, total_spent, credit_limit)
        VALUES (#{companyId}, #{balance}, #{totalTopUp}, #{totalSpent}, 0)
        ON DUPLICATE KEY UPDATE
            balance = #{balance},
            total_top_up = #{totalTopUp},
            total_spent = #{totalSpent}
    </insert>

</mapper>
```

- [ ] **Create EnterpriseBalance entity for platform-service** (same fields, different package)

```java
package com.parttime.platform.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnterpriseBalance {
    private Long companyId;
    private BigDecimal balance;
    private BigDecimal totalTopUp;
    private BigDecimal totalSpent;
    private BigDecimal creditLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Create EnterpriseBalanceService interface**

```java
package com.parttime.platform.service;

import com.parttime.platform.pojo.cmd.EnterpriseBalanceAdjustCmd;

public interface EnterpriseBalanceService {
    void adjust(EnterpriseBalanceAdjustCmd cmd);
}
```

- [ ] **Create EnterpriseBalanceServiceImpl**

```java
package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.EnterpriseBalanceMapper;
import com.parttime.platform.pojo.cmd.EnterpriseBalanceAdjustCmd;
import com.parttime.platform.pojo.entity.EnterpriseBalance;
import com.parttime.platform.service.EnterpriseBalanceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class EnterpriseBalanceServiceImpl implements EnterpriseBalanceService {

    @Resource
    private EnterpriseBalanceMapper enterpriseBalanceMapper;

    @Override
    @Transactional
    public void adjust(EnterpriseBalanceAdjustCmd cmd) {
        EnterpriseBalance eb = enterpriseBalanceMapper.findByCompanyId(cmd.getCompanyId());
        BigDecimal balance = (eb == null ? BigDecimal.ZERO : eb.getBalance());
        BigDecimal totalTopUp = (eb == null ? BigDecimal.ZERO : eb.getTotalTopUp());
        BigDecimal totalSpent = (eb == null ? BigDecimal.ZERO : eb.getTotalSpent());

        BigDecimal newBalance = balance.add(cmd.getAmount());
        BigDecimal newTotalTopUp = totalTopUp.add(cmd.getAmount());
        enterpriseBalanceMapper.upsert(cmd.getCompanyId(), newBalance, newTotalTopUp, totalSpent);
    }
}
```

- [ ] **Create EnterpriseBalanceController**

```java
package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.EnterpriseBalanceAdjustCmd;
import com.parttime.platform.service.EnterpriseBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/enterprise/balance")
public class EnterpriseBalanceController {

    @Resource
    private EnterpriseBalanceService enterpriseBalanceService;

    @Operation(summary = "运营后台直接调整企业余额（充值/扣款）")
    @PostMapping("/adjust")
    public void adjust(@RequestBody EnterpriseBalanceAdjustCmd cmd) {
        enterpriseBalanceService.adjust(cmd);
    }
}
```

- [ ] **Commit**

```bash
git add platform-service/src/main/java/com/parttime/platform/pojo/cmd/EnterpriseBalanceAdjustCmd.java platform-service/src/main/java/com/parttime/platform/service/EnterpriseBalanceService.java platform-service/src/main/java/com/parttime/platform/service/impl/EnterpriseBalanceServiceImpl.java platform-service/src/main/java/com/parttime/platform/controller/EnterpriseBalanceController.java platform-service/src/main/java/com/parttime/platform/mapper/EnterpriseBalanceMapper.java platform-service/src/main/resources/mapper/EnterpriseBalanceMapper.xml platform-service/src/main/java/com/parttime/platform/pojo/entity/EnterpriseBalance.java
git commit -m "feat(platform-service): add enterprise balance direct adjust endpoint"
```

---

### Task 7: enterprise-pc Balance Page

**Files:**
- Create: `enterprise-pc/src/views/balance/BalancePage.vue`
- Create: `enterprise-pc/src/api/balance.js`
- Modify: `enterprise-pc/src/router/index.js`

- [ ] **Add balance API module**

```js
// enterprise-pc/src/api/balance.js
import request from './request'

export function getBalance() {
  return request.get('/enterprise/balance')
}

export function topUp(amount) {
  return request.post('/enterprise/balance/top-up', { amount })
}

export function getTransactions(params) {
  return request.get('/enterprise/balance/transactions', { params })
}
```

- [ ] **Create BalancePage.vue**

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBalance, topUp, getTransactions } from '../../api/balance'

const balanceInfo = ref({
  balance: 0,
  creditLimit: 0,
  totalTopUp: 0,
  totalSpent: 0,
  usableBalance: 0
})
const transactions = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const topUpDialogVisible = ref(false)
const topUpAmount = ref(0)

async function fetchBalance() {
  try {
    balanceInfo.value = await getBalance()
  } catch {}
}

async function fetchTransactions() {
  loading.value = true
  try {
    const res = await getTransactions({ page: page.value, pageSize: pageSize.value })
    transactions.value = Array.isArray(res) ? res : (res.records || [])
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function handleTopUp() {
  if (!topUpAmount.value || topUpAmount.value <= 0) {
    ElMessage.warning('请输入充值金额')
    return
  }
  try {
    await ElMessageBox.confirm(`确定充值 ${topUpAmount.value} 元？`, '提示')
    await topUp(topUpAmount.value)
    ElMessage.success('充值成功')
    topUpDialogVisible.value = false
    topUpAmount.value = 0
    fetchBalance()
    fetchTransactions()
  } catch {}
}

const typeLabels = {
  TOP_UP: { label: '充值', color: 'green' },
  SETTLEMENT: { label: '结算支出', color: 'red' },
  SETTLEMENT_REFUND: { label: '结算退款', color: 'orange' }
}

onMounted(() => {
  fetchBalance()
  fetchTransactions()
})
</script>

<template>
  <div class="balance-page">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">账户余额</div>
            <div class="stat-value" style="color: #409eff">{{ balanceInfo.balance }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">信用额度</div>
            <div class="stat-value" style="color: #67c23a">{{ balanceInfo.creditLimit }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">可用额度</div>
            <div class="stat-value" style="color: #e6a23c">{{ balanceInfo.usableBalance }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">累计充值</div>
            <div class="stat-value" style="color: #909399">{{ balanceInfo.totalTopUp }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 16px">
      <div class="toolbar">
        <el-button type="primary" @click="topUpDialogVisible = true">充值</el-button>
      </div>
      <el-table :data="transactions" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ row.createdAt }}</template>
        </el-table-column>
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'TOP_UP' ? 'success' : (row.type === 'SETTLEMENT' ? 'danger' : 'warning')" size="small">
              {{ typeLabels[row.type]?.label || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.amount > 0 ? '#67c23a' : '#f56c6c' }">
              {{ row.amount > 0 ? '+' : '' }}{{ row.amount }} 元
            </span>
          </template>
        </el-table-column>
        <el-table-column label="说明">
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchTransactions"
        />
      </div>
    </el-card>

    <el-dialog v-model="topUpDialogVisible" title="充值" width="400px">
      <el-form label-width="80px">
        <el-form-item label="充值金额">
          <el-input-number v-model="topUpAmount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topUpDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTopUp">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.balance-page {
  padding: 20px;
}
.stat-item {
  text-align: center;
  padding: 20px 0;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-top: 8px;
}
.stat-label {
  font-size: 14px;
  color: #909399;
}
.toolbar {
  margin-bottom: 16px;
}
.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
```

- [ ] **Modify router — add `/balance` route**

```js
// Add to routes array in enterprise-pc/src/router/index.js
  {
    path: '/balance',
    name: 'BalancePage',
    component: () => import('../views/balance/BalancePage.vue'),
    meta: { requiresAuth: true, title: '账户余额' }
  },
```

- [ ] **Commit**

```bash
git add enterprise-pc/src/api/balance.js enterprise-pc/src/views/balance/BalancePage.vue enterprise-pc/src/router/index.js
git commit -m "feat(enterprise-pc): add balance page with top-up and transaction history"
```

---

### Task 8: enterprise-uniapp Balance Pages

**Files:**
- Create: `enterprise-uniapp/src/pages/balance/balanceList.vue`
- Create: `enterprise-uniapp/src/api/balance.js`

- [ ] **Create balance API module**

```js
// enterprise-uniapp/src/api/balance.js
export function getBalance(request) {
  return request('GET', '/enterprise/balance')
}

export function topUp(request, amount) {
  return request('POST', '/enterprise/balance/top-up', { amount })
}

export function getTransactions(request, params) {
  return request('GET', '/enterprise/balance/transactions', params)
}
```

- [ ] **Create balanceList.vue**

```vue
<script setup>
import { ref, onShow } from 'vue'
import { request } from '@/api/request'
import { getBalance, topUp, getTransactions } from '@/api/balance'

const balanceInfo = ref({
  balance: 0,
  creditLimit: 0,
  usableBalance: 0,
  totalTopUp: 0,
  totalSpent: 0
})
const transactions = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const showTopUpDialog = ref(false)
const topUpAmount = ref('')

async function fetchBalance() {
  try {
    balanceInfo.value = await getBalance(request)
  } catch {}
}

async function fetchTransactions() {
  try {
    const res = await getTransactions(request, { page: page.value, pageSize: pageSize.value })
    transactions.value = Array.isArray(res) ? res : (res.records || [])
    total.value = res.total || 0
  } catch {}
}

async function handleTopUp() {
  const amount = parseFloat(topUpAmount.value)
  if (!amount || amount <= 0) {
    uni.showToast({ title: '请输入有效金额', icon: 'none' })
    return
  }
  try {
    await topUp(request, amount)
    uni.showToast({ title: '充值成功', icon: 'success' })
    showTopUpDialog.value = false
    topUpAmount.value = ''
    page.value = 1
    fetchBalance()
    fetchTransactions()
  } catch {}
}

function onPageChange(e) {
  page.value = e
  fetchTransactions()
}

onShow(() => {
  page.value = 1
  fetchBalance()
  fetchTransactions()
})

function formatAmount(amount) {
  const prefix = amount > 0 ? '+' : ''
  return prefix + amount.toFixed(2) + ' 元'
}

function amountColor(amount) {
  return amount > 0 ? '#67c23a' : '#f56c6c'
}

function typeLabel(type) {
  const map = { TOP_UP: '充值', SETTLEMENT: '结算支出', SETTLEMENT_REFUND: '结算退款' }
  return map[type] || type
}
</script>

<template>
  <view class="page">
    <view class="balance-cards">
      <view class="card-row">
        <view class="card">
          <text class="card-label">账户余额</text>
          <text class="card-value primary">{{ balanceInfo.balance }}</text>
        </view>
        <view class="card">
          <text class="card-label">信用额度</text>
          <text class="card-value success">{{ balanceInfo.creditLimit }}</text>
        </view>
      </view>
      <view class="card-row">
        <view class="card">
          <text class="card-label">可用额度</text>
          <text class="card-value warning">{{ balanceInfo.usableBalance }}</text>
        </view>
        <view class="card">
          <text class="card-label">累计充值</text>
          <text class="card-value info">{{ balanceInfo.totalTopUp }}</text>
        </view>
      </view>
    </view>

    <view class="toolbar">
      <button class="top-up-btn" @click="showTopUpDialog = true">充值</button>
    </view>

    <view class="section-title">账户流水</view>

    <view v-for="item in transactions" :key="item.id" class="txn-item">
      <view class="txn-left">
        <text class="txn-type">{{ typeLabel(item.type) }}</text>
        <text class="txn-desc">{{ item.description || '-' }}</text>
        <text class="txn-time">{{ item.createdAt }}</text>
      </view>
      <text class="txn-amount" :style="{ color: amountColor(item.amount) }">{{ formatAmount(item.amount) }}</text>
    </view>

    <uni-load-more v-if="total > pageSize" :status="page * pageSize >= total ? 'noMore' : 'more'" />

    <uni-popup ref="topUpPopup" type="dialog">
      <uni-popup-dialog title="充值" :before-close="true" @close="showTopUpDialog = false" @confirm="handleTopUp">
        <input v-model="topUpAmount" type="digit" placeholder="请输入充值金额" class="top-up-input" />
      </uni-popup-dialog>
    </uni-popup>
  </view>
</template>

<style>
.page { min-height: 100vh; background: #f5f5f5; padding: 16rpx; }
.balance-cards { padding: 16rpx 0; }
.card-row { display: flex; gap: 16rpx; margin-bottom: 16rpx; }
.card { flex: 1; background: #fff; border-radius: 16rpx; padding: 32rpx 24rpx; text-align: center; }
.card-label { font-size: 26rpx; color: #999; display: block; }
.card-value { font-size: 40rpx; font-weight: 600; margin-top: 8rpx; display: block; }
.primary { color: #409eff; }
.success { color: #67c23a; }
.warning { color: #e6a23c; }
.info { color: #909399; }
.toolbar { padding: 16rpx 0; }
.top-up-btn { width: 100%; background: #409eff; color: #fff; border: none; border-radius: 12rpx; padding: 24rpx; font-size: 30rpx; }
.section-title { font-size: 28rpx; color: #666; padding: 16rpx 0 8rpx; }
.txn-item { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 8rpx; display: flex; justify-content: space-between; align-items: center; }
.txn-left { flex: 1; }
.txn-type { font-size: 28rpx; font-weight: 500; color: #333; display: block; }
.txn-desc { font-size: 24rpx; color: #999; margin-top: 4rpx; display: block; }
.txn-time { font-size: 22rpx; color: #ccc; margin-top: 4rpx; display: block; }
.txn-amount { font-size: 32rpx; font-weight: 600; }
.top-up-input { border: 2rpx solid #ddd; border-radius: 12rpx; padding: 20rpx; font-size: 30rpx; margin: 20rpx 0; }
</style>
```

- [ ] **Commit**

```bash
git add enterprise-uniapp/src/pages/balance/balanceList.vue enterprise-uniapp/src/api/balance.js
git commit -m "feat(enterprise-uniapp): add balance page with top-up and transaction history"
```

---

### Task 9: enterprise-uniapp pages.json + Home Nav

**Files:**
- Modify: `enterprise-uniapp/src/pages.json`
- Modify: `enterprise-uniapp/src/pages/home/index.vue`

- [ ] **Add balance page to pages.json**

```json
// Add to pages array in enterprise-uniapp/src/pages.json
    {"path": "pages/balance/balanceList", "style": {"navigationBarTitleText": "账户余额", "enablePullDownRefresh": true}}
```

- [ ] **Add "账户余额" to home page nav**

In `enterprise-uniapp/src/pages/home/index.vue`, add to the `sections` array:

```js
// Add to the 基础管理 section's items array under 工作地点:
      { name: '账户余额', icon: '🏦', path: '/pages/balance/balanceList' },
```

So the sections become:

```js
const sections = [
  {
    name: '招聘管理',
    items: [
      { name: '发布职位', icon: '📋', path: '/pages/jobs/jobList' },
      { name: '报名管理', icon: '👥', path: '/pages/applications/applicationList' },
      { name: '排班管理', icon: '📅', path: '/pages/schedules/scheduleList' },
      { name: '考勤管理', icon: '⏱', path: '/pages/attendance/attendanceList' },
      { name: '结算账单', icon: '💰', path: '/pages/settlement/settlementList' }
    ]
  },
  {
    name: '基础管理',
    items: [
      { name: '工作地点', icon: '📍', path: '/pages/locations/locationList' },
      { name: '职位模版', icon: '📄', path: '/pages/templates/templateList' },
      { name: '账户余额', icon: '🏦', path: '/pages/balance/balanceList' }
    ]
  }
]
```

- [ ] **Commit**

```bash
git add enterprise-uniapp/src/pages.json enterprise-uniapp/src/pages/home/index.vue
git commit -m "feat(enterprise-uniapp): add balance page route and home nav entry"
```
