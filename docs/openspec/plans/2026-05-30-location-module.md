# 工作地点模块 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a reusable work location CRUD module to enterprise-pc + enterprise-service, with integration into JobForm for auto-filling location fields.

**Architecture:** New `company_locations` table, full backend CRUD layer following Account module patterns (POST-style controller, service, mapper), frontend list page with inline dialog (matching AccountList pattern), and a location picker dialog in JobForm.

**Tech Stack:** Spring Boot + MyBatis + MySQL (backend), Vue 3 + Element Plus + Axios (frontend)

---

### Task 1: Database table

**Files:**
- Create: `scripts/company_locations.sql`

- [ ] **Write and run the DDL**

```sql
CREATE TABLE IF NOT EXISTS company_locations (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id  BIGINT       NOT NULL,
  name        VARCHAR(100) COMMENT '地点名称',
  province    VARCHAR(50),
  city        VARCHAR(50),
  district    VARCHAR(50),
  address     VARCHAR(255) COMMENT '详细地址',
  latitude    DECIMAL(10,7),
  longitude   DECIMAL(10,7),
  status      VARCHAR(20) DEFAULT 'ENABLED' COMMENT 'ENABLED / DISABLED',
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

Run: `mysql -u root -D part_time_work < scripts/company_locations.sql`

- [ ] **Verify table exists**

Run: `mysql -u root -D part_time_work -e "DESCRIBE company_locations;"`
Expected: 12 columns as defined above

---

### Task 2: Backend - Entity + VO + Cmd

**Files:**
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/entity/CompanyLocation.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/CompanyLocationVO.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/cmd/LocationCreateCmd.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/cmd/LocationUpdateCmd.java`

- [ ] **Create Entity: CompanyLocation.java**

