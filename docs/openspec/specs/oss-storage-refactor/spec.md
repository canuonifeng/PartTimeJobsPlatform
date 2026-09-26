# oss-storage-refactor 规格

## 需求

### REQ-1: 双 bucket 存储模型
**优先级**: P0

**描述**: 系统文件存储划分为阿里云 OSS 私有 bucket 与公有 bucket 两类；上传时按业务场景自动落入对应 bucket。

**场景**:
- 场景 1：工人提交实名认证（身份证正反面）→ 文件落入**私有** bucket，路径含 workerId
- 场景 2：企业提交资质（营业执照）→ 文件落入**私有** bucket，路径含 enterpriseId
- 场景 3：企业上传职位图片、Logo → 文件落入**公有** bucket
- 场景 4：平台上传培训课件（视频/音频/文档）、运营素材 → 文件落入**公有** bucket
- 场景 5：工人上传头像 → 文件落入**公有** bucket

**约束**:
- 私有 bucket 默认不可匿名读；公有 bucket 可匿名读
- 文件对象路径包含业务域与所有者标识（如 `realname/{workerId}/...`、`license/{enterpriseId}/...`、`job/{companyId}/...`、`training/...`、`avatar/{workerId}/...`）

**验收标准**:
- [ ] 上传后返回的对象 key 带正确的 bucket 标识与路径前缀
- [ ] 公有文件可直接通过公开 URL 访问；私有文件直接访问返回 403

### REQ-2: 前端直传（STS 临时凭证）
**优先级**: P0

**描述**: 前端上传不再走后端中转，改为请求 STS 临时凭证后直传 OSS。

**场景**:
- 场景 1：worker-uniapp 实名认证上传 → 请求 `/api/worker/files/sts` → 拿临时凭证直传私有 bucket
- 场景 2：enterprise-pc 上传营业执照 → 请求 `/api/enterprise/files/sts` → 直传私有 bucket
- 场景 3：enterprise-pc 上传职位图片/Logo → 直传公有 bucket
- 场景 4：platform-pc 上传培训课件/运营素材 → 请求 `/api/admin/files/sts` → 直传公有 bucket
- 场景 5：worker-uniapp 上传头像 → 直传公有 bucket

**约束**:
- STS 返回：bucket、region/endpoint、临时 AccessKeyId、AccessKeySecret、SecurityToken、有效期（建议 30–60 分钟）、允许写入的对象前缀
- 对象 key 由**后端下发前缀 + 前端生成文件名**，禁止前端任意指定路径，防止越权覆盖
- STS 接口鉴权：必须登录（工人/企业/平台管理员分别走各自认证）

**验收标准**:
- [ ] 三个服务各有一个 STS 接口，返回完整直传参数
- [ ] 前端四处端（worker-uniapp / enterprise-pc / enterprise-uniapp / platform-pc）上传走直传，不再调旧中转接口
- [ ] 未登录调用 STS 返回 401/403

### REQ-3: 私有文件签名 URL 访问
**优先级**: P0

**描述**: 私有 bucket 文件通过后端签发短期签名 URL 访问，URL 过期即失效。

**场景**:
- 场景 1：工人查看自己的实名认证资料 → 请求签名 URL 成功
- 场景 2：企业审核人员查看某工人实名认证资料 → 请求签名 URL 成功
- 场景 3：平台审核人员审核工人实名/企业资质 → 请求签名 URL 成功
- 场景 4：无权限人员（如其他工人）请求他人实名资料 → 403
- 场景 5：签名 URL 过期后再访问 → 403

**约束**:
- 签名 URL 有效期建议 5–15 分钟，返回后由前端即时使用
- 权限矩阵见 REQ-4

**验收标准**:
- [ ] 私有文件查看统一走签名 URL 接口（按对象 key 或按业务实体 ID）
- [ ] 权限不足返回 403，不泄露文件内容

### REQ-4: 私有文件访问权限矩阵
**优先级**: P0

**描述**: 私有文件（实名认证、企业资质）的查看权限按角色与归属控制。

| 文件类型 | 本人 | 企业审核人员 | 平台审核人员 | 其他 |
|---|---|---|---|---|
| 工人实名认证资料（身份证正反面） | ✅ | ✅（报名/排班审核该工人的企业） | ✅ | ❌ |
| 企业资质文件（营业执照等） | ✅（企业本人） | — | ✅ | ❌ |

**约束**:
- "企业审核人员"特指对**该工人已报名/已排班岗位所属企业**的登录用户
- 平台审核人员为登录的平台管理员
- 拒绝访问时返回 403

**验收标准**:
- [ ] 按上述矩阵逐项验证通过/拒绝
- [ ] 鉴权逻辑在服务端，不依赖前端隐藏

### REQ-5: 文件 URL 持久化与兼容
**优先级**: P1

**描述**: 数据库中原 URL 字段升级为支持 OSS 对象标识；保留旧字段结构兼容。

**场景**:
- 场景 1：`worker_real_name_auth.id_card_front_url` 存 OSS 对象 key（私有）或完整 URL（迁移后），读取时按需转签名 URL
- 场景 2：`enterprise_real_name_auth.business_license_url` 同理
- 场景 3：jobs 职位图片、enterprise Logo、培训课时媒体存公有完整 URL

**约束**:
- 不新增大规模表结构变更；以字段内容约定（完整 URL vs 对象 key 标识）区分
- 旧本地文件 URL（`/uploads/...`）读取兼容：能正常展示，不强制迁移

**验收标准**:
- [ ] 新上传文件 URL 写入格式统一、可解析
- [ ] 历史 `/uploads/` 地址在页面上仍可展示（dev 环境）

### REQ-6: 配置与多环境
**优先级**: P1

**描述**: OSS 配置（endpoint、bucket 名、AK/SK、STS RoleArn）通过环境变量注入，支持 dev/prod 多环境。

**场景**:
- 场景 1：dev 环境配置指向测试 bucket，无需真实 AK
- 场景 2：prod 环境通过环境变量注入真实 OSS 配置

**约束**:
- 不提交真实 AK/SK 到仓库
- 未配置 OSS 时服务可降级（上传返回配置错误提示或保留本地 fallback，二者取一并在文档说明）

**验收标准**:
- [ ] 配置项集中在各服务 application.yml + 环境变量
- [ ] 仓库无真实密钥
