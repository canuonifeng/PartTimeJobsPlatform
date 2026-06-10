# Job 岗位地址与定位 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add structured address (province/city/district + detailed address) and geolocation coordinates (lat/lng) with map picking to the job creation/editing flow across all platforms.

**Architecture:** DB migration adds 6 columns to `jobs` and `c_job` tables. Backend entities/commands/VOs updated in enterprise-service and c-service. Frontend forms updated with region cascading pickers and map-based location picking (AMap JS API on web, `uni.chooseLocation()` on mobile). Worker-facing detail page shows location on `<map>` component.

**Tech Stack:** AMap JS API 2.0 (enterprise-pc), uni-app built-in `chooseLocation` + `<map>` (mobile), Element Plus `<el-cascader>` (enterprise-pc), MyBatis (both services)

---

### Task 1: Database migration SQL

**Files:**
- Create: `enterprise-service/src/main/resources/db/migration/V8__add_job_address_location.sql`
- Create: `c-service/src/main/resources/db/migration/V3__add_c_job_address_location.sql`

- [ ] **Create V8 migration for `jobs` table**

```sql
ALTER TABLE jobs
  ADD COLUMN province  VARCHAR(50)  DEFAULT NULL COMMENT '省' AFTER location,
  ADD COLUMN city      VARCHAR(50)  DEFAULT NULL COMMENT '市' AFTER province,
  ADD COLUMN district  VARCHAR(50)  DEFAULT NULL COMMENT '区' AFTER city,
  ADD COLUMN address   VARCHAR(200) DEFAULT NULL COMMENT '详细地址（街道门牌号）' AFTER district,
  ADD COLUMN latitude  DECIMAL(10,7) DEFAULT NULL COMMENT '纬度' AFTER address,
  ADD COLUMN longitude DECIMAL(10,7) DEFAULT NULL COMMENT '经度' AFTER address;
```

- [ ] **Create V3 migration for `c_job` table**

```sql
ALTER TABLE c_job
  ADD COLUMN province  VARCHAR(50)  DEFAULT NULL COMMENT '省',
  ADD COLUMN city      VARCHAR(50)  DEFAULT NULL COMMENT '市',
  ADD COLUMN district  VARCHAR(50)  DEFAULT NULL COMMENT '区',
  ADD COLUMN address   VARCHAR(200) DEFAULT NULL COMMENT '详细地址',
  ADD COLUMN latitude  DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  ADD COLUMN longitude DECIMAL(10,7) DEFAULT NULL COMMENT '经度';
```

- [ ] **Commit**

```bash
git add enterprise-service/src/main/resources/db/migration/V8__add_job_address_location.sql
git add c-service/src/main/resources/db/migration/V3__add_c_job_address_location.sql
git commit -m "feat: add address & location columns to jobs and c_job tables"
```

---

### Task 2: enterprise-service Job entity + Cmd + VO — add 6 fields

**Files:**
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/Job.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/cmd/JobCreateCmd.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/cmd/UpdateJobCmd.java`
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/JobVO.java`

- [ ] **Add fields to `Job.java` entity** (after `location` field). Add `import java.math.BigDecimal;` to imports.

```java
    @Schema(description = "省")
    private String province;
    @Schema(description = "市")
    private String city;
    @Schema(description = "区")
    private String district;
    @Schema(description = "详细地址（街道门牌号）")
    private String address;
    @Schema(description = "纬度")
    private BigDecimal latitude;
    @Schema(description = "经度")
    private BigDecimal longitude;
```

- [ ] **Add same 6 fields to `JobCreateCmd.java`** (same field declarations)

- [ ] **Add same 6 fields to `UpdateJobCmd.java`** (same field declarations)

- [ ] **Add same 6 fields to `JobVO.java`** (same field declarations)

- [ ] **Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/Job.java
git add enterprise-service/src/main/java/com/parttime/enterprise/pojo/cmd/JobCreateCmd.java
git add enterprise-service/src/main/java/com/parttime/enterprise/pojo/cmd/UpdateJobCmd.java
git add enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/JobVO.java
git commit -m "feat: add address & location fields to enterprise-service job classes"
```

---

### Task 3: enterprise-service JobMapper.xml — update SQL

**Files:**
- Modify: `enterprise-service/src/main/resources/mapper/JobMapper.xml`

- [ ] **Update resultMap** (add result lines after `location`)

```xml
        <result property="province" column="province"/>
        <result property="city" column="city"/>
        <result property="district" column="district"/>
        <result property="address" column="address"/>
        <result property="latitude" column="latitude"/>
        <result property="longitude" column="longitude"/>
