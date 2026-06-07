# Enterprise Logo + Nearby Jobs Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show enterprise information, enterprise logo, and distance on the worker job list, and sort jobs by the worker's current location.

**Architecture:** `enterprises.company_logo` is the source of truth. The platform service owns edits to enterprise records and also updates denormalized `c_job.company_logo` rows so the worker-side can render without extra joins. The c-service returns logo/name/distance in job summaries and sorts by Haversine distance when coordinates are provided. The worker app requests location on page enter and passes the coordinates through the existing job list API.

**Tech Stack:** Spring Boot 3.2.5 + MyBatis XML/annotations, MySQL/Flyway SQL, Vue 3 + uni-app, Element Plus, Vite

---

### Task 1: Add `company_logo` to the database and demo data

**Files:**
- Create: `platform-service/src/main/resources/db/migration/V7__add_company_logo_to_enterprises.sql`
- Create: `c-service/src/main/resources/db/migration/V5__add_company_logo_to_c_job.sql`
- Update: `scripts/seed-data.sql`
- Create: `scripts/backfill-company-logo.sql`

- [ ] **Write the enterprise-side migration**

```sql
ALTER TABLE enterprises
  ADD COLUMN company_logo VARCHAR(500) DEFAULT NULL COMMENT '企业logoURL';
```

- [ ] **Write the worker-side migration**

```sql
ALTER TABLE c_job
  ADD COLUMN company_logo VARCHAR(500) DEFAULT NULL COMMENT '企业logoURL';
```

- [ ] **Backfill the seeded enterprise and job rows**

```sql
UPDATE enterprises SET company_logo = 'https://cdn.example.com/logos/beijing-xunjie.png' WHERE id = 1;
UPDATE enterprises SET company_logo = 'https://cdn.example.com/logos/shanghai-fengsheng.png' WHERE id = 2;
UPDATE enterprises SET company_logo = 'https://cdn.example.com/logos/guangzhou-tianhui.png' WHERE id = 3;
UPDATE enterprises SET company_logo = 'https://cdn.example.com/logos/shenzhen-chuangxiang.png' WHERE id = 4;

UPDATE c_job SET company_logo = 'https://cdn.example.com/logos/meiwei.png' WHERE company_id = 1;
UPDATE c_job SET company_logo = 'https://cdn.example.com/logos/jisu.png' WHERE company_id = 2;
UPDATE c_job SET company_logo = 'https://cdn.example.com/logos/jiexin.png' WHERE company_id = 3;
UPDATE c_job SET company_logo = 'https://cdn.example.com/logos/zhuoyue.png' WHERE company_id = 4;
```

- [ ] **Update the seed SQL inserts**

For `enterprises`:

```sql
INSERT INTO enterprises (id, company_name, company_logo, contact_name, contact_phone, company_address, business_license, status, registration_id) VALUES
(1, '北京迅捷物流有限公司', 'https://cdn.example.com/logos/beijing-xunjie.png', '王经理', '13800138001', '北京市朝阳区建国路88号', 'BL-2024001', 'ACTIVE', 1),
(2, '上海丰盛餐饮管理有限公司', 'https://cdn.example.com/logos/shanghai-fengsheng.png', '李店长', '13900139002', '上海市浦东新区陆家嘴路100号', 'BL-2024002', 'ACTIVE', 2),
(3, '广州天汇商贸有限公司', 'https://cdn.example.com/logos/guangzhou-tianhui.png', '陈主管', '13700137003', '广州市天河区天河路200号', 'BL-2024003', 'ACTIVE', 3),
(4, '深圳创想科技有限公司', 'https://cdn.example.com/logos/shenzhen-chuangxiang.png', '张总', '13600136004', '深圳市南山区科技园路300号', 'BL-2024004', 'ACTIVE', 4);
```

For `c_job`:

