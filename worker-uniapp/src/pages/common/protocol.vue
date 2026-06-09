<template>
  <view class="protocol-page">
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="nav-title">{{ title }}</text>
      <view class="nav-placeholder"></view>
    </view>

    <view class="content-area" v-if="loading">
      <view class="loading-box">
        <text class="loading-text">加载中...</text>
      </view>
    </view>

    <view class="content-area" v-else-if="content">
      <view class="markdown-body" v-html="renderedContent"></view>
    </view>

    <view class="content-area" v-else>
      <view class="empty-box">
        <text class="empty-text">内容加载失败</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/api/request'

const title = ref('')
const content = ref('')
const loading = ref(true)
const configKey = ref('')

onLoad((params) => {
  configKey.value = params?.key || ''
  title.value = configKey.value === 'privacy_policy' ? '隐私政策' : '用户协议'
  loadContent()
})

function goBack() {
  uni.navigateBack()
}

async function loadContent() {
  loading.value = true
  try {
    const data = await request({
      url: `/api/auth/configs/${configKey.value}`,
      method: 'GET'
    })
    content.value = data?.value || ''
  } catch {
    content.value = ''
  } finally {
    loading.value = false
  }
}

const renderedContent = computed(() => {
  if (!content.value) return ''
  return markdownToHtml(content.value)
})

function markdownToHtml(md: string): string {
  let html = md
  html = html.replace(/^### (.*$)/gm, '<h3>$1</h3>')
  html = html.replace(/^## (.*$)/gm, '<h2>$1</h2>')
  html = html.replace(/^# (.*$)/gm, '<h1>$1</h1>')
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\*(.*?)\*/g, '<em>$1</em>')
  html = html.replace(/^\|(.*)\|$/gm, (match) => {
    const cells = match.split('|').filter(c => c.trim())
    if (cells.every(c => c.trim().match(/^-+$/))) return ''
    return '<tr>' + cells.map(c => `<td>${c.trim()}</td>`).join('') + '</tr>'
  })
  html = html.replace(/(<tr>.*<\/tr>\n?)+/g, (match) => `<table>${match}</table>`)
  html = html.replace(/^- (.*$)/gm, '<li>$1</li>')
  html = html.replace(/(<li>.*<\/li>\n?)+/g, (match) => `<ul>${match}</ul>`)
  html = html.replace(/^\d+\. (.*$)/gm, '<li>$1</li>')
  html = html.replace(/^-{3,}$/gm, '<hr/>')
  html = html.replace(/\n{2,}/g, '</p><p>')
  html = html.replace(/\n/g, '<br/>')
  html = '<p>' + html + '</p>'
  html = html.replace(/<p><\/p>/g, '')
  html = html.replace(/<p>(<h[1-3]>)/g, '$1')
  html = html.replace(/(<\/h[1-3]>)<\/p>/g, '$1')
  html = html.replace(/<p>(<ul>)/g, '$1')
  html = html.replace(/(<\/ul>)<\/p>/g, '$1')
  html = html.replace(/<p>(<table>)/g, '$1')
  html = html.replace(/(<\/table>)<\/p>/g, '$1')
  html = html.replace(/<p>(<hr\/>)<\/p>/g, '$1')
  return html
}
</script>

<style scoped>
.protocol-page {
  min-height: 100vh;
  background: #f8f9fa;
}

.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  background: #ffffff;
  border-bottom: 1rpx solid #eee;
}

.nav-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 36rpx;
  color: #333;
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.nav-placeholder {
  width: 60rpx;
}

.content-area {
  padding: 88rpx 32rpx 48rpx;
}

.loading-box,
.empty-box {
  display: flex;
  justify-content: center;
  padding-top: 200rpx;
}

.loading-text,
.empty-text {
  font-size: 28rpx;
  color: #999;
}

.markdown-body {
  font-size: 28rpx;
  line-height: 1.8;
  color: #333;
}

.markdown-body :deep(h1) {
  font-size: 40rpx;
  font-weight: 700;
  margin: 32rpx 0 16rpx;
  color: #111;
}

.markdown-body :deep(h2) {
  font-size: 34rpx;
  font-weight: 600;
  margin: 28rpx 0 12rpx;
  color: #222;
}

.markdown-body :deep(h3) {
  font-size: 30rpx;
  font-weight: 600;
  margin: 24rpx 0 8rpx;
  color: #333;
}

.markdown-body :deep(strong) {
  font-weight: 600;
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16rpx 0;
  font-size: 24rpx;
}

.markdown-body :deep(td) {
  padding: 12rpx 16rpx;
  border: 1rpx solid #e8e8e8;
}

.markdown-body :deep(ul) {
  padding-left: 32rpx;
  margin: 8rpx 0;
}

.markdown-body :deep(li) {
  margin: 4rpx 0;
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1rpx solid #eee;
  margin: 24rpx 0;
}
</style>
