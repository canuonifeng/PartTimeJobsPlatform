# 零工平台 (Part-Time Job SaaS Platform)

连接企业与兼职人员的零工 SaaS 平台。

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
│              MySQL + Redis + RocketMQ                   │
└─────────────────────────────────────────────────────────┘
```

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17, Spring Boot 3.2.5, MyBatis 3.x, Flyway |
| 前端 PC | Vue 3.4, Element Plus, Pinia, Axios |
| 小程序 | UniApp 3.x (微信小程序 + H5) |
| 数据库 | MySQL 8.x, Redis |
| 消息队列 | RocketMQ |
| 定时任务 | XXL-Job |
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
- RocketMQ (enterprise-service / c-service)
- XXL-Job (enterprise-service / c-service)

## 快速启动

```bash
# 启动企业端后端
cd enterprise-service && mvn spring-boot:run

# 启动工人端后端
cd c-service && mvn spring-boot:run

# 启动平台管理端后端
cd platform-service && mvn spring-boot:run

# 启动企业 PC 前端
cd enterprise-pc && npm install && npm run dev

# 启动平台 PC 前端
cd platform-pc && npm install && npm run dev
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
│       └── db/migration/    # Flyway 数据库迁移
├── c-service/               # 工人端后端
├── platform-service/        # 平台管理端后端
├── enterprise-pc/           # 企业 PC 前端
├── platform-pc/             # 平台 PC 前端
├── worker-uniapp/           # 工人端小程序
└── enterprise-uniapp/       # 企业端小程序
```

## 数据库

三个服务共享同一 MySQL 实例，使用独立 schema：

- `enterprise-service` + `c-service`: 共享核心业务库
- `platform-service`: 独立管理库
