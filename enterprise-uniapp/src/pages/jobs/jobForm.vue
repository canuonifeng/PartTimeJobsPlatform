<script setup>
import { ref, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createJob, updateJob, getJob, getCategories, getJobTags, getRates, getSchedules } from '@/api/jobs'
import { listTemplates } from '@/api/templates'
import { listLocations } from '@/api/locations'
import regions from '@/assets/regions.json'

const isEdit = ref(false)
const jobId = ref('')
const saving = ref(false)
const categories = ref([])
const tagGroups = ref([])
const selectedTagIds = ref([])
const tagLoading = ref(false)
const tagLoadFailed = ref(false)
const responsibilityEditorReady = ref(false)
const requirementEditorReady = ref(false)

const formData = ref({
  title: '',
  description: '',
  requirements: '',
  contactPhone: '',
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

function getResponseList(res) {
  if (Array.isArray(res)) return res
  if (!res || typeof res !== 'object') return []
  if (Array.isArray(res.data)) return res.data
  if (Array.isArray(res.records)) return res.records
  return []
}

function normalizeTagId(id) {
  const numericId = Number(id)
  return Number.isFinite(numericId) ? numericId : null
}

function normalizeTagIds(ids) {
  const source = Array.isArray(ids) ? ids : []
  return [...new Set(source.map(normalizeTagId).filter(id => id !== null))]
}

function getGroupTags(group) {
  return Array.isArray(group?.tags) ? group.tags : []
}

function normalizeTagGroups(groups) {
  return getResponseList(groups).map(group => ({
    ...group,
    tags: getGroupTags(group)
  }))
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

function setEditorContents() {
  nextTick(() => {
    if (responsibilityEditorReady.value) {
      uni.createSelectorQuery()
        .select('#responsibilityEditor')
        .context(res => {
          res?.context?.setContents({ html: formData.value.description || '' })
        })
        .exec()
    }
    if (requirementEditorReady.value) {
      uni.createSelectorQuery()
        .select('#requirementEditor')
        .context(res => {
          res?.context?.setContents({ html: formData.value.requirements || '' })
        })
        .exec()
    }
  })
}

function onResponsibilityEditorReady() {
  responsibilityEditorReady.value = true
  setEditorContents()
}

function onRequirementEditorReady() {
  requirementEditorReady.value = true
  setEditorContents()
}

function onResponsibilityEditorInput(e) {
  formData.value.description = e.detail.html || ''
}

function onRequirementEditorInput(e) {
  formData.value.requirements = e.detail.html || ''
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
  formData.value.requirements = tpl.requirements || ''
  formData.value.categoryId = tpl.categoryId ?? ''
  formData.value.province = tpl.province || ''
  formData.value.city = tpl.city || ''
  formData.value.district = tpl.district || ''
  formData.value.address = tpl.address || ''
  formData.value.latitude = tpl.latitude || null
  formData.value.longitude = tpl.longitude || null
  selectedTemplateName.value = tpl.title || ''
  setEditorContents()
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
  await loadJobTags()
  await openTemplatePicker()
  await openLocationPicker()
  if (isEdit.value) {
    await loadJobDetail()
  }
})

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
    const list = getResponseList(res)
    const flatList = flattenCategories(list)
    categories.value = flatList
    categoryNames.value = flatList.map(c => c.pickerLabel)
  } catch (e) {
    console.error('Failed to load categories', e)
  }
}

async function loadJobTags() {
  tagLoading.value = true
  tagLoadFailed.value = false
  try {
    const res = await getJobTags()
    tagGroups.value = normalizeTagGroups(res)
  } catch (e) {
    tagGroups.value = []
    tagLoadFailed.value = true
    console.error('Failed to load job tags', e)
  } finally {
    tagLoading.value = false
  }
}

