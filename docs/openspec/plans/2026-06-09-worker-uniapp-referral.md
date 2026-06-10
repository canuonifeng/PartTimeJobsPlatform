# Worker-UniApp Referral Pages Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add referral reward pages to worker-uniapp, allowing workers to invite friends and track referral rewards.

**Architecture:** Create API module for referral endpoints, main referral page with stats/link/poster, and referral records page with referee list. Follow existing codebase patterns for API calls and Vue components.

**Tech Stack:** Vue 3 Composition API, TypeScript, UniApp, existing request utility

---

## Pattern Notes

The task description uses a different API signature than the existing codebase. This plan adapts to match actual patterns:

- **API import:** `import request from './request'` (default, not named)
- **API call format:** `request({ url: '...', method: 'GET', data: params })` (object config)
- **Lifecycle:** Use `onShow` from `@dcloudio/uni-app` for page show events (not `onMounted`)
- **TypeScript:** Add interfaces for type safety

---

### Task 1: Create referral API module

**Files:**
- Create: `worker-uniapp/src/api/referral.js`

- [ ] **Step 1: Create the API module**

```javascript
// worker-uniapp/src/api/referral.js
import request from './request'

export function getReferralLink() {
  return request({
    url: '/api/referral/link',
    method: 'GET'
  })
}

export function getReferralPoster() {
  return request({
    url: '/api/referral/poster',
    method: 'GET'
  })
}

export function getReferralStats() {
  return request({
    url: '/api/referral/stats',
    method: 'GET'
  })
}

export function getReferees(params) {
  return request({
    url: '/api/referral/referees',
    method: 'GET',
    data: params
  })
}

export function getReferralRewards(params) {
  return request({
    url: '/api/referral/rewards',
    method: 'GET',
    data: params
  })
}
```

- [ ] **Step 2: Commit**

```bash
git add worker-uniapp/src/api/referral.js
git commit -m "feat(worker-uniapp): add referral API module"
```

---

### Task 2: Create referral main page

**Files:**
- Create: `worker-uniapp/src/pages/referral/referral.vue`

- [ ] **Step 1: Create the referral page**

```vue
<template>
  <view class="referral-page">
    <view class="header">
      <text class="title">邀请好友</text>
      <text class="subtitle">邀请好友加入平台，获得奖励</text>
    </view>

    <view class="stats-card">
      <view class="stat-item">
        <text class="stat-value">{{ stats.totalReferees }}</text>
        <text class="stat-label">邀请人数</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.totalRewardAmount }}</text>
        <text class="stat-label">累计奖励(元)</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.pendingRewardAmount }}</text>
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
      <image v-if="posterUrl" :src="posterUrl" class="poster-image" mode="aspectFit" />
      <button class="share-btn" @click="sharePoster">分享海报</button>
    </view>

    <view class="section">
      <navigator url="/pages/referral/referralRecords" class="records-link">
        <text>查看邀请记录</text>
        <text class="arrow">></text>
      </navigator>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getReferralLink, getReferralPoster, getReferralStats } from '@/api/referral'

interface ReferralStats {
  totalReferees: number
  totalRewardAmount: number
  pendingRewardAmount: number
}

const referralLink = ref('')
const referralCode = ref('')
const posterUrl = ref('')
const stats = ref<ReferralStats>({
  totalReferees: 0,
  totalRewardAmount: 0,
  pendingRewardAmount: 0
})

async function fetchReferralInfo() {
  try {
    const linkRes: any = await getReferralLink()
    referralLink.value = linkRes.link || ''
    referralCode.value = linkRes.code || ''

    const posterRes: any = await getReferralPoster()
    posterUrl.value = posterRes.posterUrl || ''

    const statsRes: any = await getReferralStats()
    stats.value = {
      totalReferees: statsRes.totalReferees || 0,
      totalRewardAmount: statsRes.totalRewardAmount || 0,
      pendingRewardAmount: statsRes.pendingRewardAmount || 0
    }
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
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

function sharePoster() {
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}

onShow(() => {
  fetchReferralInfo()
})
</script>

<style scoped>
.referral-page {
  min-height: 100vh;
  padding: 30rpx;
  background: #f7f8fa;
  box-sizing: border-box;
}

.header {
  text-align: center;
  padding: 40rpx 0;
}

.title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
  display: block;
}

.subtitle {
  font-size: 26rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}

.stats-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 36rpx;
  display: flex;
  justify-content: space-around;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 40rpx;
  font-weight: 600;
  color: #409eff;
  display: block;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}

.section {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 16rpx;
  display: block;
}

.link-box {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.link-text {
  flex: 1;
  font-size: 24rpx;
  color: #666;
  background: #f5f5f5;
  padding: 16rpx;
  border-radius: 8rpx;
  word-break: break-all;
}

.copy-btn {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 16rpx 32rpx;
  font-size: 26rpx;
}

.poster-image {
  width: 100%;
  height: 400rpx;
  border-radius: 8rpx;
  margin-bottom: 16rpx;
}

.share-btn {
  background: #67c23a;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 24rpx;
  font-size: 28rpx;
}

.records-link {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
}

.arrow {
  color: #999;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add worker-uniapp/src/pages/referral/referral.vue
git commit -m "feat(worker-uniapp): add referral main page"
```

