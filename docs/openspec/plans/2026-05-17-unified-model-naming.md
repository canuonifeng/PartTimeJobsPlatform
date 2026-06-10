# Unified Model Naming Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove C/B side prefixes from the remaining shared business model names and supporting copy so the codebase speaks one domain language for `岗位`, `排班`, `报名`, `工人`, and `企业`.

**Architecture:** Keep the existing service split, REST paths, and database tables. Rename only the remaining side-prefixed mapper contracts and their XML namespaces, then update the service implementations and tests that consume them. Leave already-shared business models such as `Job`, `JobDetailVO`, `JobSummaryVO`, `WorkerVO`, and `JobApplicationVO` in place.

**Tech Stack:** Spring Boot 3.2.5, MyBatis, JUnit 5, Mockito, Maven, uni-app.

---

### Task 1: Rename enterprise sync mappers

**Files:**
- Move: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/CJobMapper.java` -> `enterprise-service/src/main/java/com/parttime/enterprise/mapper/JobSyncMapper.java`
- Move: `enterprise-service/src/main/resources/mapper/CJobMapper.xml` -> `enterprise-service/src/main/resources/mapper/JobSyncMapper.xml`
- Move: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/CWorkerMapper.java` -> `enterprise-service/src/main/java/com/parttime/enterprise/mapper/WorkerSyncMapper.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/JobServiceImpl.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/ApplicationServiceImpl.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/CompanyWorkerServiceImpl.java`
- Modify: `enterprise-service/src/test/java/com/parttime/enterprise/service/JobServiceTest.java`
- Modify: `enterprise-service/src/test/java/com/parttime/enterprise/service/ApplicationServiceTest.java`

- [ ] **Step 1: Update the tests first so the rename is explicit**

In `JobServiceTest.java`, replace the mock type and field name:

```java
@Mock
private JobSyncMapper jobSyncMapper;
```

and update the publish verification:

```java
verify(jobSyncMapper).upsert(
        eq(1L), eq(1L), eq("美味餐饮管理有限公司"), eq("https://cdn.example.com/logos/meiwei.png"),
        isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
        isNull(), isNull(), isNull(), isNull(), isNull(), eq("PUBLISHED"), eq("[]"), isNull(), isNull()
);
```

In `ApplicationServiceTest.java`, replace the worker sync mock with:

```java
@Mock
private WorkerSyncMapper workerSyncMapper;
```

and keep the existing expectation on `findWorkerNameById(...)`.

- [ ] **Step 2: Run the enterprise service tests and confirm they fail before the rename lands**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -f enterprise-service/pom.xml -Dtest=JobServiceTest,ApplicationServiceTest test`

Expected: FAIL with compile errors for `JobSyncMapper` and `WorkerSyncMapper` until the production files are renamed.

- [ ] **Step 3: Rename the production mapper contracts and XML namespace**

Use these final signatures:

```java
@Mapper
public interface JobSyncMapper {
    void upsert(Long jobId, Long companyId, String companyName, String companyLogo,
                String title, String description, String location,
                String province, String city, String district, String address,
                BigDecimal latitude, BigDecimal longitude,
                Long categoryId,
                String rateType, BigDecimal rateAmount,
                String status, String scheduleInfo,
                Integer headcount, LocalDateTime deadline);
}
```

```java
@Mapper
public interface WorkerSyncMapper {
    @Select("SELECT name FROM c_worker WHERE id = #{id}")
    String findWorkerNameById(Long id);
}
```

Update the XML namespace in `JobSyncMapper.xml` to:

```xml
<mapper namespace="com.parttime.enterprise.mapper.JobSyncMapper">
```

Update the service implementations to inject the new names:

```java
@Resource
private JobSyncMapper jobSyncMapper;

@Resource
private WorkerSyncMapper workerSyncMapper;
```

- [ ] **Step 4: Run the enterprise service tests again**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -f enterprise-service/pom.xml -Dtest=JobServiceTest,ApplicationServiceTest test`

Expected: PASS.

### Task 2: Rename platform sync mapper

**Files:**
- Move: `platform-service/src/main/java/com/parttime/platform/mapper/CJobMapper.java` -> `platform-service/src/main/java/com/parttime/platform/mapper/JobSyncMapper.java`
- Move: `platform-service/src/main/resources/mapper/CJobMapper.xml` -> `platform-service/src/main/resources/mapper/JobSyncMapper.xml`
- Modify: `platform-service/src/main/java/com/parttime/platform/service/impl/EnterpriseServiceImpl.java`
- Modify: `platform-service/src/test/java/com/parttime/platform/service/EnterpriseServiceTest.java`

- [ ] **Step 1: Update the platform test to use the new mapper name**

In `EnterpriseServiceTest.java`, replace the mock field with:

```java
@Mock
private JobSyncMapper jobSyncMapper;
```

and keep the existing sync verification:

```java
verify(jobSyncMapper).updateCompanyLogoByCompanyId(1L, "https://cdn.example.com/new.png");
```

- [ ] **Step 2: Run the platform service test and confirm it fails before the rename lands**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -f platform-service/pom.xml -Dtest=EnterpriseServiceTest test`

Expected: FAIL with compile errors for `JobSyncMapper` until the production files are renamed.

- [ ] **Step 3: Rename the production mapper contract and XML namespace**

Use this final signature:

```java
@Mapper
public interface JobSyncMapper {
    void updateCompanyLogoByCompanyId(Long companyId, String companyLogo);
}
```

Update the XML namespace in `JobSyncMapper.xml` to:

```xml
<mapper namespace="com.parttime.platform.mapper.JobSyncMapper">
```

Update `EnterpriseServiceImpl` to inject `JobSyncMapper` and call `jobSyncMapper.updateCompanyLogoByCompanyId(...)`.

- [ ] **Step 4: Run the platform service test again**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -f platform-service/pom.xml -Dtest=EnterpriseServiceTest test`

Expected: PASS.

### Task 3: Clean remaining side wording and verify no regressions

**Files:**
- Modify: `todo.md`
- Verify only: `c-service/src/main/java/com/parttime/cservice/**/*.java`

- [ ] **Step 1: Replace the live note with business nouns**

Update `todo.md` from:

```md
B端分享C端的岗位详情的连接
统一模型，不要分C端和B端，例如：岗位、排班
```

to:

```md
岗位分享链路统一为同一套业务命名
统一模型，不要分端，例如：岗位、排班、报名
```

- [ ] **Step 2: Verify the c-service already uses shared nouns and needs no rename**

Run: `rg -n "CJobMapper|CWorkerMapper|C端|B端|C-side|B-side" c-service/src/main/java c-service/src/test/java`

Expected: no output.

- [ ] **Step 3: Run the full build checks**

Run:

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -f enterprise-service/pom.xml test
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -f platform-service/pom.xml test
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -f c-service/pom.xml test
```

Expected: all three commands pass.
