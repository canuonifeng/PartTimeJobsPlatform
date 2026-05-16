<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getJob, createJob, updateJob } from '../../api/job'
import regions from '../../assets/regions.json'
import LocationPicker from '../../components/LocationPicker.vue'

const router = useRouter()
const route = useRoute()
const isEdit = !!route.params.id
const loading = ref(false)
const formRef = ref(null)

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
  salaryRates: [{ type: '', rate: '' }],
  scheduleSlots: [{ dayOfWeek: '', startTime: '', endTime: '' }],
  status: 'DRAFT'
})

const categoryOptions = [
  { value: 'RETAIL', label: '零售' },
  { value: 'CATERING', label: '餐饮' },
  { value: 'EDUCATION', label: '教育' },
  { value: 'LOGISTICS', label: '物流' },
  { value: 'EVENT', label: '活动' },
  { value: 'OTHER', label: '其他' }
]

const statusOptions = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'PUBLISHED', label: '发布' }
]

const dayOfWeekOptions = [
  { value: 'MONDAY', label: '周一' },
  { value: 'TUESDAY', label: '周二' },
  { value: 'WEDNESDAY', label: '周三' },
  { value: 'THURSDAY', label: '周四' },
  { value: 'FRIDAY', label: '周五' },
  { value: 'SATURDAY', label: '周六' },
  { value: 'SUNDAY', label: '周日' }
]

const showLocationPicker = ref(false)

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
}

function addSalaryRate() {
  form.value.salaryRates.push({ type: '', rate: '' })
}

function removeSalaryRate(index) {
  form.value.salaryRates.splice(index, 1)
}

function addScheduleSlot() {
  form.value.scheduleSlots.push({ dayOfWeek: '', startTime: '', endTime: '' })
}

function removeScheduleSlot(index) {
  form.value.scheduleSlots.splice(index, 1)
}

async function fetchDetail() {
  if (!isEdit) return
  loading.value = true
  try {
    const res = await getJob(route.params.id)
    form.value = { ...res }
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (isEdit) {
      await updateJob(route.params.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await createJob(form.value)
      ElMessage.success('创建成功')
    }
    router.push('/jobs')
  } finally {
    loading.value = false
  }
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
        <el-form-item label="职位名称" prop="title" :rules="[{ required: true, message: '请输入职位名称' }]">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="职位描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="省/市/区" prop="province" :rules="[{ required: true, message: '请选择省市区' }]">
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

        <el-divider>薪资标准</el-divider>
        <el-form-item v-for="(item, index) in form.salaryRates" :key="index" :label="`薪资 ${index + 1}`">
          <div style="display: flex; gap: 8px; align-items: center">
            <el-input v-model="item.type" placeholder="类型（如：时薪）" style="width: 140px" />
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
            <el-select v-model="item.dayOfWeek" placeholder="星期" style="width: 110px">
              <el-option v-for="opt in dayOfWeekOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
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
        <LocationPicker v-model="showLocationPicker" :latitude="form.latitude || 39.9042" :longitude="form.longitude || 116.4074" @confirm="onLocationConfirm" />
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.job-form {
  padding: 20px;
}
</style>
