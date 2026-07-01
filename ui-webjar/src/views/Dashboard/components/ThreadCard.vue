<template>
  <div class="card" v-if="hasThreads">
    <h3>{{ t('dashboard.threadInfo') }}</h3>
    <div class="info-list">
      <div class="info-item">
        <span class="label">{{ t('dashboard.activeThreads') }}</span>
        <span class="value text-blue">{{ jvmInfo.threadCount || 0 }}</span>
      </div>
      <div class="info-item">
        <span class="label">{{ t('dashboard.peakThreads') }}</span>
        <span class="value">{{ jvmInfo.peakThreadCount || 0 }}</span>
      </div>
      <div class="info-item">
        <span class="label">{{ t('dashboard.daemonThreads') }}</span>
        <span class="value">{{ jvmInfo.daemonThreadCount || 0 }}</span>
      </div>
      <div class="info-item">
        <span class="label">{{ t('dashboard.totalThreadsStarted') }}</span>
        <span class="value">{{ jvmInfo.totalStartedThreadCount || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineProps({
  jvmInfo: {
    type: Object,
    default: () => ({})
  },
  hasThreads: {
    type: Boolean,
    default: false
  }
})
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

.text-blue { color: #409eff; }
</style>