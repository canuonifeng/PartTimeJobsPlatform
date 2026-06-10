## ADDED Requirements

### Requirement: 奖励规则可配置
系统 SHALL 支持运营后台配置奖励规则，包括有效天数、奖励金额、最少打工次数、最少收入、审核机制、发放方式。

#### Scenario: 获取奖励规则
- **WHEN** 调用获取奖励规则接口
- **THEN** 系统返回当前配置的所有配置项

#### Scenario: 修改奖励规则
- **WHEN** 运营后台修改奖励规则
- **THEN** 系统更新配置，新规则对后续邀请生效

### Requirement: 被邀请人在有效期内完成打工后发放奖励
系统 SHALL 在被邀请人注册后有效天数内完成规定打工次数后，自动创建奖励记录。

#### Scenario: 被邀请人完成规定打工次数
- **WHEN** 被邀请人在有效天数内完成规定打工次数
- **THEN** 系统在 referral_reward 表中创建奖励记录，金额为配置的奖励金额，状态为 PENDING

#### Scenario: 被邀请人超过有效期
- **WHEN** 被邀请人超过有效天数仍未完成规定打工次数
- **THEN** 系统不发放奖励，邀请记录标记为过期

#### Scenario: 被邀请人已完成过奖励
- **WHEN** 被邀请人已满足条件并发放过奖励
- **THEN** 系统不重复发放奖励

### Requirement: 奖励金额使用配置值
系统 SHALL 使用配置的奖励金额发放奖励。

#### Scenario: 验证奖励金额
- **WHEN** 创建奖励记录
- **THEN** 奖励金额为 referral_config 中配置的 reward_amount 值

### Requirement: 奖励状态管理
系统 SHALL 管理奖励状态：PENDING（待审核）、AUDITING（审核中）、GRANTED（已发放）、REJECTED（已拒绝）、FAILED（发放失败）。

#### Scenario: 奖励审核通过
- **WHEN** 奖励审核通过
- **THEN** 状态更新为 GRANTED，granted_at 记录发放时间

#### Scenario: 奖励审核拒绝
- **WHEN** 奖励审核拒绝
- **THEN** 状态更新为 REJECTED，audit_remark 记录拒绝原因

#### Scenario: 奖励发放失败
- **WHEN** 奖励发放失败
- **THEN** 状态更新为 FAILED，记录失败原因

### Requirement: 签退时检查奖励条件
系统 SHALL 在每次签退时检查被邀请人是否满足奖励条件。

#### Scenario: 签退时满足条件
- **WHEN** 被邀请人签退，且在有效天数内完成规定打工次数
- **THEN** 系统自动创建奖励记录

#### Scenario: 签退时不满足条件
- **WHEN** 被邀请人签退，但未在有效天数内或打工次数不足
- **THEN** 系统不创建奖励记录

### Requirement: 审核机制可配置
系统 SHALL 支持配置是否需要审核。

#### Scenario: 需要审核
- **WHEN** need_audit = true
- **THEN** 奖励记录状态为 PENDING，需要平台审核后发放

#### Scenario: 不需要审核
- **WHEN** need_audit = false
- **THEN** 奖励记录状态直接为 GRANTED，自动发放奖励

### Requirement: 发放方式可配置
系统 SHALL 支持配置发放方式。

#### Scenario: 自动发放
- **WHEN** release_method = auto
- **THEN** 奖励发放到用户余额

#### Scenario: 手动提现
- **WHEN** release_method = manual
- **THEN** 奖励记录状态为 GRANTED，用户需要手动提现
