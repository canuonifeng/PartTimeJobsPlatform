# AGENTS.md

## 项目导航图

这是零工平台的工作导航文件，用于快速定位模块、代码入口、文档、脚本和验证命令。默认使用中文沟通。

## 一、系统全景

```text
零工平台
├── 后端服务
│   ├── c-service              # 工人端后端，端口 8082
│   ├── enterprise-service     # 企业端后端，端口 8081
│   └── platform-service       # 平台端后端，端口 8083
├── 前端应用
│   ├── worker-uniapp          # 工人端 UniApp，小程序/H5
│   ├── enterprise-uniapp      # 企业端 UniApp，小程序/H5
│   ├── enterprise-pc          # 企业 PC 管理端
│   └── platform-pc            # 平台 PC 管理端
├── 数据与脚本
│   └── scripts/               # 数据库初始化、迁移、修复脚本
├── 项目文档
│   ├── docs/后端技术规范.md
│   ├── docs/前端技术规范.md
│   ├── docs/openspec/         # OpenSpec 变更与规格
│   └── docs/superpowers/      # 历史设计和执行计划
└── 启动脚本
    └── start.sh
```

核心业务流程：企业发布职位 → 工人报名 → 企业审核 → 排班 → 打卡考勤 → 薪资结算 → 工人提现。

## 二、后端导航

### 1. 服务定位

| 服务 | 目录 | 端口 | 主要职责 |
|---|---|---:|---|
| 工人端后端 | `c-service/` | 8082 | 找活、报名、排班、打卡、收入、提现、个人中心 |
| 企业端后端 | `enterprise-service/` | 8081 | 职位、报名审核、排班、考勤、结算、企业资金 |
| 平台端后端 | `platform-service/` | 8083 | 企业管理、工人管理、岗位分类、平台配置 |

### 2. 通用代码结构

```text
<service>/src/main/java/com/parttime/<module>/
├── config/              # Spring Security、CORS、JWT、框架配置
├── controller/          # HTTP API 入口
├── service/             # 业务接口
├── service/impl/        # 业务实现和事务边界
├── mapper/              # MyBatis Mapper 接口
├── pojo/
│   ├── entity/          # 数据库实体
│   ├── vo/              # 响应对象
│   └── cmd/             # 请求参数对象
└── filter/              # 过滤器
```

MyBatis XML 位置：`<service>/src/main/resources/mapper/`。

### 3. 常用入口

- 工人端职位/报名：`c-service/src/main/java/com/parttime/cservice/controller/JobController.java`
- 工人端考勤：`c-service/src/main/java/com/parttime/cservice/controller/AttendanceController.java`
- 工人端收入/提现：`c-service/src/main/java/com/parttime/cservice/controller/WithdrawalController.java`
- 工人端我的页：`c-service/src/main/java/com/parttime/cservice/controller/ProfileController.java`
- 工人端设置：`c-service/src/main/java/com/parttime/cservice/controller/WorkerSettingsController.java`
- 企业端结算：`enterprise-service/src/main/java/com/parttime/enterprise/service/impl/SettlementServiceImpl.java`

### 4. 后端改动路线

新增或修改接口通常按以下顺序排查和修改：

1. `controller/`：确认 URL、请求方法、认证用户来源。
2. `service/` 和 `service/impl/`：确认业务规则、事务、VO 映射。
3. `mapper/`：确认 Mapper 方法签名和 `@Param`。
4. `resources/mapper/`：确认 SQL、字段别名、排序、分页。
5. `pojo/entity`、`pojo/vo`、`pojo/cmd`：同步字段。
6. `src/test/java/`：补 Controller、Service 或 Mapper XML 测试。
7. `scripts/`：如有表结构或历史数据变化，补迁移脚本。

### 5. 后端规范文档

详细规范见：`docs/后端技术规范.md`。

## 三、前端导航

### 1. 应用定位

| 应用 | 目录 | 技术 | 主要职责 |
|---|---|---|---|
| 工人端 | `worker-uniapp/` | UniApp + Vue 3 | 找活、报名、排班、打卡、收入、我的 |
| 企业小程序 | `enterprise-uniapp/` | UniApp + Vue 3 | 企业移动端操作 |
| 企业 PC | `enterprise-pc/` | Vue 3 + Element Plus | 企业管理后台 |
| 平台 PC | `platform-pc/` | Vue 3 + Element Plus | 平台管理后台 |

### 2. UniApp 目录入口

```text
<uniapp>/src/
├── api/                  # 接口封装
├── pages/                # 页面
├── store/                # 状态管理
├── static/               # 静态资源
├── App.vue
└── main.js
```

工人端常用入口：

- 找活列表：`worker-uniapp/src/pages/jobs/jobList.vue`
- 岗位详情：`worker-uniapp/src/pages/jobs/jobDetail.vue`
- 报名确认：`worker-uniapp/src/pages/jobs/applyConfirm.vue`
- 我的排班：`worker-uniapp/src/pages/schedule/schedule.vue`
- 打卡：`worker-uniapp/src/pages/attendance/clockIn.vue`
- 收入明细：`worker-uniapp/src/pages/earnings/earnings.vue`
- 我的页面：`worker-uniapp/src/pages/profile/profile.vue`
- 设置页：`worker-uniapp/src/pages/settings/settings.vue`
- 请求封装：`worker-uniapp/src/api/request.js`

