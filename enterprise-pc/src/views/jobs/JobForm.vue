<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getJob, createJob, updateJob } from '../../api/job'
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

const showLocationPicker = ref(false)
const templateDialogVisible = ref(false)
const availableTemplates = ref([])
const selectedTemplateName = ref('')

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

async function fetchDetail() {
  if (!isEdit) return
  selectedTemplateName.value = ''
  loading.value = true
  try {
    const res = await getJob(route.params.id)
    form.value = {
      title: res.title || '',
      description: res.description || '',
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
      salaryRates: (res.rates || []).map((r) => ({ type: r.type || '', rate: r.amount || '' })),
      scheduleSlots: (res.schedules || []).map((s) => ({ date: s.scheduleDate || '', startTime: s.startTime || '', endTime: s.endTime || '' })),
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
    rates: form.value.salaryRates.filter((r) => r.type && r.rate).map((r) => ({ type: r.type, amount: Number(r.rate), currency: 'CNY' })),
    schedules: form.value.scheduleSlots.filter((s) => s.date && s.startTime && s.endTime).map((s) => ({ scheduleDate: s.date, startTime: s.startTime, endTime: s.endTime, slotsAvailable: 1 }))
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
    availableLocations.value = await listLocations()
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

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="job-form">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑职位' : '新建职位' }}</span>
      </template>
      <el-form ref="formRef" :model="form" label-width="120px" style="max-width: 800px">
        <el-form-item label="职位模版">
          <el-button @click="openTemplatePicker">选择职位模版</el-button>
          <span v-if="selectedTemplateName" style="margin-left:12px;color:#909399;font-size:13px">已选：{{ selectedTemplateName }}</span>
        </el-form-item>
        <el-form-item label="职位名称" prop="title" :rules="[{ required: true, message: '请输入职位名称' }]">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="职位描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="工作地点">
          <el-button @click="openLocationPicker">选择已有地点</el-button>
        </el-form-item>
        <el-form-item label="省/市/区" prop="province">
          <el-cascader v-model="regionSelected" :options="regions" placeholder="选择省/市/区" style="width: 100%" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" placeholder="街道、门牌号" />
        </el-form-item>
        <el-form-item label="坐标定位">
          <el-button @click="showLocationPicker = true">选择位置</el-button>
          <span v-if="form.latitude" style="margin-left:12px;color:#999">{{ form.latitude.toFixed(6) }}, {{ form.longitude.toFixed(6) }}</span>
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="form.category" style="width: 200px">
            <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="招聘人数" prop="headcount">
          <el-input-number v-model="form.headcount" :min="1" />
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker v-model="form.deadline" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="岗位图片">
          <div style="display:flex;gap:12px;align-items:center">
            <el-upload
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleImageSuccess"
              :before-upload="beforeImageUpload"
            >
              <el-button type="primary">上传图片</el-button>
            </el-upload>
            <el-input v-model="form.imageUrl" placeholder="或输入图片URL" style="width:300px" clearable />
            <el-image v-if="form.imageUrl" :src="form.imageUrl" style="width:60px;height:60px;border-radius:4px" fit="cover" />
          </div>
        </el-form-item>

        <el-divider>薪资标准</el-divider>
        <el-form-item v-for="(item, index) in form.salaryRates" :key="index" :label="`薪资 ${index + 1}`">
          <div style="display: flex; gap: 8px; align-items: center">
            <el-select v-model="item.type" placeholder="薪资类型" style="width: 140px">
              <el-option label="时薪" value="HOURLY" />
              <el-option label="日薪" value="DAILY" />
              <el-option label="计件" value="PIECEWORK" />
            </el-select>
            <el-input-number v-model="item.rate" :min="0" :precision="2" placeholder="金额" />
            <el-button v-if="form.salaryRates.length > 1" type="danger" :icon="Delete" circle @click="removeSalaryRate(index)" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="addSalaryRate">添加薪资</el-button>
        </el-form-item>

        <el-divider>排班时段</el-divider>
        <el-form-item v-for="(item, index) in form.scheduleSlots" :key="index" :label="`时段 ${index + 1}`">
          <div style="display: flex; gap: 8px; align-items: center">
            <el-date-picker v-model="item.date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 140px" />
            <el-time-picker v-model="item.startTime" placeholder="开始时间" value-format="HH:mm" style="width: 130px" />
            <el-time-picker v-model="item.endTime" placeholder="结束时间" value-format="HH:mm" style="width: 130px" />
            <el-button v-if="form.scheduleSlots.length > 1" type="danger" :icon="Delete" circle @click="removeScheduleSlot(index)" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="addScheduleSlot">添加时段</el-button>
        </el-form-item>

        <el-divider />
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
          <el-button @click="router.push('/jobs')">取消</el-button>
        </el-form-item>
      </el-form>
      <LocationPicker v-model="showLocationPicker" :latitude="form.latitude || 39.9042" :longitude="form.longitude || 116.4074" @confirm="onLocationConfirm" />
    </el-card>

    <el-dialog v-model="templateDialogVisible" title="选择职位模版" width="600px">
      <el-table :data="availableTemplates" stripe @row-click="selectTemplate" highlight-current-row>
        <el-table-column prop="title" label="职位名称" width="150" />
        <el-table-column prop="description" label="职位描述" min-width="250" show-overflow-tooltip />
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
</style>