```sql
INSERT INTO c_job (job_id, company_id, company_name, company_logo, title, description, location, category_id, category_name, rate_type, rate_amount, status, headcount, deadline) VALUES
(1, 1, '美味餐饮管理有限公司', 'https://cdn.example.com/logos/meiwei.png', '餐厅服务员', '负责餐厅日常接待、点餐、上菜等工作。', '北京市朝阳区建国路88号', 1, '餐饮服务', 'HOURLY', 25.00, 'PUBLISHED', 10, CONCAT(DATE_ADD(CURDATE(), INTERVAL 30 DAY), ' 23:59:59'));
```

- [ ] **Verify the schema and seed backfill manually**

Run:

```bash
mysql -u part_time_work -ppassword123 part_time_work < scripts/backfill-company-logo.sql
mysql -u part_time_work -ppassword123 part_time_work -e "SELECT id, company_name, company_logo FROM enterprises ORDER BY id; SELECT job_id, company_name, company_logo FROM c_job ORDER BY job_id LIMIT 4;"
```

Expected:
- `enterprises.company_logo` is populated for the demo enterprises.
- `c_job.company_logo` is populated for the worker-side demo jobs.

- [ ] **Commit**

```bash
git add platform-service/src/main/resources/db/migration/V7__add_company_logo_to_enterprises.sql
git add c-service/src/main/resources/db/migration/V5__add_company_logo_to_c_job.sql
git add scripts/seed-data.sql scripts/backfill-company-logo.sql
git commit -m "feat: add enterprise logo columns and seed data"
```

---

### Task 2: Make platform-service the source of truth for enterprise logos

**Files:**
- Modify: `platform-service/src/main/java/com/parttime/platform/pojo/entity/Enterprise.java`
- Modify: `platform-service/src/main/java/com/parttime/platform/pojo/cmd/EnterpriseCreateCmd.java`
- Modify: `platform-service/src/main/java/com/parttime/platform/pojo/cmd/EnterpriseUpdateCmd.java`
- Modify: `platform-service/src/main/java/com/parttime/platform/pojo/vo/EnterpriseVO.java`
- Modify: `platform-service/src/main/resources/mapper/EnterpriseMapper.xml`
- Create: `platform-service/src/main/java/com/parttime/platform/mapper/CJobMapper.java`
- Create: `platform-service/src/main/resources/mapper/CJobMapper.xml`
- Modify: `platform-service/src/main/java/com/parttime/platform/service/impl/EnterpriseServiceImpl.java`
- Modify: `platform-pc/src/views/enterprises/EnterpriseList.vue`
- Modify: `platform-service/src/test/java/com/parttime/platform/service/EnterpriseServiceTest.java` (new)

- [ ] **Add `companyLogo` to the enterprise DTOs and entity**

Use the same field in all four classes:

```java
    @Schema(description = "企业logoURL")
    private String companyLogo;
```

- [ ] **Update `EnterpriseMapper.xml`**

Add the logo column everywhere the enterprise row is mapped:

```xml
        <result property="companyLogo" column="company_logo"/>
```

```xml
        INSERT INTO enterprises (company_name, company_logo, contact_name, contact_phone, company_address, business_license, status, registration_id)
        VALUES (#{companyName}, #{companyLogo}, #{contactName}, #{contactPhone}, #{companyAddress}, #{businessLicense}, #{status}, #{registrationId})
```

```xml
        SET company_name = #{companyName},
            company_logo = #{companyLogo},
            contact_name = #{contactName},
            contact_phone = #{contactPhone},
            company_address = #{companyAddress},
            business_license = #{businessLicense}
```

- [ ] **Add a platform-side `CJobMapper` for logo fan-out**

`platform-service/src/main/java/com/parttime/platform/mapper/CJobMapper.java`:

```java
@Mapper
public interface CJobMapper {
    int updateCompanyLogoByCompanyId(@Param("companyId") Long companyId,
                                     @Param("companyLogo") String companyLogo);
}
```

`platform-service/src/main/resources/mapper/CJobMapper.xml`:

```xml
<mapper namespace="com.parttime.platform.mapper.CJobMapper">
    <update id="updateCompanyLogoByCompanyId">
        UPDATE c_job
        SET company_logo = #{companyLogo}
        WHERE company_id = #{companyId}
    </update>
</mapper>
```

- [ ] **Update `EnterpriseServiceImpl` to persist and fan out the logo**