---

### Task 3: Create referral records page

**Files:**
- Create: `worker-uniapp/src/pages/referral/referralRecords.vue`

- [ ] **Step 1: Create the referral records page**

```vue
<template>
  <view class="records-page">
    <view class="header">
      <text class="title">邀请记录</text>
      <text class="total">共 {{ total }} 人</text>
    </view>

    <view v-if="referees.length === 0 && !loading" class="empty">
      <text>暂无邀请记录</text>
    </view>

    <view v-for="item in referees" :key="item.id" class="record-item">
      <view class="record-left">
        <text class="record-name">{{ item.name }}</text>
        <text class="record-phone">{{ item.phone }}</text>
        <text class="record-time">邀请时间: {{ item.boundAt }}</text>
      </view>
      <view class="record-right">
        <view class="work-info">
          <text class="work-count">打工 {{ item.workCount }} 次</text>
          <text class="work-hours">工时 {{ item.workHours }} h</text>
        </view>
        <text class="reward-status" :style="{ color: rewardStatusColor(item.rewardStatus) }">
          {{ rewardStatusText(item.rewardStatus) }}
        </text>
      </view>
    </view>

    <uni-load-more v-if="total > pageSize" :status="page * pageSize >= total ? 'noMore' : 'more'" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getReferees } from '@/api/referral'

interface Referee {
  id: number | string
  name: string
  phone: string
  boundAt: string
  workCount: number
  workHours: number
  rewardStatus: string
}

const referees = ref<Referee[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)

async function fetchReferees() {
  loading.value = true
  try {
    const res: any = await getReferees({ page: page.value, pageSize: pageSize.value })
    referees.value = res.records || []
    total.value = res.total || 0
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function rewardStatusText(status: string) {
  const map: Record<string, string> = {
    PENDING: '待审核',
    AUDITING: '审核中',
    GRANTED: '已发放',
    REJECTED: '已拒绝',
    NOT_QUALIFIED: '未达标'
  }
  return map[status] || status
}

function rewardStatusColor(status: string) {
  const map: Record<string, string> = {
    PENDING: '#e6a23c',
    AUDITING: '#909399',
    GRANTED: '#67c23a',
    REJECTED: '#f56c6c',
    NOT_QUALIFIED: '#c0c4cc'
  }
  return map[status] || '#909399'
}

onShow(() => {
  page.value = 1
  fetchReferees()
})
</script>

<style scoped>
.records-page {
  min-height: 100vh;
  padding: 30rpx;
  background: #f7f8fa;
  box-sizing: border-box;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
}

.title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.total {
  font-size: 26rpx;
  color: #999;
}

.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
}

.record-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 16rpx;
  display: flex;
  justify-content: space-between;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.record-left {
  flex: 1;
}

.record-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  display: block;
}

.record-phone {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
  display: block;
}

.record-time {
  font-size: 22rpx;
  color: #ccc;
  margin-top: 4rpx;
  display: block;
}

.record-right {
  text-align: right;
}

.work-info {
  margin-bottom: 8rpx;
}

.work-count {
  font-size: 24rpx;
  color: #666;
  display: block;
}

.work-hours {
  font-size: 24rpx;
  color: #666;
  display: block;
}

.reward-status {
  font-size: 26rpx;
  font-weight: 500;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add worker-uniapp/src/pages/referral/referralRecords.vue
git commit -m "feat(worker-uniapp): add referral records page"
```

---

### Task 4: Update pages.json

**Files:**
- Modify: `worker-uniapp/src/pages.json`

- [ ] **Step 1: Add referral pages to pages.json**

Add the following entries to the `pages` array (after the existing pages, before the closing bracket):

```json
{
  "path": "pages/referral/referral",
  "style": {
    "navigationBarTitleText": "邀请好友"
  }
},
{
  "path": "pages/referral/referralRecords",
  "style": {
    "navigationBarTitleText": "邀请记录"
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add worker-uniapp/src/pages.json
git commit -m "feat(worker-uniapp): register referral pages in pages.json"
```

---

### Task 5: Verify build

**Files:**
- None (verification only)

- [ ] **Step 1: Run H5 build**

```bash
cd worker-uniapp && npm run build:h5
```

Expected: Build succeeds without errors

- [ ] **Step 2: Run WeChat mini-program build**

```bash
cd worker-uniapp && npm run build:mp-weixin
```

Expected: Build succeeds without errors

---

### Task 6: Final commit (all files together)

- [ ] **Step 1: Create final combined commit**

```bash
git add worker-uniapp/src/api/referral.js worker-uniapp/src/pages/referral/referral.vue worker-uniapp/src/pages/referral/referralRecords.vue worker-uniapp/src/pages.json
git commit -m "feat(worker-uniapp): add referral reward system pages

- Add referral API module with link, poster, stats, referees, rewards endpoints
- Add referral main page with stats display, link copy, poster share
- Add referral records page with referee list and reward status
- Register new pages in pages.json"
```
