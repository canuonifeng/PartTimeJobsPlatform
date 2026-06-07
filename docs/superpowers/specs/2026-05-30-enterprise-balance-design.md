# Enterprise Account Balance System Design

## Overview
Add an enterprise account balance system: each enterprise has a balance + credit limit, can top up (self-service with simulated payment or platform-admin direct), and settlement deducts from enterprise balance to worker balance. Enterprise has its own transaction history (流水).

## Tables

### enterprise_balances
| Column | Type | Notes |
|--------|------|-------|
| company_id | BIGINT PK | |
| balance | DECIMAL(12,2) DEFAULT 0.00 | Current available balance |
| total_top_up | DECIMAL(12,2) DEFAULT 0.00 | Cumulative top-up amount |
| total_spent | DECIMAL(12,2) DEFAULT 0.00 | Cumulative settlement spend |
| credit_limit | DECIMAL(12,2) DEFAULT 0.00 | Credit line per enterprise |
| created_at | DATETIME | |
| updated_at | DATETIME | |

### enterprise_balance_transactions
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT AUTO_INCREMENT PK | |
| company_id | BIGINT NOT NULL | |
| amount | DECIMAL(12,2) NOT NULL | Positive=income (top-up, refund), Negative=expense (settlement) |
| type | VARCHAR(20) NOT NULL | TOP_UP / SETTLEMENT / SETTLEMENT_REFUND |
| related_bill_id | BIGINT | For SETTLEMENT/SETTLEMENT_REFUND |
| related_top_up_id | BIGINT | For TOP_UP (self-service) |
| description | VARCHAR(255) | |
| created_at | DATETIME | |

### enterprise_top_up_records
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT AUTO_INCREMENT PK | |
| company_id | BIGINT NOT NULL | |
| amount | DECIMAL(12,2) NOT NULL | |
| status | VARCHAR(20) DEFAULT 'PROCESSING' | PROCESSING / COMPLETED / FAILED |
| serial_number | VARCHAR(64) | Generated unique serial |
| third_party_serial_no | VARCHAR(128) | Simulated third-party serial |
| third_party_platform | VARCHAR(50) | e.g. SIMULATED_PAY |
| completed_at | DATETIME | |
| created_at | DATETIME | |
| updated_at | DATETIME | |

## Flows

### Self-service Top-up (enterprise-service)
1. `POST /api/enterprise/balance/top-up` with `{amount}`
2. Create `enterprise_top_up_records` (status=PROCESSING, serial_number=TOP+timestamp+random)
3. Simulate completion: status=COMPLETED, third_party_serial_no=generated, completed_at=now
4. Upsert `enterprise_balances`: balance += amount, total_top_up += amount
5. Insert `enterprise_balance_transactions` (type=TOP_UP, amount=+amount, related_top_up_id)
6. Return the completed record

### Platform-admin Direct Top-up (platform-service)
1. `POST /api/platform/enterprise/{companyId}/balance/adjust` with `{amount, description}`
2. Upsert `enterprise_balances`: balance += amount, total_top_up += amount
3. Insert `enterprise_balance_transactions` (type=TOP_UP, amount=+amount, description)
4. Return updated balance

### Settlement Deduction (enterprise-service, modify SettlementServiceImpl)
In `payFromAttendanceRecords`:
1. Sum total `actual_pay` across all attendance records
2. Query `enterprise_balances` for company; if no row exists, treat as balance=0
3. Validate: `balance + credit_limit >= total_pay` — if not, throw "企业余额不足"
4. Proceed with existing bill/worker-balance creation
5. Upsert `enterprise_balances`: balance -= total_pay, total_spent += total_pay
6. For each bill, insert `enterprise_balance_transactions` (type=SETTLEMENT, amount=-actual_pay, related_bill_id)

### Unsettle Refund (enterprise-service, modify SettlementServiceImpl)
In `unsettle`:
1. After existing refund logic, upsert `enterprise_balances`: balance += actual_pay
2. Insert `enterprise_balance_transactions` (type=SETTLEMENT_REFUND, amount=+actual_pay, related_bill_id)

### Balance Query (enterprise-service)
`GET /api/enterprise/balance` returns:
```json
{
  "balance": 5000.00,
  "creditLimit": 2000.00,
  "totalTopUp": 10000.00,
  "totalSpent": 5000.00,
  "usableBalance": 7000.00
}
```

### Transaction History (enterprise-service)
`GET /api/enterprise/balance/transactions?page=1&size=10` returns paginated `PageVO<EnterpriseTransactionVO>`.

## Files to Create

### enterprise-service
| File | Path |
|------|------|
| EnterpriseBalance.java | `.../pojo/entity/EnterpriseBalance.java` |
| EnterpriseBalanceTransaction.java | `.../pojo/entity/EnterpriseBalanceTransaction.java` |
| EnterpriseTopUpRecord.java | `.../pojo/entity/EnterpriseTopUpRecord.java` |
| EnterpriseBalanceVO.java | `.../pojo/vo/EnterpriseBalanceVO.java` |
| EnterpriseTransactionVO.java | `.../pojo/vo/EnterpriseTransactionVO.java` |
| EnterpriseBalanceMapper.java | `.../mapper/EnterpriseBalanceMapper.java` |
| EnterpriseBalanceTransactionMapper.java | `.../mapper/EnterpriseBalanceTransactionMapper.java` |
| EnterpriseTopUpRecordMapper.java | `.../mapper/EnterpriseTopUpRecordMapper.java` |
| EnterpriseBalanceMapper.xml | `.../resources/mapper/EnterpriseBalanceMapper.xml` |
| EnterpriseBalanceTransactionMapper.xml | `.../resources/mapper/EnterpriseBalanceTransactionMapper.xml` |
| EnterpriseTopUpRecordMapper.xml | `.../resources/mapper/EnterpriseTopUpRecordMapper.xml` |
| EnterpriseBalanceService.java | `.../service/EnterpriseBalanceService.java` |
| EnterpriseBalanceServiceImpl.java | `.../service/impl/EnterpriseBalanceServiceImpl.java` |
| EnterpriseBalanceController.java | `.../controller/EnterpriseBalanceController.java` |

### platform-service
| File | Path |
|------|------|
| EnterpriseBalanceMapper.java (copy) | `.../mapper/EnterpriseBalanceMapper.java` |
| EnterpriseBalanceTransactionMapper.java (copy) | `.../mapper/EnterpriseBalanceTransactionMapper.java` |
| EnterpriseBalanceMapper.xml (copy) | `.../resources/mapper/EnterpriseBalanceMapper.xml` |
| EnterpriseBalanceTransactionMapper.xml (copy) | `.../resources/mapper/EnterpriseBalanceTransactionMapper.xml` |
| PlatformEnterpriseBalanceController.java | `.../controller/PlatformEnterpriseBalanceController.java` |
| PlatformEnterpriseBalanceService.java | `.../service/PlatformEnterpriseBalanceService.java` |
| PlatformEnterpriseBalanceServiceImpl.java | `.../service/impl/PlatformEnterpriseBalanceServiceImpl.java` |

## Files to Modify

### enterprise-service
- `SettlementServiceImpl.java` — add balance check + deduction in `payFromAttendanceRecords`; add refund in `unsettle`

### Frontends
- `enterprise-pc` — new balance page + transactions page; add nav items
- `enterprise-uniapp` — new balance pages; add nav items

## Future (not in scope)
- Platform-admin frontend page for enterprise balance management
