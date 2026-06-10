# Job Application Counts And Records Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show total and pending application counts on job lists, and move application records into a separate paginated records page for both enterprise PC and enterprise mini app.

**Architecture:** The enterprise backend will expose application counts on the job list response and provide paginated application list APIs for a job. The enterprise PC and mini app job lists will render the new counters and link into a dedicated application records page that supports review actions without embedding the records into job detail.

**Tech Stack:** Spring Boot, MyBatis, Vue 3, Element Plus, UniApp

---

### Task 1: Add application counts to job list responses

**Files:**
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/JobVO.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/JobServiceImpl.java:163-175,377-398`
- Modify: `enterprise-service/src/test/java/com/parttime/enterprise/service/JobServiceTest.java`
- Modify: `enterprise-service/src/test/java/com/parttime/enterprise/controller/JobControllerTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void getJobsByCompany_shouldIncludeApplicationCounts() {
    Job job = new Job();
    job.setId(1L);
    job.setCompanyId(1L);
    job.setTitle("Software Engineer");
    job.setStatus("PUBLISHED");

    when(jobMapper.findByCompanyId(1L)).thenReturn(List.of(job));
    when(jobApplicationMapper.countByJobIdAndStatus(1L, "PENDING")).thenReturn(2);
    when(jobApplicationMapper.findByJobId(1L)).thenReturn(List.of());

    List<JobVO> jobs = jobService.getJobsByCompany(1L, null);

    assertThat(jobs.get(0).getApplicationCount()).isEqualTo(2);
    assertThat(jobs.get(0).getPendingApplicationCount()).isEqualTo(2);
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -f enterprise-service/pom.xml -Dtest=JobServiceTest,JobControllerTest test`
Expected: FAIL because `JobVO` has no application count fields yet.

- [ ] **Step 3: Write minimal implementation**

```java
// JobVO
private Integer applicationCount;
private Integer pendingApplicationCount;

// JobServiceImpl.getJobsByCompany
int totalCount = jobApplicationMapper.countByJobIdAndStatus(job.getId(), "PENDING")
        + jobApplicationMapper.countByJobIdAndStatus(job.getId(), "ACCEPTED")
        + jobApplicationMapper.countByJobIdAndStatus(job.getId(), "REJECTED");
response.setApplicationCount(totalCount);
response.setPendingApplicationCount(jobApplicationMapper.countByJobIdAndStatus(job.getId(), "PENDING"));
```

- [ ] **Step 4: Run test to verify it passes**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -f enterprise-service/pom.xml -Dtest=JobServiceTest,JobControllerTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/JobVO.java enterprise-service/src/main/java/com/parttime/enterprise/service/impl/JobServiceImpl.java enterprise-service/src/test/java/com/parttime/enterprise/service/JobServiceTest.java enterprise-service/src/test/java/com/parttime/enterprise/controller/JobControllerTest.java
git commit -m "feat: add job application counts"
```

### Task 2: Add paginated application records API

**Files:**
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/controller/ApplicationController.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/ApplicationService.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/ApplicationServiceImpl.java`
- Modify: `enterprise-service/src/test/java/com/parttime/enterprise/service/ApplicationServiceTest.java`
- Modify: `enterprise-service/src/test/java/com/parttime/enterprise/controller/ApplicationControllerTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void getApplicationsByJob_shouldReturnPaginatedRecords() {
    // request page 1 size 10 and expect a list wrapper or paged response
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -f enterprise-service/pom.xml -Dtest=ApplicationServiceTest,ApplicationControllerTest test`
Expected: FAIL because there is no paginated API yet.

- [ ] **Step 3: Write minimal implementation**

```java
public PagedResult<JobApplicationVO> getApplicationsByJob(Long jobId, String jobTitle, String status, int page, int pageSize) {
    List<JobApplicationVO> all = getApplicationsByJob(jobId, jobTitle, status);
    // slice in-memory for minimal implementation
    return new PagedResult<>(...);
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -f enterprise-service/pom.xml -Dtest=ApplicationServiceTest,ApplicationControllerTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/controller/ApplicationController.java enterprise-service/src/main/java/com/parttime/enterprise/service/ApplicationService.java enterprise-service/src/main/java/com/parttime/enterprise/service/impl/ApplicationServiceImpl.java enterprise-service/src/test/java/com/parttime/enterprise/service/ApplicationServiceTest.java enterprise-service/src/test/java/com/parttime/enterprise/controller/ApplicationControllerTest.java
git commit -m "feat: add application records api"
```

### Task 3: Move enterprise mini app application records into a dedicated page

**Files:**
- Modify: `enterprise-uniapp/src/pages/jobs/jobList.vue`
- Modify: `enterprise-uniapp/src/pages/jobs/jobDetail.vue`
- Create: `enterprise-uniapp/src/pages/jobs/applicationList.vue`
- Modify: `enterprise-uniapp/src/pages.json`
- Modify: `enterprise-uniapp/src/api/jobs.js`

- [ ] **Step 1: Write the failing test**

```vue
<button @click="navigateToApplications(job.id)">报名记录</button>
```

- [ ] **Step 2: Run build to verify it fails**

Run: `npm run build:mp-weixin`
Expected: FAIL until the new page and API exist.

- [ ] **Step 3: Write minimal implementation**

```js
export function getApplications(params) {
  return request('GET', '/applications', params)
}
```

- [ ] **Step 4: Run build to verify it passes**

Run: `npm run build:mp-weixin`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add enterprise-uniapp/src/pages/jobs/jobList.vue enterprise-uniapp/src/pages/jobs/jobDetail.vue enterprise-uniapp/src/pages/jobs/applicationList.vue enterprise-uniapp/src/pages.json enterprise-uniapp/src/api/jobs.js
git commit -m "feat: add mini app application records page"
```

### Task 4: Add application counts and records entry to enterprise PC

**Files:**
- Modify: `enterprise-pc/src/views/jobs/JobList.vue`
- Modify: `enterprise-pc/src/views/applications/ApplicationList.vue`
- Modify: `enterprise-pc/src/api/job.js`
- Modify: `enterprise-pc/src/api/application.js`

- [ ] **Step 1: Write the failing test**

```vue
<el-table-column prop="applicationCount" label="报名人数" width="100" />
<el-table-column prop="pendingApplicationCount" label="待审核" width="100" />
```

- [ ] **Step 2: Run build to verify it fails**

Run: `npm run build`
Expected: FAIL until the new columns and navigation are in place.

- [ ] **Step 3: Write minimal implementation**

```js
// job list row should display applicationCount and pendingApplicationCount
// add a button to open ApplicationList with the jobId filter
```

- [ ] **Step 4: Run build to verify it passes**

Run: `npm run build`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add enterprise-pc/src/views/jobs/JobList.vue enterprise-pc/src/views/applications/ApplicationList.vue enterprise-pc/src/api/job.js enterprise-pc/src/api/application.js
git commit -m "feat: add pc application counts and records entry"
```

### Task 5: Verify end-to-end behavior

**Files:**
- Verify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/ApplicationServiceImpl.java`
- Verify: `enterprise-uniapp/src/pages/jobs/applicationList.vue`
- Verify: `enterprise-pc/src/views/applications/ApplicationList.vue`

- [ ] **Step 1: Run backend tests**

Run: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home && mvn -f enterprise-service/pom.xml -Dtest=JobServiceTest,JobControllerTest,ApplicationServiceTest,ApplicationControllerTest test`
Expected: PASS.

- [ ] **Step 2: Run mini app build**

Run: `npm run build:mp-weixin`
Expected: PASS.

- [ ] **Step 3: Run PC build**

Run: `npm run build`
Expected: PASS.