async function loadJobDetail() {
  try {
    const job = await getJob(jobId.value) || {}
    formData.value = {
      title: job.title || '',
      description: job.description || '',
      requirements: job.requirements || '',
      contactPhone: job.contactPhone || '',
      location: job.location || '',
      province: job.province || '',
      city: job.city || '',
      district: job.district || '',
      address: job.address || '',
      latitude: job.latitude || null,
      longitude: job.longitude || null,
      categoryId: job.categoryId ?? '',
      headcount: job.headcount || 1,
      deadline: job.deadline || '',
      imageUrl: job.imageUrl || '',
      salaryRates: [{ type: '', amount: '' }],
      schedules: [{ date: '', startTime: '', endTime: '' }]
    }
    selectedTagIds.value = normalizeTagIds(job.tagIds || (Array.isArray(job.tags) ? job.tags.map(tag => getTagId(tag)) : []))
    setEditorContents()
    try {
      const rateRes = await getRates(jobId.value)
      rates.value = Array.isArray(rateRes)
        ? rateRes.map(rate => ({
            id: rate.id,
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
            id: sched.id,
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
    requirements: formData.value.requirements,
    contactPhone: formData.value.contactPhone,
    tagIds: normalizeTagIds(selectedTagIds.value),
    location: formData.value.location,
    province: formData.value.province || null,
    city: formData.value.city || null,
    district: formData.value.district || null,
    address: formData.value.address || null,
    latitude: formData.value.latitude ?? null,
    longitude: formData.value.longitude ?? null,
    categoryId: formData.value.categoryId === '' ? null : formData.value.categoryId,
    headcount: formData.value.headcount,
    deadline: formData.value.deadline ? `${formData.value.deadline} 23:59:59` : null,
    rates: rates.value
      .filter(rate => rate.amount)
      .map(rate => ({
        id: rate.id,
        type: rate.type,
        amount: Number(rate.amount),
        currency: rate.currency || 'CNY'
      })),
    schedules: schedules.value
      .filter(sched => sched.date && sched.startTime && sched.endTime)
      .map(sched => ({
        id: sched.id,
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
    formData.value.categoryId = getCategoryId(cat)
  }
}

function getTagId(tag) {
  return tag?.id ?? tag?.tagId ?? ''
}

function isTagSelected(tag) {
  const id = normalizeTagId(getTagId(tag))
  return id !== null && selectedTagIds.value.includes(id)
}

function toggleTag(tag) {
  const id = normalizeTagId(getTagId(tag))
  if (id === null) return
  const index = selectedTagIds.value.indexOf(id)
  if (index >= 0) {
    selectedTagIds.value.splice(index, 1)
  } else {
    selectedTagIds.value = normalizeTagIds([...selectedTagIds.value, id])
  }
}

function getCategoryIndex() {
  const id = formData.value.categoryId
  return categories.value.findIndex(c => sameCategoryId(getCategoryId(c), id))
}

function getSelectedCategoryName() {
  const index = getCategoryIndex()
  return index >= 0 ? categoryNames.value[index] : ''
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
  <view class="page">
    <view class="header">
      <view class="header-left" @click="uni.navigateBack()">
        <text class="back-arrow">‹</text>
      </view>
      <text class="header-title">{{ isEdit ? '编辑职位' : '发布职位' }}</text>
      <view class="header-right"></view>
    </view>

    <scroll-view scroll-y class="form-scroll">
      <view class="form-section">
        <view class="section-title-row">
          <view class="section-accent"></view>
          <text class="section-title">岗位信息</text>
        </view>

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
              <text v-if="getSelectedCategoryName()" class="picker-value">{{ getSelectedCategoryName() }}</text>
              <text v-else class="picker-placeholder">请选择职位类型</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="label">岗位职责</text>
          <editor
            id="responsibilityEditor"
            class="editor"
            placeholder="请输入岗位职责"
            @ready="onResponsibilityEditorReady"
            @input="onResponsibilityEditorInput"
          />
        </view>

        <view class="form-item">
          <text class="label">任职要求</text>
          <editor
            id="requirementEditor"
            class="editor"
            placeholder="请输入任职要求"
            @ready="onRequirementEditorReady"
            @input="onRequirementEditorInput"
          />
        </view>

        <view class="form-item">
          <text class="label">联系方式</text>
          <input v-model="formData.contactPhone" class="input" type="number" placeholder="请输入联系电话" />
        </view>

        <view class="form-item">
          <text class="label">岗位标签</text>
          <view v-if="tagLoading" class="tag-hint">标签加载中...</view>
          <view v-else-if="tagLoadFailed" class="tag-hint error" @click="loadJobTags">标签加载失败，点击重试</view>
          <view v-else-if="!tagGroups.length" class="tag-hint">暂无可选标签</view>
          <view v-else>
            <view v-for="group in tagGroups" :key="group.id" class="tag-group">
              <text class="tag-group-title">{{ group.name }}</text>
              <view class="tag-list">
                <view
                  v-for="tag in getGroupTags(group)"
                  :key="tag.id"
                  class="tag-chip"
                  :class="{ active: isTagSelected(tag) }"
                  @click="toggleTag(tag)"
                >{{ tag.name }}</view>
              </view>
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="label">职位图片</text>
          <view class="picker-btn" @click="chooseImage">选择图片</view>
          <image v-if="formData.imageUrl" class="preview-image" :src="formData.imageUrl" mode="aspectFill" @click="previewImage" />
        </view>
      </view>

      <view class="form-section">
        <view class="section-title-row">
          <view class="section-accent"></view>
          <text class="section-title">地址信息</text>
        </view>

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
          <view class="picker-btn" @click="chooseLocation">选择位置</view>
          <text v-if="formData.latitude" class="location-coords">{{ formData.latitude }}, {{ formData.longitude }}</text>
        </view>
      </view>

      <view class="form-section">
        <view class="section-title-row">
          <view class="section-accent"></view>
          <text class="section-title">招聘信息</text>
        </view>

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
          <view class="section-title-row">
            <view class="section-accent"></view>
            <text class="section-title">薪资标准</text>
          </view>
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
          <view class="section-title-row">
            <view class="section-accent"></view>
            <text class="section-title">排班时段</text>
          </view>
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
.page {
  min-height: 100vh;
  background: #f6f8f7;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  background: #fff;
  border-bottom: 2rpx solid #eee;
}
.header-left {
  width: 60rpx;
}
.back-arrow {
  font-size: 40rpx;
  color: #333;
  font-weight: 300;
}
.header-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1f2933;
}
.header-right {
  width: 60rpx;
}
.form-scroll {
  padding: 24rpx 32rpx;
}
.form-section {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 12rpx 34rpx rgba(23, 83, 53, 0.08);
}
.section-title-row {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}
.section-accent {
  width: 3rpx;
  height: 28rpx;
  background: #07c160;
  border-radius: 2rpx;
  margin-right: 12rpx;
}
.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2933;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}
.section-header .section-title-row {
  margin-bottom: 0;
}
.add-btn {
  font-size: 26rpx;
  color: #07c160;
  padding: 8rpx 12rpx;
}
.picker-btn {
  width: 100%;
  height: 78rpx;
  line-height: 78rpx;
  background: #fff;
  border: 2rpx solid #07c160;
  border-radius: 14rpx;
  color: #07c160;
  text-align: center;
  font-size: 28rpx;
}
.selected-tip {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #07c160;
}
.form-item {
  margin-bottom: 24rpx;
}
.label {
  display: block;
  font-size: 26rpx;
  color: #64748b;
  margin-bottom: 8rpx;
}
.input {
  width: 100%;
  height: 78rpx;
  border: 2rpx solid #edf0f3;
  border-radius: 14rpx;
  background: #fafafa;
  padding: 0 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}
.editor {
  width: 100%;
  min-height: 220rpx;
  border: 2rpx solid #edf0f3;
  border-radius: 14rpx;
  padding: 16rpx 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  background: #fafafa;
}
.tag-hint {
  padding: 18rpx 0;
  font-size: 24rpx;
  color: #999;
}
.tag-hint.error {
  color: #ff3b30;
}
.tag-group {
  margin-top: 16rpx;
}
.tag-group-title {
  display: block;
  font-size: 24rpx;
  color: #64748b;
  margin-bottom: 12rpx;
}
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}
.tag-chip {
  padding: 10rpx 20rpx;
  border: 2rpx solid #edf0f3;
  border-radius: 999rpx;
  font-size: 24rpx;
  color: #64748b;
  background: #fff;
}
.tag-chip.active {
  border-color: #07c160;
  color: #07c160;
  background: #eafaf1;
}
.picker {
  height: 78rpx;
  border: 2rpx solid #edf0f3;
  border-radius: 14rpx;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  background: #fafafa;
}
.picker-value {
  font-size: 28rpx;
  color: #1f2933;
}
.picker-placeholder {
  font-size: 28rpx;
  color: #ccc;
}
.sub-item {
  background: #f6f8f7;
  border-radius: 14rpx;
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
  height: 64rpx;
  border-color: #edf0f3;
  padding: 0 12rpx;
  min-width: 100rpx;
}
.picker-sm text {
  font-size: 24rpx;
}
.input-sm {
  height: 64rpx;
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
  background: linear-gradient(135deg, #18c86b, #08a95a);
  color: #fff;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 700;
  text-align: center;
  border: none;
}
.save-btn[disabled] {
  opacity: 0.6;
}
.save-btn::after {
  border: none;
}
.location-btn {
  height: 64rpx;
  line-height: 64rpx;
  font-size: 24rpx;
  background: #07c160;
  color: #fff;
  border-radius: 14rpx;
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
  border-radius: 14rpx;
  margin-top: 20rpx;
}
</style>
