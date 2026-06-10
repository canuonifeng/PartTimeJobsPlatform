# 平台后台企业管理模块 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在平台管理后台（platform-pc）新增企业管理模块，支持编辑/停用已审核企业、管理企业下的登录账号，并在注册审核通过时自动创建企业记录。

**Architecture:** 统一在 platform-service 中管理企业与账号数据，新增 enterprises 和 enterprise_accounts 表（共享 MySQL）。前端复用现有 el-card + el-table + el-dialog 模式。企业端认证从内存改为从 enterprise_accounts 表读取。

**Tech Stack:** Spring Boot 3.2.5 + MyBatis + Flyway + Vue 3 + Element Plus

---

### Task 1: 数据库迁移 — 新增 enterprises 和 enterprise_accounts 表

**Files:**
- Create: `platform-service/src/main/resources/db/migration/V4__create_enterprise_tables.sql`
- Modify: `scripts/seed-data.sql`

- [ ] **Step 1: 创建 Flyway 迁移文件**

```sql
-- V4__create_enterprise_tables.sql
CREATE TABLE IF NOT EXISTS enterprises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(200) NOT NULL,
    contact_name VARCHAR(100),
    contact_phone VARCHAR(20),
    company_address VARCHAR(500),
    business_license VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / SUSPENDED',
    registration_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_registration_id (registration_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS enterprise_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enterprise_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT 'ADMIN/HR/MANAGER/FINANCE',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    INDEX idx_enterprise_id (enterprise_id),
    FOREIGN KEY (enterprise_id) REFERENCES enterprises(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: 在 seed-data.sql 末尾添加演示数据**

在 seed-data.sql 的 INSERT 语句末尾添加：
```sql
-- 通过审核的企业记录（手动补齐 4 家已在注册表中的企业）
INSERT INTO enterprises (id, company_name, contact_name, contact_phone, company_address, business_license, status, registration_id) VALUES
(1, '北京迅捷物流有限公司', '王经理', '13800138001', '北京市朝阳区建国路88号', 'BL-2024001', 'ACTIVE', 1),
(2, '上海丰盛餐饮管理有限公司', '李店长', '13900139002', '上海市浦东新区陆家嘴路100号', 'BL-2024002', 'ACTIVE', 2),
(3, '广州天汇商贸有限公司', '陈主管', '13700137003', '广州市天河区天河路200号', 'BL-2024003', 'ACTIVE', 3),
(4, '深圳创想科技有限公司', '张总', '13600136004', '深圳市南山区科技园路300号', 'BL-2024004', 'ACTIVE', 4);

-- 企业端登录账号（对应企业端原有的 4 个角色：admin/hr/manager/finance）
INSERT INTO enterprise_accounts (enterprise_id, username, password, display_name, role, status) VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '企业管理员', 'ADMIN', 'ACTIVE'),
(1, 'hr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '人力资源', 'HR', 'ACTIVE'),
(1, 'manager', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '运营经理', 'MANAGER', 'ACTIVE'),
(1, 'finance', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '财务', 'FINANCE', 'ACTIVE');
```

密码为 `admin123` 的 BCrypt 哈希，生产环境需替换。

- [ ] **Step 3: 提交**

```bash
git add platform-service/src/main/resources/db/migration/V4__create_enterprise_tables.sql scripts/seed-data.sql
git commit -m "feat: add enterprises and enterprise_accounts tables"
```

---

### Task 2: 后端实体和命令/视图对象（platform-service）

**Files:**
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/entity/Enterprise.java`
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/entity/EnterpriseAccount.java`
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/cmd/EnterpriseUpdateCmd.java`
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/cmd/EnterpriseAccountCreateCmd.java`
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/cmd/EnterpriseAccountUpdateCmd.java`
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/cmd/ResetPasswordCmd.java`
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/vo/EnterpriseVO.java`
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/vo/EnterpriseAccountVO.java`

- [ ] **Step 1: 创建 Enterprise.java**

```java
package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Enterprise {
    @Schema(description = "企业ID")
    private Long id;
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "联系人")
    private String contactName;
    @Schema(description = "联系电话")
    private String contactPhone;
    @Schema(description = "企业地址")
    private String companyAddress;
    @Schema(description = "营业执照")
    private String businessLicense;
    @Schema(description = "状态: ACTIVE/SUSPENDED")
    private String status;
    @Schema(description = "关联注册申请ID")
    private Long registrationId;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 EnterpriseAccount.java**

```java
package com.parttime.platform.pojo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnterpriseAccount {
    @Schema(description = "账号ID")
    private Long id;
    @Schema(description = "企业ID")
    private Long enterpriseId;
    @Schema(description = "登录名")
    private String username;
    @Schema(description = "密码(BCrypt)")
    private String password;
    @Schema(description = "显示名")
    private String displayName;
    @Schema(description = "角色: ADMIN/HR/MANAGER/FINANCE")
    private String role;
    @Schema(description = "状态: ACTIVE/DISABLED")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 3: 创建 Cmd 类**

```java
// EnterpriseUpdateCmd.java
package com.parttime.platform.pojo.cmd;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnterpriseUpdateCmd {
    @Schema(description = "企业ID")
    private Long id;
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "联系人")
    private String contactName;
    @Schema(description = "联系电话")
    private String contactPhone;
    @Schema(description = "企业地址")
    private String companyAddress;
    @Schema(description = "营业执照")
    private String businessLicense;
}
```

```java
// EnterpriseAccountCreateCmd.java
package com.parttime.platform.pojo.cmd;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnterpriseAccountCreateCmd {
    @Schema(description = "企业ID")
    private Long enterpriseId;
    @Schema(description = "登录名")
    private String username;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "显示名")
    private String displayName;
    @Schema(description = "角色: ADMIN/HR/MANAGER/FINANCE")
    private String role;
}
```

```java
// EnterpriseAccountUpdateCmd.java
package com.parttime.platform.pojo.cmd;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnterpriseAccountUpdateCmd {
    @Schema(description = "账号ID")
    private Long id;
    @Schema(description = "显示名")
    private String displayName;
    @Schema(description = "角色")
    private String role;
}
```

```java
// ResetPasswordCmd.java
package com.parttime.platform.pojo.cmd;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ResetPasswordCmd {
    @Schema(description = "账号ID")
    private Long id;
    @Schema(description = "新密码")
    private String newPassword;
}
```

- [ ] **Step 4: 创建 VO 类**

```java
// EnterpriseVO.java
package com.parttime.platform.pojo.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnterpriseVO {
    @Schema(description = "企业ID")
    private Long id;
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "联系人")
    private String contactName;
    @Schema(description = "联系电话")
    private String contactPhone;
    @Schema(description = "企业地址")
    private String companyAddress;
    @Schema(description = "营业执照")
    private String businessLicense;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "关联注册申请ID")
    private Long registrationId;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
