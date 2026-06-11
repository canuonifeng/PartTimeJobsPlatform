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

function getCategoryId(category) {
  return category.id ?? category.categoryId ?? category.code ?? ''
}

function sameCategoryId(a, b) {
  return String(a ?? '') === String(b ?? '')
}

function getCategoryName(category) {
  return category.name || category.categoryName || ''
}

function flattenCategories(list, parents = []) {
  return list.flatMap(category => {
    const name = getCategoryName(category)
    const path = [...parents, name].filter(Boolean)
    const item = {
      ...category,
      pickerLabel: path.join(' / ')
    }
    return [item, ...flattenCategories(category.children || [], path)]
  })
}

async function loadCategories() {
  try {
    const res = await getCategories()
    const list = Array.isArray(res) ? res : (res.data || res.records || [])
    categories.value = flattenCategories(list)
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
  const c = categories.value.find(c => sameCategoryId(getCategoryId(c), id))
  return c?.pickerLabel || c?.name || c?.categoryName || '-'
}
</script>

<template>
  <view class="page e-page">
    <view class="header e-header">
      <view class="e-header-row">
        <view class="header-copy">
          <text class="e-header-title">职位模版</text>
          <text class="e-header-desc">沉淀常用职位信息，快速复用发布</text>
        </view>
        <view class="header-add e-action-primary" @click="openCreate">+ 新增</view>
      </view>
    </view>

    <view class="content e-content">
      <view v-if="loading" class="e-empty">
        <text class="e-empty-title">加载中...</text>
      </view>
      <view v-else-if="templates.length === 0" class="e-empty">
        <text class="e-empty-title">暂无模版</text>
        <text class="e-empty-desc">点击右上角创建职位模版</text>
      </view>
      <view v-else>
        <view v-for="tpl in templates" :key="tpl.id" class="e-card">
          <view class="e-card-title-row">
            <text class="e-card-title">{{ tpl.title || '未命名模版' }}</text>
          </view>

          <view class="e-info-grid">
            <view class="e-info-pill">
              <text class="e-info-label">职位类别</text>
              <text class="e-info-value">{{ categoryName(tpl.categoryId) }}</text>
            </view>
            <view class="e-info-pill">
              <text class="e-info-label">工作地点</text>
              <text class="e-info-value">{{ [tpl.province, tpl.city, tpl.district, tpl.address].filter(Boolean).join(' ') || '未设置' }}</text>
            </view>
          </view>

          <view class="e-action-row">
            <view class="e-action-pill e-action-blue" @click="openEdit(tpl)">编辑</view>
            <view class="e-action-pill e-action-red" @click="handleDelete(tpl.id)">删除</view>
          </view>
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
</style>
