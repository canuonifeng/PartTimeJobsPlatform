<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete, SuccessFilled } from '@element-plus/icons-vue'
import { getJob, createJob, updateJob, getJobTags } from '../../api/job'
import { listLocations } from '../../api/location'
import { listTemplates } from '../../api/template'
import regions from '../../assets/regions.json'
import LocationPicker from '../../components/LocationPicker.vue'

const router = useRouter()
const route = useRoute()
const isEdit = !!route.params.id
const loading = ref(false)
const locationDialogVisible = ref(false)
const availableLocations = ref([])
const tagGroups = ref([])
const tagLoading = ref(false)
const tagLoadFailed = ref(false)
const formRef = ref(null)
const uploadUrl = '/api/files/upload'

function handleImageSuccess(response) {
  if (response?.url) {
    form.value.imageUrl = response.url
  }
}
function beforeImageUpload(file) {
  const isImg = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImg) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const form = ref({
  title: '',
  description: '',
  requirements: '',
  contactPhone: '',
  tagIds: [],
  location: '',
  category: '',
  headcount: 1,
  deadline: '',
  province: '',
  city: '',
  district: '',
  address: '',
  latitude: null,
  longitude: null,
  imageUrl: '',
  salaryRates: [{ type: '', rate: '' }],
  scheduleSlots: [{ date: '', startTime: '', endTime: '' }],
  autoApprove: null,
  status: 'DRAFT'
})

const categoryOptions = [
  { value: 1, label: '餐饮服务' },
  { value: 2, label: '物流配送' },
  { value: 3, label: '家政保洁' },
  { value: 4, label: '活动促销' },
  { value: 5, label: '教育培训' },
  { value: 6, label: '美容美发' },
  { value: 7, label: '其他' }
]

const statusOptions = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'PUBLISHED', label: '发布' }
]

function getResponseList(res) {
  if (Array.isArray(res)) return res
  if (!res || typeof res !== 'object') return []
  if (Array.isArray(res.data)) return res.data
  if (Array.isArray(res.records)) return res.records
  return []
}

function getGroupTags(group) {
  return Array.isArray(group?.tags) ? group.tags : []
}

function normalizeTagId(id) {
  const value = Number(id)
  return Number.isFinite(value) ? value : null
}

function normalizeTagIds(ids) {
  return [...new Set((Array.isArray(ids) ? ids : []).map(normalizeTagId).filter(id => id !== null))]
}

const showLocationPicker = ref(false)
const templateDialogVisible = ref(false)
const availableTemplates = ref([])
const selectedTemplateName = ref('')
const selectedLocationName = ref('')

const regionSelected = computed({
  get: () => {
    const arr = []
    if (form.value.province) arr.push(form.value.province)
    if (form.value.city) arr.push(form.value.city)
    if (form.value.district) arr.push(form.value.district)
    return arr
  },
  set: (val) => {
    if (!val || val.length === 0) {
      form.value.province = ''
      form.value.city = ''
      form.value.district = ''
      return
    }
    form.value.province = val[0] || ''
    form.value.city = val[1] || ''
    form.value.district = val[2] || ''
  }
})

function onLocationConfirm(pos) {
  form.value.latitude = pos.latitude
  form.value.longitude = pos.longitude
  if (pos.province) form.value.province = pos.province
  if (pos.city) form.value.city = pos.city
  if (pos.district) form.value.district = pos.district
  if (pos.address) form.value.address = pos.address
}

function addSalaryRate() {
  form.value.salaryRates.push({ type: '', rate: '' })
}

function removeSalaryRate(index) {
  form.value.salaryRates.splice(index, 1)
}

function addScheduleSlot() {
  form.value.scheduleSlots.push({ date: '', startTime: '', endTime: '' })
}

function removeScheduleSlot(index) {
  form.value.scheduleSlots.splice(index, 1)
}

async function loadJobTags() {
  tagLoading.value = true
  tagLoadFailed.value = false
  try {
    tagGroups.value = getResponseList(await getJobTags()).map(group => ({
      ...group,
      tags: getGroupTags(group)
    }))
  } catch {
    tagGroups.value = []
    tagLoadFailed.value = true
  } finally {
    tagLoading.value = false
  }
}

