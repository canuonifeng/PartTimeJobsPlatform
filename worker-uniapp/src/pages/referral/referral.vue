<template>
  <view class="page">
    <view class="header">
      <text class="title">邀请好友</text>
      <text class="subtitle">邀请好友加入平台，获得奖励</text>
    </view>

    <view class="stats-card">
      <view class="stat-item">
        <text class="stat-value">{{ stats.totalReferees || 0 }}</text>
        <text class="stat-label">邀请人数</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.totalRewardAmount || 0 }}</text>
        <text class="stat-label">累计奖励(元)</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.pendingRewardAmount || 0 }}</text>
        <text class="stat-label">待发放(元)</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">邀请链接</text>
      <view class="link-box">
        <text class="link-text">{{ referralLink }}</text>
        <button class="copy-btn" @click="copyLink">复制</button>
      </view>
    </view>

    <view class="section">
      <text class="section-title">邀请海报</text>
      <canvas canvas-id="posterCanvas" id="posterCanvas" class="poster-canvas" />
      <canvas canvas-id="qrcodeCanvas" style="width:200px;height:200px;position:fixed;left:-9999px;"></canvas>
      <image v-if="posterUrl" :src="posterUrl" class="poster-image" mode="aspectFit" />
      <button class="share-btn" @click="generatePoster">{{ posterUrl ? '保存海报' : '生成海报' }}</button>
    </view>

    <view class="section">
      <navigator url="/pages/referral/referralRecords" class="records-link">
        <text>查看邀请记录</text>
        <text class="arrow">›</text>
      </navigator>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getReferralLink, getReferralStats } from '@/api/referral'
import drawQrcode from 'weapp-qrcode'

const referralLink = ref('')
const referralCode = ref('')
const inviterName = ref('')
const posterUrl = ref('')
const stats = ref({
  totalReferees: 0,
  totalRewardAmount: 0,
  pendingRewardAmount: 0
})

async function fetchReferralInfo() {
  try {
    const linkRes = await getReferralLink()
    referralLink.value = linkRes.link
    referralCode.value = linkRes.code
    inviterName.value = linkRes.inviterName || ''

    const statsRes = await getReferralStats()
    stats.value = statsRes
  } catch (e) {
    console.error('获取邀请信息失败', e)
  }
}

function copyLink() {
  uni.setClipboardData({
    data: referralLink.value,
    success: () => {
      uni.showToast({ title: '链接已复制', icon: 'success' })
    }
  })
}

