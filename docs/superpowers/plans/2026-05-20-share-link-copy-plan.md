# Share Link Copy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace enterprise mini program card sharing with a copied C-end mini program URL Scheme that opens the worker job detail page.

**Architecture:** The enterprise backend will validate the job owner and build a plain `weixin://dl/business/` scheme for the C-end mini program using the job id as the query. The enterprise mini app will stop using `open-type="share"` and instead fetch the scheme text, copy it to the clipboard, and notify the user.

**Tech Stack:** Spring Boot, MyBatis, UniApp, Vue 3, WeChat mini program URL Scheme

---

### Task 1: Add backend share-link endpoint

**Files:**
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/config/WeChatMiniProgramConfig.java`
- Modify: `enterprise-service/src/main/resources/application.yml`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/JobShareLinkVO.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/JobShareService.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/JobShareServiceImpl.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/controller/JobShareController.java`
- Modify: `enterprise-service/src/test/java/com/parttime/enterprise/controller/JobShareControllerTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void getShareLink_returns_scheme_text() throws Exception {
    when(jobShareService.getShareLink(42L))
            .thenReturn(new JobShareLinkVO(42L, "weixin://dl/business/?appid=test_appid&path=/pages/jobs/jobDetail&query=id%3D42&env_version=release"));

    mockMvc.perform(get("/api/jobs/share-link").param("id", "42"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jobId").value(42))
            .andExpect(jsonPath("$.link").exists());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -f enterprise-service/pom.xml -Dtest=JobShareControllerTest test`
Expected: FAIL because `/api/jobs/share-link` and `JobShareLinkVO` do not exist yet.

- [ ] **Step 3: Write minimal implementation**

```java
public JobShareLinkVO getShareLink(Long jobId) {
    Job job = jobMapper.findById(jobId)
            .orElseThrow(() -> new BusinessException("职位不存在"));
    Long currentCompanyId = SecurityUtil.getCurrentCompanyId();
    if (!currentCompanyId.equals(job.getCompanyId())) {
        throw new BusinessException("无权分享该职位");
    }
    if (!"PUBLISHED".equals(job.getStatus())) {
        throw new BusinessException("仅已发布职位可生成链接");
    }
    String link = "weixin://dl/business/?appid=" + weChatMiniProgramConfig.getCustomerAppId()
            + "&path=/pages/jobs/jobDetail&query=id%3D" + jobId
            + "&env_version=release";
    return new JobShareLinkVO(jobId, link);
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -f enterprise-service/pom.xml -Dtest=JobShareControllerTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/config/WeChatMiniProgramConfig.java enterprise-service/src/main/resources/application.yml enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/JobShareLinkVO.java enterprise-service/src/main/java/com/parttime/enterprise/service/JobShareService.java enterprise-service/src/main/java/com/parttime/enterprise/service/impl/JobShareServiceImpl.java enterprise-service/src/main/java/com/parttime/enterprise/controller/JobShareController.java enterprise-service/src/test/java/com/parttime/enterprise/controller/JobShareControllerTest.java docs/superpowers/plans/2026-05-20-share-link-copy-plan.md
git commit -m "feat: copy mini program share link"
```

### Task 2: Update enterprise mini program share actions

**Files:**
- Modify: `enterprise-uniapp/src/api/jobs.js`
- Modify: `enterprise-uniapp/src/pages/jobs/jobList.vue`
- Modify: `enterprise-uniapp/src/pages/jobs/jobDetail.vue`

- [ ] **Step 1: Write the failing test**

```vue
<button class="action-btn share-btn" @click="handleShare(job.id)">复制链接</button>
```

- [ ] **Step 2: Run build to verify it fails**

Run: `npm run build:mp-weixin`
Expected: FAIL until the new API and handlers exist.

- [ ] **Step 3: Write minimal implementation**

```js
export function getJobShareLink(id) {
  return request('GET', `/jobs/share-link?id=${id}`)
}

async function handleShare(id) {
  const res = await getJobShareLink(id)
  const link = res?.link || res?.data?.link || ''
  await new Promise((resolve, reject) => {
    uni.setClipboardData({ data: link, success: resolve, fail: reject })
  })
  uni.showToast({ title: '链接已复制', icon: 'success' })
}
```

- [ ] **Step 4: Run build to verify it passes**

Run: `npm run build:mp-weixin`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add enterprise-uniapp/src/api/jobs.js enterprise-uniapp/src/pages/jobs/jobList.vue enterprise-uniapp/src/pages/jobs/jobDetail.vue
git commit -m "feat: copy enterprise share link"
```

### Task 3: Verify the end-to-end behavior

**Files:**
- Verify: `enterprise-service/src/test/java/com/parttime/enterprise/controller/JobShareControllerTest.java`
- Verify: `enterprise-uniapp/src/pages/jobs/jobList.vue`
- Verify: `enterprise-uniapp/src/pages/jobs/jobDetail.vue`

- [ ] **Step 1: Run backend tests**

Run: `mvn -f enterprise-service/pom.xml -Dtest=JobShareControllerTest,JobServiceTest test`
Expected: PASS.

- [ ] **Step 2: Run mini program build**

Run: `npm run build:mp-weixin`
Expected: PASS.

- [ ] **Step 3: Manual smoke check**

Open the enterprise job list and detail pages, click the share action, and confirm the clipboard contains a `weixin://dl/business/` link with the job id in the query string.
```
