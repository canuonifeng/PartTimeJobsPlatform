<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElDialog, ElButton, ElInput, ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  latitude: { type: Number, default: 39.9042 },
  longitude: { type: Number, default: 116.4074 }
})
const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = ref(false)
const keyword = ref('')
const searching = ref(false)
let map = null
let marker = null

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    keyword.value = ''
    nextTick(() => initMap())
  }
})

function initMap() {
  if (!window.AMap) return
  map = new window.AMap.Map('location-map-container', {
    zoom: 14,
    center: [props.longitude, props.latitude]
  })
  marker = new window.AMap.Marker({
    position: [props.longitude, props.latitude],
    draggable: true,
    map: map
  })
  map.on('click', (e) => {
    marker.setPosition(e.lnglat)
  })
}

function handleSearch() {
  if (!keyword.value.trim() || !map) return
  searching.value = true
  try {
    const placeSearch = new window.AMap.PlaceSearch({ city: '', map: map })
    placeSearch.search(keyword.value, (status, result) => {
      searching.value = false
      if (status === 'complete' && result.poiList && result.poiList.pois.length > 0) {
        const poi = result.poiList.pois[0]
        map.setCenter(poi.location)
        map.setZoom(15)
        marker.setPosition(poi.location)
      } else {
        ElMessage.warning('未找到该地址')
      }
    })
  } catch (e) {
    searching.value = false
    ElMessage.error('搜索失败')
  }
  setTimeout(() => { searching.value = false }, 10000)
}

function geocodePosition(lnglat, callback) {
  try {
    const geocoder = new window.AMap.Geocoder({ city: '', radius: 1000 })
    geocoder.getAddress(lnglat, (status, result) => {
      if (status === 'complete' && result.regeocode) {
        const ad = result.regeocode.addressComponent || {}
        callback({
          formattedAddress: result.regeocode.formattedAddress || '',
          name: (result.regeocode.pois && result.regeocode.pois.length > 0) ? result.regeocode.pois[0].name : '',
          province: ad.province || '',
          city: ad.city || ad.province || '',
          district: ad.district || ''
        })
      } else {
        callback({ formattedAddress: '', name: '', province: '', city: '', district: '' })
      }
    })
  } catch (e) {
    callback({ formattedAddress: '', name: '', province: '', city: '', district: '' })
  }
}

function handleConfirm() {
  if (marker) {
    const pos = marker.getPosition()
    const lnglat = [pos.getLng(), pos.getLat()]
    geocodePosition(lnglat, (addr) => {
      emit('confirm', {
        latitude: pos.getLat(),
        longitude: pos.getLng(),
        address: addr.name || addr.formattedAddress,
        name: addr.name,
        province: addr.province,
        city: addr.city,
        district: addr.district
      })
    })
  }
  visible.value = false
  emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog :model-value="visible" title="选择位置" width="600px" @close="emit('update:modelValue', false)">
    <div style="display:flex;gap:8px;margin-bottom:10px">
      <el-input v-model="keyword" placeholder="搜索地址" @keyup.enter="handleSearch" />
      <el-button type="primary" :loading="searching" @click="handleSearch">搜索</el-button>
    </div>
    <div id="location-map-container" style="width:100%;height:400px"></div>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>
