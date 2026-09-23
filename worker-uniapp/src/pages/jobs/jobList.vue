<template>
  <view class="job-list-page">
    <view class="jobs-header">
      <view class="brand-row">
        <text class="brand-name">老登e站</text>
        <text class="brand-tagline">日结灵活 · 安全可靠</text>
      </view>
      <view class="city-search-row">
        <view class="city-box" @click="goProfile">
          <view class="city-icon"></view>
          <text class="city-name">{{ cityName || locationText }}</text>
        </view>
        <view class="search-box">
          <text class="search-icon">🔍</text>
          <input
            class="search-input"
            v-model="keyword"
            placeholder="搜职位、公司或地点"
            confirm-type="search"
            @confirm="onSearch"
          />
          <text v-if="keyword" class="search-clear" @click.stop="clearSearch">×</text>
        </view>
      </view>
    </view>

    <scroll-view class="quick-scroll" scroll-x :show-scrollbar="false">
      <view class="quick-chips">
        <view
          v-for="q in quickFilters"
          :key="q.key"
          class="quick-chip"
          :class="{ active: quickFilter === q.key }"
          @click="onQuickFilterChange(q.key)"
        >
          <text class="chip-text">{{ q.label }}</text>
        </view>
      </view>
    </scroll-view>

    <view class="category-toggle" @click="categoryExpanded = !categoryExpanded">
      <text class="category-toggle-text">全部分类</text>
      <text class="category-toggle-arrow">{{ categoryExpanded ? '▲' : '▼' }}</text>
    </view>
    <scroll-view v-if="categoryExpanded" class="category-scroll" scroll-x :show-scrollbar="false">
      <view class="category-chips">
        <view
          v-for="cat in categories"
          :key="cat.key"
          class="category-chip"
          :class="{ active: categoryId === cat.id }"
          @click="onCategoryChange(cat.id)"
        >
          <text class="chip-text">{{ cat.name }}</text>
        </view>
      </view>
    </scroll-view>

    <uni-load-more v-if="loading && page === 1" status="loading" />

    <scroll-view
      class="job-scroll"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="job-list-inner">
        <view v-for="job in jobList" :key="job.id" class="job-card" :class="{ 'annotation-card': job.taskType === 'ANNOTATION' }" @click="goDetail(job.id)">
          <view v-if="job.taskType === 'ANNOTATION'" class="annotation-tag-row">
            <text class="annotation-tag">标注任务</text>
          </view>
          <view class="job-title-row">
            <text class="job-title">{{ job.title }}</text>
            <view class="job-pay-block">
              <text class="job-pay">{{ job.taskType === 'ANNOTATION' ? formatAnnotationPrice(job) : formatRates(job.rates, job.minRate, job.maxRate) }}</text>
              <text v-if="hotTagText(job)" class="job-hot-tag">{{ hotTagText(job) }}</text>
            </view>
          </view>
          <view class="job-meta">
            <text v-if="job.taskType === 'ANNOTATION'" class="job-address">共{{ job.totalItems || 0 }}条标注</text>
            <text v-else class="job-address">{{ job.location || '附近' }}</text>
            <text v-if="job.taskType !== 'ANNOTATION' && job.distanceKm != null && job.distanceKm > 0" class="job-distance">{{ job.distanceKm }}km</text>
            <text v-else-if="job.taskType !== 'ANNOTATION' && job.distanceKm != null && job.distanceKm === 0" class="job-distance near">附近</text>
          </view>
          <view class="job-tags">
            <text v-for="(tag, idx) in getDisplayTags(job)" :key="idx" class="job-tag" :class="tagClass(tag)">{{ tag }}</text>
          </view>
          <view class="job-card-footer">
            <view class="job-company">
              <view class="company-logo">
                <text>企</text>
              </view>
              <text class="company-name">{{ job.companyName || '优选企业' }}</text>
            </view>
            <view class="job-apply-btn" @click.stop="goApply(job.id)">{{ job.taskType === 'ANNOTATION' ? '抢单' : '去报名' }}</view>
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
  <LoginSheet />
  <InviteFloat />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onMounted } from 'vue'
import { getCategories, getJobs } from '@/api/jobs'
import InviteFloat from '@/components/InviteFloat.vue'
import LoginSheet from '@/components/LoginSheet.vue'

type SearchEvent = { detail?: { value?: string }, value?: string }
type JobCategory = { id?: number, name: string, key?: string, children?: JobCategory[] }
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
  tags?: ({ id: number, name?: string | null, groupName?: string } | null)[]
  jobType?: string
  experience?: string
  applyCount?: number
  urgent?: boolean
  newlyPosted?: boolean
  taskType?: string
  totalItems?: number
  unitPrice?: number
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
const locationLoaded = ref(false)
const cityName = ref('')
const quickFilter = ref<'all' | 'work' | 'annotation' | 'nearby' | 'urgent'>('all')
const categoryExpanded = ref(false)

