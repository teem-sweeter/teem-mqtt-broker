<template>
  <div class="card" v-if="hasJvmMemory">
    <h3>JVM 内存</h3>
    <div class="info-list">
      <div class="info-item">
        <span class="label">堆内存使用</span>
        <span class="value">{{ formatMemory(jvmInfo.heapMemory) }}</span>
      </div>
      <div class="info-item">
        <span class="label">非堆内存</span>
        <span class="value">{{ formatMemory(jvmInfo.nonHeapMemory) }}</span>
      </div>
    </div>
    <div class="progress-section">
      <div class="progress-label">
        <span>堆内存</span>
        <span>{{ heapUsagePercent }}%</span>
      </div>
      <el-progress :percentage="heapUsagePercent" :stroke-width="10" :color="heapColor" :show-text="false" />
    </div>
    <div class="progress-section">
      <div class="progress-label">
        <span>非堆内存</span>
        <span>{{ nonHeapUsagePercent }}%</span>
      </div>
      <el-progress :percentage="nonHeapUsagePercent" :stroke-width="10" :color="nonHeapColor" :show-text="false" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  jvmInfo: {
    type: Object,
    default: () => ({})
  },
  hasJvmMemory: {
    type: Boolean,
    default: false
  }
})

const heapUsagePercent = computed(() => {
  const heap = props.jvmInfo.heapMemory
  if (!heap || !heap.max || !heap.used) return 0
  return Math.round((heap.used / heap.max) * 100)
})

const nonHeapUsagePercent = computed(() => {
  const nonHeap = props.jvmInfo.nonHeapMemory
  if (!nonHeap || !nonHeap.max || !nonHeap.used || nonHeap.max <= 0) return 0
  return Math.round((nonHeap.used / nonHeap.max) * 100)
})

const heapColor = computed(() => {
  const p = heapUsagePercent.value
  if (p >= 90) return '#f56c6c'
  if (p >= 70) return '#e6a23c'
  return '#409eff'
})

const nonHeapColor = computed(() => {
  const p = nonHeapUsagePercent.value
  if (p >= 90) return '#f56c6c'
  if (p >= 70) return '#e6a23c'
  return '#67c23a'
})

function formatMemory(mem) {
  if (!mem || !mem.used) return '-'
  const usedMB = (mem.used / 1024 / 1024).toFixed(0)
  const maxVal = mem.max > 0 ? mem.max : mem.committed
  const maxMB = maxVal ? (maxVal / 1024 / 1024).toFixed(0) : '?'
  return `${usedMB}MB / ${maxMB}MB`
}
</script>

<style scoped>
.card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 16px;
  box-shadow: var(--shadow-sm);
}

.card h3 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-light);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
}

.info-item .label {
  font-size: 13px;
  color: var(--text-placeholder);
}

.info-item .value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.progress-section {
  margin-top: 10px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-placeholder);
  margin-bottom: 6px;
}
</style>