In `create(EnterpriseCreateCmd cmd)`:

```java
        e.setCompanyLogo(cmd.getCompanyLogo());
```

In `update(EnterpriseUpdateCmd cmd)`:

```java
        if (cmd.getCompanyLogo() != null) e.setCompanyLogo(cmd.getCompanyLogo());
        enterpriseMapper.update(e);
        cJobMapper.updateCompanyLogoByCompanyId(e.getId(), e.getCompanyLogo());
```

Also inject `CJobMapper`:

```java
    @Resource
    private CJobMapper cJobMapper;
```

- [ ] **Add logo inputs to `platform-pc/src/views/enterprises/EnterpriseList.vue`**

For both create and edit dialogs, add:

```vue
<el-form-item label="企业logo">
  <el-input v-model="createDialog.form.companyLogo" placeholder="logo图片URL" />
</el-form-item>
```

and the matching edit field.

For table rendering, add a logo column near company name:

```vue
<el-table-column label="Logo" width="80">
  <template #default="{ row }">
    <el-avatar :src="row.companyLogo" :size="36">
      {{ row.companyName?.slice(0, 1) }}
    </el-avatar>
  </template>
</el-table-column>
```

- [ ] **Write a focused service test**

Create `platform-service/src/test/java/com/parttime/platform/service/EnterpriseServiceTest.java` with a case that verifies create/update copies `companyLogo` and update triggers the c_job fan-out:

```java
@Mock private EnterpriseMapper enterpriseMapper;
@Mock private CJobMapper cJobMapper;

@Test
void update_shouldPersistLogoAndSyncCJob() {
    Enterprise e = new Enterprise();
    e.setId(1L);
    e.setCompanyName("示例企业");
    e.setCompanyLogo("https://cdn.example.com/logos/demo.png");
    when(enterpriseMapper.findById(1L)).thenReturn(Optional.of(e));

    EnterpriseUpdateCmd cmd = new EnterpriseUpdateCmd();
    cmd.setId(1L);
    cmd.setCompanyLogo("https://cdn.example.com/logos/new.png");

    enterpriseService.update(cmd);

    verify(enterpriseMapper).update(any(Enterprise.class));
    verify(cJobMapper).updateCompanyLogoByCompanyId(1L, "https://cdn.example.com/logos/new.png");
}
```

- [ ] **Commit**

```bash
git add platform-service/src/main/java/com/parttime/platform/pojo/entity/Enterprise.java
git add platform-service/src/main/java/com/parttime/platform/pojo/cmd/EnterpriseCreateCmd.java
git add platform-service/src/main/java/com/parttime/platform/pojo/cmd/EnterpriseUpdateCmd.java
git add platform-service/src/main/java/com/parttime/platform/pojo/vo/EnterpriseVO.java
git add platform-service/src/main/resources/mapper/EnterpriseMapper.xml
git add platform-service/src/main/java/com/parttime/platform/mapper/CJobMapper.java
git add platform-service/src/main/resources/mapper/CJobMapper.xml
git add platform-service/src/main/java/com/parttime/platform/service/impl/EnterpriseServiceImpl.java
git add platform-pc/src/views/enterprises/EnterpriseList.vue
git add platform-service/src/test/java/com/parttime/platform/service/EnterpriseServiceTest.java
git commit -m "feat: add enterprise logo source of truth"
```

---

### Task 3: Sync enterprise logos into `c_job` and expose them in job summaries

**Files:**
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/EnterpriseMapper.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/CJobMapper.java`
- Modify: `enterprise-service/src/main/resources/mapper/CJobMapper.xml`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/JobServiceImpl.java`
- Modify: `enterprise-service/src/test/java/com/parttime/enterprise/service/JobServiceTest.java`

- [ ] **Extend `EnterpriseMapper` with a logo lookup**

```java
String findCompanyLogoById(@Param("id") Long id);
```

- [ ] **Add `companyLogo` to the enterprise-service `CJobMapper` contract**

