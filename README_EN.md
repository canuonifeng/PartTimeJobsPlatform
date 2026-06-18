# Part-Time Job SaaS Platform

A full-stack, multi-terminal zero-commission part-time job platform connecting enterprises with part-time workers. The platform uses a "Recruitment Plan + Shift Center" product model to streamline recruitment, scheduling, attendance, and payroll.

## System Architecture

```text
Part-Time Job Platform
├── Backend Services
│   ├── c-service              # Worker API, port 8082
│   ├── enterprise-service     # Enterprise API, port 8081
│   └── platform-service       # Platform Admin API, port 8083
├── Frontend Apps
│   ├── worker-uniapp          # Worker mini-program / H5
│   ├── enterprise-uniapp      # Enterprise mini-program / H5
│   ├── enterprise-pc          # Enterprise PC management
│   └── platform-pc            # Platform PC management
├── Database Scripts
│   └── scripts/               # DB initialization & migration
└── Documentation
    └── docs/
```

## Core Business Flow

```
Create Recruitment Plan → Publish Shifts → Workers Select Shifts to Apply → Enterprise Reviews Applications → Schedule & On-duty → Clock In/Out → Payroll Settlement
```

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.2.5, MyBatis, MySQL 8, Redis, RocketMQ, XXL-Job
- **Frontend**: Vue 3.4, Element Plus, UniApp (WeChat Mini-Program + H5)
- **Security**: JWT + Spring Security

## Services

| Service | Directory | Port | Responsibilities |
|---|---|---|---|
| Worker API | `c-service/` | 8082 | Job search, applications, scheduling, attendance, earnings, withdrawals |
| Enterprise API | `enterprise-service/` | 8081 | Recruitment plans, shift management, application review, attendance, payroll |
| Platform API | `platform-service/` | 8083 | Enterprise/worker management, job categories, system config |

## Quick Start

```bash
# Database
mysql --default-character-set=utf8mb4 -h localhost -P 3306 -u root -p < scripts/init.sql

# Backend
cd enterprise-service && mvn spring-boot:run
cd c-service && mvn spring-boot:run

# Frontend
cd enterprise-pc && npm install && npm run dev
cd worker-uniapp && npm install && npm run dev:h5
```