```java
package com.parttime.enterprise.pojo.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CompanyLocation {
    private Long id;
    private Long companyId;
    private String name;
    private String province;
    private String city;
    private String district;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Create VO: CompanyLocationVO.java**

```java
package com.parttime.enterprise.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CompanyLocationVO {
    @Schema(description = "地点ID")
    private Long id;
    @Schema(description = "地点名称")
    private String name;
    @Schema(description = "省")
    private String province;
    @Schema(description = "市")
    private String city;
    @Schema(description = "区")
    private String district;
    @Schema(description = "详细地址")
    private String address;
    @Schema(description = "纬度")
    private BigDecimal latitude;
    @Schema(description = "经度")
    private BigDecimal longitude;
    @Schema(description = "状态 ENABLED/DISABLED")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
```

- [ ] **Create Cmd: LocationCreateCmd.java**

```java
package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LocationCreateCmd {
    @Schema(description = "地点名称")
    private String name;
    @Schema(description = "省")
    private String province;
    @Schema(description = "市")
    private String city;
    @Schema(description = "区")
    private String district;
    @Schema(description = "详细地址")
    private String address;
    @Schema(description = "纬度")
    private BigDecimal latitude;
    @Schema(description = "经度")
    private BigDecimal longitude;
}
```

- [ ] **Create Cmd: LocationUpdateCmd.java**

```java
package com.parttime.enterprise.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LocationUpdateCmd {
    @Schema(description = "地点ID")
    private Long id;
    @Schema(description = "地点名称")
    private String name;
    @Schema(description = "省")
    private String province;
    @Schema(description = "市")
    private String city;
    @Schema(description = "区")
    private String district;
    @Schema(description = "详细地址")
    private String address;
    @Schema(description = "纬度")
    private BigDecimal latitude;
    @Schema(description = "经度")
    private BigDecimal longitude;
}
```

---

### Task 3: Backend - Mapper

**Files:**
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/CompanyLocationMapper.java`
- Create: `enterprise-service/src/main/resources/mapper/CompanyLocationMapper.xml`

- [ ] **Create CompanyLocationMapper.java**

```java
package com.parttime.enterprise.mapper;

import com.parttime.enterprise.pojo.entity.CompanyLocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CompanyLocationMapper {
    List<CompanyLocation> findByCompanyId(@Param("companyId") Long companyId);
    Optional<CompanyLocation> findById(@Param("id") Long id);
    int insert(CompanyLocation location);
    int update(CompanyLocation location);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    int deleteById(@Param("id") Long id);
}
```

- [ ] **Create CompanyLocationMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.enterprise.mapper.CompanyLocationMapper">

    <resultMap id="LocationResultMap" type="com.parttime.enterprise.pojo.entity.CompanyLocation">
        <id property="id" column="id"/>
        <result property="companyId" column="company_id"/>
        <result property="name" column="name"/>
        <result property="province" column="province"/>
        <result property="city" column="city"/>
        <result property="district" column="district"/>
        <result property="address" column="address"/>
        <result property="latitude" column="latitude"/>
        <result property="longitude" column="longitude"/>
        <result property="status" column="status"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>

    <select id="findByCompanyId" resultMap="LocationResultMap">
        SELECT * FROM company_locations WHERE company_id = #{companyId} ORDER BY created_at DESC
    </select>

    <select id="findById" resultMap="LocationResultMap">
        SELECT * FROM company_locations WHERE id = #{id}
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO company_locations (company_id, name, province, city, district, address, latitude, longitude, status)
        VALUES (#{companyId}, #{name}, #{province}, #{city}, #{district}, #{address}, #{latitude}, #{longitude}, 'ENABLED')
    </insert>

    <update id="update">
        UPDATE company_locations
        SET name = #{name}, province = #{province}, city = #{city}, district = #{district},
            address = #{address}, latitude = #{latitude}, longitude = #{longitude}
        WHERE id = #{id}
    </update>

    <update id="updateStatus">
        UPDATE company_locations SET status = #{status} WHERE id = #{id}
    </update>

    <delete id="deleteById">
        DELETE FROM company_locations WHERE id = #{id}
    </delete>

</mapper>
```

---

### Task 4: Backend - Service

**Files:**
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/service/CompanyLocationService.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/CompanyLocationServiceImpl.java`

- [ ] **Create CompanyLocationService.java**

```java
package com.parttime.enterprise.service;

import com.parttime.enterprise.pojo.cmd.LocationCreateCmd;
import com.parttime.enterprise.pojo.cmd.LocationUpdateCmd;
import com.parttime.enterprise.pojo.vo.CompanyLocationVO;

import java.util.List;

public interface CompanyLocationService {
    List<CompanyLocationVO> list(Long companyId);
    CompanyLocationVO create(LocationCreateCmd cmd, Long companyId);
    CompanyLocationVO update(LocationUpdateCmd cmd);
    void delete(Long id);
    void enable(Long id);
    void disable(Long id);
}
```

- [ ] **Create CompanyLocationServiceImpl.java**

```java
package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.CompanyLocationMapper;
import com.parttime.enterprise.pojo.cmd.LocationCreateCmd;
import com.parttime.enterprise.pojo.cmd.LocationUpdateCmd;
import com.parttime.enterprise.pojo.entity.CompanyLocation;
import com.parttime.enterprise.pojo.vo.CompanyLocationVO;
import com.parttime.enterprise.service.CompanyLocationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyLocationServiceImpl implements CompanyLocationService {

    @Resource
    private CompanyLocationMapper companyLocationMapper;

    @Override
    public List<CompanyLocationVO> list(Long companyId) {
        return companyLocationMapper.findByCompanyId(companyId)
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public CompanyLocationVO create(LocationCreateCmd cmd, Long companyId) {
        CompanyLocation entity = new CompanyLocation();
        entity.setCompanyId(companyId);
        entity.setName(cmd.getName());
        entity.setProvince(cmd.getProvince());
        entity.setCity(cmd.getCity());
        entity.setDistrict(cmd.getDistrict());
        entity.setAddress(cmd.getAddress());
        entity.setLatitude(cmd.getLatitude());
        entity.setLongitude(cmd.getLongitude());
        companyLocationMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public CompanyLocationVO update(LocationUpdateCmd cmd) {
        CompanyLocation entity = companyLocationMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("地点不存在"));
        entity.setName(cmd.getName());
        entity.setProvince(cmd.getProvince());
        entity.setCity(cmd.getCity());
        entity.setDistrict(cmd.getDistrict());
        entity.setAddress(cmd.getAddress());
        entity.setLatitude(cmd.getLatitude());
        entity.setLongitude(cmd.getLongitude());
        companyLocationMapper.update(entity);
        return toVO(entity);
    }

    @Override
    public void delete(Long id) {
        CompanyLocation entity = companyLocationMapper.findById(id)
                .orElseThrow(() -> new BusinessException("地点不存在"));
        companyLocationMapper.deleteById(id);
    }

    @Override
    public void enable(Long id) {
        companyLocationMapper.updateStatus(id, "ENABLED");
    }

    @Override
    public void disable(Long id) {
        companyLocationMapper.updateStatus(id, "DISABLED");
    }

    private CompanyLocationVO toVO(CompanyLocation entity) {
        CompanyLocationVO vo = new CompanyLocationVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setProvince(entity.getProvince());
        vo.setCity(entity.getCity());
        vo.setDistrict(entity.getDistrict());
        vo.setAddress(entity.getAddress());
        vo.setLatitude(entity.getLatitude());
        vo.setLongitude(entity.getLongitude());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
```

---

### Task 5: Backend - Controller

**Files:**
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/controller/CompanyLocationController.java`

- [ ] **Create CompanyLocationController.java**

```java
package com.parttime.enterprise.controller;

import com.parttime.enterprise.config.SecurityUtil;
import com.parttime.enterprise.pojo.cmd.LocationCreateCmd;
import com.parttime.enterprise.pojo.cmd.LocationUpdateCmd;
import com.parttime.enterprise.pojo.vo.CompanyLocationVO;
import com.parttime.enterprise.service.CompanyLocationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
public class CompanyLocationController {

    @Resource
    private CompanyLocationService companyLocationService;

    @Operation(summary = "获取地点列表")
    @PostMapping("/list")
    public ResponseEntity<List<CompanyLocationVO>> list() {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ResponseEntity.ok(companyLocationService.list(companyId));
    }

    @Operation(summary = "新增地点")
    @PostMapping("/create")
    public ResponseEntity<CompanyLocationVO> create(@RequestBody LocationCreateCmd cmd) {
        Long companyId = SecurityUtil.getCurrentCompanyId();
        return ResponseEntity.ok(companyLocationService.create(cmd, companyId));
    }

    @Operation(summary = "修改地点")
    @PostMapping("/update")
    public ResponseEntity<CompanyLocationVO> update(@RequestBody LocationUpdateCmd cmd) {
        return ResponseEntity.ok(companyLocationService.update(cmd));
    }

    @Operation(summary = "删除地点")
    @PostMapping("/delete")
    public ResponseEntity<Void> delete(@RequestBody Map<String, Long> body) {
        companyLocationService.delete(body.get("id"));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "启用地点")
    @PostMapping("/enable")
    public ResponseEntity<Void> enable(@RequestBody Map<String, Long> body) {
        companyLocationService.enable(body.get("id"));
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "禁用地点")
    @PostMapping("/disable")
    public ResponseEntity<Void> disable(@RequestBody Map<String, Long> body) {
        companyLocationService.disable(body.get("id"));
        return ResponseEntity.ok().build();
    }
}
```

---

### Task 6: Frontend - API

**Files:**
- Create: `enterprise-pc/src/api/location.js`

- [ ] **Create api/location.js**

```javascript
import request from './request'

export function listLocations() {
  return request.post('/locations/list')
}

export function createLocation(data) {
  return request.post('/locations/create', data)
}

export function updateLocation(data) {
  return request.post('/locations/update', data)
}

export function deleteLocation(id) {
  return request.post('/locations/delete', { id })
}

export function enableLocation(id) {
  return request.post('/locations/enable', { id })
}

export function disableLocation(id) {
  return request.post('/locations/disable', { id })
}
```

---

### Task 7: Frontend - LocationList page

**Files:**
- Create: `enterprise-pc/src/views/locations/LocationList.vue`

- [ ] **Create LocationList.vue**

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listLocations, createLocation, updateLocation, deleteLocation, enableLocation, disableLocation } from '../../api/location'

const loading = ref(false)
const locations = ref([])
const formDialog = ref({ visible: false, isEdit: false, form: {} })

async function fetchData() {
  loading.value = true
  try {
    locations.value = await listLocations()
  } finally {
    loading.value = false
  }
}

function resetForm() {
  return { name: '', province: '', city: '', district: '', address: '', latitude: null, longitude: null }
}

function handleAdd() {
  formDialog.value = { visible: true, isEdit: false, form: resetForm() }
}

function handleEdit(row) {
  formDialog.value = {
    visible: true,
    isEdit: true,
    form: {
      id: row.id,
      name: row.name,
      province: row.province,
      city: row.city,
      district: row.district,
      address: row.address,
      latitude: row.latitude,
      longitude: row.longitude
    }
  }
}

async function confirmSave() {
  const f = formDialog.value
  if (f.isEdit) {
    await updateLocation(f.form)
    ElMessage.success('修改成功')
  } else {
    await createLocation(f.form)
    ElMessage.success('新增成功')
  }
  f.visible = false
  await fetchData()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除地点 "${row.name}"？`, '确认')
    await deleteLocation(row.id)
    ElMessage.success('已删除')
    await fetchData()
  } catch { /* cancelled */ }
}

