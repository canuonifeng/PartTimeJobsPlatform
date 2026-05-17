# Enterprise Share QR Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let enterprise staff share jobs from the enterprise mini program and generate a real QR code that opens the worker mini program job detail page.

**Architecture:** Keep enterprise job publishing as-is, but add a dedicated job-share backend path that asks WeChat for a worker mini-program code image and returns it as base64 JSON. On the enterprise mini program side, add share handlers on list/detail pages plus a QR preview modal on the detail page. The worker mini program already accepts `id` on the job detail route, so no worker code change is expected unless the smoke test proves otherwise.

**Tech Stack:** Spring Boot 3.2.5, MyBatis, Spring MVC, Vue 3, uni-app, WeChat mini-program `onShareAppMessage`, WeChat `getwxacodeunlimit` API.

---

### Task 1: Add enterprise backend QR generation

**Files:**
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/config/WeChatMiniProgramConfig.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/JobShareCodeVO.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/service/JobShareService.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/JobShareServiceImpl.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/controller/JobShareController.java`
- Modify: `enterprise-service/src/main/resources/application.yml`

- [ ] **Step 1: Write the failing test**

Create `enterprise-service/src/test/java/com/parttime/enterprise/controller/JobShareControllerTest.java` with a mocked service and this expectation:

```java
@Test
void getShareCode_returns_jobId_path_and_base64_image() throws Exception {
    when(jobShareService.getShareCode(42L)).thenReturn(new JobShareCodeVO(42L, "/pages/jobs/jobDetail?id=42", "iVBORw0KGgoAAA"));

    mockMvc.perform(get("/api/jobs/share-code").param("id", "42"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jobId").value(42))
            .andExpect(jsonPath("$.path").value("/pages/jobs/jobDetail?id=42"))
            .andExpect(jsonPath("$.imageBase64").value("iVBORw0KGgoAAA"));
}
```

- [ ] **Step 2: Run the test and confirm it fails**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -f enterprise-service/pom.xml -Dtest=JobShareControllerTest test`

Expected: FAIL because `/api/jobs/share-code` and the share service do not exist yet.

- [ ] **Step 3: Implement the minimal backend**

Add a dedicated service that:
- loads the enterprise job by `id`
- rejects non-`PUBLISHED` jobs with a `400` or business exception
- builds the worker path exactly as `/pages/jobs/jobDetail?id={jobId}`
- fetches a WeChat access token using `wechat.worker-app-id` and `wechat.worker-app-secret`
- calls `https://api.weixin.qq.com/wxa/getwxacodeunlimit`
- converts the returned PNG bytes to a base64 string
- returns `JobShareCodeVO(jobId, path, imageBase64)`

Use this response shape:

```java
@Data
@AllArgsConstructor
public class JobShareCodeVO {
    private Long jobId;
    private String path;
    private String imageBase64;
}
```

Expose `GET /api/jobs/share-code?id=123` from `JobShareController`.

- [ ] **Step 4: Run the backend test again**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -f enterprise-service/pom.xml -Dtest=JobShareControllerTest test`

Expected: PASS.

- [ ] **Step 5: Run the backend test suite**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -f enterprise-service/pom.xml test`

Expected: PASS with no new security or serialization regressions.

### Task 2: Add enterprise mini program sharing UI

**Files:**
- Modify: `enterprise-uniapp/src/api/jobs.js`
- Modify: `enterprise-uniapp/src/pages/jobs/jobList.vue`
- Modify: `enterprise-uniapp/src/pages/jobs/jobDetail.vue`

- [ ] **Step 1: Write the failing test**

Add a lightweight component-level expectation by wiring the new API and share UI in the pages first, then verify with a build. The concrete behavior to implement is:

```js
export function getJobShareCode(id) {
  return request('GET', `/jobs/share-code?id=${id}`)
}
```

and the share card path must be:

```js
`/pages/jobs/jobDetail?id=${job.id}`
```

For the page handlers, use this share payload shape:

```js
onShareAppMessage((res) => ({
  title: job.value?.title || '职位详情',
  path: `/pages/jobs/jobDetail?id=${res?.target?.dataset?.id || job.value?.id}`
}))
```

- [ ] **Step 2: Run the build and confirm it fails**

Run: `npm run build:mp-weixin`

Expected: FAIL until the new API and share handlers exist.

- [ ] **Step 3: Implement the page changes**

In `jobList.vue`:
- add a `分享` button to each job card footer
- mark it with `open-type="share"`
- use `@click.stop` so it does not trigger navigation
- implement `onShareAppMessage` so the shared path uses the clicked job id

In `jobDetail.vue`:
- add a `分享职位` button for the current job
- implement `onShareAppMessage` and `onShareTimeline` with the current job id
- add a `生成二维码` button
- call `getJobShareCode(job.value.id)`
- show the returned base64 PNG in a full-screen overlay using a conditional `<view>` plus an `<image>` tag

Use this client-side source shape:

```js
const shareCodeSrc = computed(() => shareCodeBase64.value ? `data:image/png;base64,${shareCodeBase64.value}` : '')
```

- [ ] **Step 4: Run the mini program build again**

Run: `npm run build:mp-weixin`

Expected: PASS and emit a normal `dist/dev/mp-weixin` build.

- [ ] **Step 5: Smoke test the share UI**

Open enterprise mini program devtools and verify:
- list page shares the selected job, not a generic page
- detail page shares the current job
- clicking `生成二维码` renders an image instead of an error

### Task 3: End-to-end verify worker scan path

**Files:**
- Verify only: `worker-uniapp/src/pages/jobs/jobDetail.vue`
- Verify only: `worker-uniapp/src/pages.json`

- [ ] **Step 1: Confirm the worker route already accepts `id`**

The worker job detail page already reads `page.options.id` and loads the job by that id, so the QR target path should work without a code change. If the smoke test shows a route mismatch, then update only the worker job detail loader to read the query parameter from the current page options.

- [ ] **Step 2: Run the worker build sanity check**

Run: `npm run build:mp-weixin`

Expected: PASS with no route regressions.

- [ ] **Step 3: Do the manual scan flow**

1. Log in to the enterprise mini program.
2. Open a `PUBLISHED` job.
3. Generate the QR code.
4. Scan it in WeChat.
5. Confirm it opens the worker mini program job detail page with the same `id`.
6. Confirm `立即报名` still works on the worker page.
