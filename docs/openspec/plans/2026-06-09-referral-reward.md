# Referral Reward Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现邀请奖励系统，包括邀请链接/海报生成、邀请关系绑定、奖励规则配置、奖励审核发放、被邀请人列表展示。

**Architecture:** c-service 实现邀请相关功能（邀请码生成、链接/海报、绑定、奖励检查），platform-service 实现奖励规则配置和审核功能。数据库新增 4 张表（referral_code、referral_record、referral_reward、referral_config）。

**Tech Stack:** Spring Boot + MyBatis + MySQL（c-service、platform-service 共享 DB `part_time_work`）；worker-uniapp（工人端 UniApp）；platform-pc（平台端 Vue 3 + Element Plus）

---

### Task 1: SQL Script

**Files:**
- Create: `scripts/19_referral_reward.sql`

- [ ] **Create the SQL file with 4 tables**

```sql
-- 邀请码表
CREATE TABLE IF NOT EXISTS referral_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_id BIGINT NOT NULL,
    code VARCHAR(8) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_worker (worker_id)
);

-- 邀请记录表
CREATE TABLE IF NOT EXISTS referral_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    referrer_id BIGINT NOT NULL,
    referee_id BIGINT NOT NULL,
    referral_code VARCHAR(8) NOT NULL,
    bound_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_referee (referee_id),
    INDEX idx_referrer_id (referrer_id)
);

-- 邀请奖励表
CREATE TABLE IF NOT EXISTS referral_reward (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    referral_record_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'AUDITING', 'GRANTED', 'REJECTED', 'FAILED') DEFAULT 'PENDING',
    audit_remark VARCHAR(200),
    granted_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_referral_record_id (referral_record_id),
    INDEX idx_status (status)
);

-- 奖励规则配置表
CREATE TABLE IF NOT EXISTS referral_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(50) NOT NULL UNIQUE,
    config_value VARCHAR(200) NOT NULL,
    description VARCHAR(200),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 默认配置
INSERT INTO referral_config (config_key, config_value, description) VALUES
('need_audit', 'true', '是否需要审核：true-需要，false-不需要'),
('audit_days', '0', '审核时限（天），0表示无限制'),
('reward_amount', '20', '邀请人奖励金额（元）'),
('valid_days', '30', '被邀请人注册后有效天数'),
('min_work_count', '3', '被邀请人最少打工次数'),
('min_income', '0', '被邀请人最少收入（元），0表示不限制'),
('release_method', 'manual', '发放方式：auto-自动发放到余额，manual-手动提现');
```

- [ ] **Commit**

```bash
git add scripts/19_referral_reward.sql
git commit -m "feat: add referral reward tables DDL"
```

---

### Task 2: c-service Entities + VOs

**Files:**
- Create: `c-service/src/main/java/com/parttime/cservice/pojo/entity/ReferralCode.java`
- Create: `c-service/src/main/java/com/parttime/cservice/pojo/entity/ReferralRecord.java`
- Create: `c-service/src/main/java/com/parttime/cservice/pojo/entity/ReferralReward.java`
- Create: `c-service/src/main/java/com/parttime/cservice/pojo/entity/ReferralConfig.java`
- Create: `c-service/src/main/java/com/parttime/cservice/pojo/vo/ReferralLinkVO.java`
- Create: `c-service/src/main/java/com/parttime/cservice/pojo/vo/ReferralStatsVO.java`
- Create: `c-service/src/main/java/com/parttime/cservice/pojo/vo/RefereeVO.java`
- Create: `c-service/src/main/java/com/parttime/cservice/pojo/vo/ReferralRewardVO.java`

- [ ] **Create ReferralCode entity**

```java
package com.parttime.cservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReferralCode {
    private Long id;
    private Long workerId;
    private String code;
    private LocalDateTime createdAt;
}
```

- [ ] **Create ReferralRecord entity**

```java
package com.parttime.cservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReferralRecord {
    private Long id;
    private Long referrerId;
    private Long refereeId;
    private String referralCode;
    private LocalDateTime boundAt;
}
```

- [ ] **Create ReferralReward entity**

```java
package com.parttime.cservice.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReferralReward {
    private Long id;
    private Long referralRecordId;
    private BigDecimal amount;
    private String status;       // PENDING, AUDITING, GRANTED, REJECTED, FAILED
    private String auditRemark;
    private LocalDateTime grantedAt;
    private LocalDateTime createdAt;
}
```

- [ ] **Create ReferralConfig entity**

```java
package com.parttime.cservice.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReferralConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime updatedAt;
}
```

- [ ] **Create ReferralLinkVO**

```java
package com.parttime.cservice.pojo.vo;

import lombok.Data;

@Data
public class ReferralLinkVO {
    private String code;
    private String link;
}
```

- [ ] **Create ReferralStatsVO**

```java
package com.parttime.cservice.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReferralStatsVO {
    private int totalReferees;
    private BigDecimal totalRewardAmount;
    private BigDecimal pendingRewardAmount;
}
```

- [ ] **Create RefereeVO**

```java
package com.parttime.cservice.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RefereeVO {
    private Long id;
    private String name;
    private String phone;
    private int workCount;
    private BigDecimal workHours;
    private String rewardStatus;  // PENDING, AUDITING, GRANTED, REJECTED, NOT_QUALIFIED
    private String boundAt;
}
```

- [ ] **Create ReferralRewardVO**

```java
package com.parttime.cservice.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReferralRewardVO {
    private Long id;
    private String refereeName;
    private BigDecimal amount;
    private String status;
    private String createdAt;
    private String grantedAt;
}
```

- [ ] **Commit**

```bash
git add c-service/src/main/java/com/parttime/cservice/pojo/entity/ReferralCode.java c-service/src/main/java/com/parttime/cservice/pojo/entity/ReferralRecord.java c-service/src/main/java/com/parttime/cservice/pojo/entity/ReferralReward.java c-service/src/main/java/com/parttime/cservice/pojo/entity/ReferralConfig.java c-service/src/main/java/com/parttime/cservice/pojo/vo/ReferralLinkVO.java c-service/src/main/java/com/parttime/cservice/pojo/vo/ReferralStatsVO.java c-service/src/main/java/com/parttime/cservice/pojo/vo/RefereeVO.java c-service/src/main/java/com/parttime/cservice/pojo/vo/ReferralRewardVO.java
git commit -m "feat(c-service): add referral reward entities and VOs"
```

