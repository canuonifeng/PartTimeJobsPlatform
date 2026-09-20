<template>
  <div class="pro-table">
    <div v-if="$slots.filter" class="pro-table__filter">
      <slot name="filter"></slot>
    </div>
    <div class="pro-table__toolbar">
      <slot name="toolbar"></slot>
    </div>
    <el-table
      ref="tableRef"
      :data="data"
      v-loading="loading"
      stripe
      border
      @selection-change="onSelectionChange"
    >
      <el-table-column v-if="selection" type="selection" width="48" />
      <el-table-column
        v-for="col in columns"
        :key="col.prop"
        :prop="col.prop"
        :label="col.label"
        :width="col.width"
        :min-width="col.minWidth"
        :show-overflow-tooltip="col.tooltip !== false"
        :align="col.align || 'left'"
      >
        <template v-if="$slots[col.slot]" #default="scope">
          <slot :name="col.slot" :row="scope.row" :$index="scope.$index" />
        </template>
      </el-table-column>
    </el-table>
    <div class="pro-table__pagination">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :current-page="page"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  columns: { type: Array, default: () => [] },
  data: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  selection: { type: Boolean, default: false }
})

const emit = defineEmits(['page-change', 'size-change', 'selection-change'])

const tableRef = ref(null)
const selectedRows = ref([])

function onSelectionChange(rows) {
  selectedRows.value = rows
  emit('selection-change', rows)
}

function onPageChange(p) {
  emit('page-change', p)
}

function onSizeChange(s) {
  emit('size-change', s)
}

defineExpose({
  clearSelection: () => tableRef.value && tableRef.value.clearSelection(),
  getSelection: () => selectedRows.value
})
</script>

<style scoped>
.pro-table__filter { margin-bottom: 12px; }
.pro-table__toolbar { margin-bottom: 12px; display: flex; gap: 8px; }
.pro-table__pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
