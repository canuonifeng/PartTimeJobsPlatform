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
      <canvas canvas-id="qrcodeCanvas" id="qrcodeCanvas" class="qrcode-canvas" />
      <image v-if="posterUrl" :src="posterUrl" class="poster-image" mode="widthFix" />
      <button class="share-btn" @click="handlePosterAction">{{ posterUrl ? '复制邀请链接' : '生成海报' }}</button>
      <text v-if="posterUrl" class="poster-tip">长按图片保存后分享给好友</text>
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
const isGenerating = ref(false)
const stats = ref({
  totalReferees: 0,
  totalRewardAmount: 0,
  pendingRewardAmount: 0
})

async function fetchReferralInfo() {
  try {
    const linkRes = await getReferralLink()
    referralCode.value = linkRes.code
    referralLink.value = 'https://www.linggong.tech/invite?code=' + linkRes.code
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
  if (isGenerating.value) return
  isGenerating.value = true
  uni.showLoading({ title: '生成海报中', mask: true })

  try {
    const ctx = uni.createCanvasContext('posterCanvas')
    const w = 600
    const h = 900

    // Background gradient
    const grd = ctx.createLinearGradient(0, 0, w, h)
    grd.addColorStop(0, '#1aab5a')
    grd.addColorStop(0.5, '#20c26b')
    grd.addColorStop(1, '#2ad879')
    ctx.setFillStyle(grd)
    ctx.fillRect(0, 0, w, h)

    // Decorative circles
    ctx.setFillStyle('rgba(255,255,255,0.1)')
    ctx.beginPath()
    ctx.arc(w * 0.8, h * 0.2, 120, 0, 2 * Math.PI)
    ctx.fill()
    ctx.beginPath()
    ctx.arc(w * 0.2, h * 0.7, 80, 0, 2 * Math.PI)
    ctx.fill()

    // Title
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

    // White card
    ctx.setFillStyle('#ffffff')
    ctx.shadowColor = 'rgba(0,0,0,0.15)'
    ctx.shadowBlur = 20
    ctx.shadowOffsetY = 4
    ctx.fillRect(boxX, boxY, boxW, boxH)
    ctx.shadowColor = 'transparent'

    // Invite code label
    ctx.setFillStyle('#333333')
    ctx.setFontSize(20)
    ctx.setTextAlign('center')
    ctx.fillText('我的邀请码', w / 2, boxY + 40)

    // Invite code
    ctx.setFillStyle('#20c26b')
    ctx.setFontSize(36)
    ctx.fillText(referralCode.value, w / 2, boxY + 85)

    // Divider
    ctx.setStrokeStyle('#e5e7eb')
    ctx.setLineWidth(1)
    ctx.beginPath()
    ctx.moveTo(boxX + 30, boxY + 110)
    ctx.lineTo(boxX + boxW - 30, boxY + 110)
    ctx.stroke()

    // QR code position
    const qrSize = 200
    const qrX = (w - qrSize) / 2
    const qrY = boxY + 130

    // QR code white background
    ctx.setFillStyle('#ffffff')
    ctx.fillRect(qrX - 10, qrY - 10, qrSize + 20, qrSize + 20)

    // Draw QR to separate canvas (drawQrcode calls ctx.draw internally with callback)
    const qrTempPath = await new Promise((resolve) => {
      const qrCtx = uni.createCanvasContext('qrcodeCanvas')
      drawQrcode({
        ctx: qrCtx,
        width: qrSize,
        height: qrSize,
        x: 0,
        y: 0,
        text: referralLink.value,
        background: '#ffffff',
        foreground: '#1f2937',
        callback: () => {
          setTimeout(() => {
            uni.canvasToTempFilePath({
              canvasId: 'qrcodeCanvas',
              width: qrSize,
              height: qrSize,
              success: (qrRes) => resolve(qrRes.tempFilePath),
              fail: () => resolve('')
            })
          }, 50)
        }
      })
    })
    if (qrTempPath) {
      ctx.drawImage(qrTempPath, qrX, qrY, qrSize, qrSize)
    }

    // QR code hint
    ctx.setFillStyle('#6b7280')
    ctx.setFontSize(16)
    ctx.setTextAlign('center')
    ctx.fillText('长按识别二维码', w / 2, qrY + qrSize + 30)

    ctx.setFillStyle('#333333')
    ctx.setFontSize(18)
    ctx.fillText('或复制下方链接注册', w / 2, qrY + qrSize + 60)

    // Link text
    const linkText = referralLink.value
    ctx.setFillStyle('#20c26b')
    ctx.setFontSize(14)
    let y = qrY + qrSize + 85
    for (let i = 0; i < linkText.length; i += 20) {
      ctx.fillText(linkText.slice(i, i + 20), w / 2, y)
      y += 22
    }

    // Footer area
    ctx.setFillStyle('rgba(255,255,255,0.15)')
    ctx.fillRect(0, h - 160, w, 160)

    ctx.setFillStyle('#ffffff')
    ctx.setFontSize(20)
    ctx.setTextAlign('center')
    ctx.fillText('零工平台 · 安全可靠', w / 2, h - 110)

    ctx.setFillStyle('rgba(255,255,255,0.7)')
    ctx.setFontSize(16)
    ctx.fillText('加入我们，开启灵活工作', w / 2, h - 80)

    // Final draw and export
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
            isGenerating.value = false
            uni.showToast({ title: '海报生成成功', icon: 'success' })
          },
          fail: (err) => {
            console.error('canvasToTempFilePath fail:', err)
            uni.hideLoading()
            isGenerating.value = false
            uni.showToast({ title: '海报生成失败', icon: 'none' })
          }
        })
      }, 300)
    })
  } catch (e) {
    console.error('generatePoster error:', e)
    uni.hideLoading()
    isGenerating.value = false
    uni.showToast({ title: '海报生成失败', icon: 'none' })
  }
}

function handlePosterAction() {
  if (posterUrl.value) {
    copyPosterImage()
  } else {
    generatePoster()
  }
}

async function copyPosterImage() {
  if (!posterUrl.value) return
  // #ifdef H5
  try {
    const res = await fetch(posterUrl.value)
    const blob = await res.blob()
    if (navigator.clipboard && window.ClipboardItem) {
      await navigator.clipboard.write([
        new ClipboardItem({ [blob.type]: blob })
      ])
      uni.showToast({ title: '海报已复制', icon: 'success' })
    } else {
      uni.showToast({ title: '当前浏览器不支持复制图片', icon: 'none' })
    }
  } catch (e) {
    console.error('copy image error:', e)
    uni.showToast({ title: '复制失败，请长按图片保存', icon: 'none' })
  }
  // #endif
  // #ifndef H5
  uni.showToast({ title: '请长按图片保存后分享', icon: 'none', duration: 2000 })
  // #endif
}

function savePoster() {
  if (!posterUrl.value) return
  // #ifdef H5
  // H5: open image in new tab
  uni.previewImage({
    urls: [posterUrl.value],
    current: posterUrl.value
  })
  // #endif
  // #ifndef H5
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
  // #endif
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
.qrcode-canvas { position: fixed; left: -9999rpx; width: 200px; height: 200px; }
.poster-image { width: 100%; border-radius: 16rpx; margin-bottom: 24rpx; }
.share-btn { background: #20c26b; color: #fff; border: none; border-radius: 40rpx; padding: 24rpx; font-size: 30rpx; font-weight: 600; }
.poster-tip { display: block; text-align: center; font-size: 24rpx; color: #9ca3af; margin-top: 16rpx; }
.records-link { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; }
.arrow { color: #999; }
</style>