async function handleToggle(row) {
  try {
    if (row.status === 'ENABLED') {
      await disableLocation(row.id)
      ElMessage.success('已禁用')
    } else {
      await enableLocation(row.id)
      ElMessage.success('已启用')
    }
    await fetchData()
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(fetchData)
</script>

<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>工作地点</span>
        <el-button type="primary" @click="handleAdd">新建地点</el-button>
      </div>
    </template>
    <el-table :data="locations" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="name" label="名称" min-width="120" />
      <el-table-column prop="province" label="省" width="100" />
      <el-table-column prop="city" label="市" width="100" />
      <el-table-column prop="district" label="区" width="100" />
      <el-table-column prop="address" label="详细地址" min-width="200" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
            {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 'ENABLED' ? 'warning' : 'success'" @click="handleToggle(row)">
            {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="formDialog.visible" :title="formDialog.isEdit ? '编辑地点' : '新建地点'" width="500px" :close-on-click-modal="false">
    <el-form :model="formDialog.form" label-width="80px">
      <el-form-item label="名称">
        <el-input v-model="formDialog.form.name" placeholder="如：总部、分店A" />
      </el-form-item>
      <el-form-item label="省">
        <el-input v-model="formDialog.form.province" />
      </el-form-item>
      <el-form-item label="市">
        <el-input v-model="formDialog.form.city" />
      </el-form-item>
      <el-form-item label="区">
        <el-input v-model="formDialog.form.district" />
      </el-form-item>
      <el-form-item label="详细地址">
        <el-input v-model="formDialog.form.address" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="纬度">
        <el-input-number v-model="formDialog.form.latitude" :precision="7" :step="0.01" style="width:100%" />
      </el-form-item>
      <el-form-item label="经度">
        <el-input-number v-model="formDialog.form.longitude" :precision="7" :step="0.01" style="width:100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmSave">保存</el-button>
    </template>
  </el-dialog>
</template>
```

---

### Task 8: Frontend - Router + Nav

**Files:**
- Modify: `enterprise-pc/src/router/index.js`
- Modify: `enterprise-pc/src/App.vue`

- [ ] **Add /locations route to router/index.js**

Insert before the `/settings` route:

```javascript
  {
    path: '/locations',
    name: 'LocationList',
    component: () => import('../views/locations/LocationList.vue'),
    meta: { requiresAuth: true, title: '工作地点' }
  },
```

- [ ] **Add nav item to App.vue**

Insert before the "企业设置" menu item:

```html
        <el-menu-item index="/locations">
          <el-icon><Location /></el-icon>
          <span>工作地点</span>
        </el-menu-item>
```

Also verify `MapLocation` icon exists in element-plus icons. If not, use `Location` icon instead.

---

### Task 9: Frontend - JobForm location picker

**Files:**
- Modify: `enterprise-pc/src/views/jobs/JobForm.vue`

- [ ] **Import listLocations API at top of script**

```javascript
import { listLocations } from '../../api/location'
```

- [ ] **Add reactive state for location picker**

```javascript
const locationDialogVisible = ref(false)
const availableLocations = ref([])
```

- [ ] **Add template: location picker button + dialog**

Insert immediately before the first form item (province), after the `</el-form-item>` for imageUrl:

```html
        <el-form-item label="工作地点">
          <el-button @click="openLocationPicker">选择已有地点</el-button>
        </el-form-item>
```

- [ ] **Add the location picker dialog**

Insert after the last `</el-form>` but before `</el-card>` or at end of template:

```html
  <el-dialog v-model="locationDialogVisible" title="选择工作地点" width="600px">
    <el-table :data="availableLocations" stripe @row-click="selectLocation" highlight-current-row>
      <el-table-column prop="name" label="名称" width="120" />
      <el-table-column prop="province" label="省" width="80" />
      <el-table-column prop="city" label="市" width="80" />
      <el-table-column prop="district" label="区" width="80" />
      <el-table-column prop="address" label="详细地址" min-width="180" />
    </el-table>
    <template #footer>
      <el-button @click="locationDialogVisible = false">取消</el-button>
    </template>
  </el-dialog>
```

- [ ] **Add handler functions**

```javascript
async function openLocationPicker() {
  try {
    availableLocations.value = await listLocations()
    locationDialogVisible.value = true
  } catch {
    ElMessage.error('加载地点列表失败')
  }
}

function selectLocation(loc) {
  form.value.imageUrl = form.value.imageUrl || ''  // preserve existing
  form.value.province = loc.province || ''
  form.value.city = loc.city || ''
  form.value.district = loc.district || ''
  form.value.address = loc.address || ''
  form.value.latitude = loc.latitude
  form.value.longitude = loc.longitude
  locationDialogVisible.value = false
  ElMessage.success(`已选择地点：${loc.name}`)
}
```

---

### Task 10: Build & verify

**Files:**
- (no files, just commands)

- [ ] **Compile enterprise-service**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn compile -f enterprise-service/pom.xml`
Expected: BUILD SUCCESS

- [ ] **Build enterprise-pc**

Run: `npm run build`
Workdir: `enterprise-pc`
Expected: Build complete with no errors

- [ ] **Restart enterprise-service**

Run: `JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn spring-boot:run -f enterprise-service/pom.xml`
Verify: Starts without errors