async function fetchDetail() {
  if (!isEdit) return
  selectedTemplateName.value = ''
  loading.value = true
  try {
    const res = await getJob(route.params.id)
    form.value = {
      title: res.title || '',
      description: res.description || '',
      requirements: res.requirements || '',
      contactPhone: res.contactPhone || '',
      tagIds: normalizeTagIds(res.tagIds || (Array.isArray(res.tags) ? res.tags.map(tag => tag?.id ?? tag?.tagId) : [])),
      location: res.location || '',
      category: res.categoryId || '',
      headcount: res.headcount || 1,
      deadline: res.deadline ? res.deadline.slice(0, 10) : '',
      province: res.province || '',
      city: res.city || '',
      district: res.district || '',
      address: res.address || '',
      latitude: res.latitude || null,
      longitude: res.longitude || null,
      imageUrl: res.imageUrl || '',
      autoApprove: res.autoApprove ?? null,
      salaryRates: (res.rates || []).map((r) => ({ id: r.id, type: r.type || '', rate: r.amount || '' })),
      scheduleSlots: (res.schedules || []).map((s) => ({ id: s.id, date: s.scheduleDate || '', startTime: s.startTime || '', endTime: s.endTime || '' })),
      status: res.status || 'DRAFT'
    }
  } finally {
    loading.value = false
  }
}