const allCategory = { id: undefined, name: '全部', key: 'all' }
const categories = ref<JobCategory[]>([allCategory])

const quickFilters = [
  { key: 'all', label: '全部' },
  { key: 'work', label: '零工' },
  { key: 'annotation', label: '标注' },
  { key: 'nearby', label: '附近' },
  { key: 'urgent', label: '急招' }
]



const locationText = computed(() => {
  if (locationLoaded.value && currentLocation.value) return '定位已获取'
  if (locationLoaded.value && !currentLocation.value) return '定位失败'
  return '定位中...'
})

function flattenCategories(list: JobCategory[], parentName?: string): JobCategory[] {
  return list.reduce((result: JobCategory[], item) => {
    const name = parentName ? `${parentName} / ${item.name}` : item.name
    result.push({ id: item.id, name, key: String(item.id ?? name) })
    if (Array.isArray(item.children) && item.children.length > 0) {
      result.push(...flattenCategories(item.children, name))
    }
    return result
  }, [])
}

async function loadCategories() {
  try {
    const res = await getCategories()
    const list = Array.isArray(res) ? res : []
    categories.value = [allCategory, ...flattenCategories(list)]
  } catch {
    categories.value = [allCategory]
  }
}

function applyJobs(list: JobItem[], append: boolean) {
  if (append) {
    if (list.length > 0) jobList.value.push(...list)
  } else {
    jobList.value = list
  }
  hasMore.value = list.length >= pageSize
}

async function fetchJobs(p: number, append: boolean = false) {
  if (!append) loading.value = true
  else loadingMore.value = true
  try {
    const params: Record<string, any> = {
      keyword: keyword.value || undefined,
      categoryId: categoryId.value,
      page: p,
      pageSize,
      latitude: currentLocation.value?.latitude,
      longitude: currentLocation.value?.longitude
    }
    if (quickFilter.value === 'work') {
      params.taskType = 'WORK'
    } else if (quickFilter.value === 'annotation') {
      params.taskType = 'ANNOTATION'
    } else if (quickFilter.value === 'urgent') {
      params.urgent = true
    } else if (quickFilter.value === 'nearby' && currentLocation.value) {
      params.sort = 'distance'
    }
    const res: { list?: JobItem[] } | JobItem[] | null | undefined = await getJobs(params)
    const list = Array.isArray(res) ? res : (res?.records || res?.list || [])
    applyJobs(list, append)
  } catch {
    if (!append) jobList.value = []
    hasMore.value = false
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
    refreshing.value = false
  }
}

function loadCurrentLocation() {
  locationLoaded.value = false
  return new Promise<void>((resolve) => {
    uni.getLocation({
      type: 'wgs84',
      success(res) {
        currentLocation.value = { latitude: res.latitude, longitude: res.longitude }
        locationLoaded.value = true
        reverseGeocode(res.latitude, res.longitude)
        resolve()
      },
      fail() {
        currentLocation.value = null
        locationLoaded.value = true
        resolve()
      }
    })
  })
}