---

### Task 3: c-service Mappers + XML

**Files:**
- Create: `c-service/src/main/java/com/parttime/cservice/mapper/ReferralCodeMapper.java`
- Create: `c-service/src/main/java/com/parttime/cservice/mapper/ReferralRecordMapper.java`
- Create: `c-service/src/main/java/com/parttime/cservice/mapper/ReferralRewardMapper.java`
- Create: `c-service/src/main/java/com/parttime/cservice/mapper/ReferralConfigMapper.java`
- Create: `c-service/src/main/resources/mapper/ReferralCodeMapper.xml`
- Create: `c-service/src/main/resources/mapper/ReferralRecordMapper.xml`
- Create: `c-service/src/main/resources/mapper/ReferralRewardMapper.xml`
- Create: `c-service/src/main/resources/mapper/ReferralConfigMapper.xml`

- [ ] **Create ReferralCodeMapper interface**

```java
package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.ReferralCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReferralCodeMapper {

    ReferralCode findByWorkerId(@Param("workerId") Long workerId);

    ReferralCode findByCode(@Param("code") String code);

    void insert(ReferralCode referralCode);
}
```

- [ ] **Create ReferralCodeMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.cservice.mapper.ReferralCodeMapper">

    <resultMap id="ReferralCodeResultMap" type="com.parttime.cservice.pojo.entity.ReferralCode">
        <id property="id" column="id"/>
        <result property="workerId" column="worker_id"/>
        <result property="code" column="code"/>
        <result property="createdAt" column="created_at"/>
    </resultMap>

    <select id="findByWorkerId" resultMap="ReferralCodeResultMap">
        SELECT * FROM referral_code WHERE worker_id = #{workerId}
    </select>

    <select id="findByCode" resultMap="ReferralCodeResultMap">
        SELECT * FROM referral_code WHERE code = #{code}
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO referral_code (worker_id, code) VALUES (#{workerId}, #{code})
    </insert>

</mapper>
```

- [ ] **Create ReferralRecordMapper interface**

```java
package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.ReferralRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReferralRecordMapper {

    ReferralRecord findByRefereeId(@Param("refereeId") Long refereeId);

    List<ReferralRecord> findByReferrerId(@Param("referrerId") Long referrerId);

    void insert(ReferralRecord referralRecord);

    int countByReferrerId(@Param("referrerId") Long referrerId);
}
```

- [ ] **Create ReferralRecordMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.cservice.mapper.ReferralRecordMapper">

    <resultMap id="ReferralRecordResultMap" type="com.parttime.cservice.pojo.entity.ReferralRecord">
        <id property="id" column="id"/>
        <result property="referrerId" column="referrer_id"/>
        <result property="refereeId" column="referee_id"/>
        <result property="referralCode" column="referral_code"/>
        <result property="boundAt" column="bound_at"/>
    </resultMap>

    <select id="findByRefereeId" resultMap="ReferralRecordResultMap">
        SELECT * FROM referral_record WHERE referee_id = #{refereeId}
    </select>

    <select id="findByReferrerId" resultMap="ReferralRecordResultMap">
        SELECT * FROM referral_record WHERE referrer_id = #{referrerId} ORDER BY bound_at DESC
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO referral_record (referrer_id, referee_id, referral_code) VALUES (#{referrerId}, #{refereeId}, #{referralCode})
    </insert>

    <select id="countByReferrerId" resultType="int">
        SELECT COUNT(*) FROM referral_record WHERE referrer_id = #{referrerId}
    </select>

</mapper>
```

- [ ] **Create ReferralRewardMapper interface**

```java
package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.ReferralReward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReferralRewardMapper {

    ReferralReward findByReferralRecordId(@Param("referralRecordId") Long referralRecordId);

    List<ReferralReward> findByReferrerIdPage(@Param("referrerId") Long referrerId,
                                               @Param("offset") int offset,
                                               @Param("pageSize") int pageSize);

    long countByReferrerId(@Param("referrerId") Long referrerId);

    void insert(ReferralReward referralReward);

    void updateStatus(@Param("id") Long id,
                      @Param("status") String status,
                      @Param("auditRemark") String auditRemark);
}
```

- [ ] **Create ReferralRewardMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.cservice.mapper.ReferralRewardMapper">

    <resultMap id="ReferralRewardResultMap" type="com.parttime.cservice.pojo.entity.ReferralReward">
        <id property="id" column="id"/>
        <result property="referralRecordId" column="referral_record_id"/>
        <result property="amount" column="amount"/>
        <result property="status" column="status"/>
        <result property="auditRemark" column="audit_remark"/>
        <result property="grantedAt" column="granted_at"/>
        <result property="createdAt" column="created_at"/>
    </resultMap>

    <select id="findByReferralRecordId" resultMap="ReferralRewardResultMap">
        SELECT * FROM referral_reward WHERE referral_record_id = #{referralRecordId}
    </select>

    <select id="findByReferrerIdPage" resultMap="ReferralRewardResultMap">
        SELECT rr.* FROM referral_reward rr
        INNER JOIN referral_record r ON rr.referral_record_id = r.id
        WHERE r.referrer_id = #{referrerId}
        ORDER BY rr.created_at DESC
        LIMIT #{pageSize} OFFSET #{offset}
    </select>

    <select id="countByReferrerId" resultType="long">
        SELECT COUNT(*) FROM referral_reward rr
        INNER JOIN referral_record r ON rr.referral_record_id = r.id
        WHERE r.referrer_id = #{referrerId}
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO referral_reward (referral_record_id, amount, status) VALUES (#{referralRecordId}, #{amount}, #{status})
    </insert>

    <update id="updateStatus">
        UPDATE referral_reward SET status = #{status}, audit_remark = #{auditRemark} WHERE id = #{id}
    </update>

</mapper>
```

- [ ] **Create ReferralConfigMapper interface**

```java
package com.parttime.cservice.mapper;

import com.parttime.cservice.pojo.entity.ReferralConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReferralConfigMapper {

    List<ReferralConfig> findAll();

    ReferralConfig findByKey(@Param("configKey") String configKey);

    void upsert(@Param("configKey") String configKey,
                @Param("configValue") String configValue,
                @Param("description") String description);
}
```

- [ ] **Create ReferralConfigMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.cservice.mapper.ReferralConfigMapper">

    <resultMap id="ReferralConfigResultMap" type="com.parttime.cservice.pojo.entity.ReferralConfig">
        <id property="id" column="id"/>
        <result property="configKey" column="config_key"/>
        <result property="configValue" column="config_value"/>
        <result property="description" column="description"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>

    <select id="findAll" resultMap="ReferralConfigResultMap">
        SELECT * FROM referral_config
    </select>

    <select id="findByKey" resultMap="ReferralConfigResultMap">
        SELECT * FROM referral_config WHERE config_key = #{configKey}
    </select>

    <insert id="upsert">
        INSERT INTO referral_config (config_key, config_value, description)
        VALUES (#{configKey}, #{configValue}, #{description})
        ON DUPLICATE KEY UPDATE config_value = #{configValue}
    </insert>

</mapper>
```

- [ ] **Commit**

```bash
git add c-service/src/main/java/com/parttime/cservice/mapper/ReferralCodeMapper.java c-service/src/main/java/com/parttime/cservice/mapper/ReferralRecordMapper.java c-service/src/main/java/com/parttime/cservice/mapper/ReferralRewardMapper.java c-service/src/main/java/com/parttime/cservice/mapper/ReferralConfigMapper.java c-service/src/main/resources/mapper/ReferralCodeMapper.xml c-service/src/main/resources/mapper/ReferralRecordMapper.xml c-service/src/main/resources/mapper/ReferralRewardMapper.xml c-service/src/main/resources/mapper/ReferralConfigMapper.xml
git commit -m "feat(c-service): add referral reward mappers and XML"
```

---

### Task 4: c-service Service Interface + Implementation

**Files:**
- Create: `c-service/src/main/java/com/parttime/cservice/service/ReferralService.java`
- Create: `c-service/src/main/java/com/parttime/cservice/service/impl/ReferralServiceImpl.java`

- [ ] **Create ReferralService interface**

```java
package com.parttime.cservice.service;

import com.parttime.cservice.pojo.vo.ReferralLinkVO;
import com.parttime.cservice.pojo.vo.ReferralStatsVO;
import com.parttime.cservice.pojo.vo.RefereeVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.ReferralRewardVO;
import com.parttime.cservice.pojo.entity.ReferralConfig;

import java.util.List;

public interface ReferralService {

    ReferralLinkVO getReferralLink(Long workerId);

    String getReferralPoster(Long workerId);

    void bindReferral(Long refereeId, String code);

    void checkAndGrantReward(Long refereeId);

    ReferralStatsVO getReferralStats(Long workerId);

    PageVO<RefereeVO> getReferees(Long workerId, int page, int pageSize);

    PageVO<ReferralRewardVO> getReferralRewards(Long workerId, int page, int pageSize);

    List<ReferralConfig> getConfig();

    void updateConfig(List<ReferralConfig> configs);
}
```

- [ ] **Create ReferralServiceImpl**

```java
package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.ReferralCodeMapper;
import com.parttime.cservice.mapper.ReferralRecordMapper;
import com.parttime.cservice.mapper.ReferralRewardMapper;
import com.parttime.cservice.mapper.ReferralConfigMapper;
import com.parttime.cservice.pojo.entity.ReferralCode;
import com.parttime.cservice.pojo.entity.ReferralRecord;
import com.parttime.cservice.pojo.entity.ReferralReward;
import com.parttime.cservice.pojo.entity.ReferralConfig;
import com.parttime.cservice.pojo.vo.ReferralLinkVO;
import com.parttime.cservice.pojo.vo.ReferralStatsVO;
import com.parttime.cservice.pojo.vo.RefereeVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.ReferralRewardVO;
import com.parttime.cservice.service.ReferralService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReferralServiceImpl implements ReferralService {

    @Resource
    private ReferralCodeMapper referralCodeMapper;

    @Resource
    private ReferralRecordMapper referralRecordMapper;

    @Resource
    private ReferralRewardMapper referralRewardMapper;

    @Resource
    private ReferralConfigMapper referralConfigMapper;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final DateTimeFormatter BEIJING_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ReferralLinkVO getReferralLink(Long workerId) {
        ReferralCode code = referralCodeMapper.findByWorkerId(workerId);
        if (code == null) {
            code = generateReferralCode(workerId);
        }
        ReferralLinkVO vo = new ReferralLinkVO();
        vo.setCode(code.getCode());
        vo.setLink("https://worker.example.com/invite?code=" + code.getCode());
        return vo;
    }

    @Override
    public String getReferralPoster(Long workerId) {
        ReferralCode code = referralCodeMapper.findByWorkerId(workerId);
        if (code == null) {
            code = generateReferralCode(workerId);
        }
        // TODO: 生成海报图片，返回 URL
        return "https://cdn.example.com/poster/" + code.getCode() + ".jpg";
    }

    @Override
    @Transactional
    public void bindReferral(Long refereeId, String code) {
        if (code == null || code.isEmpty()) {
            return;
        }

        ReferralCode referralCode = referralCodeMapper.findByCode(code);
        if (referralCode == null) {
            throw new RuntimeException("邀请码无效");
        }

        if (referralCode.getWorkerId().equals(refereeId)) {
            throw new RuntimeException("不能邀请自己");
        }

        ReferralRecord existing = referralRecordMapper.findByRefereeId(refereeId);
        if (existing != null) {
            throw new RuntimeException("您已绑定邀请人");
        }

        ReferralRecord record = new ReferralRecord();
        record.setReferrerId(referralCode.getWorkerId());
        record.setRefereeId(refereeId);
        record.setReferralCode(code);
        referralRecordMapper.insert(record);
    }

    @Override
    @Transactional
    public void checkAndGrantReward(Long refereeId) {
        ReferralRecord record = referralRecordMapper.findByRefereeId(refereeId);
        if (record == null) {
            return;
        }

        ReferralReward existing = referralRewardMapper.findByReferralRecordId(record.getId());
        if (existing != null) {
            return;
        }

        List<ReferralConfig> configs = referralConfigMapper.findAll();
        int validDays = getConfigValue(configs, "valid_days", 30);
        int minWorkCount = getConfigValue(configs, "min_work_count", 3);
        BigDecimal minIncome = getConfigDecimalValue(configs, "min_income", BigDecimal.ZERO);
        boolean needAudit = Boolean.parseBoolean(getConfigStringValue(configs, "need_audit", "true"));

        if (record.getBoundAt().plusDays(validDays).isBefore(LocalDateTime.now())) {
            return;
        }

        // TODO: 查询被邀请人打工次数和收入
        int workCount = 0;
        BigDecimal income = BigDecimal.ZERO;

        if (workCount < minWorkCount || income.compareTo(minIncome) < 0) {
            return;
        }

        BigDecimal rewardAmount = getConfigDecimalValue(configs, "reward_amount", new BigDecimal("20"));

        ReferralReward reward = new ReferralReward();
        reward.setReferralRecordId(record.getId());
        reward.setAmount(rewardAmount);
        reward.setStatus(needAudit ? "PENDING" : "GRANTED");
        if (!needAudit) {
            reward.setGrantedAt(LocalDateTime.now());
        }
        referralRewardMapper.insert(reward);
    }

    @Override
    public ReferralStatsVO getReferralStats(Long workerId) {
        int totalReferees = referralRecordMapper.countByReferrerId(workerId);
        List<ReferralRecord> records = referralRecordMapper.findByReferrerId(workerId);

        BigDecimal totalRewardAmount = BigDecimal.ZERO;
        BigDecimal pendingRewardAmount = BigDecimal.ZERO;

        for (ReferralRecord record : records) {
            ReferralReward reward = referralRewardMapper.findByReferralRecordId(record.getId());
            if (reward != null) {
                totalRewardAmount = totalRewardAmount.add(reward.getAmount());
                if ("PENDING".equals(reward.getStatus()) || "AUDITING".equals(reward.getStatus())) {
                    pendingRewardAmount = pendingRewardAmount.add(reward.getAmount());
                }
            }
        }

        ReferralStatsVO vo = new ReferralStatsVO();
        vo.setTotalReferees(totalReferees);
        vo.setTotalRewardAmount(totalRewardAmount);
        vo.setPendingRewardAmount(pendingRewardAmount);
        return vo;
    }

    @Override
    public PageVO<RefereeVO> getReferees(Long workerId, int page, int pageSize) {
        List<ReferralRecord> records = referralRecordMapper.findByReferrerId(workerId);
        long total = records.size();

        int offset = (page - 1) * pageSize;
        List<RefereeVO> list = records.stream()
                .skip(offset)
                .limit(pageSize)
                .map(record -> {
                    RefereeVO vo = new RefereeVO();
                    vo.setId(record.getRefereeId());
                    vo.setName("用户" + record.getRefereeId());
                    vo.setPhone("138****" + (record.getRefereeId() % 10000));
                    vo.setWorkCount(0);
                    vo.setWorkHours(BigDecimal.ZERO);

                    ReferralReward reward = referralRewardMapper.findByReferralRecordId(record.getId());
                    vo.setRewardStatus(reward != null ? reward.getStatus() : "NOT_QUALIFIED");

                    vo.setBoundAt(record.getBoundAt() != null ? record.getBoundAt().format(BEIJING_FMT) : "");
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageVO<>(list, total);
    }

    @Override
    public PageVO<ReferralRewardVO> getReferralRewards(Long workerId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ReferralReward> rewards = referralRewardMapper.findByReferrerIdPage(workerId, offset, pageSize);
        long total = referralRewardMapper.countByReferrerId(workerId);

        List<ReferralRewardVO> list = rewards.stream().map(reward -> {
            ReferralRewardVO vo = new ReferralRewardVO();
            vo.setId(reward.getId());
            vo.setAmount(reward.getAmount());
            vo.setStatus(reward.getStatus());
            vo.setCreatedAt(reward.getCreatedAt() != null ? reward.getCreatedAt().format(BEIJING_FMT) : "");
            vo.setGrantedAt(reward.getGrantedAt() != null ? reward.getGrantedAt().format(BEIJING_FMT) : "");
            return vo;
        }).collect(Collectors.toList());

        return new PageVO<>(list, total);
    }

    @Override
    public List<ReferralConfig> getConfig() {
        return referralConfigMapper.findAll();
    }

    @Override
    @Transactional
    public void updateConfig(List<ReferralConfig> configs) {
        for (ReferralConfig config : configs) {
            referralConfigMapper.upsert(config.getConfigKey(), config.getConfigValue(), config.getDescription());
        }
    }

    private ReferralCode generateReferralCode(Long workerId) {
        String code;
        do {
            code = generateRandomCode();
        } while (referralCodeMapper.findByCode(code) != null);

        ReferralCode referralCode = new ReferralCode();
        referralCode.setWorkerId(workerId);
        referralCode.setCode(code);
        referralCodeMapper.insert(referralCode);
        return referralCode;
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    private int getConfigValue(List<ReferralConfig> configs, String key, int defaultValue) {
        return configs.stream()
                .filter(c -> key.equals(c.getConfigKey()))
                .map(c -> Integer.parseInt(c.getConfigValue()))
                .findFirst()
                .orElse(defaultValue);
    }

    private BigDecimal getConfigDecimalValue(List<ReferralConfig> configs, String key, BigDecimal defaultValue) {
        return configs.stream()
                .filter(c -> key.equals(c.getConfigKey()))
                .map(c -> new BigDecimal(c.getConfigValue()))
                .findFirst()
                .orElse(defaultValue);
    }

    private String getConfigStringValue(List<ReferralConfig> configs, String key, String defaultValue) {
        return configs.stream()
                .filter(c -> key.equals(c.getConfigKey()))
                .map(ReferralConfig::getConfigValue)
                .findFirst()
                .orElse(defaultValue);
    }
}
```

- [ ] **Commit**

```bash
git add c-service/src/main/java/com/parttime/cservice/service/ReferralService.java c-service/src/main/java/com/parttime/cservice/service/impl/ReferralServiceImpl.java
git commit -m "feat(c-service): add referral service interface and implementation"
```

---

### Task 5: c-service Controller

**Files:**
- Create: `c-service/src/main/java/com/parttime/cservice/controller/ReferralController.java`

- [ ] **Create ReferralController**

```java
package com.parttime.cservice.controller;

import com.parttime.cservice.config.JwtUtil;
import com.parttime.cservice.pojo.vo.ReferralLinkVO;
import com.parttime.cservice.pojo.vo.ReferralStatsVO;
import com.parttime.cservice.pojo.vo.RefereeVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.ReferralRewardVO;
import com.parttime.cservice.pojo.entity.ReferralConfig;
import com.parttime.cservice.service.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/referral")
public class ReferralController {

    @Resource
    private ReferralService referralService;

    @Resource
    private JwtUtil jwtUtil;

    @Operation(summary = "获取邀请链接")
    @GetMapping("/link")
    public ReferralLinkVO getReferralLink(HttpServletRequest request) {
        Long workerId = jwtUtil.getUserIdFromRequest(request);
        return referralService.getReferralLink(workerId);
    }

    @Operation(summary = "获取邀请海报")
    @GetMapping("/poster")
    public Map<String, String> getReferralPoster(HttpServletRequest request) {
        Long workerId = jwtUtil.getUserIdFromRequest(request);
        String posterUrl = referralService.getReferralPoster(workerId);
        return Map.of("posterUrl", posterUrl);
    }

    @Operation(summary = "邀请统计概览")
    @GetMapping("/stats")
    public ReferralStatsVO getReferralStats(HttpServletRequest request) {
        Long workerId = jwtUtil.getUserIdFromRequest(request);
        return referralService.getReferralStats(workerId);
    }

    @Operation(summary = "被邀请人列表")
    @GetMapping("/referees")
    public PageVO<RefereeVO> getReferees(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long workerId = jwtUtil.getUserIdFromRequest(request);
        return referralService.getReferees(workerId, page, pageSize);
    }

    @Operation(summary = "我的邀请奖励列表")
    @GetMapping("/rewards")
    public PageVO<ReferralRewardVO> getReferralRewards(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long workerId = jwtUtil.getUserIdFromRequest(request);
        return referralService.getReferralRewards(workerId, page, pageSize);
    }

    @Operation(summary = "获取奖励规则配置")
    @GetMapping("/config")
    public List<ReferralConfig> getConfig() {
        return referralService.getConfig();
    }

    @Operation(summary = "修改奖励规则配置")
    @PutMapping("/config")
    public void updateConfig(@RequestBody List<ReferralConfig> configs) {
        referralService.updateConfig(configs);
    }
}
```

- [ ] **Commit**

```bash
git add c-service/src/main/java/com/parttime/cservice/controller/ReferralController.java
git commit -m "feat(c-service): add referral controller"
```

---

### Task 6: platform-service Entities + Mappers + Controller

**Files:**
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/entity/ReferralConfig.java`
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/entity/ReferralReward.java`
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/entity/ReferralRecord.java`
- Create: `platform-service/src/main/java/com/parttime/platform/mapper/ReferralConfigMapper.java`
- Create: `platform-service/src/main/java/com/parttime/platform/mapper/ReferralRewardMapper.java`
- Create: `platform-service/src/main/java/com/parttime/platform/mapper/ReferralRecordMapper.java`
- Create: `platform-service/src/main/resources/mapper/ReferralConfigMapper.xml`
- Create: `platform-service/src/main/resources/mapper/ReferralRewardMapper.xml`
- Create: `platform-service/src/main/resources/mapper/ReferralRecordMapper.xml`
- Create: `platform-service/src/main/java/com/parttime/platform/service/ReferralService.java`
- Create: `platform-service/src/main/java/com/parttime/platform/service/impl/ReferralServiceImpl.java`
- Create: `platform-service/src/main/java/com/parttime/platform/controller/ReferralController.java`

- [ ] **Create entities** (same as c-service, different package)

```java
package com.parttime.platform.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReferralConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime updatedAt;
}

@Data
public class ReferralReward {
    private Long id;
    private Long referralRecordId;
    private BigDecimal amount;
    private String status;
    private String auditRemark;
    private LocalDateTime grantedAt;
    private LocalDateTime createdAt;
}

@Data
public class ReferralRecord {
    private Long id;
    private Long referrerId;
    private Long refereeId;
    private String referralCode;
    private LocalDateTime boundAt;
}
```

- [ ] **Create Mappers + XML** (same pattern as c-service)

- [ ] **Create ReferralService interface**

```java
package com.parttime.platform.service;

import com.parttime.platform.pojo.entity.ReferralConfig;
import com.parttime.platform.pojo.entity.ReferralReward;
import com.parttime.platform.vo.ReferralAuditVO;
import com.parttime.platform.vo.PageVO;

import java.util.List;

public interface ReferralService {

    List<ReferralConfig> getConfig();

    void updateConfig(List<ReferralConfig> configs);

    PageVO<ReferralAuditVO> getAuditList(int page, int pageSize);

    void approveReward(Long rewardId, String remark);

    void rejectReward(Long rewardId, String remark);
}
```

- [ ] **Create ReferralServiceImpl**

```java
package com.parttime.platform.service.impl;

import com.parttime.platform.mapper.ReferralConfigMapper;
import com.parttime.platform.mapper.ReferralRewardMapper;
import com.parttime.platform.mapper.ReferralRecordMapper;
import com.parttime.platform.pojo.entity.ReferralConfig;
import com.parttime.platform.pojo.entity.ReferralReward;
import com.parttime.platform.service.ReferralService;
import com.parttime.platform.vo.ReferralAuditVO;
import com.parttime.platform.vo.PageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReferralServiceImpl implements ReferralService {

    @Resource
    private ReferralConfigMapper referralConfigMapper;

    @Resource
    private ReferralRewardMapper referralRewardMapper;

    @Resource
    private ReferralRecordMapper referralRecordMapper;

    private static final DateTimeFormatter BEIJING_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<ReferralConfig> getConfig() {
        return referralConfigMapper.findAll();
    }

    @Override
    @Transactional
    public void updateConfig(List<ReferralConfig> configs) {
        for (ReferralConfig config : configs) {
            referralConfigMapper.upsert(config.getConfigKey(), config.getConfigValue(), config.getDescription());
        }
    }

    @Override
    public PageVO<ReferralAuditVO> getAuditList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ReferralReward> rewards = referralRewardMapper.findByStatusPage("PENDING", offset, pageSize);
        long total = referralRewardMapper.countByStatus("PENDING");

        List<ReferralAuditVO> list = rewards.stream().map(reward -> {
            ReferralAuditVO vo = new ReferralAuditVO();
            vo.setId(reward.getId());
            vo.setAmount(reward.getAmount());
            vo.setStatus(reward.getStatus());
            vo.setCreatedAt(reward.getCreatedAt() != null ? reward.getCreatedAt().format(BEIJING_FMT) : "");
            return vo;
        }).collect(Collectors.toList());

        return new PageVO<>(list, total);
    }

    @Override
    @Transactional
    public void approveReward(Long rewardId, String remark) {
        ReferralReward reward = referralRewardMapper.findById(rewardId);
        if (reward == null) {
            throw new RuntimeException("奖励记录不存在");
        }
        if (!"PENDING".equals(reward.getStatus()) && !"AUDITING".equals(reward.getStatus())) {
            throw new RuntimeException("当前状态无法审核");
        }
        referralRewardMapper.updateStatus(rewardId, "GRANTED", remark);
        referralRewardMapper.updateGrantedAt(rewardId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void rejectReward(Long rewardId, String remark) {
        ReferralReward reward = referralRewardMapper.findById(rewardId);
        if (reward == null) {
            throw new RuntimeException("奖励记录不存在");
        }
        if (!"PENDING".equals(reward.getStatus()) && !"AUDITING".equals(reward.getStatus())) {
            throw new RuntimeException("当前状态无法审核");
        }
        referralRewardMapper.updateStatus(rewardId, "REJECTED", remark);
    }
}
```

- [ ] **Create ReferralController**

```java
package com.parttime.platform.controller;

import com.parttime.platform.pojo.entity.ReferralConfig;
import com.parttime.platform.service.ReferralService;
import com.parttime.platform.vo.ReferralAuditVO;
import com.parttime.platform.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/referral")
public class ReferralController {

    @Resource
    private ReferralService referralService;

    @Operation(summary = "获取奖励规则配置")
    @GetMapping("/config")
    public List<ReferralConfig> getConfig() {
        return referralService.getConfig();
    }

    @Operation(summary = "修改奖励规则配置")
    @PutMapping("/config")
    public void updateConfig(@RequestBody List<ReferralConfig> configs) {
        referralService.updateConfig(configs);
    }

    @Operation(summary = "待审核奖励列表")
    @GetMapping("/audit/list")
    public PageVO<ReferralAuditVO> getAuditList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return referralService.getAuditList(page, pageSize);
    }

    @Operation(summary = "审核通过")
    @PostMapping("/audit/{id}/approve")
    public void approveReward(@PathVariable Long id, @RequestBody Map<String, String> body) {
        referralService.approveReward(id, body.get("remark"));
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/audit/{id}/reject")
    public void rejectReward(@PathVariable Long id, @RequestBody Map<String, String> body) {
        referralService.rejectReward(id, body.get("remark"));
    }
}
```

- [ ] **Commit**

```bash
git add platform-service/src/main/java/com/parttime/platform/pojo/entity/ReferralConfig.java platform-service/src/main/java/com/parttime/platform/pojo/entity/ReferralReward.java platform-service/src/main/java/com/parttime/platform/pojo/entity/ReferralRecord.java platform-service/src/main/java/com/parttime/platform/mapper/ReferralConfigMapper.java platform-service/src/main/java/com/parttime/platform/mapper/ReferralRewardMapper.java platform-service/src/main/java/com/parttime/platform/mapper/ReferralRecordMapper.java platform-service/src/main/resources/mapper/ReferralConfigMapper.xml platform-service/src/main/resources/mapper/ReferralRewardMapper.xml platform-service/src/main/resources/mapper/ReferralRecordMapper.xml platform-service/src/main/java/com/parttime/platform/service/ReferralService.java platform-service/src/main/java/com/parttime/platform/service/impl/ReferralServiceImpl.java platform-service/src/main/java/com/parttime/platform/controller/ReferralController.java
git commit -m "feat(platform-service): add referral config and audit controller"
```

---

### Task 7: worker-uniapp API + Pages

**Files:**
- Create: `worker-uniapp/src/api/referral.js`
- Create: `worker-uniapp/src/pages/referral/referral.vue`
- Create: `worker-uniapp/src/pages/referral/referralRecords.vue`
- Modify: `worker-uniapp/src/pages.json`

- [ ] **Create referral API module**

```javascript
// worker-uniapp/src/api/referral.js
import { request } from './request'

export function getReferralLink() {
  return request('GET', '/referral/link')
}

export function getReferralPoster() {
  return request('GET', '/referral/poster')
}

export function getReferralStats() {
  return request('GET', '/referral/stats')
}

export function getReferees(params) {
  return request('GET', '/referral/referees', params)
}

export function getReferralRewards(params) {
  return request('GET', '/referral/rewards', params)
}
```

- [ ] **Create referral.vue page**

```vue
<script setup>
import { ref, onShow } from 'vue'
import { getReferralLink, getReferralPoster, getReferralStats } from '@/api/referral'

const referralLink = ref('')
const referralCode = ref('')
const posterUrl = ref('')
const stats = ref({
  totalReferees: 0,
  totalRewardAmount: 0,
  pendingRewardAmount: 0
})

async function fetchReferralInfo() {
  try {
    const linkRes = await getReferralLink()
    referralLink.value = linkRes.link
    referralCode.value = linkRes.code

    const posterRes = await getReferralPoster()
    posterUrl.value = posterRes.posterUrl

    const statsRes = await getReferralStats()
    stats.value = statsRes
  } catch {}
}

function copyLink() {
  uni.setClipboardData({
    data: referralLink.value,
    success: () => {
      uni.showToast({ title: '链接已复制', icon: 'success' })
    }
  })
}

function sharePoster() {
  // TODO: 分享海报到微信
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}

onShow(() => {
  fetchReferralInfo()
})
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">邀请好友</text>
      <text class="subtitle">邀请好友加入平台，获得奖励</text>
    </view>

    <view class="stats-card">
      <view class="stat-item">
        <text class="stat-value">{{ stats.totalReferees }}</text>
        <text class="stat-label">邀请人数</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.totalRewardAmount }}</text>
        <text class="stat-label">累计奖励(元)</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.pendingRewardAmount }}</text>
        <text class="stat-label">待发放(元)</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">邀请链接</text>
      <view class="link-box">
        <text class="link-text">{{ referralLink }}</text>
        <button class="copy-btn" @click="copyLink">复制</button>
      </view>
    </view>

    <view class="section">
      <text class="section-title">邀请海报</text>
      <image v-if="posterUrl" :src="posterUrl" class="poster-image" mode="aspectFit" />
      <button class="share-btn" @click="sharePoster">分享海报</button>
    </view>

    <view class="section">
      <navigator url="/pages/referral/referralRecords" class="records-link">
        <text>查看邀请记录</text>
        <text class="arrow">></text>
      </navigator>
    </view>
  </view>