```

- [ ] **Update INSERT** (add columns after `location`)

```sql
    INSERT INTO jobs (company_id, title, description, location, province, city, district, address, latitude, longitude, category_id, headcount, status, deadline)
    VALUES (#{companyId}, #{title}, #{description}, #{location}, #{province}, #{city}, #{district}, #{address}, #{latitude}, #{longitude}, #{categoryId}, #{headcount}, #{status}, #{deadline})
```

- [ ] **Update UPDATE** (add columns after `location = #{location}`)

```sql
    SET title = #{title}, description = #{description}, location = #{location},
        province = #{province}, city = #{city}, district = #{district}, address = #{address},
        latitude = #{latitude}, longitude = #{longitude},
        category_id = #{categoryId}, headcount = #{headcount}, deadline = #{deadline}
```

- [ ] **Commit**

```bash
git add enterprise-service/src/main/resources/mapper/JobMapper.xml
git commit -m "feat: update JobMapper SQL for address & location columns"
```

---

### Task 4: enterprise-service JobServiceImpl — auto-join location + handle new fields

**Files:**
- Modify: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/JobServiceImpl.java`

- [ ] **Update `createJob` method** — set province, city, district, address, lat, lng on the Job entity, and auto-join `location`:

After `job.setLocation(request.getLocation());` add:

```java
        job.setProvince(request.getProvince());
        job.setCity(request.getCity());
        job.setDistrict(request.getDistrict());
        job.setAddress(request.getAddress());
        job.setLatitude(request.getLatitude());
        job.setLongitude(request.getLongitude());
        if (request.getProvince() != null || request.getCity() != null || request.getDistrict() != null || request.getAddress() != null) {
            StringBuilder sb = new StringBuilder();
            if (request.getProvince() != null) sb.append(request.getProvince());
            if (request.getCity() != null) sb.append(' ').append(request.getCity());
            if (request.getDistrict() != null) sb.append(' ').append(request.getDistrict());
            if (request.getAddress() != null) sb.append(' ').append(request.getAddress());
            job.setLocation(sb.toString().trim());
        }
```

- [ ] **Update `updateJob` method** — handle new fields in the if-set pattern:

After `if (request.getLocation() != null) job.setLocation(request.getLocation());` add:

```java
        if (request.getProvince() != null) job.setProvince(request.getProvince());
        if (request.getCity() != null) job.setCity(request.getCity());
        if (request.getDistrict() != null) job.setDistrict(request.getDistrict());
        if (request.getAddress() != null) job.setAddress(request.getAddress());
        if (request.getLatitude() != null) job.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) job.setLongitude(request.getLongitude());
```

- [ ] **Update `toResponse` method** — map from entity to VO:

After `response.setLocation(job.getLocation());` add:

```java
        response.setProvince(job.getProvince());
        response.setCity(job.getCity());
        response.setDistrict(job.getDistrict());
        response.setAddress(job.getAddress());
        response.setLatitude(job.getLatitude());
        response.setLongitude(job.getLongitude());
```

Add import for `java.math.BigDecimal` at top of file.

- [ ] **Commit**

```bash
git add enterprise-service/src/main/java/com/parttime/enterprise/service/impl/JobServiceImpl.java
git commit -m "feat: handle address & location fields in JobServiceImpl create/update/response"
```

---

### Task 5: c-service Job entity + VOs — add 6 fields

**Files:**
- Modify: `c-service/src/main/java/com/parttime/cservice/pojo/entity/Job.java`
- Modify: `c-service/src/main/java/com/parttime/cservice/pojo/vo/JobDetailVO.java`
- Modify: `c-service/src/main/java/com/parttime/cservice/pojo/vo/JobSummaryVO.java`

- [ ] **Add fields to `c-service Job.java` entity** (after `location`)

```java
    @Schema(description = "省")
    private String province;
    @Schema(description = "市")
    private String city;
    @Schema(description = "区")
    private String district;
    @Schema(description = "详细地址（街道门牌号）")
    private String address;
    @Schema(description = "纬度")
    private BigDecimal latitude;
    @Schema(description = "经度")
    private BigDecimal longitude;
