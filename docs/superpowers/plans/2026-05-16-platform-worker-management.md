# 平台后台 — 兼职管理模块 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 平台后台新增兼职管理功能，运营人员可查看、搜索、编辑、封禁/解封兼职用户。

**Architecture:** platform-service 直连 `c_worker` + `worker_profiles` 表（同一 MySQL 实例，同一 schema `part_time_work`），无需跨 schema 查询。新建 Mapper/Service/Controller，前端复用 EnterpriseList 的 el-card + el-table + el-dialog 模式。

**Tech Stack:** Spring Boot 3.2.5 + MyBatis 3.x (XML mapper) / Vue 3 + Element Plus

---

### Task 1: WorkerVO / WorkerUpdateCmd

**Files:**
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/vo/WorkerVO.java`
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/cmd/WorkerUpdateCmd.java`

**Steps:**

- [ ] Create `WorkerVO.java`:

```java
package com.parttime.platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkerVO {
    @Schema(description = "兼职ID")
    private Long id;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "电话")
    private String phone;
    @Schema(description = "微信标识")
    private String wechatCode;
    @Schema(description = "OpenID")
    private String openId;
    @Schema(description = "头像URL")
    private String avatarUrl;
    @Schema(description = "技能标签")
    private String skills;
    @Schema(description = "可用时间")
    private String availableDays;
    @Schema(description = "状态: ACTIVE/DISABLED")
    private String status;
    @Schema(description = "注册时间")
    private LocalDateTime createdAt;
}
```

- [ ] Create `WorkerUpdateCmd.java`:

```java
package com.parttime.platform.pojo.cmd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WorkerUpdateCmd {
    @Schema(description = "兼职ID")
    private Long id;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "电话")
    private String phone;
}
```

---

### Task 2: WorkerMapper (Java + XML)

**Files:**
- Create: `platform-service/src/main/java/com/parttime/platform/mapper/WorkerMapper.java`
- Create: `platform-service/src/main/resources/mapper/WorkerMapper.xml`

**Steps:**

- [ ] Create `WorkerMapper.java`:

```java
package com.parttime.platform.mapper;

import com.parttime.platform.pojo.entity.Worker;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface WorkerMapper {
    List<Worker> findAll(@Param("status") String status, @Param("keyword") String keyword);
    Optional<Worker> findById(@Param("id") Long id);
    int update(Worker worker);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
```

- [ ] Create `WorkerMapper.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.parttime.platform.mapper.WorkerMapper">

    <resultMap id="WorkerResultMap" type="com.parttime.platform.pojo.entity.Worker">
        <id property="id" column="id"/>
        <result property="name" column="name"/>
        <result property="phone" column="phone"/>
        <result property="wechatCode" column="wechat_code"/>
        <result property="openId" column="open_id"/>
        <result property="avatarUrl" column="avatar_url"/>
        <result property="status" column="status"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>

    <select id="findAll" resultMap="WorkerResultMap">
        SELECT id, name, phone, wechat_code, open_id, avatar_url, status, created_at, updated_at
        FROM c_worker
        <where>
            <if test="status != null and status != ''">
                AND status = #{status}
            </if>
            <if test="keyword != null and keyword != ''">
                AND (name LIKE CONCT('%', #{keyword}, '%') OR phone LIKE CONCT('%', #{keyword}, '%'))
            </if>
        </where>
        ORDER BY created_at DESC
    </select>

    <select id="findById" resultMap="WorkerResultMap">
        SELECT id, name, phone, wechat_code, open_id, avatar_url, status, created_at, updated_at
        FROM c_worker WHERE id = #{id}
    </select>

    <update id="update">
        UPDATE c_worker SET name = #{name}, phone = #{phone} WHERE id = #{id}
    </update>

    <update id="updateStatus">
        UPDATE c_worker SET status = #{status} WHERE id = #{id}
    </update>

</mapper>
```

---

### Task 3: Worker 实体类 + WorkerService

**Files:**
- Create: `platform-service/src/main/java/com/parttime/platform/pojo/entity/Worker.java`
- Create: `platform-service/src/main/java/com/parttime/platform/service/WorkerService.java`
- Create: `platform-service/src/main/java/com/parttime/platform/service/impl/WorkerServiceImpl.java`

**Steps:**

- [ ] Create `Worker.java`:

