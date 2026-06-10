# Worker & Enterprise Verification Design

**Date:** 2026-05-31
**Scope:** Three-phase delivery in a single round.

---

## Goals

1. Add gender/birthday to worker profile.
2. Worker real-name authentication (platform admin review).
3. Enterprise real-name authentication (platform admin review).
4. Worker application requires complete basic info (name/phone/gender/birthday); popup form if missing.
5. Worker withdrawal requires APPROVED real-name; redirect to real-name page if not.
6. Worker withdrawal requires a single bound bank card; redirect to bind page if not.

## Non-Goals

- Third-party identity / OCR / liveness APIs (use admin manual review).
- Multiple bank cards per worker (single card, upsert).
- Modifying `c_worker` or `enterprises` primary tables.
- Audit history of real-name submissions (single row per worker/enterprise; mutate in place).

---

## Phase 1 — Worker Basic Info + Application Pre-fill

### Schema
Alter `worker_profiles`:
- Add `gender VARCHAR(10) NULL` (`MALE` / `FEMALE` / `OTHER`).
- Add `birthday DATE NULL`.

Migration: `scripts/6_worker_profile_extras.sql`.

### c-service API
- `GET /api/worker/profile` → `{workerId, name, phone, gender, birthday, avatarUrl}`. Auto-creates row if missing.
- `PUT /api/worker/profile` body `{name, phone, gender, birthday, avatarUrl}` (full update).
- `GET /api/worker/profile/completeness` → `{complete: boolean, missing: string[]}`. Required fields: `name`, `phone`, `gender`, `birthday`.

### c-uniapp
- `pages/profile/edit.vue`: edit form (gender radio, birthday date picker).
- Application flow: on apply click → call completeness → if `complete=false` show popup with missing fields → on save → call original apply.

---

## Phase 2 — Real-name Authentication (worker + enterprise)

### Schema
New table `worker_real_name_auth`:
```
id BIGINT PK AUTO_INCREMENT,
worker_id BIGINT NOT NULL UNIQUE,
real_name VARCHAR(100) NOT NULL,
id_card_no VARCHAR(32) NOT NULL,
id_card_front_url VARCHAR(500),
id_card_back_url VARCHAR(500),
status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING/APPROVED/REJECTED
reject_reason VARCHAR(500),
submitted_at DATETIME NOT NULL,
reviewed_at DATETIME,
reviewer_id BIGINT,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

New table `enterprise_real_name_auth`:
```
id BIGINT PK AUTO_INCREMENT,
enterprise_id BIGINT NOT NULL UNIQUE,
legal_person_name VARCHAR(100) NOT NULL,
legal_person_id_card VARCHAR(32) NOT NULL,
unified_social_credit_code VARCHAR(64) NOT NULL,
business_license_url VARCHAR(500) NOT NULL,
status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
reject_reason VARCHAR(500),
submitted_at DATETIME NOT NULL,
reviewed_at DATETIME,
reviewer_id BIGINT,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

Migration: `scripts/7_real_name_auth.sql`.

### State Machine
PENDING → APPROVED (review pass) / REJECTED (review fail).
REJECTED → may resubmit (overwrite row, status back to PENDING).
APPROVED → terminal (no resubmit).

### c-service API (worker)
- `POST /api/worker/real-name` body `{realName, idCardNo, idCardFrontUrl, idCardBackUrl}`. Reject if status=PENDING or APPROVED.
- `GET /api/worker/real-name` → `{status, realName, idCardNo (masked), rejectReason, submittedAt, reviewedAt}` or `{status: 'NONE'}`.

### enterprise-service API
- `POST /api/enterprise/real-name` body `{legalPersonName, legalPersonIdCard, unifiedSocialCreditCode, businessLicenseUrl}`. Same submit rules as worker.
- `GET /api/enterprise/real-name`.

### platform-service API (admin review)
- `GET /api/admin/worker-real-name?status=&page=&pageSize=` paginated.
- `POST /api/admin/worker-real-name/{id}/approve` (uses operator id from token).
- `POST /api/admin/worker-real-name/{id}/reject` body `{reason}`.
- Same trio for `/api/admin/enterprise-real-name`.

### Frontends
- c-uniapp `pages/auth/realName.vue`: form + status banner + reject reason.
- enterprise-pc `views/auth/RealNameAuth.vue`: form + status banner; menu under enterprise settings group.
- platform-pc `views/auth/WorkerRealNameList.vue` and `EnterpriseRealNameList.vue`: list + detail dialog with image preview + approve/reject actions.

---

## Phase 3 — Bank Card + Withdrawal Gating

### Schema
New table `worker_bank_cards`:
```
id BIGINT PK AUTO_INCREMENT,
worker_id BIGINT NOT NULL UNIQUE,
card_holder VARCHAR(100) NOT NULL,
card_number VARCHAR(32) NOT NULL,
bank_name VARCHAR(100) NOT NULL,
bank_branch VARCHAR(200),
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```
Migration: `scripts/8_worker_bank_cards.sql`.

### c-service API
- `GET /api/worker/bank-card` → card or `null`.
- `PUT /api/worker/bank-card` body `{cardHolder, cardNumber, bankName, bankBranch}` (upsert).
- `DELETE /api/worker/bank-card`.

### Withdrawal Gating
Inside `WithdrawalServiceImpl.requestWithdrawal`:
1. Real-name status != APPROVED → throw `"请先完成实名认证"`.
2. No bank card → throw `"请先绑定银行卡"`.
3. Existing balance check.
4. Write `withdrawal_record.bank_info` = JSON string of bound card.

### c-uniapp
- `pages/bank/bankCard.vue`: view/bind/unbind single card.
- Withdrawal page: on enter call real-name + bank-card; if missing show modal with "去实名" / "去绑卡" buttons; block amount submit otherwise.

---

## Error Handling
- All controllers return `{code, msg, data}` shape consistent with existing modules.
- Validation errors → HTTP 400 with `msg` populated.
- Admin approve/reject on non-PENDING row → 400 `"状态不允许操作"`.

## Testing
- Service-layer unit tests per new service:
  - `WorkerProfileServiceTest` (completeness boolean, partial fields).
  - `WorkerRealNameServiceTest` (state machine: submit/resubmit/approve/reject).
  - `EnterpriseRealNameServiceTest` (same).
  - `WorkerBankCardServiceTest` (upsert, delete).
  - `WithdrawalServiceTest` (gating order: real-name fail, bank fail, balance fail, happy path).
- Build verification:
  - `mvn compile` per backend module.
  - `npm run build` for platform-pc and enterprise-pc.
  - `npm run build:mp-weixin` for enterprise-uniapp (n/a here) and c-uniapp.

## Out of Scope / Decisions
- ID card number stored plaintext (MVP); masking only in GET responses.
- No SMS verification on bank card binding (MVP).
- Real-name resubmit overwrites prior row in place; no history table.
