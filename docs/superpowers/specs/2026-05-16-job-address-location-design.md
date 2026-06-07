# Job 岗位地址与定位设计

## 概述

为岗位增加结构化地址（省/市/区）和经纬度坐标，支持地图选点和位置展示。

## 数据库变更

`jobs` 表新增 6 列：

```sql
ALTER TABLE jobs
  ADD COLUMN province  VARCHAR(50)  DEFAULT NULL COMMENT '省' AFTER location,
  ADD COLUMN city      VARCHAR(50)  DEFAULT NULL COMMENT '市' AFTER province,
  ADD COLUMN district  VARCHAR(50)  DEFAULT NULL COMMENT '区' AFTER city,
  ADD COLUMN address   VARCHAR(200) DEFAULT NULL COMMENT '详细地址（街道门牌号）' AFTER district,
  ADD COLUMN latitude  DECIMAL(10,7) DEFAULT NULL COMMENT '纬度' AFTER address,
  ADD COLUMN longitude DECIMAL(10,7) DEFAULT NULL COMMENT '经度' AFTER address;
```

`location` 字段保留，创建/更新时自动拼接 `province city district address`（空格分隔），作为冗余文本用于列表展示和搜索。查询时无需关联 province/city/district 即可直接展示。

## 后端改动

### enterprise-service

| 文件 | 改动 |
|------|------|
| `Job.java` entity | 新增 `province`, `city`, `district`, `address`, `latitude`, `longitude` 字段 |
| `JobCreateCmd.java` | 同上 6 字段 |
| `UpdateJobCmd.java` | 同上 6 字段 |
| `JobVO.java` | 同上 6 字段 |
| `JobMapper.xml` | insert/update SQL 包含新列，resultMap 包含新列 |
| `JobService.java` | create/update 时自动拼接 `location` |

### c-service

| 文件 | 改动 |
|------|------|
| `Job.java` entity | 新增 6 字段（读模型，只映射查询结果） |
| `JobDetailVO.java` | 新增 province, city, district, address, latitude, longitude |
| `JobSummaryVO.java` | 新增 province, city, district（列表只需文本，不含坐标） |

## 前端改动

### enterprise-pc — `JobForm.vue`

- **省市区**：`<el-cascader :options="regionData" />` 三级联动选择
- **详细地址**：`<el-input v-model="form.address" />` 文本输入
- **坐标选择**：点击"选择位置"打开高德地图弹窗，地图可拖拽/点击选点，确认后回填 lat/lng

### enterprise-uniapp — `JobForm.vue`

- **省市区**：`<picker mode="multiSelector" :range="regionRange" />` 三级联动
- **详细地址**：`<input v-model="form.address" />`
- **坐标选择**：`uni.chooseLocation()` 调起内置地图选点，自动回填地址名称 + lat/lng

### worker-uniapp — `jobDetail.vue`

- 新增地图展示区域：`<map :latitude="job.latitude" :longitude="job.longitude" :markers="markers" />`
- 地址区域显示：`省 市 区 + 详细地址`

### 省市区数据

内置一份 China region JSON 数据（省/市/区三级），格式：

```json
[
  {
    "value": "110000",
    "label": "北京市",
    "children": [
      {
        "value": "110100",
        "label": "北京市",
        "children": [
          { "value": "110101", "label": "东城区" },
          { "value": "110102", "label": "西城区" }
        ]
      }
    ]
  }
]
```

数据文件统一来源（可从 china-region NPM 包提取或手工维护一份常用的）：

```
worker-uniapp/src/assets/regions.json
enterprise-pc/src/assets/regions.json
enterprise-uniapp/src/assets/regions.json
```

enterprise-pc 使用 `<el-cascader>` 直接消费此格式。uni-app 端需转成 `multiSelector` 所需的多级数组格式。

## 地图集成

### enterprise-pc（Web 端）

- 使用高德地图 JS API 2.0
- 在 `index.html` 通过 `<script>` 加载：`https://webapi.amap.com/maps?v=2.0&key=YOUR_KEY`
- 弹窗组件 `LocationPicker.vue`：
  - 初始化地图，中心点默认公司地址或北京
  - 添加可拖拽标记
  - 点击地图/拖拽标记后获取 `lng` + `lat`
  - 通过 AMap.Geocoder 逆地理编码获取地址文本
- Key 配置：暂放前端常量，后续改为环境变量

### enterprise-uniapp / worker-uniapp

- 使用 uni-app 内置能力，无需额外 SDK
- `uni.chooseLocation()`：返回 `name`(位置名称)、`address`(地址)、`latitude`、`longitude`
- `<map>` 组件：展示位置标记
