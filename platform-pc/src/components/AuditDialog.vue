<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="560px"
    @update:model-value="$emit('update:modelValue', $event)"
    @closed="handleClosed"
  >
    <div class="audit-dialog__info">
      <slot></slot>
    </div>
    <el-form label-position="top">
      <el-form-item label="审核意见">
        <el-input
          v-model="reason"
          type="textarea"
          :rows="4"
          placeholder="请输入审核意见"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleCancel">取 消</el-button>
      <el-button @click="emitResult(false)">拒 绝</el-button>
      <el-button type="primary" @click="emitResult(true)">通 过</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '审核' }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const reason = ref('')

watch(() => props.modelValue, (v) => {
  if (v) reason.value = ''
})

function emitResult(passed) {
  emit('confirm', { passed, reason: reason.value })
}

function handleCancel() {
  emit('update:modelValue', false)
}

function handleClosed() {
  reason.value = ''
}
</script>
