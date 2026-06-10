# 微信提现系统设计文档

## 1. 项目概述

### 1.1 项目名称
微信提现系统 - 商户转账到个人账户

### 1.2 项目目标
实现工人提现功能，支持微信零钱和银行卡两种提现方式，通过微信支付API实现实时到账。

### 1.3 项目范围
- 修改现有提现流程，支持多种提现方式
- 集成微信支付商家转账到零钱API
- 集成微信支付企业付款到银行卡API
- 实现实时到账功能

## 2. 需求分析

### 2.1 功能需求
1. **提现方式选择**：工人提现时可选择微信零钱或银行卡
2. **微信零钱转账**：调用微信支付商家转账到零钱API
3. **银行卡转账**：调用微信支付企业付款到银行卡API
4. **实时到账**：提现申请后立即到账
5. **金额限制**：最低1元，最高1000元
6. **次数限制**：每天最多3次
7. **手续费**：不收取手续费

### 2.2 非功能需求
- **性能**：提现请求响应时间<2秒
- **可用性**：系统可用性>99.9%
- **安全性**：支付信息加密传输，证书安全存储
- **可扩展性**：支持未来新增其他提现方式

## 3. 系统设计

### 3.1 架构设计
采用方案1：直接集成微信支付API，在C端服务中直接调用微信支付接口。

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   工人端 (C端)   │    │   微信支付API    │    │   商户后台      │
│                 │    │                 │    │                 │
│  提现申请 ──────┼───►│  商家转账到零钱   │    │  商户账户余额    │
│  选择提现方式    │    │  企业付款到银行卡  │    │                 │
│                 │◄───┤                 │    │                 │
│  提现结果       │    │  转账结果        │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 3.2 数据库设计

#### 3.2.1 修改withdrawal_record表
```sql
-- 添加提现方式字段
ALTER TABLE withdrawal_record ADD COLUMN withdrawal_method VARCHAR(20) NOT NULL DEFAULT 'WECHAT' COMMENT '提现方式: WECHAT-微信零钱, BANK_CARD-银行卡';

-- 添加银行账户信息字段
ALTER TABLE withdrawal_record ADD COLUMN bank_account VARCHAR(100) COMMENT '银行账户信息';

-- 添加微信OpenID字段
ALTER TABLE withdrawal_record ADD COLUMN open_id VARCHAR(100) COMMENT '微信OpenID';

-- 添加索引
CREATE INDEX idx_withdrawal_worker_method ON withdrawal_record(worker_id, withdrawal_method);
CREATE INDEX idx_withdrawal_created_at ON withdrawal_record(created_at);
```

#### 3.2.2 新增withdrawal_config表（可选）
```sql
-- 提现配置表
CREATE TABLE withdrawal_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(50) NOT NULL COMMENT '配置键',
    config_value VARCHAR(200) NOT NULL COMMENT '配置值',
    description VARCHAR(200) COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_key (config_key)
) COMMENT '提现配置表';

-- 插入默认配置
INSERT INTO withdrawal_config (config_key, config_value, description) VALUES
('MIN_AMOUNT', '1.00', '最低提现金额'),
('MAX_AMOUNT', '1000.00', '最高提现金额'),
('DAILY_LIMIT', '3', '每日提现次数限制');
```

### 3.3 API设计

#### 3.3.1 修改提现申请接口
**POST /api/withdrawals**

请求体：
```json
{
  "amount": 100.00,
  "withdrawalMethod": "WECHAT",
  "bankAccountId": 123
}
```

响应体：
```json
{
  "id": 1,
  "workerId": 100,
  "amount": 100.00,
  "status": "COMPLETED",
  "withdrawalMethod": "WECHAT",
  "thirdPartySerialNo": "WX123456789",
  "requestedAt": "2026-06-08T10:00:00",
  "completedAt": "2026-06-08T10:00:01"
}
```

#### 3.3.2 新增获取提现方式接口
**GET /api/withdrawal-methods**

响应体：
```json
{
  "methods": [
    {"code": "WECHAT", "name": "微信零钱", "available": true, "description": "实时到账微信零钱"},
    {"code": "BANK_CARD", "name": "银行卡", "available": true, "description": "实时到账银行卡"}
  ]
}
```

#### 3.3.3 新增获取银行卡列表接口
**GET /api/bank-cards**

响应体：
```json
{
  "cards": [
    {
      "id": 1,
      "bankName": "工商银行",
      "cardNumber": "****1234",
      "cardHolder": "张三",
      "isDefault": true
    }
  ]
}
```

