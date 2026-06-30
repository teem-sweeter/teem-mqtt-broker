<template>
  <div class="card" v-if="hasSystem">
    <h3>系统信息</h3>
    <div class="info-list">
      <div class="info-item" v-if="systemInfo.name">
        <span class="label">操作系统</span>
        <span class="value">{{ systemInfo.name }} {{ systemInfo.version || '' }}</span>
      </div>
      <div class="info-item" v-if="systemInfo.arch">
        <span class="label">架构</span>
        <span class="value">{{ systemInfo.arch }}</span>
      </div>
      <div class="info-item" v-if="systemInfo.availableProcessors">
        <span class="label">CPU 核心</span>
        <span class="value">{{ systemInfo.availableProcessors }}</span>
      </div>
      <div class="info-item" v-if="systemInfo.processCpuLoad != null && systemInfo.processCpuLoad >= 0">
        <span class="label">CPU 使用率</span>
        <span class="value" :class="cpuUsageClass">{{ formatPercent(systemInfo.processCpuLoad) }}</span>
      </div>
      <div class="info-item" v-if="systemInfo.systemLoadAverage >= 0">
        <span class="label">系统负载</span>
        <span class="value">{{ systemInfo.systemLoadAverage.toFixed(2) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  systemInfo: {
    type: Object,
    default: () => ({})
  },
  hasSystem: {
    type: Boolean,
    default: false
  }
})

const cpuUsageClass = computed(() => {
  const v = props.systemInfo.processCpuLoad
  if (v === undefined || v === null || v < 0) return ''
  if (v >= 0.9) return 'text-red'
  if (v >= 0.7) return 'text-orange'
  return 'text-green'
})

function formatPercent(value) {
  if (value === undefined || value === null || value < 0) return '-'
  return (value * 100).toFixed(1) + '%'
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

.text-green { color: #67c23a; }
.text-red { color: #f56c6c; }
.text-orange { color: #e6a23c; }
</style>