```java
void upsert(@Param("jobId") Long jobId,
            @Param("companyId") Long companyId,
            @Param("companyName") String companyName,
            @Param("companyLogo") String companyLogo,
            @Param("title") String title,
            @Param("description") String description,
            @Param("location") String location,
            @Param("province") String province,
            @Param("city") String city,
            @Param("district") String district,
            @Param("address") String address,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("categoryId") Long categoryId,
            @Param("rateType") String rateType,
            @Param("rateAmount") BigDecimal rateAmount,
            @Param("status") String status,
            @Param("scheduleInfo") String scheduleInfo,
            @Param("headcount") Integer headcount,
            @Param("deadline") LocalDateTime deadline);
```

`CJobMapper.xml` should include `company_logo` in the insert and update lists:

```xml
INSERT INTO c_job (job_id, company_id, company_name, company_logo, title, description, location,
                   province, city, district, address, latitude, longitude,
                   category_id, rate_type, rate_amount, status, schedule_info,
                   headcount, deadline)
VALUES (#{jobId}, #{companyId}, #{companyName}, #{companyLogo}, #{title}, #{description}, #{location},
        #{province}, #{city}, #{district}, #{address}, #{latitude}, #{longitude},
        #{categoryId}, #{rateType}, #{rateAmount}, #{status}, #{scheduleInfo},
        #{headcount}, #{deadline})
ON DUPLICATE KEY UPDATE
    company_name = VALUES(company_name),
    company_logo = VALUES(company_logo),
    title = VALUES(title),
    description = VALUES(description),
    location = VALUES(location),
    province = VALUES(province),
    city = VALUES(city),
    district = VALUES(district),
    address = VALUES(address),
    latitude = VALUES(latitude),
    longitude = VALUES(longitude),
    category_id = VALUES(category_id),
    rate_type = VALUES(rate_type),
    rate_amount = VALUES(rate_amount),
    status = VALUES(status),
    schedule_info = VALUES(schedule_info),
    headcount = VALUES(headcount),
    deadline = VALUES(deadline)
```

- [ ] **Update `syncToCJob` in `JobServiceImpl`**

Replace the current fan-out block with:

```java
String companyName = enterpriseMapper.findCompanyNameById(job.getCompanyId());
String companyLogo = enterpriseMapper.findCompanyLogoById(job.getCompanyId());
cJobMapper.upsert(
        job.getId(), job.getCompanyId(), companyName, companyLogo,
        job.getTitle(), job.getDescription(), job.getLocation(),
        job.getProvince(), job.getCity(), job.getDistrict(), job.getAddress(),
        job.getLatitude(), job.getLongitude(),
        job.getCategoryId(),
        null, null,
        job.getStatus(),
        scheduleInfo,
        job.getHeadcount(),
        job.getDeadline()
);
```

- [ ] **Add a list distance field to worker summaries**

In `c-service/src/main/java/com/parttime/cservice/pojo/vo/JobSummaryVO.java`:

```java
    @Schema(description = "企业名称")
    private String companyName;
    @Schema(description = "企业logo")
    private String companyLogo;
    @Schema(description = "距离(公里)")
    private BigDecimal distanceKm;
```

- [ ] **Update `JobServiceTest` for logo sync**

Add mocks:

```java
@Mock private EnterpriseMapper enterpriseMapper;
@Mock private CJobMapper cJobMapper;
```

Add an assertion in `publishJob_shouldTransitionFromDraftToPublished`:

```java
when(enterpriseMapper.findCompanyNameById(1L)).thenReturn("美味餐饮管理有限公司");
when(enterpriseMapper.findCompanyLogoById(1L)).thenReturn("https://cdn.example.com/logos/meiwei.png");
verify(cJobMapper).upsert(
        eq(1L), eq(1L), eq("美味餐饮管理有限公司"), eq("https://cdn.example.com/logos/meiwei.png"),
        anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
        any(), any(), any(), anyString(), any(), anyString(), anyString(), anyInt(), any());
```

