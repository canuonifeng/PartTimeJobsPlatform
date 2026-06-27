## 1. 后端：微信手机号登录接口

- [x] 1.1 新增 WeChatPhoneLoginCmd（code、encryptedData、iv）
- [x] 1.2 新增 LoginVO 增加 phone 字段
- [x] 1.3 AuthController 新增 POST /api/auth/wechat-phone-login 接口
- [x] 1.4 WorkerService 新增 loginWithWechatPhone 方法
- [x] 1.5 WorkerServiceImpl 实现微信手机号登录逻辑（code→openId、解密手机号、用户匹配/创建/绑定）
- [x] 1.6 WorkerMapper 新增 bindOpenId 方法（已有用户绑定 openId）

## 2. 后端：手机号不可修改

- [x] 2.1 WorkerServiceImpl.updateProfile 中忽略 phone 字段变更

## 3. 前端：工人端微信登录

- [x] 3.1 src/api/auth.js 新增 wechatPhoneLogin 接口
- [x] 3.2 src/pages/login/login.vue 重构登录页，优先展示微信一键登录按钮
- [x] 3.3 实现 wx.login + getPhoneNumber 联合调用逻辑

## 4. 测试验证

- [x] 4.1 c-service 编译通过
- [x] 4.2 工人端 UniApp 构建通过
