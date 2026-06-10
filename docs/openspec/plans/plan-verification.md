# Worker & Enterprise Verification Implementation Plan

**Goal:** Worker basic info (gender/birthday), worker/enterprise real-name auth with platform admin review, bank card binding, and application/withdrawal gating.

**Architecture:** Three-phase delivery. Phase 1 extends worker_profiles + profile API + application gating. Phase 2 adds real-name auth (c-service, enterprise-service, platform-service review). Phase 3 adds single bank card + withdrawal gating.

**Tech Stack:** Spring Boot (JDK 17), MyBatis, Vue 3 + Element Plus, uni-app, MySQL.

---

## Phase 1 — Worker Basic Info + Application Gating

### Task 1: SQL migration
Create `scripts/6_worker_profile_extras.sql`:
```
ALTER TABLE worker_profiles ADD COLUMN gender VARCHAR(10) NULL;
ALTER TABLE worker_profiles ADD COLUMN birthday DATE NULL;
```

### Task 2: WorkerProfile entity
Modify `c-service/src/main/java/com/parttime/cservice/pojo/entity/WorkerProfile.java`: add `gender String`, `birthday LocalDate`.

### Task 3: Mapper
Modify `WorkerProfileMapper.java` + `WorkerProfileMapper.xml`:
- `WorkerProfile findByWorkerId(Long)`
- `void upsert(WorkerProfile)` (INSERT ... ON DUPLICATE KEY UPDATE on worker_id)

### Task 4: WorkerProfileService (TDD)
Create interface + impl + `ProfileCompletenessVO`:
- `getProfile(workerId)` — auto-creates row if absent.
- `updateProfile(workerId, name, phone, gender, birthday, avatarUrl)`.
- `getCompleteness(workerId)` — required: name, phone, gender, birthday.

Test: `WorkerProfileServiceTest` covers auto-create, update, completeness true/false/missing list.

Verify: `cd c-service && mvn test -Dtest=WorkerProfileServiceTest -q`.

### Task 5: WorkerProfileController
Create controller mapped at `/api/worker/profile`:
- `GET /` → getProfile.
- `PUT /` → updateProfile.
- `GET /completeness` → completeness VO.
workerId from `X-Worker-Id` header (same as withdrawal).

Verify: `cd c-service && mvn compile -q`.

### Task 6: c-uniapp profile edit
Create `c-uniapp/src/api/profile.js`, `c-uniapp/src/pages/profile/edit.vue`.
Modify `pages.json`, `pages/home/index.vue` to add entry "个人资料".

Verify: `cd c-uniapp && npm run build:mp-weixin`.

### Task 7: Application gating backend
Modify `JobApplicationController` apply endpoint: call `WorkerProfileService.getCompleteness` first; if not complete throw "请先完善个人信息".

### Task 8: Application gating frontend
Modify job apply page in c-uniapp: on click apply → call completeness → if false show popup form (name/phone/gender/birthday) → save → re-apply.

Phase 1 commit: `feat(phase1): worker basic info + application gating`.

---

## Phase 2 — Real-name Authentication

### Task 9: SQL migration
Create `scripts/7_real_name_auth.sql` with `worker_real_name_auth` and `enterprise_real_name_auth` tables as in spec.

### Task 10: Worker real-name — c-service (TDD)
Create entity, mapper (+ XML), service interface + impl, controller, service test.
- `submitRealName(workerId, payload)`: NONE/REJECTED → upsert PENDING; PENDING → "已提交，请等待审核"; APPROVED → "已通过认证".
- `getRealNameStatus(workerId)`: returns status or `NONE`.
Endpoints: `POST /api/worker/real-name`, `GET /api/worker/real-name`.

Verify: `mvn test -Dtest=WorkerRealNameAuthServiceTest -q`.

### Task 11: Enterprise real-name — enterprise-service (TDD)
Mirror Task 10 for `enterprise_real_name_auth`. Endpoints under `/api/enterprise/real-name`. enterpriseId from `X-Enterprise-Id`.

### Task 12: Platform admin review service (TDD)
In platform-service:
- `WorkerRealNameAuthReviewService`: list (page + status filter), approve(id, reviewerId), reject(id, reviewerId, reason). Only PENDING allowed.
- `EnterpriseRealNameAuthReviewService`: same shape.
Controllers under `/api/admin/worker-real-name/...` and `/api/admin/enterprise-real-name/...`.

### Task 13: Platform PC review UI
Create `platform-pc/src/views/auth/WorkerRealNameList.vue`, `EnterpriseRealNameList.vue`, `api/workerRealName.js`, `api/enterpriseRealName.js`.
Modify router and App.vue: add nav "实名审核-兼职" / "实名审核-企业".

Verify: `cd platform-pc && npm run build`.

### Task 14: c-uniapp worker real-name page
Create `c-uniapp/src/pages/auth/realName.vue` + `api/realName.js`.
Show current status, form for submit, reject reason banner.
Add route + home entry.

Verify: `cd c-uniapp && npm run build:mp-weixin`.

### Task 15: enterprise-pc real-name page
Create `enterprise-pc/src/views/auth/RealNameAuth.vue` + `api/realName.js`.
Modify router + App.vue add menu "实名认证".

Verify: `cd enterprise-pc && npm run build`.

Phase 2 commit: `feat(phase2): real-name auth with platform review`.

---

## Phase 3 — Bank Card + Withdrawal Gating

### Task 16: SQL migration
Create `scripts/8_worker_bank_cards.sql` with `worker_bank_cards` (UNIQUE worker_id).

### Task 17: WorkerBankCard backend (TDD)
Entity, mapper (+ XML), service (`upsert`, `find`, `delete`), controller mapped at `/api/worker/bank-card`.

Verify: `mvn test -Dtest=WorkerBankCardServiceTest -q`.

### Task 18: Withdrawal gating (TDD)
Modify `WithdrawalServiceImpl.requestWithdrawal`:
1. If real-name status != APPROVED → throw "请先完成实名认证".
2. If no bank card → throw "请先绑定银行卡".
3. Existing balance check.
4. Store card JSON in `withdrawal_record.bank_info`.

Tests cover all four branches.

Verify: `mvn test -Dtest=WithdrawalServiceTest -q`.

### Task 19: c-uniapp bank card page
Create `c-uniapp/src/pages/bank/bankCard.vue` + `api/bankCard.js`.
Add route + home entry.

### Task 20: c-uniapp withdrawal gating
Modify withdrawal page: on enter call real-name + bank-card; if missing show modal with "去实名" / "去绑卡" deep links; block submit otherwise.

Verify: `cd c-uniapp && npm run build:mp-weixin`.

Phase 3 commit: `feat(phase3): bank card + withdrawal gating`.

---

## Verification Summary

Each phase ends with:
- All new service tests passing.
- `mvn compile` on touched backend modules.
- `npm run build` on touched PC frontends.
- `npm run build:mp-weixin` on c-uniapp if touched.
- Phase commit with `feat(phaseN): ...` message.
- Push only after all three phases pass, unless user requests per-phase push.
