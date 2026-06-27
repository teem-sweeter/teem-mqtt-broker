<template>
  <div class="card" v-if="hasDisk">
    <h3>磁盘信息</h3>
    <div class="info-list">
      <div class="info-item">
        <span class="label">总空间</span>
        <span class="value">{{ formatBytes(diskInfo.totalSpace) }}</span>
      </div>
      <div class="info-item">
        <span class="label">可用空间</span>
        <span class="value text-green">{{ formatBytes(diskInfo.usableSpace) }}</span>
      </div>
      <div class="info-item">
        <span class="label">已用空间</span>
        <span class="value">{{ formatBytes((diskInfo.totalSpace || 0) - (diskInfo.usableSpace || 0)) }}</span>
      </div>
    </div>
    <div class="progress-section" style="margin-top: 16px;">
      <div class="progress-label">
        <span>磁盘使用</span>
        <span>{{ diskUsagePercent }}%</span>
      </div>
      <el-progress :percentage="diskUsagePercent" :stroke-width="10" :color="diskColor" :show-text="false" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  diskInfo: {
    type: Object,
    default: () => ({})
  },
  hasDisk: {
    type: Boolean,
    default: false
  }
})

const diskUsagePercent = computed(() => {
  const { totalSpace, usableSpace } = props.diskInfo
  if (!totalSpace || !usableSpace) return 0
  return Math.round(((totalSpace - usableSpace) / totalSpace) * 100)
})

const diskColor = computed(() => {
  const p = diskUsagePercent.value
  if (p >= 90) return '#f56c6c'
  if (p >= 80) return '#e6a23c'
  return '#409eff'
})

function formatBytes(bytes) {
  if (!bytes) return '-'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<style scoped>
.card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 24px;
  box-shadow: var(--shadow-sm);
}

.card h3 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
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

.progress-section {
  margin-top: 16px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-placeholder);
  margin-bottom: 8px;
}
</style>