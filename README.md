# 零工平台 (Part-Time Job SaaS Platform)

零工平台是一套面向灵活用工场景的招聘与用工管理系统，连接企业、兼职人员与平台运营方，覆盖职位发布、报名审核、排班、考勤、薪资结算等核心流程。

平台包含工人端、企业端和平台端三类产品：工人可通过小程序浏览岗位、报名、查看排班、打卡和提现；企业可通过小程序或 PC 后台发布职位、审核报名、管理排班考勤并完成薪资结算；平台运营方可通过管理后台维护企业、工人、岗位分类和系统配置。

适用于兼职招聘、门店临时用工、活动用工、排班考勤管理和灵活用工结算等业务场景。

## 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                     Frontend Apps                       │
├────────────┬─────────────┬──────────────┬───────────────┤
│ enterprise-│   worker-   │  platform-   │  enterprise-  │
│    pc      │   uniapp    │     pc       │    uniapp     │
│ (Vue+Ele)  │ (小程序/H5)  │ (Vue+Ele)    │ (小程序/H5)   │
├────────────┴─────────────┴──────────────┴───────────────┤
│                     Backend Services                    │
├────────────┬─────────────┬──────────────────────────────┤
│ enterprise │   c-service │     platform-service         │
│  -service  │  (工人端)    │     (平台管理端)              │
│  (企业端)   │             │                              │
├────────────┴─────────────┴──────────────────────────────┤
│                      MySQL + Redis                      │
└─────────────────────────────────────────────────────────┘
```

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17, Spring Boot 3.2.5, MyBatis 3.0.3 |
| 前端 PC | Vue 3.4, Element Plus, Pinia, Axios |
| 小程序 | UniApp 3.x (微信小程序 + H5) |
| 数据库 | MySQL 8.x |
| 缓存 | Redis |
| 认证 | JWT + Spring Security |
| API 文档 | SpringDoc OpenAPI 2.3.0 |

## 模块说明

### 后端服务 (3 个)

| 服务 | 说明 | 职责 |
|---|---|---|
| `enterprise-service` | 企业端后端 | 职位管理、应聘管理、排班考勤、薪资、消息通知 |
| `c-service` | 工人端后端 | 找活浏览、报名、打卡、提现、个人中心 |
| `platform-service` | 平台管理端后端 | 企业管理、工人管理、职位分类、系统配置、数据统计 |

### 前端应用 (4 个)

| 应用 | 说明 | 技术 |
|---|---|---|
| `enterprise-pc` | 企业 PC 管理后台 | Vue 3 + Element Plus |
| `worker-uniapp` | 工人端小程序/H5 | UniApp (微信小程序 + H5) |
| `platform-pc` | 平台管理 PC 后台 | Vue 3 + Element Plus |
| `enterprise-uniapp` | 企业端小程序 | UniApp |

## 核心业务流程

```
企业发布职位 → 工人报名 → 企业审核 → 排班 → 打卡考勤 → 薪资结算
```

## 开发环境要求

- JDK 17 (系统默认 JDK 11，需切换：`export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home`)
- MySQL 8.x
- Redis
- Maven 3.8+
- Node.js 18+

## 快速启动

### 方式一：一键启动生产构建

`start.sh` 会构建并启动 3 个后端服务，同时构建 PC 与 H5 前端产物。启动前请先准备本地外部配置文件，配置文件不提交到 Git。

```bash
# 准备本地配置与日志目录
mkdir -p config logs

# 按本地环境创建并填写数据库、Redis、JWT 等配置
vi config/c-service-env.yml
vi config/enterprise-service-env.yml
vi config/platform-service-env.yml

# 一键启动
chmod +x start.sh
./start.sh
```

后端服务端口与接口前缀：

| 服务 | 端口 | 接口前缀 |
|---|---:|---|
| `enterprise-service` | 8081 | `/api/enterprise` |
| `c-service` | 8082 | `/api/worker` |
| `platform-service` | 8083 | `/api/admin` |

### 方式二：开发环境分别启动

```bash
# 后端需使用 JDK 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null)

# 企业端后端：http://localhost:8081/api/enterprise
cd enterprise-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 工人端后端：http://localhost:8082/api/worker
cd c-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 平台端后端：http://localhost:8083/api/admin
cd platform-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

前端开发服务：

```bash
# 企业 PC 前端
cd enterprise-pc
npm install
npm run dev

# 平台 PC 前端
cd platform-pc
npm install
npm run dev

# 企业小程序 / H5
cd enterprise-uniapp
npm install
npm run dev:h5
npm run dev:mp-weixin

# 工人小程序 / H5
cd worker-uniapp
npm install
npm run dev:h5
npm run dev:mp-weixin
```

## 项目结构

```
├── enterprise-service/      # 企业端后端
│   ├── src/main/java/       # Java 源码
│   │   └── com/parttime/enterprise/
│   │       ├── controller/  # API 控制器
│   │       ├── service/     # 业务逻辑
│   │       ├── mapper/      # MyBatis 数据访问
│   │       └── pojo/        # 实体 / CMD / VO
│   └── src/main/resources/
│       ├── mapper/          # MyBatis XML 映射
│       └── application*.yml # 服务配置
├── c-service/               # 工人端后端
├── platform-service/        # 平台管理端后端
├── enterprise-pc/           # 企业 PC 前端
├── platform-pc/             # 平台 PC 前端
├── worker-uniapp/           # 工人端小程序
└── enterprise-uniapp/       # 企业端小程序
```

## 数据库

三个服务本地开发默认连接同一个 MySQL 数据库：

- 默认数据库：`part_time_work`
- 默认账号：`part_time_work`
- 初始化和迁移脚本位于 `scripts/` 目录
- 当前 Flyway 默认关闭，数据库变更以 `scripts/` 中的 SQL 脚本为准
