<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="640px"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-descriptions :column="column" border>
      <el-descriptions-item
        v-for="item in items"
        :key="item.prop"
        :label="item.label"
        :span="item.span || 1"
      >
        <slot v-if="$slots[item.slot]" :name="item.slot" :data="data">
          {{ format(item, data) }}
        </slot>
        <template v-else>{{ format(item, data) }}</template>
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button type="primary" @click="$emit('update:modelValue', false)">关 闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '详情' },
  items: { type: Array, default: () => [] },
  data: { type: Object, default: () => ({}) },
  column: { type: Number, default: 2 }
})

function format(item, data) {
  const v = data ? data[item.prop] : ''
  if (v === null || v === undefined || v === '') return '-'
  return item.formatter ? item.formatter(v) : v
}
</script>