```

```java
// EnterpriseAccountVO.java
package com.parttime.platform.pojo.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnterpriseAccountVO {
    @Schema(description = "账号ID")
    private Long id;
    @Schema(description = "企业ID")
    private Long enterpriseId;
    @Schema(description = "登录名")
    private String username;
    @Schema(description = "显示名")
    private String displayName;
    @Schema(description = "角色")
    private String role;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 5: 提交**

---

### Task 3: Mapper 层（platform-service）

**Files:**
- Create: `platform-service/src/main/java/com/parttime/platform/mapper/EnterpriseMapper.java`
- Create: `platform-service/src/main/java/com/parttime/platform/mapper/EnterpriseAccountMapper.java`
- Create: `platform-service/src/main/resources/mapper/EnterpriseMapper.xml`
- Create: `platform-service/src/main/resources/mapper/EnterpriseAccountMapper.xml`

- [ ] **Step 1: 创建 EnterpriseMapper.java**

```java
package com.parttime.platform.mapper;
import com.parttime.platform.pojo.entity.Enterprise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface EnterpriseMapper {
    List<Enterprise> findAll();
    List<Enterprise> findByStatus(@Param("status") String status);
    Optional<Enterprise> findById(@Param("id") Long id);
    int insert(Enterprise enterprise);
    int update(Enterprise enterprise);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
```

- [ ] **Step 2: 创建 EnterpriseMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.platform.mapper.EnterpriseMapper">
    <resultMap id="EnterpriseResultMap" type="com.parttime.platform.pojo.entity.Enterprise">
        <id property="id" column="id"/>
        <result property="companyName" column="company_name"/>
        <result property="contactName" column="contact_name"/>
        <result property="contactPhone" column="contact_phone"/>
        <result property="companyAddress" column="company_address"/>
        <result property="businessLicense" column="business_license"/>
        <result property="status" column="status"/>
        <result property="registrationId" column="registration_id"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>

    <select id="findAll" resultMap="EnterpriseResultMap">
        SELECT * FROM enterprises ORDER BY created_at DESC
    </select>

    <select id="findByStatus" resultMap="EnterpriseResultMap">
        SELECT * FROM enterprises WHERE status = #{status} ORDER BY created_at DESC
    </select>

    <select id="findById" resultMap="EnterpriseResultMap">
        SELECT * FROM enterprises WHERE id = #{id}
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO enterprises (company_name, contact_name, contact_phone, company_address, business_license, status, registration_id)
        VALUES (#{companyName}, #{contactName}, #{contactPhone}, #{companyAddress}, #{businessLicense}, #{status}, #{registrationId})
    </insert>

    <update id="update">
        UPDATE enterprises
        SET company_name = #{companyName},
            contact_name = #{contactName},
            contact_phone = #{contactPhone},
            company_address = #{companyAddress},
            business_license = #{businessLicense}
        WHERE id = #{id}
    </update>

    <update id="updateStatus">
        UPDATE enterprises SET status = #{status} WHERE id = #{id}
    </update>
</mapper>
```

- [ ] **Step 3: 创建 EnterpriseAccountMapper.java**

```java
package com.parttime.platform.mapper;
import com.parttime.platform.pojo.entity.EnterpriseAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface EnterpriseAccountMapper {
    List<EnterpriseAccount> findByEnterpriseId(@Param("enterpriseId") Long enterpriseId);
    Optional<EnterpriseAccount> findById(@Param("id") Long id);
    Optional<EnterpriseAccount> findByUsername(@Param("username") String username);
    int insert(EnterpriseAccount account);
    int update(EnterpriseAccount account);
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    int deleteById(@Param("id") Long id);
}
```

- [ ] **Step 4: 创建 EnterpriseAccountMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.platform.mapper.EnterpriseAccountMapper">
    <resultMap id="EnterpriseAccountResultMap" type="com.parttime.platform.pojo.entity.EnterpriseAccount">
        <id property="id" column="id"/>
        <result property="enterpriseId" column="enterprise_id"/>
        <result property="username" column="username"/>
        <result property="password" column="password"/>
        <result property="displayName" column="display_name"/>
        <result property="role" column="role"/>
        <result property="status" column="status"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>

    <select id="findByEnterpriseId" resultMap="EnterpriseAccountResultMap">
        SELECT id, enterprise_id, username, display_name, role, status, created_at, updated_at
        FROM enterprise_accounts WHERE enterprise_id = #{enterpriseId} ORDER BY created_at ASC
    </select>

    <select id="findById" resultMap="EnterpriseAccountResultMap">
        SELECT * FROM enterprise_accounts WHERE id = #{id}
    </select>

    <select id="findByUsername" resultMap="EnterpriseAccountResultMap">
        SELECT * FROM enterprise_accounts WHERE username = #{username}
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO enterprise_accounts (enterprise_id, username, password, display_name, role, status)
        VALUES (#{enterpriseId}, #{username}, #{password}, #{displayName}, #{role}, 'ACTIVE')
    </insert>

    <update id="update">
        UPDATE enterprise_accounts
        SET display_name = #{displayName}, role = #{role}
        WHERE id = #{id}
    </update>

    <update id="updatePassword">
        UPDATE enterprise_accounts SET password = #{password} WHERE id = #{id}
    </update>

    <delete id="deleteById">
        DELETE FROM enterprise_accounts WHERE id = #{id}
    </delete>
</mapper>
```

- [ ] **Step 5: 提交**

---

### Task 4: Service 层（platform-service）

**Files:**
- Create: `platform-service/src/main/java/com/parttime/platform/service/EnterpriseService.java`
- Create: `platform-service/src/main/java/com/parttime/platform/service/EnterpriseAccountService.java`
- Create: `platform-service/src/main/java/com/parttime/platform/service/impl/EnterpriseServiceImpl.java`
- Create: `platform-service/src/main/java/com/parttime/platform/service/impl/EnterpriseAccountServiceImpl.java`

- [ ] **Step 1: 创建 EnterpriseService.java**

```java
package com.parttime.platform.service;
import com.parttime.platform.pojo.cmd.EnterpriseUpdateCmd;
import com.parttime.platform.pojo.vo.EnterpriseVO;
import java.util.List;

public interface EnterpriseService {
    List<EnterpriseVO> list(String status);
    EnterpriseVO detail(Long id);
    EnterpriseVO update(EnterpriseUpdateCmd cmd);
    void suspend(Long id);
    void activate(Long id);
}
```

- [ ] **Step 2: 创建 EnterpriseServiceImpl.java**

```java
package com.parttime.platform.service.impl;
import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.EnterpriseMapper;
import com.parttime.platform.pojo.cmd.EnterpriseUpdateCmd;
import com.parttime.platform.pojo.entity.Enterprise;
import com.parttime.platform.pojo.vo.EnterpriseVO;
import com.parttime.platform.service.EnterpriseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnterpriseServiceImpl implements EnterpriseService {

    @Resource
    private EnterpriseMapper enterpriseMapper;

    @Override
    public List<EnterpriseVO> list(String status) {
        List<Enterprise> list;
        if (status != null && !status.isBlank()) {
            list = enterpriseMapper.findByStatus(status);
        } else {
            list = enterpriseMapper.findAll();
        }
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public EnterpriseVO detail(Long id) {
        Enterprise e = enterpriseMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Enterprise not found: " + id));
        return toVO(e);
    }

    @Override
    public EnterpriseVO update(EnterpriseUpdateCmd cmd) {
        Enterprise e = enterpriseMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("Enterprise not found: " + cmd.getId()));
        if (cmd.getCompanyName() != null) e.setCompanyName(cmd.getCompanyName());
        if (cmd.getContactName() != null) e.setContactName(cmd.getContactName());
        if (cmd.getContactPhone() != null) e.setContactPhone(cmd.getContactPhone());
        if (cmd.getCompanyAddress() != null) e.setCompanyAddress(cmd.getCompanyAddress());
        if (cmd.getBusinessLicense() != null) e.setBusinessLicense(cmd.getBusinessLicense());
        enterpriseMapper.update(e);
        return toVO(e);
    }

    @Override
    public void suspend(Long id) {
        Enterprise e = enterpriseMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Enterprise not found: " + id));
        if (!"ACTIVE".equals(e.getStatus())) {
            throw new BusinessException("Enterprise is not ACTIVE");
        }
        enterpriseMapper.updateStatus(id, "SUSPENDED");
    }

    @Override
    public void activate(Long id) {
        Enterprise e = enterpriseMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Enterprise not found: " + id));
        if (!"SUSPENDED".equals(e.getStatus())) {
            throw new BusinessException("Enterprise is not SUSPENDED");
        }
        enterpriseMapper.updateStatus(id, "ACTIVE");
    }

    private EnterpriseVO toVO(Enterprise e) {
        EnterpriseVO vo = new EnterpriseVO();
        vo.setId(e.getId());
        vo.setCompanyName(e.getCompanyName());
        vo.setContactName(e.getContactName());
        vo.setContactPhone(e.getContactPhone());
        vo.setCompanyAddress(e.getCompanyAddress());
        vo.setBusinessLicense(e.getBusinessLicense());
        vo.setStatus(e.getStatus());
        vo.setRegistrationId(e.getRegistrationId());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }
}
```

- [ ] **Step 3: 创建 EnterpriseAccountService.java**

```java
package com.parttime.platform.service;
import com.parttime.platform.pojo.cmd.EnterpriseAccountCreateCmd;
import com.parttime.platform.pojo.cmd.EnterpriseAccountUpdateCmd;
import com.parttime.platform.pojo.cmd.ResetPasswordCmd;
import com.parttime.platform.pojo.vo.EnterpriseAccountVO;
import java.util.List;

public interface EnterpriseAccountService {
    List<EnterpriseAccountVO> listByEnterprise(Long enterpriseId);
    EnterpriseAccountVO create(EnterpriseAccountCreateCmd cmd);
    EnterpriseAccountVO update(EnterpriseAccountUpdateCmd cmd);
    void resetPassword(ResetPasswordCmd cmd);
    void delete(Long id);
}
```

- [ ] **Step 4: 创建 EnterpriseAccountServiceImpl.java**

```java
package com.parttime.platform.service.impl;
import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.EnterpriseAccountMapper;
import com.parttime.platform.mapper.EnterpriseMapper;
import com.parttime.platform.pojo.cmd.EnterpriseAccountCreateCmd;
import com.parttime.platform.pojo.cmd.EnterpriseAccountUpdateCmd;
import com.parttime.platform.pojo.cmd.ResetPasswordCmd;
import com.parttime.platform.pojo.entity.EnterpriseAccount;
import com.parttime.platform.pojo.vo.EnterpriseAccountVO;
import com.parttime.platform.service.EnterpriseAccountService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnterpriseAccountServiceImpl implements EnterpriseAccountService {

    @Resource
    private EnterpriseAccountMapper accountMapper;
    @Resource
    private EnterpriseMapper enterpriseMapper;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public List<EnterpriseAccountVO> listByEnterprise(Long enterpriseId) {
        enterpriseMapper.findById(enterpriseId)
                .orElseThrow(() -> new BusinessException("Enterprise not found: " + enterpriseId));
        return accountMapper.findByEnterpriseId(enterpriseId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public EnterpriseAccountVO create(EnterpriseAccountCreateCmd cmd) {
        enterpriseMapper.findById(cmd.getEnterpriseId())
                .orElseThrow(() -> new BusinessException("Enterprise not found: " + cmd.getEnterpriseId()));
        if (accountMapper.findByUsername(cmd.getUsername()).isPresent()) {
            throw new BusinessException("Username already exists: " + cmd.getUsername());
        }
        EnterpriseAccount account = new EnterpriseAccount();
        account.setEnterpriseId(cmd.getEnterpriseId());
        account.setUsername(cmd.getUsername());
        account.setPassword(passwordEncoder.encode(cmd.getPassword()));
        account.setDisplayName(cmd.getDisplayName());
        account.setRole(cmd.getRole());
        account.setStatus("ACTIVE");
        accountMapper.insert(account);
        return toVO(account);
    }

    @Override
    public EnterpriseAccountVO update(EnterpriseAccountUpdateCmd cmd) {
        EnterpriseAccount account = accountMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("Account not found: " + cmd.getId()));
        if (cmd.getDisplayName() != null) account.setDisplayName(cmd.getDisplayName());
        if (cmd.getRole() != null) account.setRole(cmd.getRole());
        accountMapper.update(account);
        return toVO(account);
    }

    @Override
    public void resetPassword(ResetPasswordCmd cmd) {
        accountMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("Account not found: " + cmd.getId()));
        accountMapper.updatePassword(cmd.getId(), passwordEncoder.encode(cmd.getNewPassword()));
    }

    @Override
    public void delete(Long id) {
        accountMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Account not found: " + id));
        accountMapper.deleteById(id);
    }

    private EnterpriseAccountVO toVO(EnterpriseAccount a) {
        EnterpriseAccountVO vo = new EnterpriseAccountVO();
        vo.setId(a.getId());
        vo.setEnterpriseId(a.getEnterpriseId());
        vo.setUsername(a.getUsername());
        vo.setDisplayName(a.getDisplayName());
        vo.setRole(a.getRole());
        vo.setStatus(a.getStatus());
        vo.setCreatedAt(a.getCreatedAt());
        vo.setUpdatedAt(a.getUpdatedAt());
        return vo;
    }
}
```

- [ ] **Step 5: 提交**

---

### Task 5: Controller 层（platform-service）

**Files:**
- Create: `platform-service/src/main/java/com/parttime/platform/controller/EnterpriseController.java`
- Create: `platform-service/src/main/java/com/parttime/platform/controller/EnterpriseAccountController.java`

- [ ] **Step 1: 创建 EnterpriseController.java**

```java
package com.parttime.platform.controller;
import com.parttime.platform.pojo.cmd.EnterpriseUpdateCmd;
import com.parttime.platform.pojo.vo.EnterpriseVO;
import com.parttime.platform.service.EnterpriseService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/enterprises")
public class EnterpriseController {

    @Resource
    private EnterpriseService enterpriseService;

    @Operation(summary = "获取企业列表")
    @PostMapping("/list")
    public List<EnterpriseVO> list(@RequestBody(required = false) Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        return enterpriseService.list(status);
    }

    @Operation(summary = "获取企业详情")
    @PostMapping("/detail")
    public EnterpriseVO detail(@RequestBody Map<String, Long> body) {
        return enterpriseService.detail(body.get("id"));
    }

    @Operation(summary = "更新企业信息")
    @PostMapping("/update")
    public EnterpriseVO update(@RequestBody EnterpriseUpdateCmd cmd) {
        return enterpriseService.update(cmd);
    }

    @Operation(summary = "停用企业")
    @PostMapping("/suspend")
    public void suspend(@RequestBody Map<String, Long> body) {
        enterpriseService.suspend(body.get("id"));
    }

    @Operation(summary = "启用企业")
    @PostMapping("/activate")
    public void activate(@RequestBody Map<String, Long> body) {
        enterpriseService.activate(body.get("id"));
    }
}
```

- [ ] **Step 2: 创建 EnterpriseAccountController.java**

```java
package com.parttime.platform.controller;
import com.parttime.platform.pojo.cmd.EnterpriseAccountCreateCmd;
import com.parttime.platform.pojo.cmd.EnterpriseAccountUpdateCmd;
import com.parttime.platform.pojo.cmd.ResetPasswordCmd;
import com.parttime.platform.pojo.vo.EnterpriseAccountVO;
import com.parttime.platform.service.EnterpriseAccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class EnterpriseAccountController {

    @Resource
    private EnterpriseAccountService accountService;

    @Operation(summary = "获取企业下账号列表")
    @PostMapping("/enterprises/accounts/list")
    public List<EnterpriseAccountVO> list(@RequestBody Map<String, Long> body) {
        return accountService.listByEnterprise(body.get("enterpriseId"));
    }

    @Operation(summary = "创建账号")
    @PostMapping("/enterprises/accounts/create")
    public EnterpriseAccountVO create(@RequestBody EnterpriseAccountCreateCmd cmd) {
        return accountService.create(cmd);
    }

    @Operation(summary = "更新账号")
    @PostMapping("/accounts/update")
    public EnterpriseAccountVO update(@RequestBody EnterpriseAccountUpdateCmd cmd) {
        return accountService.update(cmd);
    }

    @Operation(summary = "重置密码")
    @PostMapping("/accounts/reset-password")
    public void resetPassword(@RequestBody ResetPasswordCmd cmd) {
        accountService.resetPassword(cmd);
    }

    @Operation(summary = "删除账号")
    @PostMapping("/accounts/delete")
    public void delete(@RequestBody Map<String, Long> body) {
        accountService.delete(body.get("id"));
    }
}
```

- [ ] **Step 3: 提交**

---

### Task 6: 注册审核通过时自动创建企业记录

**Files:**
- Modify: `platform-service/src/main/java/com/parttime/platform/service/impl/RegistrationServiceImpl.java`

- [ ] **Step 1: 修改 approveRegistration 方法**

在 `RegistrationServiceImpl` 中注入 `EnterpriseMapper`，在 `approveRegistration` 方法的状态更新后添加企业记录创建逻辑：

```java
@Resource
private EnterpriseMapper enterpriseMapper;
```

在 `approveRegistration` 方法的 `registration.setStatus("APPROVED");` 和 `registrationMapper.update(registration);` 之后添加：

```java
Enterprise enterprise = new Enterprise();
enterprise.setCompanyName(registration.getCompanyName());
enterprise.setContactName(registration.getContactName());
enterprise.setContactPhone(registration.getContactPhone());
enterprise.setCompanyAddress(registration.getCompanyAddress());
enterprise.setBusinessLicense(registration.getBusinessLicense());
enterprise.setStatus("ACTIVE");
enterprise.setRegistrationId(registration.getId());
enterpriseMapper.insert(enterprise);
```

- [ ] **Step 2: 提交**

---

### Task 7: 企业端认证改为数据库读取（enterprise-service）

**Files:**
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/EnterpriseAccountMapper.java`
- Create: `enterprise-service/src/main/resources/mapper/EnterpriseAccountMapper.xml`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/service/EnterpriseUserDetailsService.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/config/SecurityConfig.java`

- [ ] **Step 1: 创建 EnterpriseAccountMapper.java**

```java
package com.parttime.enterprise.mapper;
import com.parttime.enterprise.pojo.entity.EnterpriseAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Optional;

@Mapper
public interface EnterpriseAccountMapper {
    Optional<EnterpriseAccount> findByUsername(@Param("username") String username);
}
```

- [ ] **Step 2: 创建 EnterpriseAccountMapper.xml**

在 `enterprise-service/src/main/resources/mapper/EnterpriseAccountMapper.xml`：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.enterprise.mapper.EnterpriseAccountMapper">
    <select id="findByUsername" resultType="com.parttime.enterprise.pojo.entity.EnterpriseAccount">
        SELECT * FROM enterprise_accounts WHERE username = #{username}
    </select>
</mapper>
```

- [ ] **Step 3: 创建 EnterpriseAccount entity**

```java
package com.parttime.enterprise.pojo.entity;
import lombok.Data;

@Data
public class EnterpriseAccount {
    private Long id;
    private Long enterpriseId;
    private String username;
    private String password;
    private String displayName;
    private String role;
    private String status;
}
```

- [ ] **Step 4: 创建 EnterpriseUserDetailsService.java**

```java
package com.parttime.enterprise.service;
import com.parttime.enterprise.config.CompanyUserDetails;
import com.parttime.enterprise.mapper.EnterpriseAccountMapper;
import com.parttime.enterprise.pojo.entity.EnterpriseAccount;
import jakarta.annotation.Resource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EnterpriseUserDetailsService implements UserDetailsService {

    @Resource
    private EnterpriseAccountMapper accountMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EnterpriseAccount account = accountMapper.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new UsernameNotFoundException("User is disabled: " + username);
        }
        return new CompanyUserDetails(
                account.getUsername(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole())),
                account.getEnterpriseId()
        );
    }
}
```

- [ ] **Step 5: 修改 SecurityConfig.java**

将 `userDetailsService` 方法从 `InMemoryUserDetailsManager` 改为使用 `EnterpriseUserDetailsService` bean（由 Spring 自动注入）：

```java
@Resource
private EnterpriseUserDetailsService enterpriseUserDetailsService;

