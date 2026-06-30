<template>
  <div class="card" v-if="hasMqtt">
    <h3>MQTT Broker</h3>
    <div class="info-list">
      <div class="info-item">
        <span class="label">运行时间</span>
        <span class="value">{{ formatUptime(mqttInfo.uptime) }}</span>
      </div>
      <div class="info-item">
        <span class="label">总连接数</span>
        <span class="value">{{ mqttInfo.totalConnections || 0 }}</span>
      </div>
      <div class="info-item">
        <span class="label">接收消息</span>
        <span class="value text-blue">{{ mqttInfo.totalMessagesReceived || 0 }}</span>
      </div>
      <div class="info-item">
        <span class="label">发送消息</span>
        <span class="value text-green">{{ mqttInfo.totalMessagesSent || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
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

function formatUptime(ms) {
  if (!ms) return '-'
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  if (days > 0) return `${days}天 ${hours % 24}小时`
  if (hours > 0) return `${hours}小时 ${minutes % 60}分钟`
  if (minutes > 0) return `${minutes}分钟 ${seconds % 60}秒`
  return `${seconds}秒`
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

.text-blue { color: #409eff; }
.text-green { color: #67c23a; }
</style>