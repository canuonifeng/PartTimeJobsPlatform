<template>
  <view class="job-list-page">
    <view class="search-section">
      <view class="search-box">
        <text class="search-icon">搜</text>
        <input class="search-input" v-model="keyword" placeholder="搜索职位、公司" confirm-type="search" @confirm="onSearch" />
        <text v-if="keyword" class="search-clear" @click="onSearch('')">×</text>
      </view>
    </view>

    <scroll-view class="category-scroll" scroll-x :show-scrollbar="false">
      <view class="category-tabs">
        <view v-for="cat in categories" :key="cat.name" class="category-tab" :class="{ active: categoryId === cat.id }" @click="onCategoryChange(cat.id)">
          <text>{{ cat.name }}</text>
        </view>
      </view>
    </scroll-view>

    <uni-load-more v-if="loading && page === 1" status="loading" />

    <scroll-view class="job-scroll" scroll-y @scrolltolower="loadMore" :refresher-enabled="true" :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <view class="job-list-inner">
        <view v-for="job in jobList" :key="job.id" class="job-card" @click="goDetail(job.id)">
          <view class="job-media">
            <image v-if="job.imageUrl" class="job-image" :src="job.imageUrl" mode="aspectFill" />
            <image v-else-if="job.companyLogo" class="job-image" :src="job.companyLogo" mode="aspectFill" />
            <view v-else class="job-image placeholder">
              <text>{{ job.iconText || (job.companyName || '?').slice(0, 1) }}</text>
            </view>
          </view>
          <view class="job-content">
            <view class="job-card-header">
              <text class="job-title">{{ job.title }}</text>
              <text class="job-pay">{{ formatRates(job.rates, job.minRate, job.maxRate) }}</text>
            </view>
            <view class="job-meta">
              <text class="job-location">{{ job.location || '附近' }}</text>
              <text v-if="job.distanceKm != null" class="job-distance">{{ job.distanceKm }}km</text>
            </view>
            <view class="job-tags">
              <text v-for="tag in getSettlementTags(job)" :key="tag" class="tag">{{ tag }}</text>
            </view>
            <text class="job-company">{{ job.companyName || '优选企业' }}</text>
          </view>
        </view>

        <view v-if="jobList.length === 0 && !loading" class="empty-state">
          <text class="empty-text">暂无职位信息</text>
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

type SearchEvent = { detail?: { value?: string }, value?: string }
type JobRate = { amount: number | string, type: string }
type JobItem = {
  id: number
  categoryId?: number
  title: string
  rates?: JobRate[]
  minRate?: number
  maxRate?: number
  location?: string
  distanceKm?: number
  companyName?: string
  imageUrl?: string
  companyLogo?: string
  iconText?: string
  settlement?: string[]
  jobType?: string
  experience?: string
}

const keyword = ref('')
const categoryId = ref<number | undefined>(undefined)
const jobList = ref<JobItem[]>([])
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
  { id: 5, name: '仓库分拣' },
  { id: 6, name: '零售导购' }
]

const fallbackJobs = [
  { id: -1, categoryId: 2, title: '外卖配送员', minRate: 220, maxRate: 320, location: '望京商圈', distanceKm: 1.2, companyName: '蜂鸟配送站', iconText: '配', settlement: ['日结', '周结'] },
  { id: -2, categoryId: 5, title: '仓库分拣员', minRate: 180, maxRate: 260, location: '顺义物流园', distanceKm: 3.8, companyName: '京北仓储中心', iconText: '仓', settlement: ['日结'] },
  { id: -3, categoryId: 3, title: '家庭保洁员', minRate: 45, maxRate: 65, location: '朝阳区', distanceKm: 2.4, companyName: '安心到家家政', iconText: '洁', settlement: ['日结'] },
  { id: -4, categoryId: 3, title: '家电清洗师', minRate: 80, maxRate: 120, location: '海淀区', distanceKm: 4.1, companyName: '净享生活服务', iconText: '洗', settlement: ['周结'] },
  { id: -5, categoryId: 1, title: '餐厅服务员', minRate: 22, maxRate: 28, location: '三里屯', distanceKm: 1.8, companyName: '悦味餐饮', iconText: '餐', settlement: ['日结'] },
  { id: -6, categoryId: 4, title: '展会协助员', minRate: 180, maxRate: 240, location: '国展中心', distanceKm: 5.3, companyName: '星程会展', iconText: '展', settlement: ['日结'] },
  { id: -7, categoryId: 6, title: '超市理货员', minRate: 24, maxRate: 30, location: '大悦城', distanceKm: 2.9, companyName: '惠民生活超市', iconText: '货', settlement: ['周结'] },
  { id: -8, categoryId: 6, title: '促销导购员', minRate: 180, maxRate: 260, location: '合生汇', distanceKm: 3.2, companyName: '优选零售', iconText: '促', settlement: ['日结', '周结'] }
]

function getFallbackJobs() {
  let list = fallbackJobs
  if (categoryId.value !== undefined) {
    list = list.filter(job => job.categoryId === categoryId.value)
  }
  if (keyword.value) {
    list = list.filter(job => job.title.includes(keyword.value) || job.companyName.includes(keyword.value))
  }
  return list
}

function shouldShowFallback() {
  return !keyword.value && categoryId.value === undefined
}