### 3. PC 目录入口

```text
<pc>/src/
├── api/                  # 接口封装
├── views/                # 页面视图
├── components/           # 复用组件
├── router/               # 路由
├── stores/               # Pinia 状态
└── utils/                # 工具函数
```

### 4. 前端接口地址

- 工人端开发：`http://localhost:8082`
- 工人端生产：`http://121.199.12.23:8082`
- 企业端开发：`http://localhost:8081/api`
- 企业端生产：`http://121.199.12.23:8081/api`
- 平台端开发：`http://localhost:8083/api`
- 平台端生产：`http://121.199.12.23:8083/api`

### 5. 前端规范文档

详细规范见：`docs/前端技术规范.md`。

## 四、数据库与脚本导航

本地常用连接：

- host: `localhost`
- port: `3306`
- database: `part_time_work`
- username: `part_time_work`
- password: `password123`

执行 SQL 时使用 utf8mb4：

```bash
mysql --default-character-set=utf8mb4 -h localhost -P 3306 -u part_time_work -ppassword123 part_time_work < scripts/<file>.sql
```

常见脚本：

- `scripts/14_worker_settings.sql`：创建 `worker_settings`。
- `scripts/remove_settlement_bills.sql`：添加 `balance_transactions.related_attendance_record_id`。
- `scripts/15_repair_earnings_attendance_links.sql`：修复历史收入交易与考勤记录关联。

数据库相关改动路线：

1. 查现有表结构和 Mapper XML。
2. 新增迁移脚本。
3. 同步 Entity、VO、Service 映射、Mapper XML。
4. 本地执行脚本验证。
5. 后端测试通过后再交付。

## 五、文档导航

- `docs/后端技术规范.md`：后端目录、URL、Controller、Service、Mapper、数据库规范。
- `docs/前端技术规范.md`：前端目录、接口、页面、组件、UniApp、PC 管理端规范。
- `docs/openspec/`：OpenSpec 规格和变更记录。
  - `docs/openspec/config.yaml`
  - `docs/openspec/specs/`
  - `docs/openspec/changes/`
- `docs/superpowers/specs/`：历史设计文档。
- `docs/superpowers/plans/`：历史执行计划。

## 六、验证命令导航

### 后端

```bash
cd c-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test
cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test
cd platform-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test
```

### 工人端 UniApp

```bash
cd worker-uniapp && npm run build:h5
cd worker-uniapp && npm run build:mp-weixin
```

### 企业端 UniApp

```bash
cd enterprise-uniapp && npm run build:mp-weixin
```

### PC 管理端

```bash
cd enterprise-pc && npm run build
cd platform-pc && npm run build
```

### 通用提交前检查

```bash
git status --short --branch
git diff
git diff --check
```

## 七、排查路线

### 1. 接口 404

1. 确认前端请求地址和端口。
2. 确认 Controller 是否存在对应 `@RequestMapping` / `@GetMapping`。
3. 确认线上服务是否已部署包含该接口的 commit。
4. 确认 Nginx 或网关是否转发到正确端口。

### 2. 接口 401

1. 确认前端是否带 `Authorization: Bearer <token>`。
2. 确认 token 是否过期。
3. 确认 Spring Security 是否放行或要求认证。

### 3. SQL 缺表或缺字段

1. 查报错表名或字段名。
2. 在 `scripts/` 查对应迁移脚本。
3. 在目标数据库执行脚本。
4. 重启或重试对应接口。

### 4. 前端页面空白

1. 先看构建是否通过。
2. 看接口是否失败。
3. 看页面是否仍依赖 mock/fallback 字段。
4. 看字段名是否与后端 VO 一致。

### 5. 小程序样式异常

1. 优先检查是否用了 H5 专属 CSS 或浏览器 API。
2. 使用 `rpx` 和小程序兼容组件。
3. 跑 `npm run build:mp-weixin` 验证。

## 八、工作约定

- 默认中文沟通。
- 不主动提交、推送，除非用户明确要求。
- 提交前必须检查 `git status`、`git diff`，只提交本次任务相关文件。
- 不提交构建产物，尤其是各前端 `dist/`。
- 不提交密钥、账号密码、token 或线上敏感配置。
- 编辑代码前先理解现有风格，沿用当前项目命名、分层和实现方式。
- 不添加无关重构。
- 不添加代码注释，除非用户明确要求。

## 九、已知非阻塞构建提示

以下 warning 通常为非阻塞：

- Dart Sass `legacy-js-api` deprecation warning。
- Rollup 对 `/* #__PURE__ */` 注释的 warning。

## 十、线上部署注意

- 线上接口 404：先确认服务已部署最新 commit。
- 线上 SQL 报缺表/缺字段：先确认对应 `scripts/` 迁移已执行。
- 后端 CORS 允许来源包含 `http://121.199.12.23:*`、`http://localhost:*`、`http://127.0.0.1:*`。