function reverseGeocode(lat: number, lng: number) {
  uni.request({
    url: `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&accept-language=zh`,
    success(res: any) {
      const data = res?.data || res
      if (data?.address?.city) {
        cityName.value = data.address.city
      } else if (data?.address?.county) {
        cityName.value = data.address.county
      } else if (data?.address?.state) {
        cityName.value = data.address.state
      } else if (data?.name) {
        cityName.value = data.name
      }
    },
    fail() {
      // 静默失败
    }
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

function clearSearch() {
  keyword.value = ''
  page.value = 1
  fetchJobs(1)
}

function onCategoryChange(id: number | undefined) {
  categoryId.value = id
  quickFilter.value = 'all'
  page.value = 1
  fetchJobs(1)
}

function onQuickFilterChange(key: 'all' | 'work' | 'annotation' | 'nearby' | 'urgent') {
  quickFilter.value = key
  categoryId.value = undefined
  page.value = 1
  if (key === 'nearby' && !currentLocation.value) {
    uni.showToast({ title: '未获取到定位，已按默认排序', icon: 'none' })
  }
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
  const job = jobList.value.find(j => j.id === id)
  if (job?.taskType === 'ANNOTATION') {
    uni.navigateTo({ url: `/pages/jobs/jobDetailAnnotation?id=${id}` })
  } else {
    uni.navigateTo({ url: `/pages/jobs/jobDetail?id=${id}` })
  }
}

function goApply(id: number) {
  const job = jobList.value.find(j => j.id === id)
  if (job?.taskType === 'ANNOTATION') {
    uni.navigateTo({ url: `/pages/jobs/jobDetailAnnotation?id=${id}` })
  } else {
    uni.navigateTo({ url: `/pages/jobs/jobDetail?id=${id}` })
  }
}

function goProfile() {
  uni.switchTab({ url: '/pages/profile/profile' })
}

function getSettlementTags(job: JobItem): string[] {
  if (Array.isArray(job.tags) && job.tags.length > 0) {
    const tagNames = job.tags.map((tag) => tag?.name?.trim()).filter(Boolean) as string[]
    if (tagNames.length > 0) return tagNames
  }
  if (Array.isArray(job.settlement) && job.settlement.length > 0) return job.settlement
  const tags: string[] = []
  if (job.jobType) tags.push(job.jobType)
  if (job.experience) tags.push(job.experience)
  return tags.length > 0 ? tags : ['日结']
}

function getDisplayTags(job: JobItem): string[] {
  return getSettlementTags(job).slice(0, 4)
}

function hotTagText(job: JobItem): string {
  if (job.urgent) return '🔥 急招'
  if (job.newlyPosted) return '✨ 新上'
  return ''
}

function tagClass(tag: string): string {
  const greenTags = ['日结', '包吃', '包住', '包三餐', '环境好', '免费培训', '轻松']
  const orangeTags = ['周结', '月结', '周结工资', '月结工资']
  const blueTags = ['无需经验', '时间自由', '灵活排班', '电动车提供']
  const redTags = ['名额紧张', '快满了']
  if (greenTags.some(t => tag.includes(t))) return 'tag-green'
  if (orangeTags.some(t => tag.includes(t))) return 'tag-orange'
  if (blueTags.some(t => tag.includes(t))) return 'tag-blue'
  if (redTags.some(t => tag.includes(t))) return 'tag-red'
  return 'tag-green'
}

function rateUnit(type: string): string {
  const map: Record<string, string> = { HOURLY: '小时', DAILY: '日', PER_SHIFT: '单', MONTHLY: '月' }
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

function formatAnnotationPrice(job: JobItem): string {
  if (job.unitPrice) return `${job.unitPrice}元/条`
  if (job.rates && job.rates.length > 0) {
    return `${job.rates[0].amount}元/条`
  }
  return '价格待定'
}

async function refreshJobs() {
  await loadCategories()
  await loadCurrentLocation()
  page.value = 1
  fetchJobs(1)
}

onMounted(() => {
  if (jobList.value.length === 0) {
    refreshJobs()
  }
})
</script>

<style scoped>
.job-list-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: #f5f6fa;
}

.jobs-header {
  position: relative;
  background: linear-gradient(135deg, #20c26b 0%, #1aab5a 55%, #169950 100%);
  padding: 60rpx 32rpx 32rpx;
  color: #fff;
  overflow: hidden;
}

.jobs-header::before {
  content: '';
  position: absolute;
  top: -100rpx;
  right: -80rpx;
  width: 300rpx;
  height: 300rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.jobs-header::after {
  content: '';
  position: absolute;
  bottom: -60rpx;
  left: -40rpx;
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
}



.brand-row {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
  margin-bottom: 16rpx;
}
.brand-name {
  font-size: 44rpx;
  font-weight: 800;
  color: #fff;
  letter-spacing: 2rpx;
}
.brand-tagline {
  font-size: 24rpx;
  color: rgba(255,255,255,0.75);
}
.city-search-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.city-box {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
.city-icon {
  width: 32rpx;
  height: 32rpx;
  background: #fff;
  border-radius: 50% 50% 50% 10%;
  transform: rotate(-45deg);
  position: relative;
  margin-right: 10rpx;
}
.city-icon::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 10rpx;
  height: 10rpx;
  background: #07a857;
  border-radius: 50%;
}
.city-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #fff;
  max-width: 140rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.search-box {
  display: flex;
  align-items: center;
  flex: 1;
  height: 80rpx;
  padding: 0 24rpx;
  border-radius: 40rpx;
  background: #fff;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.search-icon {
  font-size: 30rpx;
  margin-right: 14rpx;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  height: 88rpx;
  font-size: 28rpx;
  color: #333;
}

.search-input::placeholder {
  color: #bbb;
}

.search-clear {
  width: 48rpx;
  height: 48rpx;
  line-height: 44rpx;
  text-align: center;
  font-size: 40rpx;
  color: #999;
  flex-shrink: 0;
}

.category-scroll {
  background: #f5f6fa;
  white-space: nowrap;
  padding: 0 0 16rpx;
}

.quick-scroll {
  background: #f5f6fa;
  white-space: nowrap;
  padding: 24rpx 0 16rpx;
}

.quick-chips {
  display: inline-flex;
  padding: 0 24rpx;
  gap: 16rpx;
}

.quick-chip {
  flex-shrink: 0;
  padding: 14rpx 40rpx;
  border-radius: 36rpx;
  background: #fff;
  border: 1rpx solid #eee;
  transition: all 0.2s ease;
}

.quick-chip.active {
  background: linear-gradient(135deg, #20c26b 0%, #1aab5a 100%);
  border-color: transparent;
  box-shadow: 0 6rpx 18rpx rgba(16, 185, 129, 0.35);
}

.quick-chip.active .chip-text {
  color: #fff;
  font-weight: 600;
}

.category-toggle {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 4rpx 24rpx 12rpx;
}

.category-toggle-text {
  font-size: 24rpx;
  color: #999;
}

.category-toggle-arrow {
  font-size: 20rpx;
  color: #999;
}

.category-chips {
  display: inline-flex;
  padding: 0 24rpx;
}

.category-chip {
  flex-shrink: 0;
  padding: 14rpx 32rpx;
  margin-right: 16rpx;
  border-radius: 36rpx;
  background: #fff;
  border: 1rpx solid #eee;
  transition: all 0.2s ease;
}

.category-chip.active {
  background: linear-gradient(135deg, #20c26b 0%, #1aab5a 100%);
  border-color: transparent;
  box-shadow: 0 6rpx 18rpx rgba(16, 185, 129, 0.35);
}

.chip-text {
  font-size: 26rpx;
  color: #666;
}

.category-chip.active .chip-text {
  color: #fff;
  font-weight: 600;
}

.job-scroll {
  flex: 1;
  min-height: 0;
  padding: 0;
}

.job-list-inner {
  padding: 8rpx 24rpx 40rpx;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 200rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.job-card {
  position: relative;
  padding: 28rpx;
  padding-left: 32rpx;
  margin-bottom: 20rpx;
  border-radius: 18rpx;
  background: #fff;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.05), 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.job-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  width: 6rpx;
  background: linear-gradient(180deg, #20c26b 0%, #1aab5a 100%);
  border-radius: 0 4rpx 4rpx 0;
}

.job-card:active {
  transform: translateY(2rpx) scale(0.99);
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.job-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.job-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.3;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.job-pay-block {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-shrink: 0;
}

.job-pay {
  font-size: 38rpx;
  font-weight: 800;
  color: #ff6b35;
  line-height: 1.2;
  letter-spacing: -1rpx;
}

.job-hot-tag {
  flex-shrink: 0;
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #ef4444 0%, #f97316 100%);
}

.job-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
  margin-bottom: 14rpx;
}

.job-address {
  font-size: 24rpx;
  color: #888;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.job-distance {
  font-size: 24rpx;
  font-weight: 700;
  color: #20c26b;
  flex-shrink: 0;
  background: #ecfdf3;
  padding: 4rpx 14rpx;
  border-radius: 14rpx;
}

.job-distance.near {
  color: #888;
  background: #f5f5f5;
  font-weight: 500;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-bottom: 20rpx;
}

.job-tag {
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 500;
  flex-shrink: 0;
}

.job-tag.tag-green {
  background: #e6f8ee;
  color: #ff6b35;
}

.job-tag.tag-orange {
  background: #fff3e8;
  color: #f59e0b;
}

.job-tag.tag-blue {
  background: #eff6ff;
  color: #3b82f6;
}

.job-tag.tag-red {
  background: #fee2e2;
  color: #ef4444;
}

.job-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16rpx;
  border-top: 1rpx dashed #f0f0f0;
}

.job-company {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.company-logo {
  width: 40rpx;
  height: 40rpx;
  border-radius: 8rpx;
  background: linear-gradient(135deg, #20c26b 0%, #1aab5a 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  font-weight: 700;
  flex-shrink: 0;
}

.company-name {
  font-size: 22rpx;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 260rpx;
}

.job-apply-btn {
  flex-shrink: 0;
  height: 56rpx;
  line-height: 56rpx;
  padding: 0 24rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #2ad879 0%, #20c26b 40%, #1aab5a 100%);
  color: #fff;
  font-size: 24rpx;
  font-weight: 600;
  box-shadow: 0 4rpx 12rpx rgba(32, 194, 107, 0.3);
}

.job-apply-btn:active {
  transform: scale(0.95);
  opacity: 0.85;
}

.annotation-card {
  border-left: 6rpx solid #3b82f6;
}

.annotation-card::before {
  background: linear-gradient(180deg, #3b82f6 0%, #60a5fa 100%);
}

.annotation-tag-row {
  margin-bottom: 12rpx;
}

.annotation-tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  background: #eff6ff;
  color: #3b82f6;
  font-size: 22rpx;
  font-weight: 600;
}
</style>
