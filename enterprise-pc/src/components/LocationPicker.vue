<script setup>
import { ref, watch, nextTick } from 'vue'
import { ElDialog, ElButton } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  latitude: { type: Number, default: 39.9042 },
  longitude: { type: Number, default: 116.4074 }
})
const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = ref(false)
let map = null
let marker = null

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
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

function handleConfirm() {
  if (marker) {
    const pos = marker.getPosition()
    emit('confirm', {
      latitude: pos.lat,
      longitude: pos.lng,
      address: ''
    })
  }
  visible.value = false
  emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog :model-value="visible" title="选择位置" width="600px" @close="emit('update:modelValue', false)">
    <div id="location-map-container" style="width:100%;height:400px"></div>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>
