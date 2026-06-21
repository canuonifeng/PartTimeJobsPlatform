<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createLocation, updateLocation } from '@/api/locations'
import regions from '@/assets/regions.json'

const isEdit = ref(false)
const saving = ref(false)
const editId = ref(null)

const form = ref({
  name: '',
  province: '',
  city: '',
  district: '',
  address: '',
  latitude: null,
  longitude: null
})

onLoad((params) => {
  if (params?.id) {
    isEdit.value = true
    editId.value = params.id
    try {
      const data = JSON.parse(decodeURIComponent(params.data || '{}'))
      form.value = {
        name: data.name || '',
        province: data.province || '',
        city: data.city || '',
        district: data.district || '',
        address: data.address || '',
        latitude: data.latitude,
        longitude: data.longitude
      }
    } catch {}
  }
})

function parseAddress(addr) {
  const result = { province: '', city: '', district: '' }
  if (!addr) return result
  for (const p of regions) {
    if (addr.startsWith(p.label) || addr.startsWith(p.value)) {
      result.province = p.label
      const rest = addr.replace(p.label, '').replace(p.value, '')
      for (const c of (p.children || [])) {
        if (rest.startsWith(c.label) || rest.startsWith(c.value)) {
          result.city = c.label
          const rest2 = rest.replace(c.label, '').replace(c.value, '')
          for (const d of (c.children || [])) {
            if (rest2.startsWith(d.label) || rest2.startsWith(d.value)) {
              result.district = d.label
              break
            }
          }
          break
        }
      }
      break
    }
  }
  return result
}

function chooseLocation() {
  uni.chooseLocation({
    latitude: form.value.latitude || undefined,
    longitude: form.value.longitude || undefined,
    success: (res) => {
      form.value.latitude = res.latitude
      form.value.longitude = res.longitude
      form.value.address = res.address || form.value.address
      form.value.name = res.name || form.value.name
      const parsed = parseAddress(res.address || '')
      if (parsed.province) form.value.province = parsed.province
      if (parsed.city) form.value.city = parsed.city
      if (parsed.district) form.value.district = parsed.district
    },
    fail: (err) => {
      if (err?.errMsg?.includes('cancel')) return
      uni.showToast({ title: err?.errMsg?.includes('deny') ? '请授权位置权限' : '定位失败', icon: 'none' })
    }
  })
}

async function handleSave() {
  if (!form.value.name) {
    uni.showToast({ title: '请输入地点名称', icon: 'none' })
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateLocation({ id: editId.value, ...form.value })
    } else {
      await createLocation(form.value)
    }
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch {
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <view class="page e-page">
    <scroll-view scroll-y class="form-scroll e-content">
      <view class="form-section e-form-section">
        <text class="section-title">基本信息</text>

        <view class="form-item e-form-row">
          <text class="label e-form-label">地点名称 *</text>
          <input v-model="form.name" class="input e-input" placeholder="例：xx大厦、xx咖啡厅" />
        </view>

        <view class="form-item e-form-row">
          <text class="label e-form-label">省/市/区</text>
          <view class="region-row">
            <input v-model="form.province" class="input e-input region-input" placeholder="省" />
            <input v-model="form.city" class="input e-input region-input" placeholder="市" />
            <input v-model="form.district" class="input e-input region-input" placeholder="区" />
          </view>
        </view>

        <view class="form-item e-form-row">
          <text class="label e-form-label">详细地址</text>
          <input v-model="form.address" class="input e-input" placeholder="街道、门牌号" />
        </view>

        <view class="form-item e-form-row">
          <text class="label e-form-label">坐标定位</text>
          <button class="location-btn" @click="chooseLocation">从地图选择</button>
          <text v-if="form.latitude" class="coords">已选坐标：{{ form.latitude }}, {{ form.longitude }}</text>
        </view>
      </view>

      <view class="form-actions e-bottom-safe">
        <button class="save-btn btn-primary submit-btn" :disabled="saving" @click="handleSave">
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<style>
.page { min-height: 100vh; background: #f5f5f5; }
.form-scroll { padding: 24rpx 32rpx; }
.form-section { background: #fff; border-radius: 16rpx; padding: 28rpx; margin-bottom: 20rpx; }
.section-title { font-size: 28rpx; font-weight: 500; color: #333; margin-bottom: 20rpx; display: block; }
.form-item { margin-bottom: 24rpx; }
.label { display: block; font-size: 26rpx; color: #666; margin-bottom: 8rpx; }
.input { width: 100%; height: 72rpx; border: 2rpx solid #e0e0e0; border-radius: 8rpx; padding: 0 20rpx; font-size: 28rpx; box-sizing: border-box; }
.region-row { display: flex; margin: 0 -6rpx; }
.region-input { flex: 1; margin: 0 6rpx; }
.location-btn { height: 60rpx; line-height: 60rpx; font-size: 24rpx; background: #007aff; color: #fff; border-radius: 8rpx; padding: 0 20rpx; }
.location-btn::after { border: none; }
.coords { font-size: 22rpx; color: #999; margin-left: 12rpx; }
.form-actions { padding: 20rpx 0 60rpx; }
.save-btn { width: 100%; height: 88rpx; line-height: 88rpx; background: #007aff; color: #fff; border-radius: 12rpx; font-size: 32rpx; text-align: center; }
.save-btn::after { border: none; }
.save-btn[disabled] { opacity: 0.6; }
</style>
