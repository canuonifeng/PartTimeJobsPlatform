# 邀请海报生成设计

## 概述
前端使用 canvas 绘制邀请海报，包含背景图、邀请码和二维码，生成后保存到相册供用户分享。

## 方案
- 前端 canvas 绘制，不依赖后端图片生成
- 使用 uni-app canvas API 兼容小程序和 H5
- 海报内容：背景图 + 邀请码文字 + 邀请链接二维码

## 海报布局
```
┌──────────────────────────┐
│                          │
│     [背景图/渐变色]       │
│                          │
│    ┌────────────────┐    │
│    │   邀请码: XXXX  │    │
│    │   [二维码]      │    │
│    │   扫码加入平台   │    │
│    └────────────────┘    │
│                          │
└──────────────────────────┘
```

## 技术实现
1. 使用 `uni.createCanvasContext` 创建 canvas 上下文
2. 绘制背景渐变色
3. 绘制邀请码文字
4. 使用第三方库生成二维码（如 uQRCode）
5. `uni.canvasToTempFilePath` 导出为临时图片
6. `uni.saveImageToPhotosAlbum` 保存到相册

## 文件变更
- `worker-uniapp/src/pages/referral/referral.vue` - 重写海报部分，使用 canvas 绘制
- `worker-uniapp/package.json` - 添加 uQRCode 依赖（如需要）

## 分享功能
- 保存到相册后用户可自行分享
- 小程序环境下可使用 `wx.shareImageMessage` 分享
