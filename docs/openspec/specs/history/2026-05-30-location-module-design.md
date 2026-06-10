# 企业工作地点模块设计

## 背景

企业端需要管理可复用的工作地点库，在创建职位时可以直接选择已有地点，将省市区、详细地址、定位信息填入职位表单。

## 数据库

表名：`company_locations`

```sql
CREATE TABLE company_locations (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  company_id  BIGINT       NOT NULL,
  name        VARCHAR(100) COMMENT '地点名称（如总部、分店A）',
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

## 后端 (enterprise-service)

遵循现有 Account 模块的 CRUD 模式，使用 `@PostMapping` 风格。

### 分层

| 层 | 文件 | 说明 |
|----|------|------|
| Entity | `pojo/entity/CompanyLocation.java` | `@Data`，对应表字段 |
| VO | `pojo/vo/CompanyLocationVO.java` | 返回给前端的视图对象 |
| Cmd | `pojo/cmd/LocationCreateCmd.java` | 新增命令 |
| Cmd | `pojo/cmd/LocationUpdateCmd.java` | 修改命令 |
| Mapper | `mapper/CompanyLocationMapper.java` | MyBatis 接口 |
| Mapper XML | `mapper/CompanyLocationMapper.xml` | SQL 映射 |
| Service | `service/CompanyLocationService.java` | 接口 |
| ServiceImpl | `service/impl/CompanyLocationServiceImpl.java` | 实现 |
| Controller | `controller/CompanyLocationController.java` | REST 接口 |

### API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/locations/list` | 查询当前企业所有地点（返回 List） |
| POST | `/api/locations/create` | 新增地点 |
| POST | `/api/locations/update` | 修改地点 |
| POST | `/api/locations/delete` | 删除地点（传 id） |
| POST | `/api/locations/enable` | 启用 |
| POST | `/api/locations/disable` | 禁用 |

所有接口通过 `SecurityUtil.getCurrentCompanyId()` 获取当前企业 ID。

## 前端 (enterprise-pc)

### API 层

`api/location.js`：

```js
export function listLocations()              { return request.post('/locations/list') }
export function createLocation(data)         { return request.post('/locations/create', data) }
export function updateLocation(data)         { return request.post('/locations/update', data) }
export function deleteLocation(id)           { return request.post('/locations/delete', { id }) }
export function enableLocation(id)           { return request.post('/locations/enable', { id }) }
export function disableLocation(id)          { return request.post('/locations/disable', { id }) }
```

### 页面

`views/locations/LocationList.vue`：

- `el-card` 包裹，header 含标题 + "新建地点"按钮
- `el-table` 展示：名称、省、市、区、详细地址、状态（启用/禁用 tag）
- 操作列：编辑、启用/禁用开关、删除
- 内嵌 `el-dialog` 表单：名称、省市区（el-cascader）、详细地址、纬度、经度
- 状态列和删除操作使用 `ElMessageBox.confirm`

### 路由

`router/index.js` 添加：

```js
{ path: '/locations', name: 'LocationList', component: () => import('../views/locations/LocationList.vue'), meta: { requiresAuth: true, title: '工作地点' } }
```

### 导航

`App.vue` 侧边栏添加"工作地点"菜单项，位于"企业设置"之前。

### JobForm.vue 集成

在职位表单的省市区/地址/定位字段上方添加一个"选择工作地点"按钮，点击弹出地点列表选择器（el-dialog，只显示已启用的地点），选中后将 name、province、city、district、address、latitude、longitude 填入表单对应字段。
