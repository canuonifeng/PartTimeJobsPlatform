<template>
  <view class="job-list-page">
    <view class="search-bar">
      <uni-search-bar :radius="100" placeholder="搜索职位、公司" @confirm="onSearch" @clear="onSearch('')" v-model="keyword" />
    </view>

    <scroll-view class="category-scroll" scroll-x show-scrollbar="false">
      <view class="category-chips">
        <view v-for="cat in categories" :key="cat.id" class="chip" :class="{ active: categoryId === cat.id }" @click="onCategoryChange(cat.id)">
          <text>{{ cat.name }}</text>
        </view>
      </view>
    </scroll-view>

    <uni-load-more v-if="loading && page === 1" status="loading" />

    <scroll-view class="job-scroll" scroll-y @scrolltolower="loadMore" :refresher-enabled="true" :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <view class="job-list-inner">
        <view v-if="jobList.length === 0 && !loading" class="empty-state">
        <text class="empty-text">暂无职位信息</text>
      </view>

      <view v-for="job in jobList" :key="job.id" class="job-card" @click="goDetail(job.id)">
        <view class="job-card-top">
          <image v-if="job.imageUrl" class="job-image" :src="job.imageUrl" mode="aspectFill" />
          <image v-else-if="job.companyLogo" class="job-image" :src="job.companyLogo" mode="aspectFill" />
          <view v-else class="job-image placeholder">
            <text>{{ (job.companyName || '?').slice(0, 1) }}</text>
          </view>
          <view class="job-main">
            <view class="job-card-header">
              <text class="job-title">{{ job.title }}</text>
              <text class="job-pay">{{ formatRates(job.rates, job.minRate, job.maxRate) }}</text>
            </view>
            <view class="job-tags">
              <text v-if="job.jobType" class="tag">{{ job.jobType }}</text>
              <text v-if="job.experience" class="tag">{{ job.experience }}</text>
            </view>
            <view class="job-card-footer">
              <text class="job-location">{{ job.location }}</text>
              <text class="job-distance">{{ job.distanceKm != null ? `${job.distanceKm}km` : '' }}</text>
            </view>
            <text class="job-company">{{ job.companyName }}</text>
          </view>
        </view>
      </view>

      <uni-load-more v-if="hasMore" :status="loadingMore ? 'loading' : 'more'" />
      <uni-load-more v-if="!hasMore && jobList.length > 0" status="noMore" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getJobs } from '@/api/jobs'

const keyword = ref('')
const categoryId = ref<number | undefined>(undefined)
const jobList = ref<any[]>([])
const page = ref(1)
const pageSize = 10
const hasMore = ref(true)
const loading = ref(false)
const loadingMore = ref(false)
const refreshing = ref(false)
const currentLocation = ref<{ latitude: number; longitude: number } | null>(null)

const categories = [
  { id: undefined, name: '全部' },
  { id: 1, name: '餐饮服务' },
  { id: 2, name: '物流配送' },
  { id: 3, name: '家政保洁' },
  { id: 4, name: '活动促销' },
  { id: 5, name: '教育培训' },
  { id: 6, name: '美容美发' },
  { id: 7, name: '其他' }
]

async function fetchJobs(p: number, append: boolean = false) {
  if (!append) loading.value = true
  else loadingMore.value = true
  try {
    const res: any = await getJobs({
      keyword: keyword.value || undefined,
      categoryId: categoryId.value,
      latitude: currentLocation.value?.latitude,
      longitude: currentLocation.value?.longitude
    })
    const list = Array.isArray(res) ? res : (res.list || [])
    if (append) {
      jobList.value.push(...list)
    } else {
      jobList.value = list
    }
    hasMore.value = list.length >= pageSize
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
    refreshing.value = false
  }
}

function loadCurrentLocation() {
  return new Promise<void>((resolve) => {
    uni.getLocation({
      type: 'wgs84',
      success(res) {
        currentLocation.value = { latitude: res.latitude, longitude: res.longitude }
        resolve()
      },
      fail() {
        currentLocation.value = null
        resolve()
      }
    })
  })
}

function onSearch(val?: string) {
  if (val !== undefined) keyword.value = val
  page.value = 1
  fetchJobs(1)
}

function onCategoryChange(id: number | undefined) {
  categoryId.value = id
  page.value = 1
  fetchJobs(1)
}

function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  page.value += 1
  fetchJobs(page.value, true)
}

function onRefresh() {
  refreshing.value = true
  page.value = 1
  fetchJobs(1)
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/jobs/jobDetail?id=${id}` })
}

function rateUnit(type) {
  const map = { HOURLY: '小时', DAILY: '日', PIECEWORK: '件', PIECE: '单', MONTHLY: '月' }
  return map[type] || '小时'
}

function formatRates(rates, fallbackMin, fallbackMax) {
  if (rates && rates.length > 0) {
    return rates.map(r => `${r.amount}元/${rateUnit(r.type)}`).join(' + ')
  }
  if (fallbackMin && fallbackMax && fallbackMin !== fallbackMax) {
    return `${fallbackMin}-${fallbackMax}元/小时`
  }
  return `${fallbackMin || fallbackMax || 0}元/小时`
}

onMounted(async () => {
  await loadCurrentLocation()
  fetchJobs(1)
})
</script>

<style scoped>
.job-list-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}
.search-bar {
  padding: 20rpx 24rpx;
  background: #fff;
  border-bottom: 2rpx solid #f0f0f0;
}
.category-scroll {
  white-space: nowrap;
  padding: 20rpx 24rpx;
  background: #fff;
}
.category-chips {
  display: inline-flex;
  gap: 20rpx;
}
.chip {
  display: inline-flex;
  padding: 16rpx 36rpx;
  border-radius: 44rpx;
  background: #f5f5f5;
  font-size: 30rpx;
  color: #666;
  transition: all 0.2s;
}
.chip.active {
  background: #07c160;
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4rpx 12rpx rgba(7, 193, 96, 0.35);
}
.job-scroll {
  flex: 1;
  padding: 0;
}
.job-list-inner {
  padding: 24rpx;
}
.empty-state {
  display: flex;
  justify-content: center;
  padding: 120rpx 0;
}
.empty-text {
  font-size: 32rpx;
  color: #999;
}
.job-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
  transition: all 0.2s;
}
.job-card:active {
  transform: scale(0.98);
}
.job-card-top {
  display: flex;
  gap: 20rpx;
}
.job-image {
  width: 200rpx;
  height: 160rpx;
  border-radius: 16rpx;
  background: #f5f5f5;
  flex-shrink: 0;
}
.job-image.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  color: #07c160;
  background: #ecfdf5;
}
.job-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.job-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}
.job-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #222;
  flex: 1;
  line-height: 1.3;
}
.job-pay {
  font-size: 30rpx;
  color: #f60;
  font-weight: 700;
  white-space: nowrap;
  margin-left: 16rpx;
  background: #fff7f0;
  padding: 6rpx 18rpx;
  border-radius: 12rpx;
  max-width: 40%;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-shrink: 0;
}
.job-tags {
  display: flex;
  gap: 12rpx;
  margin-bottom: 14rpx;
}
.tag {
  padding: 6rpx 18rpx;
  border-radius: 10rpx;
  background: #f0f0f0;
  font-size: 24rpx;
  color: #666;
}
.job-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6rpx;
}
.job-location {
  font-size: 26rpx;
  color: #888;
}
.job-company {
  display: block;
  font-size: 26rpx;
  color: #aaa;
}
.job-distance {
  font-size: 26rpx;
  color: #07c160;
  font-weight: 500;
}
</style>