```

Also update the constructor to accept these new fields, or just remove the constructor and rely on setters (since the `addJob` service method uses setters anyway).

- [ ] **Add fields to `JobDetailVO.java`** (same 6 fields)

- [ ] **Add province/city/district to `JobSummaryVO.java`** (no lat/lng for list display)

```java
    @Schema(description = "省")
    private String province;
    @Schema(description = "市")
    private String city;
    @Schema(description = "区")
    private String district;
```

- [ ] **Commit**

```bash
git add c-service/src/main/java/com/parttime/cservice/pojo/entity/Job.java
git add c-service/src/main/java/com/parttime/cservice/pojo/vo/JobDetailVO.java
git add c-service/src/main/java/com/parttime/cservice/pojo/vo/JobSummaryVO.java
git commit -m "feat: add address & location fields to c-service job classes"
```

---

### Task 6: c-service JobMapper.xml + JobServiceImpl — update SQL and sync logic

**Files:**
- Modify: `c-service/src/main/resources/mapper/JobMapper.xml`
- Modify: `c-service/src/main/java/com/parttime/cservice/service/impl/JobServiceImpl.java`

- [ ] **Update `c-service JobMapper.xml` INSERT** (add columns after `location`)

```sql
    INSERT INTO c_job (job_id, company_id, company_name, title, description, location,
                       province, city, district, address, latitude, longitude,
                       category_id, category_name, rate_type, rate_amount, status, published_at,
                       created_at, updated_at)
    VALUES (#{jobId}, #{companyId}, #{companyName}, #{title}, #{description}, #{location},
            #{province}, #{city}, #{district}, #{address}, #{latitude}, #{longitude},
            #{categoryId}, #{categoryName}, #{rateType}, #{rateAmount}, #{status}, #{publishedAt},
            #{createdAt}, #{updatedAt})
```

- [ ] **Update `c-service JobMapper.xml` UPDATE** (add columns after `location = #{location}`)

```sql
    SET title = #{title}, description = #{description}, location = #{location},
        province = #{province}, city = #{city}, district = #{district}, address = #{address},
        latitude = #{latitude}, longitude = #{longitude},
        category_id = #{categoryId}, category_name = #{categoryName},
        rate_type = #{rateType}, rate_amount = #{rateAmount},
        status = #{status}
```

- [ ] **Update `c-service JobServiceImpl.java` `addJob` method signature** — add 6 new params:

```java
    public Job addJob(Long id, String title, String description, String location,
                      String province, String city, String district, String address,
                      BigDecimal latitude, BigDecimal longitude,
                      Long categoryId, String categoryName,
                      List<JobRateInfoVO> rates, List<JobScheduleInfoVO> schedules,
                      Integer headcount, Integer acceptedCount, LocalDateTime deadline, String status)
```

Inside `addJob`, after `job.setLocation(location);` add:

```java
        job.setProvince(province);
        job.setCity(city);
        job.setDistrict(district);
        job.setAddress(address);
        job.setLatitude(latitude);
        job.setLongitude(longitude);
```

- [ ] **Commit**

```bash
git add c-service/src/main/resources/mapper/JobMapper.xml
git add c-service/src/main/java/com/parttime/cservice/service/impl/JobServiceImpl.java
git commit -m "feat: update c-service JobMapper XML and service for address & location"
```

---

### Task 7: enterprise-pc region data + LocationPicker component

**Files:**
- Create: `enterprise-pc/src/assets/regions.json`
- Create: `enterprise-pc/src/components/LocationPicker.vue`
- Modify: `enterprise-pc/src/views/jobs/JobForm.vue`

- [ ] **Create `enterprise-pc/src/assets/regions.json`** — China province/city/district cascading data.
Use a simplified JSON with the structure:
```json
[
  {
    "value": "北京市",
    "label": "北京市",
    "children": [
      {
        "value": "东城区",
        "label": "东城区"
      },
      {
        "value": "西城区",
        "label": "西城区"
      }
    ]
  },
  {
    "value": "上海市",
    "label": "上海市",
    "children": [
      {
        "value": "黄浦区",
        "label": "黄浦区"
      }
    ]
  }
]
```
Include major provinces: 北京市, 天津市, 上海市, 重庆市, 河北省, 山西省, 辽宁省, 吉林省, 黑龙江省, 江苏省, 浙江省, 安徽省, 福建省, 江西省, 山东省, 河南省, 湖北省, 湖南省, 广东省, 海南省, 四川省, 贵州省, 云南省, 陕西省, 甘肃省, 青海省, 台湾省, 内蒙古, 广西, 西藏, 宁夏, 新疆. Each province has representative cities and districts.

- [ ] **Create `LocationPicker.vue` component** — AMap-based location picker dialog.

```vue
<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElDialog, ElButton } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  latitude: { type: Number, default: 39.9042 },
  longitude: { type: Number, default: 116.4074 }
})
const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = ref(false)
let map = null
let marker = null

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    nextTick(() => initMap())
  }
})

function initMap() {
  if (!window.AMap) return
  map = new window.AMap.Map('location-map-container', {
    zoom: 14,
    center: [props.longitude, props.latitude]
  })
  marker = new window.AMap.Marker({
    position: [props.longitude, props.latitude],
    draggable: true,
    map: map
  })
  map.on('click', (e) => {
    marker.setPosition(e.lnglat)
  })
}

function handleConfirm() {
  if (marker) {
    const pos = marker.getPosition()
    emit('confirm', {
      latitude: pos.lat,
      longitude: pos.lng,
      address: ''
    })
  }
  visible.value = false
  emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog :model-value="visible" title="选择位置" width="600px" @close="emit('update:modelValue', false)">
    <div id="location-map-container" style="width:100%;height:400px"></div>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确认</el-button>
    </template>
  </el-dialog>
</template>
```

- [ ] **Update `enterprise-pc/index.html`** — Add AMap script load before `</head>`:

```html
<script src="https://webapi.amap.com/maps?v=2.0&key=YOUR_KEY"></script>
```

- [ ] **Update `JobForm.vue`** — Replace single `location` input with region picker + address + map location.

Add imports:
```js
import regions from '@/assets/regions.json'
import LocationPicker from '@/components/LocationPicker.vue'
```

Add to `form` default:
```js
  province: '',
  city: '',
  district: '',
  address: '',
  latitude: null,
  longitude: null,
```

Replace the old "工作地点" form item with:
```html
<el-form-item label="省/市/区" prop="province" :rules="[{ required: true, message: '请选择省市区' }]">
  <el-cascader v-model="regionSelected" :options="regions" placeholder="选择省/市/区" style="width: 100%" />
</el-form-item>
<el-form-item label="详细地址" prop="address">
  <el-input v-model="form.address" placeholder="街道、门牌号" />
</el-form-item>
<el-form-item label="坐标定位">
  <el-button @click="showLocationPicker = true">选择位置</el-button>
  <span v-if="form.latitude" style="margin-left:12px;color:#999">{{ form.latitude.toFixed(6) }}, {{ form.longitude.toFixed(6) }}</span>
</el-form-item>
```

Add `regionSelected` computed:
```js
const regionSelected = computed({
  get: () => form.value.province ? [form.value.province, form.value.city, form.value.district].filter(Boolean) : [],
  set: (val) => {
    if (val && val.length >= 1) form.value.province = val[0]
    if (val && val.length >= 2) form.value.city = val[1]
    if (val && val.length >= 3) form.value.district = val[2]
  }
})
```

- [ ] **Commit**

```bash
git add enterprise-pc/src/assets/regions.json
git add enterprise-pc/src/components/LocationPicker.vue
git add enterprise-pc/src/views/jobs/JobForm.vue
git add enterprise-pc/index.html
git commit -m "feat: add region cascader and map location picker to enterprise-pc JobForm"
```

---

### Task 8: enterprise-uniapp job form — region picker + map location

**Files:**
- Create: `enterprise-uniapp/src/assets/regions.json`
- Modify: `enterprise-uniapp/src/pages/jobs/jobForm.vue`

- [ ] **Create `enterprise-uniapp/src/assets/regions.json`** — same China region data as enterprise-pc but structured for multiSelector:

```json
{
  "provinces": ["北京市", "上海市", "天津市", ...],
  "cities": {
    "北京市": ["东城区", "西城区", ...],
    ...
  }
}
```

Actually, for uni-app multiSelector, the simplest approach is to have flat arrays per level:

```js
// Convert the tree data to 3 parallel arrays for picker
[{label: '北京市', children: [{label: '东城区'}, {label: '西城区'}]}]
```

Use the same tree structure as enterprise-pc's regions.json and transform it in the page script.

- [ ] **Update `jobForm.vue`** — Replace location input with region picker + address + chooseLocation.

Add to `formData`:
```js
  province: '',
  city: '',
  district: '',
  address: '',
  latitude: null,
  longitude: null,
```

Import regions:
```js
import regions from '@/assets/regions.json'
```

Setup picker data:
```js
const provinceList = regions.map(r => r.label)
const cityList = ref([])
const districtList = ref([])
const regionIndexes = ref([0, 0, 0])

function onRegionChange(e) {
  const [pIdx, cIdx, dIdx] = e.detail.value
  regionIndexes.value = [pIdx, cIdx, dIdx]
  const province = regions[pIdx]
  formData.value.province = province.label
  if (province.children?.[cIdx]) {
    formData.value.city = province.children[cIdx].label
    const city = province.children[cIdx]
    if (city.children?.[dIdx]) {
      formData.value.district = city.children[dIdx].label
    }
  }
  // Rebuild city/district lists
  cityList.value = province.children?.map(c => c.label) || []
  districtList.value = province.children?.[regionIndexes.value[1]]?.children?.map(d => d.label) || []
}
```

Replace old location input with:
```html
<view class="form-item">
  <text class="label">省/市/区</text>
  <picker mode="multiSelector" :range="[provinceList, cityList, districtList]" :value="regionIndexes" @change="onRegionChange">
    <view class="picker">
      <text v-if="formData.province" class="picker-value">{{ formData.province }} {{ formData.city }} {{ formData.district }}</text>
      <text v-else class="picker-placeholder">请选择省/市/区</text>
    </view>
  </picker>
</view>

<view class="form-item">
  <text class="label">详细地址</text>
  <input v-model="formData.address" class="input" placeholder="街道、门牌号" />
</view>

<view class="form-item">
  <text class="label">坐标定位</text>
  <button class="location-btn" @click="chooseLocation">选择位置</button>
  <text v-if="formData.latitude" class="location-coords">{{ formData.latitude }}, {{ formData.longitude }}</text>
</view>
```

Add chooseLocation handler:
```js
function chooseLocation() {
  uni.chooseLocation({
    success: (res) => {
      formData.value.latitude = res.latitude
      formData.value.longitude = res.longitude
      if (res.address && !formData.value.address) {
        formData.value.address = res.address
      }
    },
    fail: () => {
      uni.showToast({ title: '定位失败', icon: 'none' })
    }
  })
}
```

Also update `handleSave` to pass the new fields in create/update payload.

- [ ] **Commit**

```bash
git add enterprise-uniapp/src/assets/regions.json
git add enterprise-uniapp/src/pages/jobs/jobForm.vue
git commit -m "feat: add region picker and uni.chooseLocation to enterprise-uniapp job form"
```

---

### Task 9: worker-uniapp job detail — show map with location

**Files:**
- Modify: `worker-uniapp/src/pages/jobs/jobDetail.vue`

- [ ] **Add map section** in jobDetail template between `info-section` and first `section`:

```html
<view class="map-section" v-if="job.latitude && job.longitude">
  <map :latitude="job.latitude" :longitude="job.longitude" :markers="markers" style="width:100%;height:300rpx;border-radius:16rpx" />
  <text class="map-address">{{ job.province }} {{ job.city }} {{ job.district }} {{ job.address }}</text>
</view>
```

Add to script setup:
```js
const markers = computed(() => {
  if (!job.value?.latitude || !job.value?.longitude) return []
  return [{
    latitude: job.value.latitude,
    longitude: job.value.longitude,
    title: job.value.title || ''
  }]
})
```

Add styles:
```css
.map-section {
  margin-bottom: 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
}
.map-address {
  display: block;
  padding: 12rpx 16rpx;
  background: #fff;
  font-size: 24rpx;
  color: #666;
}
```

- [ ] **Commit**

```bash
git add worker-uniapp/src/pages/jobs/jobDetail.vue
git commit -m "feat: show job location map on worker-uniapp job detail page"
```