async function generatePoster() {
  if (posterUrl.value) {
    savePoster()
    return
  }

  uni.showLoading({ title: '生成海报中' })

  try {
    const ctx = uni.createCanvasContext('posterCanvas')
    const w = 600
    const h = 900

    const grd = ctx.createLinearGradient(0, 0, w, h)
    grd.addColorStop(0, '#6d28d9')
    grd.addColorStop(0.5, '#8b5cf6')
    grd.addColorStop(1, '#a78bfa')
    ctx.setFillStyle(grd)
    ctx.fillRect(0, 0, w, h)

    ctx.setFillStyle('rgba(255,255,255,0.1)')
    ctx.beginPath()
    ctx.arc(w * 0.8, h * 0.2, 120, 0, 2 * Math.PI)
    ctx.fill()
    ctx.beginPath()
    ctx.arc(w * 0.2, h * 0.7, 80, 0, 2 * Math.PI)
    ctx.fill()

    ctx.setFillStyle('#ffffff')
    ctx.setFontSize(28)
    ctx.setTextAlign('center')
    ctx.fillText('邀请好友加入', w / 2, 60)

    ctx.setFontSize(22)
    ctx.setFillStyle('rgba(255,255,255,0.8)')
    ctx.fillText('扫码或复制邀请码注册', w / 2, 95)

    if (inviterName.value) {
      ctx.setFontSize(24)
      ctx.setFillStyle('rgba(255,255,255,0.9)')
      ctx.fillText('邀请人：' + inviterName.value, w / 2, 125)
    }

    const boxX = 60
    const boxY = inviterName.value ? 160 : 130
    const boxW = w - 120
    const boxH = 490
    ctx.setFillStyle('#ffffff')
    ctx.shadowColor = 'rgba(0,0,0,0.15)'
    ctx.shadowBlur = 20
    ctx.shadowOffsetY = 4
    ctx.fillRect(boxX, boxY, boxW, boxH)
    ctx.shadowColor = 'transparent'

    ctx.setFillStyle('#333333')
    ctx.setFontSize(20)
    ctx.setTextAlign('center')
    ctx.fillText('我的邀请码', w / 2, boxY + 40)

    ctx.setFillStyle('#6d28d9')
    ctx.setFontSize(36)
    ctx.fillText(referralCode.value, w / 2, boxY + 85)

    ctx.setStrokeStyle('#e5e7eb')
    ctx.setLineWidth(1)
    ctx.beginPath()
    ctx.moveTo(boxX + 30, boxY + 110)
    ctx.lineTo(boxX + boxW - 30, boxY + 110)
    ctx.stroke()

    const qrSize = 200
    const qrX = (w - qrSize) / 2
    const qrY = boxY + 130

    await new Promise((resolve, reject) => {
      drawQrcode({
        canvasId: 'qrcodeCanvas',
        width: qrSize,
        height: qrSize,
        text: referralLink.value,
        background: '#ffffff',
        foreground: '#1f2937',
        success: resolve,
        fail: reject
      })
    })

    const tempRes = await new Promise((resolve, reject) => {
      uni.canvasToTempFilePath({
        canvasId: 'qrcodeCanvas',
        success: resolve,
        fail: reject
      })
    })

    ctx.setFillStyle('#ffffff')
    ctx.fillRect(qrX - 10, qrY - 10, qrSize + 20, qrSize + 20)

    ctx.drawImage(tempRes.tempFilePath, qrX, qrY, qrSize, qrSize)

    ctx.setFillStyle('#6b7280')
    ctx.setFontSize(16)
    ctx.setTextAlign('center')
    ctx.fillText('长按识别二维码', w / 2, qrY + qrSize + 30)

    ctx.setFillStyle('#333333')
    ctx.setFontSize(18)
    ctx.fillText('或复制下方链接注册', w / 2, qrY + qrSize + 60)

    const linkText = referralLink.value
    ctx.setFillStyle('#6d28d9')
    ctx.setFontSize(14)
    const maxLineWidth = boxW - 60
    let y = qrY + qrSize + 85
    for (let i = 0; i < linkText.length; i += 20) {
      ctx.fillText(linkText.slice(i, i + 20), w / 2, y)
      y += 22
    }

    ctx.setFillStyle('rgba(255,255,255,0.15)')
    ctx.fillRect(0, h - 160, w, 160)

    ctx.setFillStyle('#ffffff')
    ctx.setFontSize(20)
    ctx.setTextAlign('center')
    ctx.fillText('零工平台 · 安全可靠', w / 2, h - 110)

    ctx.setFillStyle('rgba(255,255,255,0.7)')
    ctx.setFontSize(16)
    ctx.fillText('加入我们，开启灵活工作', w / 2, h - 80)

    ctx.draw(false, () => {
      setTimeout(() => {
        uni.canvasToTempFilePath({
          canvasId: 'posterCanvas',
          width: w,
          height: h,
          destWidth: w * 2,
          destHeight: h * 2,
          success: (res) => {
            posterUrl.value = res.tempFilePath
            uni.hideLoading()
            savePoster()
          },
          fail: () => {
            uni.hideLoading()
            uni.showToast({ title: '海报生成失败', icon: 'none' })
          }
        })
      }, 200)
    })
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: '海报生成失败', icon: 'none' })
  }
}

function savePoster() {
  if (!posterUrl.value) return
  uni.saveImageToPhotosAlbum({
    filePath: posterUrl.value,
    success: () => {
      uni.showToast({ title: '已保存到相册', icon: 'success' })
    },
    fail: (err) => {
      if (err.errMsg && err.errMsg.includes('auth deny')) {
        uni.showModal({
          title: '提示',
          content: '需要授权保存到相册',
          success: (res) => {
            if (res.confirm) {
              uni.openSetting()
            }
          }
        })
      } else {
        uni.showToast({ title: '保存失败', icon: 'none' })
      }
    }
  })
}

onShow(() => {
  fetchReferralInfo()
})
</script>

<style>
.page { min-height: 100vh; background: #f5f5f5; padding: 16rpx; }
.header { text-align: center; padding: 40rpx 0; }
.title { font-size: 36rpx; font-weight: 600; color: #333; display: block; }
.subtitle { font-size: 26rpx; color: #999; margin-top: 8rpx; display: block; }
.stats-card { background: #fff; border-radius: 16rpx; padding: 32rpx; display: flex; justify-content: space-around; margin-bottom: 16rpx; }
.stat-item { text-align: center; }
.stat-value { font-size: 40rpx; font-weight: 600; color: #409eff; display: block; }
.stat-label { font-size: 24rpx; color: #999; margin-top: 8rpx; display: block; }
.section { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.section-title { font-size: 28rpx; font-weight: 500; color: #333; margin-bottom: 16rpx; display: block; }
.link-box { display: flex; align-items: center; gap: 16rpx; }
.link-text { flex: 1; font-size: 24rpx; color: #666; background: #f5f5f5; padding: 16rpx; border-radius: 8rpx; word-break: break-all; }
.copy-btn { background: #409eff; color: #fff; border: none; border-radius: 8rpx; padding: 16rpx 32rpx; font-size: 26rpx; }
.poster-canvas { position: fixed; left: -9999rpx; width: 600px; height: 900px; }
.poster-image { width: 100%; height: 500rpx; border-radius: 8rpx; margin-bottom: 16rpx; }
.share-btn { background: #67c23a; color: #fff; border: none; border-radius: 8rpx; padding: 24rpx; font-size: 28rpx; }
.records-link { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; }
.arrow { color: #999; }
</style>