function applyJobs(list: JobItem[], append: boolean) {
  const finalList = list.length > 0 ? list : (shouldShowFallback() ? getFallbackJobs() : [])
  if (append) {
    if (list.length > 0) jobList.value.push(...list)
  } else {
    jobList.value = finalList
  }
  hasMore.value = list.length >= pageSize
}

async function fetchJobs(p: number, append: boolean = false) {
  if (!append) loading.value = true
  else loadingMore.value = true
  try {
    const res: { list?: JobItem[] } | JobItem[] | null | undefined = await getJobs({
      keyword: keyword.value || undefined,
      categoryId: categoryId.value,
      page: p,
      pageSize,
      latitude: currentLocation.value?.latitude,
      longitude: currentLocation.value?.longitude
    })
    const list = Array.isArray(res) ? res : (res?.list || [])
    applyJobs(list, append)
  } catch {
    if (!append) jobList.value = getFallbackJobs()
    hasMore.value = false
    uni.showToast({ title: '加载失败，已展示推荐岗位', icon: 'none' })
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

function normalizeSearchValue(val?: string | SearchEvent): string {
  if (typeof val === 'string') return val
  return val?.detail?.value ?? val?.value ?? keyword.value
}

function onSearch(val?: string | SearchEvent) {
  keyword.value = normalizeSearchValue(val)
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
  if (id < 0) {
    uni.showToast({ title: '推荐岗位暂无详情', icon: 'none' })
    return
  }
  uni.navigateTo({ url: `/pages/jobs/jobDetail?id=${id}` })
}

function getSettlementTags(job: JobItem): string[] {
  if (Array.isArray(job.settlement) && job.settlement.length > 0) return job.settlement
  const tags: string[] = []
  if (job.jobType) tags.push(job.jobType)
  if (job.experience) tags.push(job.experience)
  return tags.length > 0 ? tags : ['日结']
}

function rateUnit(type: string): string {
  const map: Record<string, string> = { HOURLY: '小时', DAILY: '日', PIECEWORK: '件', PIECE: '单', MONTHLY: '月' }
  return map[type] || '小时'
}

function formatRates(rates?: JobRate[], fallbackMin?: number, fallbackMax?: number): string {
  if (rates && rates.length > 0) {
    return rates.map((r: JobRate) => `${r.amount}元/${rateUnit(r.type)}`).join(' + ')
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
  background: #f6f7fb;
}
.search-section {
  padding: 24rpx 28rpx 18rpx;
  background: linear-gradient(180deg, #ecfdf5 0%, #ffffff 100%);
}
.search-box {
  display: flex;
  align-items: center;
  height: 76rpx;
  padding: 0 24rpx;
  border-radius: 38rpx;
  background: #f3f4f6;
}
.search-icon {
  width: 42rpx;
  font-size: 24rpx;
  color: #9ca3af;
}
.search-input {
  flex: 1;
  height: 76rpx;
  font-size: 28rpx;
  color: #222;
}
.search-clear {
  width: 48rpx;
  text-align: center;
  font-size: 36rpx;
  color: #9ca3af;
}
.category-scroll {
  white-space: nowrap;
  background: #fff;
  border-bottom: 2rpx solid #f1f2f4;
}
.category-tabs {
  display: inline-flex;
  padding: 0 28rpx;
}
.category-tab {
  display: inline-flex;
  position: relative;
  align-items: center;
  justify-content: center;
  height: 86rpx;
  margin-right: 42rpx;
  font-size: 28rpx;
  color: #6b7280;
}
.category-tab.active {
  color: #111827;
  font-weight: 700;
}
.category-tab.active::after {
  position: absolute;
  left: 50%;
  bottom: 12rpx;
  width: 34rpx;
  height: 8rpx;
  border-radius: 8rpx;
  background: #07c160;
  transform: translateX(-50%);
  content: '';
}
.job-scroll {
  flex: 1;
  padding: 0;
}
.job-list-inner {
  padding: 24rpx 28rpx 32rpx;
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
  display: flex;
  padding: 24rpx;
  margin-bottom: 22rpx;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 10rpx 30rpx rgba(17, 24, 39, 0.06);
}
.job-card:active {
  transform: scale(0.99);
}
.job-media {
  flex-shrink: 0;
  margin-right: 22rpx;
}
.job-image {
  width: 132rpx;
  height: 132rpx;
  border-radius: 20rpx;
  background: #f5f5f5;
}
.job-image.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 42rpx;
  font-weight: 700;
  color: #07c160;
  background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
}
.job-content {
  flex: 1;
  min-width: 0;
}
.job-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14rpx;
}
.job-title {
  flex: 1;
  min-width: 0;
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
  line-height: 42rpx;
}
.job-pay {
  flex-shrink: 0;
  max-width: 260rpx;
  margin-left: 16rpx;
  font-size: 30rpx;
  font-weight: 800;
  color: #ff6a00;
  line-height: 40rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.job-meta {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}
.job-location {
  max-width: 240rpx;
  font-size: 25rpx;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.job-distance {
  margin-left: 14rpx;
  font-size: 25rpx;
  color: #9ca3af;
}
.job-tags {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 16rpx;
}
.tag {
  padding: 6rpx 14rpx;
  margin-right: 12rpx;
  margin-bottom: 8rpx;
  border-radius: 8rpx;
  background: #ecfdf5;
  font-size: 22rpx;
  color: #07c160;
}
.job-company {
  display: block;
  font-size: 25rpx;
  color: #9ca3af;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
