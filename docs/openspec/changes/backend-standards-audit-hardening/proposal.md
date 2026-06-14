# Proposal

## Why

当前 `c-service`、`enterprise-service`、`platform-service` 三个后端服务存在多类与 `docs/后端技术规范.md` 不一致的实现。此前企业端已开始局部规范化，但全局后端仍需要先形成统一审计清单，再分批整改，避免单点修改导致前后端接口不一致或跨服务风格继续分化。

## What Changes

- 统一审计三个后端服务的 Controller、Service、Mapper、Entity、日志与分页实现。
- 将 Controller 硬性规范违规纳入整改：仅允许 `@GetMapping` / `@PostMapping`，POST 写操作只使用 `@RequestBody` cmd，不使用 `Map` 入参/返回，不使用 `@PathVariable`。
- 将分页、N+1、Mapper 注解 SQL、Entity 关联字段、敏感日志等问题纳入分级治理。
- 对涉及前端调用的接口同步更新对应前端 API，避免接口方法或请求体变更后出现 404/405/参数丢失。
- 建立整改后的静态扫描与构建验证清单。

## Scope

- `c-service`
- `enterprise-service`
- `platform-service`
- 受后端接口变更影响的 `worker-uniapp`、`enterprise-uniapp`、`enterprise-pc`、`platform-pc` API 封装

## Out of Scope

- 不做无关业务重构。
- 不重写认证、支付、结算等核心流程。
- 不调整数据库表结构，除非整改过程中确认必须迁移字段。