</template>

<style>
.page { min-height: 100vh; background: #f5f5f5; padding: 16rpx; }
.header { text-align: center; padding: 40rpx 0; }
.title { font-size: 36rpx; font-weight: 600; color: #333; display: block; }
.subtitle { font-size: 26rpx; color: #999; margin-top: 8rpx; display: block; }
.stats-card { background: #fff; border-radius: 16rpx; padding: 32rpx; display: flex; justify-content: space-around; margin-bottom: 16rpx; }
.stat-item { text-align: center; }
.stat-value { font-size: 40rpx; font-weight: 600; color: #409eff; display: block; }
.stat-label { font-size: 24rpx; color: #999; margin-top: 8rpx; display: block; }
.section { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.section-title { font-size: 28rpx; font-weight: 500; color: #333; margin-bottom: 16rpx; display: block; }
.link-box { display: flex; align-items: center; gap: 16rpx; }
.link-text { flex: 1; font-size: 24rpx; color: #666; background: #f5f5f5; padding: 16rpx; border-radius: 8rpx; word-break: break-all; }
.copy-btn { background: #409eff; color: #fff; border: none; border-radius: 8rpx; padding: 16rpx 32rpx; font-size: 26rpx; }
.poster-image { width: 100%; height: 400rpx; border-radius: 8rpx; margin-bottom: 16rpx; }
.share-btn { background: #67c23a; color: #fff; border: none; border-radius: 8rpx; padding: 24rpx; font-size: 28rpx; }
.records-link { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; }
.arrow { color: #999; }
</style>
```

- [ ] **Create referralRecords.vue page**

```vue
<script setup>
import { ref, onShow } from 'vue'
import { getReferees } from '@/api/referral'

const referees = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)

async function fetchReferees() {
  loading.value = true
  try {
    const res = await getReferees({ page: page.value, pageSize: pageSize.value })
    referees.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function onPageChange(e) {
  page.value = e
  fetchReferees()
}

function rewardStatusText(status) {
  const map = {
    PENDING: '待审核',
    AUDITING: '审核中',
    GRANTED: '已发放',
    REJECTED: '已拒绝',
    NOT_QUALIFIED: '未达标'
  }
  return map[status] || status
}

function rewardStatusColor(status) {
  const map = {
    PENDING: '#e6a23c',
    AUDITING: '#909399',
    GRANTED: '#67c23a',
    REJECTED: '#f56c6c',
    NOT_QUALIFIED: '#c0c4cc'
  }
  return map[status] || '#909399'
}

onShow(() => {
  page.value = 1
  fetchReferees()
})
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="title">邀请记录</text>
      <text class="total">共 {{ total }} 人</text>
    </view>

    <view v-if="referees.length === 0 && !loading" class="empty">
      <text>暂无邀请记录</text>
    </view>

    <view v-for="item in referees" :key="item.id" class="record-item">
      <view class="record-left">
        <text class="record-name">{{ item.name }}</text>
        <text class="record-phone">{{ item.phone }}</text>
        <text class="record-time">邀请时间: {{ item.boundAt }}</text>
      </view>
      <view class="record-right">
        <view class="work-info">
          <text class="work-count">打工 {{ item.workCount }} 次</text>
          <text class="work-hours">工时 {{ item.workHours }} h</text>
        </view>
        <text class="reward-status" :style="{ color: rewardStatusColor(item.rewardStatus) }">
          {{ rewardStatusText(item.rewardStatus) }}
        </text>
      </view>
    </view>

    <uni-load-more v-if="total > pageSize" :status="page * pageSize >= total ? 'noMore' : 'more'" />
  </view>
</template>

<style>
.page { min-height: 100vh; background: #f5f5f5; padding: 16rpx; }
.header { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; }
.title { font-size: 32rpx; font-weight: 600; color: #333; }
.total { font-size: 26rpx; color: #999; }
.empty { text-align: center; padding: 100rpx 0; color: #999; }
.record-item { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 8rpx; display: flex; justify-content: space-between; }
.record-left { flex: 1; }
.record-name { font-size: 28rpx; font-weight: 500; color: #333; display: block; }
.record-phone { font-size: 24rpx; color: #999; margin-top: 4rpx; display: block; }
.record-time { font-size: 22rpx; color: #ccc; margin-top: 4rpx; display: block; }
.record-right { text-align: right; }
.work-info { margin-bottom: 8rpx; }
.work-count { font-size: 24rpx; color: #666; display: block; }
.work-hours { font-size: 24rpx; color: #666; display: block; }
.reward-status { font-size: 26rpx; font-weight: 500; }
</style>
```

- [ ] **Modify pages.json — add referral pages**

```json
{
  "path": "pages/referral/referral",
  "style": {
    "navigationBarTitleText": "邀请好友"
  }
},
{
  "path": "pages/referral/referralRecords",
  "style": {
    "navigationBarTitleText": "邀请记录"
  }
}
```

- [ ] **Commit**

```bash
git add worker-uniapp/src/api/referral.js worker-uniapp/src/pages/referral/referral.vue worker-uniapp/src/pages/referral/referralRecords.vue worker-uniapp/src/pages.json
git commit -m "feat(worker-uniapp): add referral pages"
```

---

### Task 8: Register Flow Integration

**Files:**
- Modify: `c-service/src/main/java/com/parttime/cservice/controller/AuthController.java`
- Modify: `worker-uniapp/src/pages/register/register.vue`

- [ ] **Modify register API to accept referral code**

In `AuthController.java`, add referral code parameter to register method:

```java
@PostMapping("/register")
public ApiResponse<?> register(@RequestBody Map<String, String> body, HttpServletResponse response) {
    String phone = body.get("phone");
    String password = body.get("password");
    String name = body.get("name");
    String referralCode = body.get("referralCode");

    // ... existing register logic ...

    // Bind referral if code provided
    if (referralCode != null && !referralCode.isEmpty()) {
        try {
            referralService.bindReferral(worker.getId(), referralCode);
        } catch (Exception e) {
            // Log error but don't fail registration
        }
    }

    // ... rest of existing logic ...
}
```

- [ ] **Modify register.vue to pass referral code**

```javascript
// In register.vue, extract referralCode from URL params
onLoad((options) => {
  if (options.code) {
    referralCode.value = options.code
  }
})

// Include referralCode in register request
const registerData = {
  phone: phone.value,
  password: password.value,
  name: name.value,
  referralCode: referralCode.value
}
```

- [ ] **Commit**

```bash
git add c-service/src/main/java/com/parttime/cservice/controller/AuthController.java worker-uniapp/src/pages/register/register.vue
git commit -m "feat: integrate referral code in register flow"
```

---

### Task 9: Sign-out Reward Check Integration

**Files:**
- Modify: `c-service/src/main/java/com/parttime/cservice/controller/AttendanceController.java`

- [ ] **Modify sign-out to check referral reward**

```java
// In sign-out method, after successful sign-out
@PostMapping("/clock-out")
public ApiResponse<?> clockOut(@RequestBody Map<String, Long> body) {
    Long recordId = body.get("recordId");
    // ... existing sign-out logic ...

    // Check referral reward for this worker
    referralService.checkAndGrantReward(workerId);

    return ApiResponse.success("签退成功");
}
```

- [ ] **Commit**

```bash
git add c-service/src/main/java/com/parttime/cservice/controller/AttendanceController.java
git commit -m "feat: add referral reward check on sign-out"
```

---

### Task 10: platform-pc Referral Config + Audit Pages

**Files:**
- Create: `platform-pc/src/api/referral.js`
- Create: `platform-pc/src/views/referral/ReferralConfig.vue`
- Create: `platform-pc/src/views/referral/ReferralAudit.vue`
- Modify: `platform-pc/src/router/index.js`

- [ ] **Create referral API module**

```javascript
// platform-pc/src/api/referral.js
import request from './request'

export function getConfig() {
  return request.get('/referral/config')
}

export function updateConfig(configs) {
  return request.put('/referral/config', configs)
}

export function getAuditList(params) {
  return request.get('/referral/audit/list', { params })
}

export function approveReward(id, remark) {
  return request.post(`/referral/audit/${id}/approve`, { remark })
}

export function rejectReward(id, remark) {
  return request.post(`/referral/audit/${id}/reject`, { remark })
}
```

- [ ] **Create ReferralConfig.vue**

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConfig, updateConfig } from '../../api/referral'

const configs = ref([])
const loading = ref(false)

async function fetchConfig() {
  loading.value = true
  try {
    configs.value = await getConfig()
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  try {
    await ElMessageBox.confirm('确定保存配置？', '提示')
    await updateConfig(configs.value)
    ElMessage.success('配置已保存')
  } catch {}
}

onMounted(() => {
  fetchConfig()
})
</script>

<template>
  <div class="config-page" v-loading="loading">
    <el-card>
      <template #header>
        <span>奖励规则配置</span>
      </template>

      <el-form label-width="180px">
        <el-form-item v-for="item in configs" :key="item.configKey" :label="item.description">
          <el-input v-if="item.configKey === 'need_audit'" v-model="item.configValue" placeholder="true/false" />
          <el-input v-else-if="item.configKey === 'release_method'" v-model="item.configValue" placeholder="auto/manual" />
          <el-input-number v-else v-model="item.configValue" :min="0" style="width: 200px" />
        </el-form-item>
      </el-form>

      <el-button type="primary" @click="handleSave">保存配置</el-button>
    </el-card>
  </div>
</template>

<style scoped>
.config-page { padding: 20px; }
</style>
```

- [ ] **Create ReferralAudit.vue**

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAuditList, approveReward, rejectReward } from '../../api/referral'

const auditList = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const rejectDialogVisible = ref(false)
const currentRewardId = ref(null)
const rejectRemark = ref('')

async function fetchAuditList() {
  loading.value = true
  try {
    const res = await getAuditList({ page: page.value, pageSize: pageSize.value })
    auditList.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function handleApprove(id) {
  try {
    await ElMessageBox.confirm('确定审核通过？', '提示')
    await approveReward(id, '审核通过')
    ElMessage.success('审核通过')
    fetchAuditList()
  } catch {}
}

function openRejectDialog(id) {
  currentRewardId.value = id
  rejectRemark.value = ''
  rejectDialogVisible.value = true
}

async function handleReject() {
  if (!rejectRemark.value) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  try {
    await rejectReward(currentRewardId.value, rejectRemark.value)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    fetchAuditList()
  } catch {}
}

onMounted(() => {
  fetchAuditList()
})
</script>

<template>
  <div class="audit-page" v-loading="loading">
    <el-card>
      <template #header>
        <span>奖励审核</span>
      </template>

      <el-table :data="auditList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="amount" label="奖励金额" width="120" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleApprove(row.id)">通过</el-button>
            <el-button type="danger" size="small" @click="openRejectDialog(row.id)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:page="page"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchAuditList"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="400px">
      <el-input v-model="rejectRemark" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.audit-page { padding: 20px; }
</style>
```

- [ ] **Modify router — add referral routes**

```js
// Add to routes array in platform-pc/src/router/index.js
{
  path: '/referral/config',
  name: 'ReferralConfig',
  component: () => import('../views/referral/ReferralConfig.vue'),
  meta: { requiresAuth: true, title: '奖励规则配置' }
},
{
  path: '/referral/audit',
  name: 'ReferralAudit',
  component: () => import('../views/referral/ReferralAudit.vue'),
  meta: { requiresAuth: true, title: '奖励审核' }
},
```

- [ ] **Commit**

```bash
git add platform-pc/src/api/referral.js platform-pc/src/views/referral/ReferralConfig.vue platform-pc/src/views/referral/ReferralAudit.vue platform-pc/src/router/index.js
git commit -m "feat(platform-pc): add referral config and audit pages"
```

---

### Task 11: Run Tests

- [ ] **Run c-service tests**

```bash
cd c-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test
```

- [ ] **Run platform-service tests**

```bash
cd platform-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test
```

- [ ] **Run worker-uniapp build**

```bash
cd worker-uniapp && npm run build:h5
```

- [ ] **Run platform-pc build**

```bash
cd platform-pc && npm run build
```

- [ ] **Final commit if all tests pass**

```bash
git add -A
git commit -m "feat: complete referral reward system"
```