function buildPayload() {
  return {
    title: form.value.title,
    description: form.value.description,
    requirements: form.value.requirements,
    contactPhone: form.value.contactPhone,
    tagIds: normalizeTagIds(form.value.tagIds),
    location: form.value.location,
    province: form.value.province || null,
    city: form.value.city || null,
    district: form.value.district || null,
    address: form.value.address || null,
    latitude: form.value.latitude || null,
    longitude: form.value.longitude || null,
    categoryId: form.value.category || null,
    headcount: form.value.headcount,
    deadline: form.value.deadline ? `${form.value.deadline} 23:59:59` : null,
    imageUrl: form.value.imageUrl || null,
    autoApprove: form.value.autoApprove,
    status: form.value.status,
    rates: form.value.salaryRates.filter((r) => r.type && r.rate).map((r) => ({ id: r.id, type: r.type, amount: Number(r.rate), currency: 'CNY' })),
    schedules: form.value.scheduleSlots.filter((s) => s.date && s.startTime && s.endTime).map((s) => ({ id: s.id, scheduleDate: s.date, startTime: s.startTime, endTime: s.endTime, slotsAvailable: 1 }))
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const payload = buildPayload()
    if (isEdit) {
      await updateJob(route.params.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createJob(payload)
      ElMessage.success('创建成功')
    }
    router.push('/jobs')
  } finally {
    loading.value = false
  }
}

async function openLocationPicker() {
  try {
    const data = await listLocations({ page: 1, pageSize: 100 })
    availableLocations.value = Array.isArray(data) ? data : (data.records || [])
    locationDialogVisible.value = true
  } catch {
    ElMessage.error('加载地点列表失败')
  }
}

function selectLocation(loc) {
  form.value.province = loc.province || ''
  form.value.city = loc.city || ''
  form.value.district = loc.district || ''
  form.value.address = loc.address || ''
  form.value.latitude = loc.latitude
  form.value.longitude = loc.longitude
  selectedLocationName.value = loc.name
  locationDialogVisible.value = false
  ElMessage.success(`已选择地点：${loc.name}`)
}

async function openTemplatePicker() {
  try {
    availableTemplates.value = await listTemplates()
    templateDialogVisible.value = true
  } catch {
    ElMessage.error('加载模版列表失败')
  }
}

function selectTemplate(tpl) {
  form.value.title = tpl.title || ''
  form.value.description = tpl.description || ''
  form.value.requirements = tpl.requirements || ''
  form.value.contactPhone = tpl.contactPhone || ''
  form.value.tagIds = normalizeTagIds(tpl.tagIds || (Array.isArray(tpl.tags) ? tpl.tags.map(tag => tag?.id ?? tag?.tagId) : []))
  form.value.category = tpl.categoryId || ''
  form.value.imageUrl = tpl.imageUrl || ''
  form.value.province = tpl.province || ''
  form.value.city = tpl.city || ''
  form.value.district = tpl.district || ''
  form.value.address = tpl.address || ''
  form.value.latitude = tpl.latitude || ''
  form.value.longitude = tpl.longitude || ''
  selectedTemplateName.value = tpl.title || ''
  templateDialogVisible.value = false
  ElMessage.success(`已选择模版：${tpl.title}`)
}

onMounted(async () => {
  await loadJobTags()
  fetchDetail()
})
</script>

<template>
  <div class="job-form">
    <el-card class="section-card">
      <template #header>
        <div class="section-header">{{ isEdit ? '编辑职位' : '新建职位' }} - 岗位信息</div>
      </template>
      <el-form ref="formRef" :model="form" label-width="120px" style="max-width: 800px">
        <el-form-item label="选择已有职位模版">
          <el-button type="primary" plain @click="openTemplatePicker" style="width: 260px; justify-content: flex-start">选择已有职位模版</el-button>
          <div v-if="selectedTemplateName" class="selected-tip">
            <el-icon><SuccessFilled /></el-icon>
            已选模版：{{ selectedTemplateName }}
          </div>
        </el-form-item>
        <el-form-item label="职位名称" prop="title" :rules="[{ required: true, message: '请输入职位名称' }]">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="职位类型" prop="category">
          <el-select v-model="form.category" style="width: 260px">
            <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位职责" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请输入HTML富文本岗位职责" />
        </el-form-item>
        <el-form-item label="任职要求" prop="requirements">
          <el-input v-model="form.requirements" type="textarea" :rows="5" placeholder="请输入HTML富文本任职要求" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" style="width: 260px" />
        </el-form-item>
        <el-form-item label="岗位标签">
          <div v-if="tagLoading" class="tag-hint">标签加载中...</div>
          <div v-else-if="tagLoadFailed" class="tag-hint error" @click="loadJobTags">标签加载失败，点击重试</div>
          <div v-else-if="!tagGroups.length" class="tag-hint">暂无可选标签</div>
          <div v-else class="tag-groups">
            <div v-for="group in tagGroups" :key="group.id" class="tag-group">
              <div class="tag-group-title">{{ group.name }}</div>
              <el-checkbox-group v-model="form.tagIds">
                <el-checkbox-button v-for="tag in getGroupTags(group)" :key="tag.id" :label="tag.id">
                  {{ tag.name }}
                </el-checkbox-button>
              </el-checkbox-group>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="职位图片">
          <div style="display:flex;gap:12px;align-items:center">
            <el-upload
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleImageSuccess"
              :before-upload="beforeImageUpload"
            >
              <el-button type="primary" plain>上传图片</el-button>
            </el-upload>
            <el-input v-model="form.imageUrl" placeholder="或输入图片URL" style="width:300px" clearable />
            <el-image v-if="form.imageUrl" :src="form.imageUrl" style="width:60px;height:60px;border-radius:4px" fit="cover" />
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="section-card">
      <template #header>
        <div class="section-header">地址信息</div>
      </template>
      <el-form ref="formRef2" :model="form" label-width="120px" style="max-width: 800px">
        <el-form-item label="选择已有工作地点">
          <el-button type="primary" plain @click="openLocationPicker" style="width: 260px; justify-content: flex-start">选择已有工作地点</el-button>
          <div v-if="selectedLocationName" class="selected-tip">
            <el-icon><SuccessFilled /></el-icon>
            已选地址：{{ selectedLocationName }}
          </div>
        </el-form-item>
        <el-form-item label="省/市/区" prop="province">
          <el-cascader v-model="regionSelected" :options="regions" placeholder="选择省/市/区" style="width: 260px" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" placeholder="街道、门牌号" />
        </el-form-item>
        <el-form-item label="坐标定位">
          <el-button type="primary" plain @click="showLocationPicker = true">选择位置</el-button>
          <span v-if="form.latitude" class="coord-text">{{ form.latitude.toFixed(6) }}, {{ form.longitude.toFixed(6) }}</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="section-card">
      <template #header>
        <div class="section-header">招聘信息</div>
      </template>
      <el-form ref="formRef3" :model="form" label-width="120px" style="max-width: 800px">
        <el-form-item label="招聘人数" prop="headcount">
          <el-input-number v-model="form.headcount" :min="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker v-model="form.deadline" type="date" placeholder="选择截止日期" value-format="YYYY-MM-DD" style="width: 260px" />
        </el-form-item>
        <el-form-item label="自动审核">
          <el-select v-model="form.autoApprove" placeholder="跟随平台默认" clearable>
            <el-option label="跟随平台默认" :value="null" />
            <el-option label="开启" :value="true" />
            <el-option label="关闭" :value="false" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="section-card">
      <template #header>
        <div class="section-header">薪资标准</div>
      </template>
      <el-form ref="formRef4" :model="form" label-width="120px" style="max-width: 800px">
        <el-form-item v-for="(item, index) in form.salaryRates" :key="index" :label="`薪资 ${index + 1}`">
          <div style="display: flex; gap: 8px; align-items: center">
            <el-select v-model="item.type" placeholder="薪资类型" style="width: 140px">
              <el-option label="时薪" value="HOURLY" />
              <el-option label="日薪" value="DAILY" />
              <el-option label="计件" value="PIECEWORK" />
            </el-select>
            <el-input-number v-model="item.rate" :min="0" :precision="2" placeholder="金额" style="width: 180px" />
            <el-button v-if="form.salaryRates.length > 1" type="danger" :icon="Delete" circle @click="removeSalaryRate(index)" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" plain @click="addSalaryRate">添加薪资</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="section-card">
      <template #header>
        <div class="section-header">排班时段</div>
      </template>
      <el-form ref="formRef5" :model="form" label-width="120px" style="max-width: 800px">
        <el-form-item v-for="(item, index) in form.scheduleSlots" :key="index" :label="`时段 ${index + 1}`">
          <div style="display: flex; gap: 8px; align-items: center">
            <el-date-picker v-model="item.date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 140px" />
            <el-time-picker v-model="item.startTime" placeholder="开始时间" value-format="HH:mm" style="width: 130px" />
            <el-time-picker v-model="item.endTime" placeholder="结束时间" value-format="HH:mm" style="width: 130px" />
            <el-button v-if="form.scheduleSlots.length > 1" type="danger" :icon="Delete" circle @click="removeScheduleSlot(index)" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" plain @click="addScheduleSlot">添加时段</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="section-card">
      <el-form ref="formRef6" :model="form" label-width="120px" style="max-width: 800px">
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">保存</el-button>
          <el-button size="large" @click="router.push('/jobs')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
      <LocationPicker v-model="showLocationPicker" :latitude="form.latitude || 39.9042" :longitude="form.longitude || 116.4074" @confirm="onLocationConfirm" />

    <el-dialog v-model="templateDialogVisible" title="选择职位模版" width="600px">
      <el-table :data="availableTemplates" stripe @row-click="selectTemplate" highlight-current-row>
        <el-table-column prop="title" label="职位名称" width="150" />
        <el-table-column prop="description" label="岗位职责" min-width="250" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="locationDialogVisible" title="选择工作地点" width="600px">
      <el-table :data="availableLocations" stripe @row-click="selectLocation" highlight-current-row>
        <el-table-column prop="name" label="名称" width="120" />
        <el-table-column prop="province" label="省" width="80" />
        <el-table-column prop="city" label="市" width="80" />
        <el-table-column prop="district" label="区" width="80" />
        <el-table-column prop="address" label="详细地址" min-width="180" />
      </el-table>
      <template #footer>
        <el-button @click="locationDialogVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.job-form {
  padding: 20px;
}
.section-card {
  margin-bottom: 20px;
}
.section-header {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.selected-tip {
  margin-top: 8px;
  font-size: 13px;
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 6px;
}
.coord-text {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}
.tag-hint {
  color: #909399;
  font-size: 13px;
  cursor: default;
}
.tag-hint.error {
  color: #f56c6c;
  cursor: pointer;
}
.tag-groups {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.tag-group-title {
  margin-bottom: 8px;
  color: #606266;
  font-size: 13px;
}
</style>
