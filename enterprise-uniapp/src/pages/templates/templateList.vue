<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { listTemplates, deleteTemplate } from '@/api/templates'
import { getCategories } from '@/api/jobs'

const templates = ref([])
const loading = ref(false)
const categories = ref([])

onMounted(() => {
  loadTemplates()
  loadCategories()
})
onPullDownRefresh(() => loadTemplates().finally(() => uni.stopPullDownRefresh()))

async function loadTemplates() {
  loading.value = true
  try {
    const res = await listTemplates()
    templates.value = Array.isArray(res) ? res : (res.records || res.data || [])
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const res = await getCategories()
    categories.value = Array.isArray(res) ? res : (res.data || res.records || [])
  } catch {}
}

function openCreate() {
  uni.navigateTo({ url: '/pages/templates/templateForm' })
}

function openEdit(tpl) {
  const data = encodeURIComponent(JSON.stringify(tpl))
  uni.navigateTo({ url: `/pages/templates/templateForm?id=${tpl.id}&data=${data}` })
}

function handleDelete(id) {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该模版吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteTemplate(id)
          uni.showToast({ title: '已删除', icon: 'success' })
          loadTemplates()
        } catch {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

function categoryName(id) {
  const c = categories.value.find(c => (c.id || c.categoryId || c.code) === id)
  return c?.name || c?.categoryName || '-'
}
</script>

<template>
  <view class="page">
    <view class="header">
      <text class="header-title">职位模版</text>
      <text class="add-btn" @click="openCreate">+ 新建</text>
    </view>
    <view class="content">
      <view v-if="loading" class="state-msg">加载中...</view>
      <view v-else-if="templates.length === 0" class="state-msg">暂无模版</view>
      <view v-else class="list">
        <view v-for="tpl in templates" :key="tpl.id" class="card">
          <view class="card-top">
            <text class="card-title">{{ tpl.title }}</text>
          </view>
          <text class="card-cat">类别：{{ categoryName(tpl.categoryId) }}</text>
          <text class="card-addr">地点：{{ [tpl.province, tpl.city, tpl.district, tpl.address].filter(Boolean).join(' ') || '未设置' }}</text>
          <view class="card-actions">
            <button class="action-btn edit" @click="openEdit(tpl)">编辑</button>
            <button class="action-btn delete" @click="handleDelete(tpl.id)">删除</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style>
.page { min-height: 100vh; background: #f5f5f5; }
.header { display: flex; justify-content: space-between; align-items: center; padding: 24rpx 32rpx; background: #fff; border-bottom: 2rpx solid #eee; }
.header-title { font-size: 34rpx; font-weight: 600; color: #333; }
.add-btn { font-size: 28rpx; color: #007aff; }
.content { padding: 24rpx 32rpx; }
.state-msg { text-align: center; padding: 80rpx 0; color: #999; font-size: 28rpx; }
.list { display: flex; flex-direction: column; gap: 20rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
.card-title { font-size: 30rpx; font-weight: 500; color: #333; }
.card-cat, .card-addr { font-size: 26rpx; color: #666; display: block; margin-top: 8rpx; }
.card-actions { display: flex; gap: 16rpx; margin-top: 16rpx; }
.action-btn { flex: 1; height: 64rpx; line-height: 64rpx; font-size: 24rpx; border-radius: 8rpx; border: 2rpx solid #ddd; background: #fff; color: #333; text-align: center; }
.action-btn::after { border: none; }
.edit { border-color: #007aff; color: #007aff; }
.delete { border-color: #ff3b30; color: #ff3b30; }
</style>
