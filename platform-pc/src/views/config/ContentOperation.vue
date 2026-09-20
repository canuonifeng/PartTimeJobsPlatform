<template>
  <el-card>
    <template #header>
      <span>内容运营</span>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="轮播图管理" name="banners">
        <el-button type="primary" size="small" style="margin-bottom:15px" @click="openCreateBanner">新增轮播图</el-button>
        <el-table :data="banners" stripe style="width:100%">
          <el-table-column label="图片" width="150">
            <template #default="{ row }">
              <el-image :src="row.imageUrl" style="width:120px;height:60px" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" width="200" />
          <el-table-column prop="linkUrl" label="跳转链接" min-width="200" show-overflow-tooltip />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
                {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="openEditBanner(row)">编辑</el-button>
              <el-button type="danger" size="small" text @click="handleDeleteBanner(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="热门推荐" name="hot">
        <el-button type="primary" size="small" style="margin-bottom:15px" @click="hotDialogVisible = true">添加推荐</el-button>
        <el-table :data="hotRecommendations" stripe style="width:100%">
          <el-table-column prop="jobTitle" label="职位名称" min-width="200" />
          <el-table-column prop="companyName" label="企业名称" width="200" />
          <el-table-column prop="categoryName" label="分类" width="120" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="danger" size="small" text @click="handleRemoveHot(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="服务费率" name="rates">
        <el-table :data="serviceFeeRates" stripe style="width:100%">
          <el-table-column prop="categoryName" label="职位分类" width="200" />
          <el-table-column prop="rate" label="服务费率" width="150">
            <template #default="{ row }">{{ (Number(row.rate) * 100).toFixed(1) }}%</template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="openEditRate(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="bannerDialogVisible" :title="bannerForm.id ? '编辑轮播图' : '新增轮播图'" width="600px">
      <el-form :model="bannerForm" label-width="90px">
        <el-form-item label="标题"><el-input v-model="bannerForm.title" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="bannerForm.imageUrl" /></el-form-item>
        <el-form-item label="跳转链接"><el-input v-model="bannerForm.linkUrl" /></el-form-item>
        <el-form-item label="位置">
          <el-select v-model="bannerForm.position" style="width:100%">
            <el-option label="首页" value="HOME" />
            <el-option label="工人端" value="WORKER" />
            <el-option label="企业端" value="ENTERPRISE" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="bannerForm.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="bannerForm.status">
            <el-radio value="ACTIVE">启用</el-radio>
            <el-radio value="INACTIVE">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bannerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBannerSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="hotDialogVisible" title="添加热门推荐" width="500px">
      <el-form label-width="90px">
        <el-form-item label="职位ID"><el-input-number v-model="hotJobId" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="hotDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddHot">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rateDialogVisible" title="编辑服务费率" width="500px">
      <el-form label-width="90px">
        <el-form-item label="分类">{{ rateForm.categoryName }}</el-form-item>
        <el-form-item label="费率">
          <el-input-number v-model="rateForm.ratePercent" :min="0" :max="100" :step="0.5" /> %
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRateSubmit">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getBanners, createBanner, updateBanner, deleteBanner, getHotRecommendations, addHotRecommendation, removeHotRecommendation, getServiceFeeRates, updateServiceFeeRate } from '../../api/content'

const activeTab = ref('banners')
const banners = ref([])
const hotRecommendations = ref([])
const serviceFeeRates = ref([])
const bannerDialogVisible = ref(false)
const bannerForm = ref({})
const hotDialogVisible = ref(false)
const hotJobId = ref(null)
const rateDialogVisible = ref(false)
const rateForm = ref({})

async function fetchBanners() {
  try { banners.value = (await getBanners()) || [] } catch {}
}
async function fetchHot() {
  try { hotRecommendations.value = (await getHotRecommendations()) || [] } catch {}
}
async function fetchRates() {
  try { serviceFeeRates.value = (await getServiceFeeRates()) || [] } catch {}
}

function openCreateBanner() {
  bannerForm.value = { position: 'HOME', sortOrder: 0, status: 'ACTIVE' }
  bannerDialogVisible.value = true
}
function openEditBanner(row) {
  bannerForm.value = { ...row }
  bannerDialogVisible.value = true
}
async function handleBannerSubmit() {
  try {
    if (bannerForm.value.id) { await updateBanner(bannerForm.value) } else { await createBanner(bannerForm.value) }
    ElMessage.success('保存成功')
    bannerDialogVisible.value = false
    await fetchBanners()
  } catch {}
}
async function handleDeleteBanner(row) {
  try {
    await deleteBanner(row.id)
    ElMessage.success('已删除')
    await fetchBanners()
  } catch {}
}
async function handleAddHot() {
  try {
    await addHotRecommendation({ jobId: hotJobId.value })
    ElMessage.success('已添加')
    hotDialogVisible.value = false
    await fetchHot()
  } catch {}
}
async function handleRemoveHot(row) {
  try {
    await removeHotRecommendation(row.jobId)
    ElMessage.success('已移除')
    await fetchHot()
  } catch {}
}
function openEditRate(row) {
  rateForm.value = { categoryId: row.categoryId, categoryName: row.categoryName, ratePercent: Number(row.rate) * 100 }
  rateDialogVisible.value = true
}
async function handleRateSubmit() {
  try {
    await updateServiceFeeRate({ categoryId: rateForm.value.categoryId, rate: rateForm.value.ratePercent / 100 })
    ElMessage.success('已更新')
    rateDialogVisible.value = false
    await fetchRates()
  } catch {}
}

onMounted(() => {
  fetchBanners()
  fetchHot()
  fetchRates()
})
</script>
