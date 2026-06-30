<template>
  <el-card>
    <template #header>
      <span>内容运营</span>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="轮播图管理" name="banners">
        <el-button type="primary" size="small" style="margin-bottom:15px" @click="handleCreateBanner">新增轮播图</el-button>
        <el-table :data="banners" stripe style="width:100%">
          <el-table-column label="图片" width="150">
            <template #default="{ row }">
              <el-image :src="row.imageUrl" style="width:120px;height:60px" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" width="200" />
          <el-table-column prop="linkUrl" label="跳转链接" min-width="200" show-overflow-tooltip />
          <el-table-column prop="sortOrder" label="排序" width="100" />
          <el-table-column prop="enabled" label="状态" width="100">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" active-text="启用" inactive-text="禁用" />
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="primary" size="small" text>上移</el-button>
              <el-button type="primary" size="small" text>下移</el-button>
              <el-button type="primary" size="small" text @click="handleEditBanner(row)">编辑</el-button>
              <el-button type="danger" size="small" text @click="handleDeleteBanner(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="热门推荐" name="hot">
        <el-button type="primary" size="small" style="margin-bottom:15px">添加推荐</el-button>
        <el-table :data="hotRecommendations" stripe style="width:100%">
          <el-table-column prop="jobTitle" label="职位名称" min-width="200" />
          <el-table-column prop="companyName" label="企业名称" width="200" />
          <el-table-column prop="sortOrder" label="排序" width="100" />
          <el-table-column prop="enabled" label="状态" width="100">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" active-text="启用" inactive-text="禁用" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" text>上移</el-button>
              <el-button type="primary" size="small" text>下移</el-button>
              <el-button type="danger" size="small" text>移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="服务费率" name="rates">
        <el-table :data="serviceFeeRates" stripe style="width:100%">
          <el-table-column prop="categoryName" label="职位分类" width="200" />
          <el-table-column prop="rate" label="服务费率" width="150">
            <template #default="{ row }">{{ (row.rate * 100).toFixed(1) }}%</template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="handleEditRate(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getBanners, getHotRecommendations, getServiceFeeRates, deleteBanner } from '../../api/content'

const activeTab = ref('banners')
const banners = ref([])
const hotRecommendations = ref([])
const serviceFeeRates = ref([])

async function fetchBanners() {
  try {
    const res = await getBanners()
    banners.value = res || []
  } catch {}
}

async function fetchHotRecommendations() {
  try {
    const res = await getHotRecommendations()
    hotRecommendations.value = res || []
  } catch {}
}

async function fetchServiceFeeRates() {
  try {
    const res = await getServiceFeeRates()
    serviceFeeRates.value = res || []
  } catch {}
}

function handleCreateBanner() {
  ElMessage.info('新建轮播图功能开发中')
}

function handleEditBanner(row) {
  ElMessage.info('编辑轮播图功能开发中')
}

async function handleDeleteBanner(row) {
  try {
    await deleteBanner(row.id)
    ElMessage.success('已删除')
    await fetchBanners()
  } catch {}
}

function handleEditRate(row) {
  ElMessage.info('编辑费率功能开发中')
}

onMounted(() => {
  fetchBanners()
  fetchHotRecommendations()
  fetchServiceFeeRates()
})
</script>
