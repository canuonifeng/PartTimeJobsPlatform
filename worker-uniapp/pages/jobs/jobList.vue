<template>
  <view class="job-list-page">
    <view class="search-bar">
      <uni-search-bar
        :radius="100"
        placeholder="搜索职位、公司"
        @confirm="onSearch"
        @clear="onSearch('')"
        v-model="keyword"
      />
    </view>

    <scroll-view
      class="category-scroll"
      scroll-x
      show-scrollbar="false"
    >
      <view class="category-chips">
        <view
          v-for="cat in categories"
          :key="cat.id"
          class="chip"
          :class="{ active: categoryId === cat.id }"
          @click="onCategoryChange(cat.id)"
        >
          <text>{{ cat.name }}</text>
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
      <view v-if="jobList.length === 0 && !loading" class="empty-state">
        <text class="empty-text">暂无职位信息</text>
      </view>

      <view
        v-for="job in jobList"
        :key="job.id"
        class="job-card"
        @click="goDetail(job.id)"
      >
        <view class="job-card-header">
          <text class="job-title">{{ job.title }}</text>
          <text class="job-pay">{{ job.minRate }}-{{ job.maxRate }}元/{{ job.rateUnit || '小时' }}</text>
        </view>
        <view class="job-tags">
          <text v-if="job.jobType" class="tag">{{ job.jobType }}</text>
          <text v-if="job.experience" class="tag">{{ job.experience }}</text>
        </view>
        <view class="job-card-footer">
          <text class="job-location">{{ job.location }}</text>
          <text class="job-company">{{ job.companyName }}</text>
        </view>
      </view>

      <uni-load-more v-if="hasMore" :status="loadingMore ? 'loading' : 'more'" />
      <uni-load-more v-if="!hasMore && jobList.length > 0" status="noMore" />
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
      page: p,
      pageSize
    })
    const list = res.list || []
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

onMounted(() => {
  fetchJobs(1)
})
</script>

<style scoped>
.job-list-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.search-bar {
  padding: 16rpx 20rpx;
  background: #fff;
}

.category-scroll {
  white-space: nowrap;
  padding: 16rpx 20rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.category-chips {
  display: inline-flex;
  gap: 16rpx;
}

.chip {
  display: inline-flex;
  padding: 12rpx 28rpx;
  border-radius: 40rpx;
  background: #f5f5f5;
  font-size: 26rpx;
  color: #666;
}

.chip.active {
  background: #e8f8ee;
  color: #07c160;
  font-weight: 500;
}

.job-scroll {
  flex: 1;
  padding: 20rpx;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 100rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #ccc;
}

.job-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.job-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.job-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.job-pay {
  font-size: 28rpx;
  color: #f60;
  font-weight: 500;
  white-space: nowrap;
  margin-left: 16rpx;
}

.job-tags {
  display: flex;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.tag {
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  background: #f5f5f5;
  font-size: 22rpx;
  color: #999;
}

.job-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.job-location {
  font-size: 24rpx;
  color: #999;
}

.job-company {
  font-size: 24rpx;
  color: #999;
}
</style>
