<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getApplications, acceptApplication, rejectApplication } from '@/api/jobs'

const applications = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20
const currentStatus = ref('')
const expandedGroups = ref({})
const statusTabs = [
  { label: '全部', value: '' },
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'ACCEPTED' },
  { label: '已拒绝', value: 'REJECTED' }
]

const groups = computed(() => {
  const map = {}
  applications.value.forEach(app => {
    const key = `${app.scheduleDate || '未知日期'}_${app.jobId || ''}`
    if (!map[key]) {
      map[key] = {
        key,
        date: app.scheduleDate || '未知日期',
        jobTitle: app.jobTitle || '-',
        timeRange: formatTimeRange(app.startTime, app.endTime),
        items: []
      }
    }
    map[key].items.push(app)
  })
  return Object.values(map).sort((a, b) => (b.date || '').localeCompare(a.date || ''))
})

function isGroupExpanded(key) {
  return expandedGroups.value[key] !== false
}

function toggleGroup(key) {
  expandedGroups.value = { ...expandedGroups.value, [key]: !isGroupExpanded(key) }
}

onLoad((options = {}) => {
  currentStatus.value = normalizeStatus(options.status)
  refreshApplications()
})

function normalizeStatus(status) {
  return statusTabs.some(tab => tab.value === status) ? status : ''
}

function refreshApplications() {
  page.value = 1
  applications.value = []
  hasMore.value = true
  return loadApplications()
}

