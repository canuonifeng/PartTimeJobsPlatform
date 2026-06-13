# Enterprise Operations Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `enterprise-service` operations APIs that provide dashboard, process, todo, exception, trend, and basic todo action data for the redesigned enterprise miniapp.

**Architecture:** Add an isolated operations module with `OperationController`, `OperationService`, `OperationMapper`, VO/CMD classes, and MyBatis SQL over existing business tables. The action API delegates to existing Application, Schedule, and Settlement services so business rules stay centralized.

**Tech Stack:** Java 17, Spring Boot 3.2.5, MyBatis XML, existing `ApiResponse` and `PageVO` response patterns.

---

## Tasks

- [ ] Add operations VO and CMD classes under `enterprise-service/src/main/java/com/parttime/enterprise/pojo`.
- [ ] Add `OperationMapper` and `OperationMapper.xml` with aggregate and todo SQL.
- [ ] Add `OperationService` and `OperationServiceImpl` to compose dashboard/process/todos/exceptions/trends and delegate actions.
- [ ] Add `OperationController` under `/api/operations`.
- [ ] Run `cd enterprise-service && JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null) mvn test`.

## File Map

- Create: `enterprise-service/src/main/java/com/parttime/enterprise/controller/OperationController.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/service/OperationService.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/service/impl/OperationServiceImpl.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/mapper/OperationMapper.java`
- Create: `enterprise-service/src/main/resources/mapper/OperationMapper.xml`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/cmd/OperationTodoActionCmd.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationDashboardVO.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationOverviewVO.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationTodayVO.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationProcessNodeVO.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationTodoSummaryVO.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationTodoItemVO.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationExceptionVO.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationTrendVO.java`
- Create: `enterprise-service/src/main/java/com/parttime/enterprise/pojo/vo/OperationTrendPointVO.java`