### 3.4 业务逻辑

#### 3.4.1 提现流程
1. 验证工人实名认证状态
2. 验证提现金额限制（1-1000元）
3. 验证提现次数限制（每天3次）
4. 验证银行卡绑定状态（银行卡提现时）
5. 扣减工人余额
6. 调用微信支付API
7. 更新提现记录状态
8. 发送提现结果通知

#### 3.4.2 微信支付集成
- **商家转账到零钱**：`POST /v3/transfer/transfer-funds`
- **企业付款到银行卡**：`POST /v3/transfer/banks`

#### 3.4.3 错误处理
- 支付失败时回滚余额
- 记录详细错误信息
- 发送失败通知

### 3.5 类设计

#### 3.5.1 新增WeChatPayService
```java
public interface WeChatPayService {
    // 商家转账到零钱
    TransferResult transferToWechat(Long workerId, BigDecimal amount, String openId, String description);
    
    // 企业付款到银行卡
    TransferResult transferToBankCard(Long workerId, BigDecimal amount, String bankAccount, String bankName, String description);
    
    // 查询转账状态
    TransferStatus queryTransferStatus(String transferNo);
}
```

#### 3.5.2 修改WithdrawalService
```java
public interface WithdrawalService {
    // 修改现有方法，增加提现方式参数
    WithdrawalVO requestWithdrawal(Long workerId, BigDecimal amount, String withdrawalMethod, Long bankAccountId);
    
    // 新增方法
    List<WithdrawalMethodVO> getAvailableMethods(Long workerId);
    List<BankCardVO> getBankCards(Long workerId);
}
```

#### 3.5.3 新增TransferResult
```java
@Data
public class TransferResult {
    private boolean success;
    private String transferNo;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime transferTime;
}
```

### 3.6 配置管理

#### 3.6.1 微信支付配置
```yaml
wechat:
  pay:
    mch-id: ${WECHAT_MCH_ID}
    api-key: ${WECHAT_API_KEY}
    cert-path: ${WECHAT_CERT_PATH}
    private-key-path: ${WECHAT_PRIVATE_KEY_PATH}
    notify-url: ${WECHAT_NOTIFY_URL}
```

#### 3.6.2 提现配置
```yaml
withdrawal:
  min-amount: 1.00
  max-amount: 1000.00
  daily-limit: 3
```

## 4. 安全设计

### 4.1 证书安全
- 微信支付证书存储在服务器安全目录
- 证书文件权限设置为600
- 定期轮换证书

### 4.2 数据安全
- 银行卡信息加密存储
- 敏感信息日志脱敏
- API调用使用HTTPS

### 4.3 访问控制
- 提现操作需要实名认证
- 银行卡绑定需要验证
- 提现记录不可篡改

## 5. 测试策略

### 5.1 单元测试
- 提现金额验证
- 提现次数验证
- 余额扣减逻辑

### 5.2 集成测试
- 微信支付API调用
- 完整提现流程
- 错误处理流程

### 5.3 Mock测试
- 模拟微信支付API响应
- 模拟网络异常
- 模拟支付失败

### 5.4 性能测试
- 并发提现测试
- 响应时间测试

## 6. 部署计划

### 6.1 环境准备
- 配置微信支付商户信息
- 部署证书文件
- 配置环境变量

### 6.2 数据库迁移
- 执行SQL脚本修改表结构
- 验证数据完整性

### 6.3 服务部署
- 部署C端服务
- 配置监控告警

### 6.4 上线验证
- 测试提现功能
- 验证到账情况
- 监控系统日志

## 7. 风险评估

### 7.1 技术风险
- 微信支付API稳定性
- 网络延迟影响
- 证书过期风险

### 7.2 业务风险
- 提现金额限制
- 次数限制
- 资金安全

### 7.3 应对措施
- 实现重试机制
- 建立监控告警
- 定期审计

## 8. 项目计划

### 8.1 开发阶段
- 第1周：数据库设计和API开发
- 第2周：微信支付集成
- 第3周：测试和调优

### 8.2 测试阶段
- 第4周：集成测试
- 第5周：性能测试

### 8.3 上线阶段
- 第6周：部署上线
- 第7周：监控优化

## 9. 附录

### 9.1 微信支付API文档
- 商家转账到零钱：https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter4_3_1.shtml
- 企业付款到银行卡：https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter4_1_1.shtml

### 9.2 相关配置
- 微信支付商户号：待配置
- 微信支付API密钥：待配置
- 微信支付证书：待配置
