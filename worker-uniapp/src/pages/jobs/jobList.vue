<template>
  <view class="job-list-page">
    <view class="jobs-header">
      <view class="greeting-row">
        <view class="greeting-col">
          <text class="greeting-text">{{ greetingText }}</text>
          <text class="greeting-sub">{{ subGreetingText }}</text>
        </view>
        <view class="avatar-box" @click="goProfile">
          <text class="avatar-emoji">👷</text>
        </view>
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

    <scroll-view class="category-scroll" scroll-x :show-scrollbar="false">
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
        <view v-for="job in jobList" :key="job.id" class="job-card" @click="goDetail(job.id)">
          <view class="job-card-top">
            <text class="job-pay">{{ formatRates(job.rates, job.minRate, job.maxRate) }}</text>
            <text v-if="hotTagText(job)" class="job-hot-tag">{{ hotTagText(job) }}</text>
          </view>
          <text class="job-title">{{ job.title }}</text>
          <view class="job-meta">
            <text class="job-meta-item">📍 {{ job.location || '附近' }}</text>
            <text v-if="job.distanceKm != null" class="job-meta-item">📏 {{ job.distanceKm }}km</text>
          </view>
          <view class="job-tags">
            <text v-for="(tag, idx) in getDisplayTags(job)" :key="idx" class="job-tag" :class="tagClass(tag)">{{ tag }}</text>
          </view>
          <view class="job-card-footer">
            <view class="job-company">
              <view class="company-logo">
                <text>{{ (job.companyName || '企').slice(0, 1) }}</text>
              </view>
              <text class="company-name">{{ job.companyName || '优选企业' }}</text>
            </view>
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

const allCategory = { id: undefined, name: '全部', key: 'all' }
const categories = ref<JobCategory[]>([allCategory])

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const subGreetingText = computed(() => {
  const count = jobList.value.length
  if (count === 0) return '发现附近的好活'
  return `附近有 ${count} 个热门职位`
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
    const res: { list?: JobItem[] } | JobItem[] | null | undefined = await getJobs({
      keyword: keyword.value || undefined,
      categoryId: categoryId.value,
      page: p,
      pageSize,
      latitude: currentLocation.value?.latitude,
      longitude: currentLocation.value?.longitude
    })
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

function clearSearch() {
  keyword.value = ''
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
  background: #f5f6fa;
}

.jobs-header {
  position: relative;
  background: linear-gradient(135deg, #20c26b 0%, #1aab5a 55%, #169950 100%);
  padding: 100rpx 32rpx 32rpx;
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

.greeting-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28rpx;
}

.greeting-col {
  display: flex;
  flex-direction: column;
}

.greeting-text {
  font-size: 40rpx;
  font-weight: 700;
  line-height: 1.3;
}

.greeting-sub {
  font-size: 26rpx;
  opacity: 0.85;
  margin-top: 6rpx;
}

.avatar-box {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(255, 255, 255, 0.4);
}

.avatar-emoji {
  font-size: 36rpx;
}

.search-box {
  display: flex;
  align-items: center;
  height: 88rpx;
  padding: 0 28rpx;
  border-radius: 44rpx;
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
  padding: 24rpx 0 16rpx;
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
  padding: 32rpx;
  padding-left: 36rpx;
  margin-bottom: 24rpx;
  border-radius: 20rpx;
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

.job-card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12rpx;
}

.job-pay {
  font-size: 48rpx;
  font-weight: 800;
  color: #20c26b;
  line-height: 1.2;
  letter-spacing: -1rpx;
}

.job-hot-tag {
  flex-shrink: 0;
  padding: 6rpx 18rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #ef4444 0%, #f97316 100%);
}

.job-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.4;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.job-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-bottom: 18rpx;
}

.job-meta-item {
  font-size: 24rpx;
  color: #888;
  flex-shrink: 0;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 24rpx;
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
  color: #20c26b;
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
  padding-top: 20rpx;
  border-top: 1rpx dashed #f0f0f0;
}

.job-company {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.company-logo {
  width: 44rpx;
  height: 44rpx;
  border-radius: 10rpx;
  background: linear-gradient(135deg, #20c26b 0%, #1aab5a 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 700;
  flex-shrink: 0;
}

.company-name {
  font-size: 24rpx;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 300rpx;
}
</style>
