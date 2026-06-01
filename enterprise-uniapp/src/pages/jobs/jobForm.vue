<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createJob, updateJob, getJob, getCategories, getRates, getSchedules } from '@/api/jobs'
import { listTemplates } from '@/api/templates'
import { listLocations } from '@/api/locations'
import regions from '@/assets/regions.json'

const isEdit = ref(false)
const jobId = ref('')
const saving = ref(false)
const categories = ref([])

const formData = ref({
  title: '',
  description: '',
  categoryId: '',
  headcount: 1,
  deadline: '',
  province: '',
  city: '',
  district: '',
  address: '',
  latitude: null,
  longitude: null,
  imageUrl: '',
  salaryRates: [{ type: '', amount: '' }],
  schedules: [{ date: '', startTime: '', endTime: '' }]
})

const rates = ref([])
const schedules = ref([])

const rateTypeLabels = ['时薪', '日薪', '计件']
const rateTypeValues = ['HOURLY', 'DAILY', 'PIECEWORK']

const categoryNames = ref([])
const availableTemplates = ref([])
const templateNames = ref([])
const availableLocations = ref([])
const locationNames = ref([])
const selectedTemplateName = ref('')
const selectedLocationName = ref('')

const provinceList = regions.map(r => r.label)

function getCitiesForProvince(pIdx) {
  return regions[pIdx]?.children?.map(c => c.label) || []
}

function getDistrictsForProvinceCity(pIdx, cIdx) {
  return regions[pIdx]?.children?.[cIdx]?.children?.map(d => d.label) || []
}

const cityList = ref([])
const districtList = ref([])
const regionIndexes = ref([0, 0, 0])

function onRegionChange(e) {
  regionIndexes.value = e.detail.value
  const [pIdx, cIdx, dIdx] = e.detail.value
  const province = regions[pIdx]
  if (province) {
    formData.value.province = province.label
    cityList.value = getCitiesForProvince(pIdx)
    const city = province.children?.[cIdx]
    if (city) {
      formData.value.city = city.label
      districtList.value = getDistrictsForProvinceCity(pIdx, cIdx)
      const district = city.children?.[dIdx]
      if (district) {
        formData.value.district = district.label
      }
    }
  }
}

function chooseImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      formData.value.imageUrl = res.tempFilePaths[0]
    }
  })
}

function previewImage() {
  uni.previewImage({
    urls: [formData.value.imageUrl],
    current: 0
  })
}

function chooseLocation() {
  uni.chooseLocation({
    success: (res) => {
      formData.value.latitude = res.latitude
      formData.value.longitude = res.longitude
      if (res.address && !formData.value.address) {
        formData.value.address = res.address
      }
    },
    fail: () => {
      uni.showToast({ title: '定位失败', icon: 'none' })
    }
  })
}

async function openTemplatePicker() {
  // 数据已在onLoad中预加载，无需重复加载
}

function onTemplatePick(e) {
  const idx = e.detail.value
  const tpl = availableTemplates.value[idx]
  if (!tpl) return
  formData.value.title = tpl.title || ''
  formData.value.description = tpl.description || ''
  formData.value.categoryId = tpl.categoryId || ''
  formData.value.province = tpl.province || ''
  formData.value.city = tpl.city || ''
  formData.value.district = tpl.district || ''
  formData.value.address = tpl.address || ''
  formData.value.latitude = tpl.latitude || null
  formData.value.longitude = tpl.longitude || null
  selectedTemplateName.value = tpl.title || ''
}

async function openLocationPicker() {
  // 数据已在onLoad中预加载，无需重复加载
}

function onLocationPick(e) {
  const idx = e.detail.value
  const loc = availableLocations.value[idx]
  if (!loc) return
  formData.value.province = loc.province || ''
  formData.value.city = loc.city || ''
  formData.value.district = loc.district || ''
  formData.value.address = loc.address || ''
  formData.value.latitude = loc.latitude || null
  formData.value.longitude = loc.longitude || null
  selectedLocationName.value = loc.name || ''
}

onLoad(async (params) => {
  if (params.id) {
    isEdit.value = true
    jobId.value = params.id
  }
  await loadCategories()
  await openTemplatePicker()
  await openLocationPicker()
  if (isEdit.value) {
    await loadJobDetail()
  }
})