- [ ] **Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/mapper/EnterpriseMapper.java
git add enterprise-service/src/main/java/com/parttime/enterprise/mapper/CJobMapper.java
git add enterprise-service/src/main/resources/mapper/CJobMapper.xml
git add enterprise-service/src/main/java/com/parttime/enterprise/service/impl/JobServiceImpl.java
git add enterprise-service/src/test/java/com/parttime/enterprise/service/JobServiceTest.java
git commit -m "feat: sync enterprise logos into worker jobs"
```

---

### Task 4: Sort worker jobs by current location and return distance

**Files:**
- Modify: `c-service/src/main/java/com/parttime/cservice/controller/JobController.java`
- Modify: `c-service/src/main/java/com/parttime/cservice/service/JobService.java`
- Modify: `c-service/src/main/java/com/parttime/cservice/service/impl/JobServiceImpl.java`
- Modify: `c-service/src/main/java/com/parttime/cservice/pojo/vo/JobSummaryVO.java`
- Modify: `c-service/src/main/resources/mapper/JobMapper.xml`
- Modify: `c-service/src/test/java/com/parttime/cservice/service/JobServiceTest.java`
- Modify: `c-service/src/test/java/com/parttime/cservice/controller/JobControllerTest.java`

- [ ] **Extend the `GET /api/jobs` contract**

`JobController.java` should accept optional coordinates:

```java
public ResponseEntity<List<JobSummaryVO>> searchJobs(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) BigDecimal minRate,
        @RequestParam(required = false) BigDecimal maxRate,
        @RequestParam(required = false) BigDecimal latitude,
        @RequestParam(required = false) BigDecimal longitude)
```

Pass them through to the service.

- [ ] **Sort by Haversine distance in `JobServiceImpl`**

Add a helper and sort before filtering to summary:

```java
private double distance(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
    if (lat2 == null || lng2 == null) return Double.MAX_VALUE;
    double earthRadius = 6371000D;
    double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
    double dLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1.doubleValue()))
            * Math.cos(Math.toRadians(lat2.doubleValue()))
            * Math.sin(dLng / 2) * Math.sin(dLng / 2);
    return earthRadius * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
}
```

In `toSummary(job)`, set the new fields:

```java
summary.setCompanyName(job.getCompanyName());
summary.setCompanyLogo(job.getCompanyLogo());
summary.setDistanceKm(distanceKm);
```

Format `distanceKm` to one decimal place in kilometers.

- [ ] **Ensure `JobMapper.xml` inserts/updates `company_logo`**

Update the `INSERT` and `UPDATE` statements to include the new column:

```sql
INSERT INTO c_job (job_id, company_id, company_name, company_logo, title, description, location,
                   province, city, district, address, latitude, longitude,
                   category_id, category_name, rate_type, rate_amount, status, published_at,
                   created_at, updated_at)
```

```sql
UPDATE c_job
SET title = #{title}, description = #{description}, location = #{location},
    province = #{province}, city = #{city}, district = #{district}, address = #{address},
    latitude = #{latitude}, longitude = #{longitude},
    category_id = #{categoryId}, category_name = #{categoryName},
    company_logo = #{companyLogo},
    rate_type = #{rateType}, rate_amount = #{rateAmount},
    status = #{status}
WHERE id = #{id}
```

- [ ] **Add a proximity regression test**

In `JobServiceTest`, seed two jobs with coordinates and verify the near one is first:

```java
jobService.addJob(1L, "Beijing Job", "北京岗位", "Beijing", 1L, "Tech", List.of(rate), List.of(schedule), 10, 0,
        LocalDateTime.of(2026, 6, 30, 23, 59), "PUBLISHED");
jobService.addJob(2L, "Shanghai Job", "上海岗位", "Shanghai", 1L, "Tech", List.of(rate), List.of(schedule), 10, 0,
        LocalDateTime.of(2026, 6, 30, 23, 59), "PUBLISHED");

List<JobSummaryVO> results = jobService.searchJobs(null, null, null, null, null,
        new BigDecimal("39.9"), new BigDecimal("116.4"));

