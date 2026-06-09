# User Invitation Reward Implementation Tasks

## 1. 数据库迁移

- [ ] 1.1 创建 referral_code 表
- [ ] 1.2 创建 referral_record 表
- [ ] 1.3 创建 referral_reward 表
- [ ] 1.4 创建 referral_config 表并插入默认配置

## 2. 后端实体类和 Mapper

- [ ] 2.1 创建 ReferralCode 实体类
- [ ] 2.2 创建 ReferralRecord 实体类
- [ ] 2.3 创建 ReferralReward 实体类
- [ ] 2.4 创建 ReferralConfig 实体类
- [ ] 2.5 创建 ReferralCodeMapper 接口和 XML
- [ ] 2.6 创建 ReferralRecordMapper 接口和 XML
- [ ] 2.7 创建 ReferralRewardMapper 接口和 XML
- [ ] 2.8 创建 ReferralConfigMapper 接口和 XML

## 3. 后端 Service

- [ ] 3.1 创建 ReferralService 接口
- [ ] 3.2 实现 ReferralServiceImpl：邀请码生成
- [ ] 3.3 实现 ReferralServiceImpl：邀请链接生成
- [ ] 3.4 实现 ReferralServiceImpl：邀请海报生成（含二维码）
- [ ] 3.5 实现 ReferralServiceImpl：邀请关系绑定（注册时自动调用）
- [ ] 3.6 实现 ReferralServiceImpl：签退时检查奖励条件并发放
- [ ] 3.7 实现 ReferralServiceImpl：邀请统计概览
- [ ] 3.8 实现 ReferralServiceImpl：被邀请人列表（含出勤和奖励状态）
- [ ] 3.9 实现 ReferralConfigService：获取和修改奖励规则配置

## 4. 后端 Controller

- [ ] 4.1 创建 ReferralController：GET /api/referral/link（获取邀请链接）
- [ ] 4.2 创建 ReferralController：GET /api/referral/poster（获取邀请海报）
- [ ] 4.3 创建 ReferralController：GET /api/referral/stats（邀请统计概览）
- [ ] 4.4 创建 ReferralController：GET /api/referral/referees（被邀请人列表，分页）
- [ ] 4.5 创建 ReferralConfigController：GET /api/referral/config（获取配置）
- [ ] 4.6 创建 ReferralConfigController：PUT /api/referral/config（修改配置）

## 5. 前端页面 - 工人端

- [ ] 5.1 创建邀请页面 referral.vue：显示邀请链接和分享功能
- [ ] 5.2 创建邀请海报生成和预览功能
- [ ] 5.3 创建邀请记录页面 referralRecords.vue：显示被邀请人列表（分页）
- [ ] 5.4 被邀请人列表显示：昵称、手机号、注册时间、出勤状态、奖励状态
- [ ] 5.5 添加路由配置
- [ ] 5.6 在"我的"页面添加邀请入口

## 6. 前端页面 - 工人端收入页

- [ ] 6.1 收入汇总页显示邀请奖励总额
- [ ] 6.2 收入明细列表展示邀请奖励记录
- [ ] 6.3 邀请奖励与工资收入分开标识

## 7. 前端页面 - 企业端（运营后台）

- [ ] 7.1 创建奖励规则配置页面 referralConfig.vue
- [ ] 7.2 实现配置表单：有效天数、奖励金额、最少打工次数
- [ ] 7.3 实现配置保存和校验

## 8. 注册流程集成

- [ ] 8.1 注册接口支持接收邀请码参数
- [ ] 8.2 注册时自动绑定邀请关系
- [ ] 8.3 前端注册页面从 URL 参数提取邀请码

## 9. 签退流程集成

- [ ] 9.1 签退接口调用奖励检查逻辑
- [ ] 9.2 满足条件时自动创建奖励记录

## 10. 测试

- [ ] 10.1 编写 ReferralService 单元测试
- [ ] 10.2 编写 ReferralConfigService 单元测试
- [ ] 10.3 编写 ReferralController 集成测试
- [ ] 10.4 编写 ReferralConfigController 集成测试
- [ ] 10.5 运行所有测试验证通过