async function loadApplications(append = false) {
  if (!append) loading.value = true
  else loadingMore.value = true
  try {
    const res = await getApplications(null, { status: currentStatus.value, page: page.value, pageSize })
    const list = Array.isArray(res) ? res : (res.records || res.data || [])
    applications.value = append ? applications.value.concat(list) : list
    hasMore.value = list.length >= pageSize
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function switchStatus(status) {
  if (currentStatus.value === status || loading.value) return
  currentStatus.value = status
  expandedGroups.value = {}
  refreshApplications()
}

function loadMore() {
  if (loading.value || loadingMore.value || !hasMore.value) return
  page.value += 1
  loadApplications(true)
}

function statusLabel(status) {
  const map = { PENDING: '待审核', ACCEPTED: '已通过', REJECTED: '已拒绝' }
  return map[status] || status || '-'
}

function statusClass(status) {
  const map = { PENDING: 'pending', ACCEPTED: 'accepted', REJECTED: 'rejected' }
  return map[status] || 'default'
}

function genderLabel(gender) {
  const map = { MALE: '男', FEMALE: '女', OTHER: '其他' }
  return map[gender] || gender || '未知'
}

function ageLabel(age) {
  return age == null ? '年龄未知' : `${age}岁`
}

function phoneLabel(phone) {
  return phone || '暂无手机号'
}

async function handleAccept(applicationId) {
  try {
    await acceptApplication(applicationId)
    uni.showToast({ title: '已通过', icon: 'success' })
    uni.showToast({ title: '下一步：考勤确认', icon: 'none', duration: 2000 })
    refreshApplications()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

async function handleReject(applicationId) {
  try {
    await rejectApplication(applicationId)
    uni.showToast({ title: '已拒绝', icon: 'success' })
    refreshApplications()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

function formatTimeRange(startTime, endTime) {
  if (!startTime || !endTime) return ''
  return `${startTime.slice(0, 5)}-${endTime.slice(0, 5)}`
}

function formatBeijingTime(value) {
  if (!value) return '暂无'
  const raw = String(value).replace('T', ' ')
  const hasTimezone = /Z$|[+-]\d{2}:?\d{2}$/.test(String(value))
  if (!hasTimezone) return raw.slice(0, 16)
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return raw.slice(0, 16)
  const beijing = new Date(date.getTime() + 8 * 60 * 60 * 1000)
  return `${beijing.getUTCFullYear()}-${pad2(beijing.getUTCMonth() + 1)}-${pad2(beijing.getUTCDate())} ${pad2(beijing.getUTCHours())}:${pad2(beijing.getUTCMinutes())}`
}

function scheduleDateLabel(value) {
  return value || '-'
}

function pad2(value) {
  return String(value).padStart(2, '0')
}
</script>

<template>
  <scroll-view scroll-y class="op-page applications-page" @scrolltolower="loadMore">
    <view class="top-space"></view>
    <view class="op-hero">
      <text class="op-hero-kicker">报名审核</text>
      <text class="op-hero-title">及时处理工人报名</text>
      <text class="op-hero-desc">{{ applications.length }} 条报名记录 · 优先处理待审核</text>
    </view>

    <view class="op-content">
      <scroll-view scroll-x class="filter-scroll" show-scrollbar="false">
        <view class="filter-row">
          <view v-for="tab in statusTabs" :key="tab.value || 'ALL'" class="filter-pill" :class="{ active: currentStatus === tab.value }" @click="switchStatus(tab.value)">{{ tab.label }}</view>
        </view>
      </scroll-view>

      <view v-if="loading" class="e-empty">
        <text class="e-empty-title">加载中...</text>
      </view>

      <view v-else-if="applications.length === 0" class="e-empty">
        <text class="e-empty-title">暂无报名记录</text>
        <text class="e-empty-desc">等待工人投递简历</text>
      </view>

      <view v-else class="application-list">
        <view v-for="group in groups" :key="group.key" class="group-section">
          <view class="group-header" @click="toggleGroup(group.key)">
            <view class="group-header-left">
              <text class="group-date">{{ group.date }}</text>
              <text class="group-title">{{ group.jobTitle }}</text>
              <text v-if="group.timeRange" class="group-time">{{ group.timeRange }}</text>
            </view>
            <view class="group-header-right">
              <text class="group-count">{{ group.items.length }}人</text>
              <text class="group-arrow" :class="{ expanded: isGroupExpanded(group.key) }">›</text>
            </view>
          </view>

          <template v-if="isGroupExpanded(group.key)">
            <view v-for="app in group.items" :key="app.applicationId" class="op-card operation-card application-card">
              <view class="op-row">
                <view class="worker-avatar">{{ (app.workerName || '工').slice(0, 1) }}</view>
                <view class="op-row-main">
                  <text class="op-row-title">{{ app.workerName || '未知姓名' }}</text>
                  <text class="op-row-desc">{{ genderLabel(app.workerGender) }} · {{ ageLabel(app.workerAge) }} · {{ phoneLabel(app.workerPhone) }}</text>
                </view>
                <text class="op-pill" :class="statusClass(app.status) === 'rejected' ? 'op-pill-danger' : statusClass(app.status) === 'pending' ? 'op-pill-warn' : ''">{{ statusLabel(app.status) }}</text>
              </view>

              <view class="info-grid">
                <view class="info-item wide">
                  <text class="info-label">报名岗位</text>
                  <text class="info-value">{{ app.jobTitle || '-' }}</text>
                </view>
                <view class="info-item">
                  <text class="info-label">排班时间</text>
                  <text class="info-value multi">{{ scheduleDateLabel(app.scheduleDate) }}</text>
                  <text class="info-value sub">{{ formatTimeRange(app.startTime, app.endTime) || '-' }}</text>
                </view>
                <view class="info-item">
                  <text class="info-label">申请时间</text>
                  <text class="info-value time">{{ formatBeijingTime(app.appliedAt) }}</text>
                </view>
              </view>

              <view v-if="app.status === 'PENDING'" class="action-row">
                <view class="action-btn reject" @click="handleReject(app.applicationId)">拒绝</view>
                <view class="action-btn accept" @click="handleAccept(app.applicationId)">通过报名</view>
              </view>
            </view>
          </template>
        </view>

        <view class="load-more-wrap">
          <uni-load-more v-if="loadingMore" status="loading" />
          <uni-load-more v-else-if="hasMore" status="more" />
          <uni-load-more v-else status="noMore" />
        </view>
      </view>
    </view>
  </scroll-view>
</template>

<style>
.applications-page { height: 100vh; }
.top-space { height: 24rpx; }
.filter-scroll { margin-bottom: 20rpx; white-space: nowrap; }
.filter-row { display: inline-flex; gap: 14rpx; padding-right: 8rpx; }
.filter-pill { display: inline-flex; align-items: center; justify-content: center; height: 62rpx; padding: 0 28rpx; border-radius: 999rpx; background: #fff; color: #64748b; font-size: 25rpx; font-weight: 800; box-shadow: 0 10rpx 24rpx rgba(23,83,53,.06); }
.filter-pill.active { background: #16a34a; color: #fff; box-shadow: 0 12rpx 28rpx rgba(22,163,74,.2); }
.application-list { display: flex; flex-direction: column; gap: 20rpx; }
.operation-card { position: relative; overflow: hidden; }
.operation-card::before { content: ''; position: absolute; left: 0; top: 28rpx; bottom: 28rpx; width: 8rpx; border-radius: 0 999rpx 999rpx 0; background: linear-gradient(180deg, #18c86b, #047857); }
.application-card { overflow: hidden; }
.worker-avatar { width: 76rpx; height: 76rpx; margin-right: 18rpx; border-radius: 26rpx; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #18c86b, #047857); color: #fff; font-size: 30rpx; font-weight: 850; flex-shrink: 0; }
.info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14rpx; margin-top: 22rpx; }
.info-item { min-width: 0; padding: 16rpx; border-radius: 18rpx; background: #f8fafc; }
.info-item.wide { grid-column: span 2; }
.info-label { display: block; font-size: 22rpx; color: #64748b; }
.info-value { display: block; margin-top: 8rpx; font-size: 26rpx; font-weight: 750; color: #1f2933; white-space: normal; overflow: visible; text-overflow: clip; line-height: 1.35; word-break: break-all; }
.info-value.multi { white-space: normal; }
.info-value.sub { margin-top: 6rpx; color: #64748b; font-size: 24rpx; }
.info-value.time { white-space: normal; line-height: 1.35; }
.action-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14rpx; margin-top: 22rpx; }
.action-btn { height: 72rpx; line-height: 72rpx; border-radius: 18rpx; text-align: center; font-size: 26rpx; font-weight: 850; }
.action-btn.reject { background: #fee2e2; color: #dc2626; }
.action-btn.accept { background: #16a34a; color: #fff; }
.load-more-wrap { padding: 16rpx 0 32rpx; }
.group-section { margin-bottom: 20rpx; }
.group-header { display: flex; align-items: center; justify-content: space-between; padding: 22rpx 24rpx; background: #fff; border-radius: 20rpx; box-shadow: 0 8rpx 20rpx rgba(23,83,53,.06); margin-bottom: 14rpx; }
.group-header-left { flex: 1; min-width: 0; display: flex; align-items: center; gap: 12rpx; }
.group-date { font-size: 24rpx; font-weight: 800; color: #16a34a; background: #ecfdf5; padding: 4rpx 14rpx; border-radius: 999rpx; }
.group-title { font-size: 26rpx; font-weight: 800; color: #1f2933; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.group-time { font-size: 22rpx; color: #64748b; flex-shrink: 0; }
.group-header-right { display: flex; align-items: center; gap: 12rpx; flex-shrink: 0; margin-left: 12rpx; }
.group-count { font-size: 22rpx; color: #64748b; font-weight: 700; }
.group-arrow { font-size: 36rpx; color: #98a3b3; line-height: 1; transition: transform .2s; }
.group-arrow.expanded { transform: rotate(90deg); }
</style>