assertThat(results.get(0).getId()).isEqualTo(1L);
assertThat(results.get(0).getDistanceKm()).isNotNull();
```

- [ ] **Commit**

```bash
git add c-service/src/main/java/com/parttime/cservice/controller/JobController.java
git add c-service/src/main/java/com/parttime/cservice/service/JobService.java
git add c-service/src/main/java/com/parttime/cservice/service/impl/JobServiceImpl.java
git add c-service/src/main/java/com/parttime/cservice/pojo/vo/JobSummaryVO.java
git add c-service/src/main/resources/mapper/JobMapper.xml
git add c-service/src/test/java/com/parttime/cservice/service/JobServiceTest.java
git add c-service/src/test/java/com/parttime/cservice/controller/JobControllerTest.java
git commit -m "feat: sort worker jobs by nearby location"
```

---

### Task 5: Update the worker job list UI to show logo, company, and distance

**Files:**
- Modify: `worker-uniapp/src/pages/jobs/jobList.vue`

- [ ] **Render enterprise logo and distance in each card**

Replace the card body with a small left-right layout:

```vue
<view class="job-card">
  <view class="job-card-top">
    <image v-if="job.companyLogo" class="company-logo" :src="job.companyLogo" mode="aspectFill" />
    <view v-else class="company-logo placeholder">{{ (job.companyName || '?').slice(0, 1) }}</view>
    <view class="job-main">
      <view class="job-card-header">
        <text class="job-title">{{ job.title }}</text>
        <text class="job-pay">{{ job.minRate }}-{{ job.maxRate }}元/{{ job.rateUnit || '小时' }}</text>
      </view>
      <view class="job-tags">
        <text v-if="job.jobType" class="tag">{{ job.jobType }}</text>
        <text v-if="job.experience" class="tag">{{ job.experience }}</text>
      </view>
      <view class="job-card-footer">
        <text class="job-location">{{ job.location }}</text>
        <text class="job-distance">{{ job.distanceKm != null ? `${job.distanceKm}km` : '' }}</text>
      </view>
      <text class="job-company">{{ job.companyName }}</text>
    </view>
  </view>
</view>
```

- [ ] **Keep the automatic geolocation on page enter**

Use the existing `loadCurrentLocation()` call on `onMounted`, and keep passing `latitude`/`longitude` into `getJobs({ keyword, categoryId, latitude, longitude })`:

```ts
const res: any = await getJobs({
  keyword: keyword.value || undefined,
  categoryId: categoryId.value,
  latitude: currentLocation.value?.latitude,
  longitude: currentLocation.value?.longitude
})
```

- [ ] **Add styles for the new layout**

```css
.job-card-top {
  display: flex;
  gap: 20rpx;
}

.company-logo {
  width: 88rpx;
  height: 88rpx;
  border-radius: 20rpx;
  background: #f5f5f5;
  flex-shrink: 0;
}

.company-logo.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #999;
}

.job-main {
  flex: 1;
}

.job-distance {
  font-size: 24rpx;
  color: #07c160;
}
```

- [ ] **Verify with a frontend build**

Run:

```bash
cd worker-uniapp && npm run build:mp-weixin
```

Expected: build completes without template or TS errors.

- [ ] **Commit**

```bash
git add worker-uniapp/src/pages/jobs/jobList.vue
git commit -m "feat: show enterprise logo and distance in worker job list"
```

---

### Task 6: End-to-end verification

**Files:**
- Verify: all modified backend/frontend files above

- [ ] **Run backend compilation**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -q -DskipTests compile
```

Run once in each module:
- `platform-service`
- `enterprise-service`
- `c-service`

Expected: exit code 0.

- [ ] **Run focused tests**

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -q -Dtest=EnterpriseServiceTest test
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -q -Dtest=JobServiceTest,JobControllerTest test
```

Expected: pass with the new logo sync and nearby sort assertions.

- [ ] **Run frontend builds**

```bash
cd platform-pc && npm run build
cd enterprise-pc && npm run build
cd worker-uniapp && npm run build:mp-weixin
```

Expected: all three builds finish cleanly.

- [ ] **Manual smoke check**

1. Open platform enterprise management and set a logo URL for a company.
2. Confirm the company logo is shown in the platform enterprise table.
3. Open worker job list, allow location, and verify the nearest jobs rise to the top.
4. Confirm each card shows company logo, company name, and a distance like `1.2km`.