async function loadCategories() {
  try {
    const res = await getCategories()
    const list = Array.isArray(res) ? res : (res.data || res.records || [])
    categories.value = list
    categoryNames.value = list.map(c => c.name || c.categoryName || '')
  } catch (e) {
    console.error('Failed to load categories', e)
  }
}

async function loadJobDetail() {
  try {
    const job = await getJob(jobId.value)
    formData.value = {
      title: job.title || '',
      description: job.description || '',
      location: job.location || '',
      province: job.province || '',
      city: job.city || '',
      district: job.district || '',
      address: job.address || '',
      latitude: job.latitude || null,
      longitude: job.longitude || null,
      categoryId: job.categoryId || '',
      headcount: job.headcount || 1,
      deadline: job.deadline || ''
    }
    try {
      const rateRes = await getRates(jobId.value)
      rates.value = Array.isArray(rateRes)
        ? rateRes.map(rate => ({
            type: rate.type || 'HOURLY',
            amount: rate.amount ?? '',
            currency: rate.currency || 'CNY'
          }))
        : []
    } catch {}
    try {
      const schedRes = await getSchedules(jobId.value)
      schedules.value = Array.isArray(schedRes)
        ? schedRes.map(sched => ({
            date: sched.scheduleDate || sched.date || '',
            startTime: sched.startTime || '',
            endTime: sched.endTime || ''
          }))
        : []
    } catch {}
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

function buildPayload() {
  return {
    title: formData.value.title,
    description: formData.value.description,
    location: formData.value.location,
    province: formData.value.province || null,
    city: formData.value.city || null,
    district: formData.value.district || null,
    address: formData.value.address || null,
    latitude: formData.value.latitude ?? null,
    longitude: formData.value.longitude ?? null,
    categoryId: formData.value.categoryId || null,
    headcount: formData.value.headcount,
    deadline: formData.value.deadline ? `${formData.value.deadline} 23:59:59` : null,
    rates: rates.value
      .filter(rate => rate.amount)
      .map(rate => ({
        type: rate.type,
        amount: Number(rate.amount),
        currency: rate.currency || 'CNY'
      })),
    schedules: schedules.value
      .filter(sched => sched.date && sched.startTime && sched.endTime)
      .map(sched => ({
        scheduleDate: sched.date,
        startTime: sched.startTime,
        endTime: sched.endTime,
        slotsAvailable: 1
      }))
  }
}

function addRate() {
  rates.value.push({ type: 'HOURLY', amount: '', currency: 'CNY' })
}

function removeRate(index) {
  rates.value.splice(index, 1)
}

function addSchedule() {
  schedules.value.push({ date: '', startTime: '', endTime: '' })
}

function removeSchedule(index) {
  schedules.value.splice(index, 1)
}

function onRateTypeChange(e, index) {
  rates.value[index].type = rateTypeValues[e.detail.value]
}

function getRateTypeIndex(type) {
  const i = rateTypeValues.indexOf(type)
  return i >= 0 ? i : 0
}

function onCategoryChange(e) {
  const idx = e.detail.value
  const cat = categories.value[idx]
  if (cat) {
    formData.value.categoryId = cat.id || cat.categoryId || cat.code || ''
  }
}

function getCategoryIndex() {
  const id = formData.value.categoryId
  return categories.value.findIndex(c => (c.id || c.categoryId || c.code) === id)
}

async function handleSave() {
  if (!formData.value.title) {
    uni.showToast({ title: '请输入职位标题', icon: 'none' })
    return
  }

  saving.value = true
  try {
    let id = jobId.value
    const payload = buildPayload()

    if (isEdit.value) {
      await updateJob(id, payload)
    } else {
      const res = await createJob(payload)
      id = res.id || res.data?.id || ''
    }

    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <view class="page form-page">
    <scroll-view scroll-y class="form-scroll">
      <view class="form-section">
        <text class="section-title">岗位信息</text>

        <view class="form-item">
          <text class="label">选择已有职位模版</text>
          <picker mode="selector" :range="templateNames" @change="onTemplatePick">
            <view class="picker-btn">
              <text>选择已有职位模版</text>
            </view>
          </picker>
          <view v-if="selectedTemplateName" class="selected-tip">
            ✔ 已选：{{ selectedTemplateName }}
          </view>
        </view>

        <view class="form-item">
          <text class="label">职位名称 *</text>
          <input v-model="formData.title" class="input" placeholder="请输入职位名称" />
        </view>

        <view class="form-item">
          <text class="label">职位类型</text>
          <picker
            mode="selector"
            :range="categoryNames"
            :value="getCategoryIndex()"
            @change="onCategoryChange"
          >
            <view class="picker">
              <text v-if="formData.categoryId" class="picker-value">{{ categoryNames[getCategoryIndex()] }}</text>
              <text v-else class="picker-placeholder">请选择职位类型</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="label">职位描述</text>
          <textarea v-model="formData.description" class="textarea" placeholder="请输入职位描述" />
        </view>

        <view class="form-item">
          <text class="label">职位图片</text>
          <button class="picker-btn" @click="chooseImage">选择图片</button>
          <image v-if="formData.imageUrl" class="preview-image" :src="formData.imageUrl" mode="aspectFill" @click="previewImage" />
        </view>
      </view>

      <view class="form-section">
        <text class="section-title">地址信息</text>

        <view class="form-item">
          <text class="label">选择已有工作地址</text>
          <picker mode="selector" :range="locationNames" @change="onLocationPick">
            <view class="picker-btn">
              <text>选择已有工作地址</text>
            </view>
          </picker>
          <view v-if="selectedLocationName" class="selected-tip">
            ✔ 已选：{{ selectedLocationName }}
          </view>
        </view>

        <view class="form-item">
          <text class="label">省/市/区</text>
          <picker mode="multiSelector" :range="[provinceList, cityList, districtList]" :value="regionIndexes" @columnchange="onRegionChange">
            <view class="picker">
              <text v-if="formData.province" class="picker-value">{{ formData.province }} {{ formData.city }} {{ formData.district }}</text>
              <text v-else class="picker-placeholder">请选择省/市/区</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="label">详细地址</text>
          <input v-model="formData.address" class="input" placeholder="街道、门牌号" />
        </view>

        <view class="form-item">
          <text class="label">坐标定位</text>
          <button class="location-btn" @click="chooseLocation">选择位置</button>
          <text v-if="formData.latitude" class="location-coords">{{ formData.latitude }}, {{ formData.longitude }}</text>
        </view>
      </view>

      <view class="form-section">
        <text class="section-title">招聘信息</text>

        <view class="form-item">
          <text class="label">招聘人数</text>
          <input v-model.number="formData.headcount" class="input" type="number" placeholder="招聘人数" />
        </view>

        <view class="form-item">
          <text class="label">截止日期</text>
          <picker
            mode="date"
            :value="formData.deadline"
            @change="(e) => formData.deadline = e.detail.value"
          >
            <view class="picker">
              <text v-if="formData.deadline" class="picker-value">{{ formData.deadline }}</text>
              <text v-else class="picker-placeholder">请选择截止日期</text>
            </view>
          </picker>
        </view>
      </view>

      <view class="form-section">
        <view class="section-header">
          <text class="section-title">薪资标准</text>
          <text class="add-btn" @click="addRate">+ 添加</text>
        </view>
        <view v-for="(rate, index) in rates" :key="index" class="sub-item">
          <view class="sub-row">
            <picker
              mode="selector"
              :range="rateTypeLabels"
              :value="getRateTypeIndex(rate.type)"
              @change="(e) => onRateTypeChange(e, index)"
            >
              <view class="picker picker-sm">
                <text>{{ rateTypeLabels[getRateTypeIndex(rate.type)] }}</text>
              </view>
            </picker>
            <input v-model="rate.amount" class="input input-sm" type="digit" placeholder="金额" />
            <text class="currency-label">CNY</text>
            <text class="remove-btn" @click="removeRate(index)">删除</text>
          </view>
        </view>
        <view v-if="rates.length === 0" class="empty-hint">
          <text>暂无薪资标准，点击上方 "添加"</text>
        </view>
      </view>

      <view class="form-section">
        <view class="section-header">
          <text class="section-title">排班时段</text>
          <text class="add-btn" @click="addSchedule">+ 添加</text>
        </view>
        <view v-for="(sched, index) in schedules" :key="index" class="sub-item">
          <view class="sub-row">
            <picker mode="date" @change="(e) => sched.date = e.detail.value">
              <view class="picker picker-sm">
                <text>{{ sched.date || '日期' }}</text>
              </view>
            </picker>
          </view>
          <view class="sub-row">
            <picker mode="time" @change="(e) => sched.startTime = e.detail.value">
              <view class="picker picker-sm">
                <text>{{ sched.startTime || '开始' }}</text>
              </view>
            </picker>
            <text class="time-sep">至</text>
            <picker mode="time" @change="(e) => sched.endTime = e.detail.value">
              <view class="picker picker-sm">
                <text>{{ sched.endTime || '结束' }}</text>
              </view>
            </picker>
            <text class="remove-btn" @click="removeSchedule(index)">删除</text>
          </view>
        </view>
        <view v-if="schedules.length === 0" class="empty-hint">
          <text>暂无排班时段，点击上方 "添加"</text>
        </view>
      </view>

      <view class="form-actions">
        <button class="save-btn" :disabled="saving" @click="handleSave">
          <text v-if="saving">保存中...</text>
          <text v-else>保存</text>
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<style>
.form-page {
  background: #f5f5f5;
}
.form-scroll {
  padding: 24rpx 32rpx;
}
.form-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 20rpx;
}
.section-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}
.section-header .section-title {
  margin-bottom: 0;
}
.add-btn {
  font-size: 26rpx;
  color: #007aff;
  padding: 8rpx 12rpx;
}
.picker-btn {
  width: 100%;
  height: 72rpx;
  line-height: 72rpx;
  background: #fff;
  border: 2rpx solid #409eff;
  border-radius: 8rpx;
  color: #409eff;
  text-align: center;
  font-size: 28rpx;
}
.selected-tip {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #67c23a;
}
.form-item {
  margin-bottom: 24rpx;
}
.label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 8rpx;
}
.input {
  width: 100%;
  height: 72rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}
.textarea {
  width: 100%;
  height: 160rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  padding: 16rpx 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}
.picker {
  height: 72rpx;
  border: 2rpx solid #e0e0e0;
  border-radius: 8rpx;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  background: #fff;
}
.picker-value {
  font-size: 28rpx;
  color: #333;
}
.picker-placeholder {
  font-size: 28rpx;
  color: #ccc;
}
.sub-item {
  background: #f9f9f9;
  border-radius: 12rpx;
  padding: 16rpx;
  margin-bottom: 12rpx;
}
.sub-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}
.sub-row:last-child {
  margin-bottom: 0;
}
.picker-sm {
  height: 60rpx;
  border-color: #ddd;
  padding: 0 12rpx;
  min-width: 100rpx;
}
.picker-sm text {
  font-size: 24rpx;
}
.input-sm {
  height: 60rpx;
  font-size: 24rpx;
  flex: 1;
  min-width: 80rpx;
  width: auto;
}
.currency-label {
  font-size: 24rpx;
  color: #999;
  flex-shrink: 0;
}
.time-sep {
  font-size: 24rpx;
  color: #999;
}
.remove-btn {
  font-size: 24rpx;
  color: #ff3b30;
  padding: 8rpx;
  flex-shrink: 0;
}
.empty-hint {
  text-align: center;
  padding: 20rpx 0;
  color: #ccc;
  font-size: 24rpx;
}
.form-actions {
  padding: 20rpx 0 60rpx;
}
.save-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #007aff;
  color: #fff;
  border-radius: 12rpx;
  font-size: 32rpx;
  text-align: center;
}
.save-btn[disabled] {
  opacity: 0.6;
}
.save-btn::after {
  border: none;
}
.location-btn {
  height: 60rpx;
  line-height: 60rpx;
  font-size: 24rpx;
  background: #007aff;
  color: #fff;
  border-radius: 8rpx;
  padding: 0 20rpx;
}
.location-coords {
  font-size: 22rpx;
  color: #999;
  margin-left: 12rpx;
}
.preview-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
  margin-top: 20rpx;
}
</style>
