<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { listLocations, deleteLocation, enableLocation, disableLocation } from '@/api/locations'

const locations = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(1)
const pageSize = 20
const total = ref(0)

onShow(() => loadLocations(1))
onPullDownRefresh(() => loadLocations(1).finally(() => uni.stopPullDownRefresh()))
onReachBottom(loadMore)

async function loadLocations(p = 1) {
  if (p === 1) loading.value = true
  else loadingMore.value = true
  try {
    const res = await listLocations({ page: p, pageSize })
    const list = Array.isArray(res) ? res : (res.records || res.data || [])
    if (p === 1) locations.value = list
    else locations.value = locations.value.concat(list)
    page.value = p
    total.value = Array.isArray(res) ? locations.value.length : (res.total || locations.value.length)
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  if (loading.value || loadingMore.value || locations.value.length >= total.value) return
  loadLocations(page.value + 1)
}

function openCreate() {
  uni.navigateTo({ url: '/pages/locations/locationForm' })
}

function openEdit(loc) {
  const data = encodeURIComponent(JSON.stringify(loc))
  uni.navigateTo({ url: `/pages/locations/locationForm?id=${loc.id}&data=${data}` })
}

function handleDelete(id) {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该地点吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteLocation(id)
          uni.showToast({ title: '已删除', icon: 'success' })
          loadLocations()
        } catch {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

async function toggleEnabled(loc) {
  try {
    if (loc.status === 'ENABLED') {
      await disableLocation(loc.id)
    } else {
      await enableLocation(loc.id)
    }
    loadLocations()
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}
</script>

<template>
  <view class="page e-page">
    <view class="header e-header">
      <view class="e-header-row">
        <view class="header-copy">
          <text class="e-header-title">工作地点</text>
          <text class="e-header-desc">维护常用办公、门店与项目地点</text>
        </view>
        <view class="header-add e-action-primary" @click="openCreate">+ 新增</view>
      </view>
    </view>

    <view class="content e-content">
      <view v-if="loading" class="e-empty">
        <text class="e-empty-title">加载中...</text>
      </view>
      <view v-else-if="locations.length === 0" class="e-empty">
        <text class="e-empty-title">暂无地点</text>
        <text class="e-empty-desc">点击右上角创建工作地点</text>
      </view>
      <view v-else>
        <view v-for="loc in locations" :key="loc.id" class="e-card">
          <view class="e-card-title-row">
            <text class="e-card-title">{{ loc.name || '未命名地点' }}</text>
            <view class="e-badge" :class="loc.status === 'ENABLED' ? 'e-badge-green' : 'e-badge-gray'">{{ loc.status === 'ENABLED' ? '已启用' : '已禁用' }}</view>
          </view>

          <view class="e-info-grid">
            <view class="e-info-pill info-wide">
              <text class="e-info-label">详细地址</text>
              <text class="e-info-value">{{ [loc.province, loc.city, loc.district, loc.address].filter(Boolean).join(' ') || '暂无地址' }}</text>
            </view>
          </view>

          <view class="e-action-row">
            <view class="e-action-pill e-action-blue" @click="openEdit(loc)">编辑</view>
            <view
              class="e-action-pill"
              :class="loc.status === 'ENABLED' ? 'e-action-orange' : 'e-action-primary'"
              @click="toggleEnabled(loc)"
            >{{ loc.status === 'ENABLED' ? '禁用' : '启用' }}</view>
            <view class="e-action-pill e-action-red" @click="handleDelete(loc.id)">删除</view>
          </view>
        </view>
        <view v-if="loadingMore" class="e-empty">
          <text class="e-empty-title">加载更多...</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.header-copy {
  flex: 1;
  min-width: 0;
}

.header-add {
  flex-shrink: 0;
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  font-size: 25rpx;
  font-weight: 700;
  line-height: 1.2;
}

.info-wide {
  width: calc(100% - 14rpx);
}
</style>