```java
package com.parttime.platform.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Worker {
    private Long id;
    private String name;
    private String phone;
    private String wechatCode;
    private String openId;
    private String avatarUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] Create `WorkerService.java`:

```java
package com.parttime.platform.service;
import com.parttime.platform.pojo.cmd.WorkerUpdateCmd;
import com.parttime.platform.pojo.vo.WorkerVO;
import java.util.List;

public interface WorkerService {
    List<WorkerVO> list(String status, String keyword);
    WorkerVO detail(Long id);
    WorkerVO update(WorkerUpdateCmd cmd);
    void ban(Long id);
    void unban(Long id);
}
```

- [ ] Create `WorkerServiceImpl.java`:

```java
package com.parttime.platform.service.impl;
import com.parttime.platform.exception.BusinessException;
import com.parttime.platform.mapper.WorkerMapper;
import com.parttime.platform.pojo.cmd.WorkerUpdateCmd;
import com.parttime.platform.pojo.entity.Worker;
import com.parttime.platform.pojo.vo.WorkerVO;
import com.parttime.platform.service.WorkerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkerServiceImpl implements WorkerService {

    @Resource
    private WorkerMapper workerMapper;

    @Override
    public List<WorkerVO> list(String status, String keyword) {
        List<Worker> list = workerMapper.findAll(status, keyword);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public WorkerVO detail(Long id) {
        Worker w = workerMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Worker not found: " + id));
        return toVO(w);
    }

    @Override
    public WorkerVO update(WorkerUpdateCmd cmd) {
        Worker w = workerMapper.findById(cmd.getId())
                .orElseThrow(() -> new BusinessException("Worker not found: " + cmd.getId()));
        if (cmd.getName() != null) w.setName(cmd.getName());
        if (cmd.getPhone() != null) w.setPhone(cmd.getPhone());
        workerMapper.update(w);
        return toVO(w);
    }

    @Override
    public void ban(Long id) {
        Worker w = workerMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Worker not found: " + id));
        if (!"ACTIVE".equals(w.getStatus())) {
            throw new BusinessException("Worker is not ACTIVE");
        }
        workerMapper.updateStatus(id, "DISABLED");
    }

    @Override
    public void unban(Long id) {
        Worker w = workerMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Worker not found: " + id));
        if (!"DISABLED".equals(w.getStatus())) {
            throw new BusinessException("Worker is not DISABLED");
        }
        workerMapper.updateStatus(id, "ACTIVE");
    }

    private WorkerVO toVO(Worker w) {
        WorkerVO vo = new WorkerVO();
        vo.setId(w.getId());
        vo.setName(w.getName());
        vo.setPhone(w.getPhone());
        vo.setWechatCode(w.getWechatCode());
        vo.setOpenId(w.getOpenId());
        vo.setAvatarUrl(w.getAvatarUrl());
        vo.setStatus(w.getStatus());
        vo.setCreatedAt(w.getCreatedAt());
        return vo;
    }
}
```

---

### Task 4: WorkerController

**Files:**
- Create: `platform-service/src/main/java/com/parttime/platform/controller/WorkerController.java`

**Steps:**

- [ ] Create `WorkerController.java`:

```java
package com.parttime.platform.controller;

import com.parttime.platform.pojo.cmd.WorkerUpdateCmd;
import com.parttime.platform.pojo.vo.WorkerVO;
import com.parttime.platform.service.WorkerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/workers")
public class WorkerController {

    @Resource
    private WorkerService workerService;

    @Operation(summary = "获取兼职列表")
    @PostMapping("/list")
    public List<WorkerVO> list(@RequestBody(required = false) Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        String keyword = body != null ? body.get("keyword") : null;
        return workerService.list(status, keyword);
    }

    @Operation(summary = "获取兼职详情")
    @PostMapping("/detail")
    public WorkerVO detail(@RequestBody Map<String, Long> body) {
        return workerService.detail(body.get("id"));
    }

    @Operation(summary = "编辑兼职信息")
    @PostMapping("/update")
    public WorkerVO update(@RequestBody WorkerUpdateCmd cmd) {
        return workerService.update(cmd);
    }

    @Operation(summary = "封禁兼职")
    @PostMapping("/ban")
    public void ban(@RequestBody Map<String, Long> body) {
        workerService.ban(body.get("id"));
    }

    @Operation(summary = "解封兼职")
    @PostMapping("/unban")
    public void unban(@RequestBody Map<String, Long> body) {
        workerService.unban(body.get("id"));
    }
}
```

---

### Task 5: 前端 API 模块

**Files:**
- Create: `platform-pc/src/api/workers.js`

**Steps:**

- [ ] Create `workers.js`:

```js
import request from './request'

export function listWorkers(params) {
  return request.post('/admin/workers/list', params)
}

export function detailWorker(id) {
  return request.post('/admin/workers/detail', { id })
}

export function updateWorker(data) {
  return request.post('/admin/workers/update', data)
}

export function banWorker(id) {
  return request.post('/admin/workers/ban', { id })
}

export function unbanWorker(id) {
  return request.post('/admin/workers/unban', { id })
}
```

---

### Task 6: 前端 WorkerList 页面

**Files:**
- Create: `platform-pc/src/views/workers/WorkerList.vue`

**Steps:**

- [ ] Create `WorkerList.vue`:

```vue
<template>
  <el-card>
    <template #header>
      <span class="card-title">兼职管理</span>
      <el-input v-model="keyword" placeholder="搜索姓名/电话" size="small" style="float:right;width:200px;margin-right:8px" clearable @clear="fetchData" @keyup.enter="fetchData" />
      <el-select v-model="statusFilter" placeholder="筛选状态" size="small" style="float:right;width:120px;margin-right:8px" @change="fetchData">
        <el-option label="全部" value="" />
        <el-option label="正常" value="ACTIVE" />
        <el-option label="已封禁" value="DISABLED" />
      </el-select>
    </template>
    <el-table :data="workers" v-loading="loading" stripe style="width:100%">
      <el-table-column prop="id" label="编号" width="60" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="wechatCode" label="微信标识" width="140" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '正常' : '已封禁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
          <el-button :type="row.status === 'ACTIVE' ? 'warning' : 'success'" size="small" text
            @click="handleToggleStatus(row)">
            {{ row.status === 'ACTIVE' ? '封禁' : '解封' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="editDialog.visible" title="编辑兼职信息" width="400px">
    <el-form :model="editDialog.form" label-width="80px">
      <el-form-item label="姓名">
        <el-input v-model="editDialog.form.name" />
      </el-form-item>
      <el-form-item label="电话">
        <el-input v-model="editDialog.form.phone" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editDialog.visible = false">取消</el-button>
      <el-button type="primary" @click="confirmEdit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listWorkers, updateWorker, banWorker, unbanWorker } from '../../api/workers'

const loading = ref(false)
const workers = ref([])
const statusFilter = ref('')
const keyword = ref('')
const editDialog = ref({ visible: false, form: {} })

async function fetchData() {
  loading.value = true
  try {
    const data = await listWorkers({
      status: statusFilter.value || undefined,
      keyword: keyword.value || undefined
    })
    workers.value = Array.isArray(data) ? data : (data.records || [])
  } finally {
    loading.value = false
  }
}

function handleEdit(row) {
  editDialog.value = { visible: true, form: { id: row.id, name: row.name, phone: row.phone } }
}

async function confirmEdit() {
  await updateWorker(editDialog.value.form)
  ElMessage.success('兼职信息已更新')
  editDialog.value.visible = false
  await fetchData()
}

async function handleToggleStatus(row) {
  const action = row.status === 'ACTIVE' ? '封禁' : '解封'
  try {
    await ElMessageBox.confirm(`确认${action}兼职 "${row.name || row.phone}"？`, '确认')
    if (row.status === 'ACTIVE') {
      await banWorker(row.id)
    } else {
      await unbanWorker(row.id)
    }
    ElMessage.success(`兼职已${action}`)
    await fetchData()
  } catch { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.card-title { font-weight: 600; font-size: 16px; }
</style>
```

---

### Task 7: 前端路由 + 侧边栏菜单

**Files:**
- Modify: `platform-pc/src/router/index.js` — 添加兼职管理路由
- Modify: `platform-pc/src/App.vue` — 添加菜单项

**Steps:**

- [ ] 在 `router/index.js` 的 routes 数组中，在 enterprises 路由后添加：

```js
  {
    path: '/admin/workers',
    name: 'WorkerList',
    component: () => import('../views/workers/WorkerList.vue')
  },
```

- [ ] 在 `App.vue` 的侧边栏菜单中，在"企业管理"菜单项后添加：

```html
<el-menu-item index="/admin/workers">
  <el-icon><User /></el-icon>
  <span>兼职管理</span>
</el-menu-item>
```

确认 `User` icon 已在 import 中或改用已有的 icon。

---

### Task 8: 编译验证

- [ ] 编译 platform-service：

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
cd platform-service && mvn compile -q
```

预期：BUILD SUCCESS
