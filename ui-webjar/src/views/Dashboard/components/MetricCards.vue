<template>
  <div class="stats-row" v-if="hasMqtt">
    <div class="metric-card">
      <div class="metric-icon metric-icon-blue">
        <el-icon :size="28"><Connection /></el-icon>
      </div>
      <div class="metric-body">
        <div class="metric-value">{{ mqttInfo.currentConnections || 0 }}</div>
        <div class="metric-label">当前连接</div>
      </div>
    </div>
    <div class="metric-card">
      <div class="metric-icon metric-icon-orange">
        <el-icon :size="28"><ChatDotRound /></el-icon>
      </div>
      <div class="metric-body">
        <div class="metric-value">{{ mqttInfo.messagesPerSecond || 0 }}<span class="unit">/s</span></div>
        <div class="metric-label">消息速率</div>
      </div>
    </div>
    <div class="metric-card">
      <div class="metric-icon metric-icon-green">
        <el-icon :size="28"><Document /></el-icon>
      </div>
      <div class="metric-body">
        <div class="metric-value">{{ mqttInfo.totalTopics || 0 }}</div>
        <div class="metric-label">主题数</div>
      </div>
    </div>
    <div class="metric-card">
      <div class="metric-icon metric-icon-red">
        <el-icon :size="28"><Bell /></el-icon>
      </div>
      <div class="metric-body">
        <div class="metric-value">{{ mqttInfo.totalSubscriptions || 0 }}</div>
        <div class="metric-label">订阅数</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Connection, ChatDotRound, Document, Bell } from '@element-plus/icons-vue'

defineProps({
  mqttInfo: {
    type: Object,
    default: () => ({})
  },
  hasMqtt: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.metric-card {
  background: var(--bg-card);
  border-radius: 10px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: var(--shadow-sm);
  min-width: 0;
}

.metric-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.metric-icon-blue {
  background: rgba(64, 158, 255, 0.12);
  color: #409eff;
}

.metric-icon-orange {
  background: rgba(230, 162, 60, 0.12);
  color: #e6a23c;
}

.metric-icon-green {
  background: rgba(103, 194, 58, 0.12);
  color: #67c23a;
}

.metric-icon-red {
  background: rgba(245, 108, 108, 0.12);
  color: #f56c6c;
}

.metric-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
  white-space: nowrap;
}

.metric-value .unit {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-placeholder);
}

.metric-label {
  font-size: 12px;
  color: var(--text-placeholder);
  margin-top: 2px;
}

@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  .metric-card {
    padding: 12px;
    gap: 10px;
  }
  .metric-icon {
    width: 38px;
    height: 38px;
  }
  .metric-value {
    font-size: 20px;
  }
}
</style>