# 邀请链接/海报生成规范

## 概述

工人端提供邀请链接和海报生成功能，方便用户分享邀请新用户。

## 邀请链接

### 格式
```
https://worker.example.com/invite?code={邀请码}
```

### 生成规则
- 邀请码为 8 位随机字符串（字母+数字）
- 每个工人只有一个固定邀请码
- 邀请码存储在 `referral_code` 表

### 接口

**获取邀请链接**
```
GET /api/referral/link
```

响应：
```json
{
  "code": 200,
  "message": "",
  "data": {
    "code": "ABC12345",
    "link": "https://worker.example.com/invite?code=ABC12345"
  }
}
```

## 邀请海报

### 生成规则
- 服务端生成海报图片
- 海报包含：用户昵称、邀请二维码、平台 logo
- 二维码内容为邀请链接

### 接口

**获取邀请海报**
```
GET /api/referral/poster
```

响应：
```json
{
  "code": 200,
  "message": "",
  "data": {
    "posterUrl": "https://cdn.example.com/poster/ABC12345.jpg"
  }
}
```

### 实现要点
- 使用服务端图片生成库（如 Java 的 Graphics2D 或第三方库）
- 二维码使用 Google ZXing 或类似库生成
- 海报图片缓存到 CDN 或本地文件系统
- 首次请求时生成，后续直接返回缓存

## 前端展示

### 邀请页面
- 显示邀请链接，支持一键复制
- 显示海报预览
- 支持保存海报到相册
- 支持分享海报到微信/朋友圈

### 分享功能
- 使用微信分享 API
- 自定义分享标题和描述
- 分享图片使用海报缩略图
