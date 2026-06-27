<template>
  <div class="card" v-if="hasGc">
    <h3>GC 信息</h3>
    <div class="info-list">
      <div v-for="gc in jvmInfo.gc" :key="gc.name" class="gc-item">
        <div class="gc-name">{{ gc.name }}</div>
        <div class="gc-stats">
          <span>次数: <b>{{ gc.collectionCount }}</b></span>
          <span>耗时: <b>{{ formatGcTime(gc.collectionTime) }}</b></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  jvmInfo: {
    type: Object,
    default: () => ({})
  },
  hasGc: {
    type: Boolean,
    default: false
  }
})

function formatGcTime(ms) {
  if (!ms && ms !== 0) return '-'
  if (ms >= 60000) return `${(ms / 60000).toFixed(1)}m`
  if (ms >= 1000) return `${(ms / 1000).toFixed(1)}s`
  return `${ms}ms`
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

.gc-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--border-light);
}

.gc-item:last-child {
  border-bottom: none;
}

.gc-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.gc-stats {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: var(--text-placeholder);
}

.gc-stats b {
  color: var(--text-secondary);
}
</style>