@Bean
public UserDetailsService userDetailsService() {
    return enterpriseUserDetailsService;
}
```

删除旧的 `userDetailsService(PasswordEncoder passwordEncoder)` 方法和 `User`、`InMemoryUserDetailsManager` 导入。

- [ ] **Step 6: 安全起见保留备用管理员账号**

在 `EnterpriseUserDetailsService` 中，如果 DB 查询不到用户，可以 fallback 到内存用户，保证在表还未初始化时系统仍可登录。

或者更简单：在 seed-data.sql 中已预置了默认账号，确保首次部署时数据库有数据。

开发环境建议保留内存用户作为 fallback。修改 `EnterpriseUserDetailsService.loadUserByUsername`：
```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    try {
        EnterpriseAccount account = accountMapper.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new UsernameNotFoundException("User is disabled: " + username);
        }
        return new CompanyUserDetails(
                account.getUsername(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole())),
                account.getEnterpriseId()
        );
    } catch (Exception e) {
        // Fallback to default admin for development
        if ("admin".equals(username)) {
            return new CompanyUserDetails("admin",
                    "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), 1L);
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
```

- [ ] **Step 7: 提交**

---

### Task 8: 前端 — API 模块

**Files:**
- Create: `platform-pc/src/api/enterprises.js`

- [ ] **Step 1: 创建 enterprises.js**

```javascript
import request from './request'

export function listEnterprises(params) {
  return request.post('/admin/enterprises/list', params)
}

export function detailEnterprise(id) {
  return request.post('/admin/enterprises/detail', { id })
}

export function updateEnterprise(data) {
  return request.post('/admin/enterprises/update', data)
}

export function suspendEnterprise(id) {
  return request.post('/admin/enterprises/suspend', { id })
}

export function activateEnterprise(id) {
  return request.post('/admin/enterprises/activate', { id })
}

export function listAccounts(enterpriseId) {
  return request.post('/admin/enterprises/accounts/list', { enterpriseId })
}

export function createAccount(data) {
  return request.post('/admin/enterprises/accounts/create', data)
}

export function updateAccount(data) {
  return request.post('/admin/accounts/update', data)
}

export function resetPassword(id, newPassword) {
  return request.post('/admin/accounts/reset-password', { id, newPassword })
}

export function deleteAccount(id) {
  return request.post('/admin/accounts/delete', { id })
}
```

- [ ] **Step 2: 提交**

---

### Task 9: 前端 — 企业列表页

**Files:**
- Create: `platform-pc/src/views/enterprises/EnterpriseList.vue`

- [ ] **Step 1: 创建 EnterpriseList.vue**

```vue
<template>
  <el-card>
    <template #header>
      <span class="card-title">企业管理</span>
      <el-select v-model="statusFilter" placeholder="筛选状态" size="small" style="float:right;width:140px" @change="fetchData">
        <el-option label="全部" value="" />
        <el-option label="已启用" value="ACTIVE" />
        <el-option label="已停用" value="SUSPENDED" />
      </el-select>
    </template>
    <el-table :data="enterprises" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="60" />
      <el-table-column prop="companyName" label="企业名称" min-width="160" />
      <el-table-column prop="contactName" label="联系人" width="120" />
      <el-table-column prop="contactPhone" label="联系电话" width="140" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '已启用' : '已停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button :type="row.status === 'ACTIVE' ? 'warning' : 'success'" size="small" text
            @click="handleToggleStatus(row)">
            {{ row.status === 'ACTIVE' ? '停用' : '启用' }}
          </el-button>
          <el-button type="primary" size="small" text @click="handleAccounts(row)">账号管理</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <!-- 编辑企业对话框 -->
  <el-dialog v-model="editDialog.visible" title="编辑企业" width="500px">
    <el-form :model="editDialog.form" label-width="100px">
      <el-form-item label="企业名称">
        <el-input v-model="editDialog.form.companyName" />
      </el-form-item>
      <el-form-item label="联系人">
        <el-input v-model="editDialog.form.contactName" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="editDialog.form.contactPhone" />
      </el-form-item>
      <el-form-item label="企业地址">
        <el-input v-model="editDialog.form.companyAddress" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmEdit">保存</el-button>
    </template>
  </el-dialog>

  <!-- 账号管理对话框 -->
  <el-dialog v-model="accountDialog.visible" :title="`账号管理 - ${accountDialog.companyName}`" width="700px">
    <el-button type="primary" size="small" style="margin-bottom:12px" @click="handleAddAccount">添加账号</el-button>
    <el-table :data="accountDialog.accounts" stripe style="width:100%">
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="displayName" label="显示名" width="120" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          {{ roleMap[row.role] || row.role }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
            {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEditAccount(row)">编辑</el-button>
          <el-button type="primary" size="small" text @click="handleResetPassword(row)">重置密码</el-button>
          <el-button type="danger" size="small" text @click="handleDeleteAccount(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <!-- 添加/编辑账号对话框 -->
  <el-dialog v-model="accountFormDialog.visible" :title="accountFormDialog.isEdit ? '编辑账号' : '添加账号'" width="400px">
    <el-form :model="accountFormDialog.form" label-width="80px">
      <el-form-item label="用户名" v-if="!accountFormDialog.isEdit">
        <el-input v-model="accountFormDialog.form.username" />
      </el-form-item>
      <el-form-item label="密码" v-if="!accountFormDialog.isEdit">
        <el-input v-model="accountFormDialog.form.password" type="password" />
      </el-form-item>
      <el-form-item label="显示名">
        <el-input v-model="accountFormDialog.form.displayName" />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="accountFormDialog.form.role" style="width:100%">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="人力资源" value="HR" />
          <el-option label="运营经理" value="MANAGER" />
          <el-option label="财务" value="FINANCE" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="accountFormDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmSaveAccount">保存</el-button>
    </template>
  </el-dialog>

  <!-- 重置密码对话框 -->
  <el-dialog v-model="resetPwdDialog.visible" title="重置密码" width="360px">
    <el-form :model="resetPwdDialog" label-width="80px">
      <el-form-item label="新密码">
        <el-input v-model="resetPwdDialog.newPassword" type="password" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="resetPwdDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmResetPassword">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listEnterprises, updateEnterprise, suspendEnterprise, activateEnterprise,
  listAccounts, createAccount, updateAccount, resetPassword, deleteAccount
} from '../../api/enterprises'

const roleMap = { ADMIN: '管理员', HR: '人力资源', MANAGER: '运营经理', FINANCE: '财务' }

const loading = ref(false)
const enterprises = ref([])
const statusFilter = ref('')

const editDialog = ref({ visible: false, form: {} })
const accountDialog = ref({ visible: false, companyName: '', enterpriseId: null, accounts: [] })
const accountFormDialog = ref({ visible: false, isEdit: false, form: { username: '', password: '', displayName: '', role: 'ADMIN' } })
const resetPwdDialog = ref({ visible: false, id: null, newPassword: '' })

async function fetchData() {
  loading.value = true
  try {
    const data = await listEnterprises({ status: statusFilter.value || undefined })
    enterprises.value = Array.isArray(data) ? data : (data.records || [])
  } finally {
    loading.value = false
  }
}

function handleEdit(row) {
  editDialog.value = { visible: true, form: { ...row } }
}

async function confirmEdit() {
  await updateEnterprise(editDialog.value.form)
  ElMessage.success('企业信息已更新')
  editDialog.value.visible = false
  await fetchData()
}

async function handleToggleStatus(row) {
  const action = row.status === 'ACTIVE' ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}企业 "${row.companyName}"？`, '确认')
    if (row.status === 'ACTIVE') {
      await suspendEnterprise(row.id)
    } else {
      await activateEnterprise(row.id)
    }
    ElMessage.success(`企业已${action}`)
    await fetchData()
  } catch { /* cancelled */ }
}

async function handleAccounts(row) {
  accountDialog.value = { visible: true, companyName: row.companyName, enterpriseId: row.id, accounts: [] }
  const data = await listAccounts(row.id)
  accountDialog.value.accounts = Array.isArray(data) ? data : []
}

function handleAddAccount() {
  accountFormDialog.value = {
    visible: true, isEdit: false,
    form: { username: '', password: '', displayName: '', role: 'ADMIN' }
  }
}

function handleEditAccount(row) {
  accountFormDialog.value = {
    visible: true, isEdit: true,
    form: { id: row.id, displayName: row.displayName, role: row.role }
  }
}

async function confirmSaveAccount() {
  const d = accountFormDialog.value
  if (d.isEdit) {
    await updateAccount(d.form)
    ElMessage.success('账号已更新')
  } else {
    await createAccount({ ...d.form, enterpriseId: accountDialog.value.enterpriseId })
    ElMessage.success('账号已创建')
  }
  d.visible = false
  const data = await listAccounts(accountDialog.value.enterpriseId)
  accountDialog.value.accounts = Array.isArray(data) ? data : []
}

function handleResetPassword(row) {
  resetPwdDialog.value = { visible: true, id: row.id, newPassword: '' }
}

async function confirmResetPassword() {
  await resetPassword(resetPwdDialog.value.id, resetPwdDialog.value.newPassword)
  ElMessage.success('密码已重置')
  resetPwdDialog.value.visible = false
}

async function handleDeleteAccount(row) {
  try {
    await ElMessageBox.confirm(`确认删除账号 "${row.username}"？`, '确认')
    await deleteAccount(row.id)
    ElMessage.success('账号已删除')
    const data = await listAccounts(accountDialog.value.enterpriseId)
    accountDialog.value.accounts = Array.isArray(data) ? data : []
  } catch { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
```

- [ ] **Step 2: 提交**

---

### Task 10: 前端 — 路由和侧边栏

**Files:**
- Modify: `platform-pc/src/router/index.js`
- Modify: `platform-pc/src/App.vue`

- [ ] **Step 1: 修改 router/index.js**

添加新路由：
```javascript
{
  path: '/enterprises',
  name: 'Enterprises',
  component: () => import('../views/enterprises/EnterpriseList.vue'),
  meta: { requiresAuth: true }
}
```

- [ ] **Step 2: 修改 App.vue**

在「职位分类」之后添加侧边栏菜单项：
```html
<el-menu-item index="/enterprises">
  <el-icon><OfficeBuilding /></el-icon><span>企业管理</span>
</el-menu-item>
```

添加图标导入：
```javascript
import { ..., OfficeBuilding } from '@element-plus/icons-vue'
```

- [ ] **Step 3: 提交**

---

### Task 11: 编译验证

- [ ] **Step 1: 编译 platform-service**

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
cd platform-service && mvn compile -q
```
预期：无错误输出

- [ ] **Step 2: 编译 enterprise-service**

```bash
cd enterprise-service && mvn compile -q
```
预期：无错误输出

- [ ] **Step 3: 提交**

---

### Task 12: 数据库重建 + 启动验证

- [ ] **Step 1: 重建数据库并导入 seed 数据**

```bash
mysql -u root -p -e "DROP DATABASE IF EXISTS part_time_work; CREATE DATABASE part_time_work CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p part_time_work < platform-service/src/main/resources/db/migration/V1__init.sql
mysql -u root -p part_time_work < enterprise-service/src/main/resources/db/migration/V2__create_job_tables.sql
mysql -u root -p part_time_work < c-service/src/main/resources/db/migration/V3__create_tables.sql
mysql -u root -p part_time_work < platform-service/src/main/resources/db/migration/V4__create_enterprise_tables.sql
mysql -u root -p --default-character-set=utf8mb4 part_time_work < scripts/seed-data.sql
```

- [ ] **Step 2: 启动后端服务验证**

分别启动 platform-service、enterprise-service、c-service，确认无启动报错。

- [ ] **Step 3: 启动前端验证**

```bash
cd platform-pc && npm run dev
```

确认「企业管理」菜单可见，列表加载正常，编辑/停用/账号管理功能可用。

- [ ] **Step 4: 提交**

---

## Spec Coverage Check

| 需求 | Task |
|------|------|
| 运营人员可管理已审核企业（列表、编辑、停用/启用） | Task 1~5, 8~10 |
| 运营人员可管理企业登录账号 | Task 1~5, 8~10 |
| 注册审核通过后自动创建企业记录 | Task 6 |
| 企业端登录从数据库读取账号 | Task 7 |
