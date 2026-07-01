<template>
  <div class="live-log-container">
    <div class="log-card">
      <div class="card-header">
        <span>{{ t('liveLog.title') }}</span>
        <div class="header-actions">
          <el-button type="success" @click="startStreaming" :disabled="isStreaming">{{ t('liveLog.start') }}</el-button>
          <el-button type="warning" @click="stopStreaming" :disabled="!isStreaming">{{ t('liveLog.stop') }}</el-button>
          <el-button type="danger" @click="clearLogs">{{ t('liveLog.clearLog') }}</el-button>
        </div>
      </div>

      <div class="log-content">
        <el-scrollbar ref="scrollbarRef" class="log-scrollbar">
          <div class="log-lines real-time-mode">
            <div
              v-for="(line, index) in logs"
              :key="index"
              class="log-line"
              :class="getLogLevelClass(line)"
            >
              <pre>{{ line }}</pre>
            </div>
            <div v-if="logs.length === 0" class="empty-placeholder">
              <div class="placeholder-text">{{ t('liveLog.noLogs') }}</div>
            </div>
          </div>
        </el-scrollbar>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const logs = ref([])
const scrollbarRef = ref(null)
const isStreaming = ref(false)

let eventSource = null

onMounted(() => {
  startStreaming()
})

const getLogLevelClass = (line) => {
  if (line.includes('ERROR') || line.includes('error')) {
    return 'log-error'
  } else if (line.includes('WARN') || line.includes('warn')) {
    return 'log-warn'
  } else if (line.includes('INFO') || line.includes('info')) {
    return 'log-info'
  } else if (line.includes('DEBUG') || line.includes('debug')) {
    return 'log-debug'
  }
  return ''
}

const scrollToBottom = () => {
  nextTick(() => {
    if (scrollbarRef.value) {
      const wrap = scrollbarRef.value.wrapRef
      if (wrap) {
        wrap.scrollTop = wrap.scrollHeight
      }
    }
  })
}

const closeStream = () => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}

const startStreaming = () => {
  isStreaming.value = true
  closeStream()

  const token = sessionStorage.token || ''
  eventSource = new EventSource(`/v1/logs/sse?token=${encodeURIComponent(token)}`)

  eventSource.onmessage = (event) => {
    logs.value.push(event.data)
    if (logs.value.length > 1000) {
      logs.value.shift()
    }
    scrollToBottom()
  }

  eventSource.onerror = (error) => {
    ElMessage.error(t('liveLog.sseError'))
    console.error('SSE error:', error)
    isStreaming.value = false
    closeStream()
  }
}

const stopStreaming = () => {
  isStreaming.value = false
  closeStream()
  ElMessage.info(t('liveLog.stopped'))
}

const clearLogs = () => {
  logs.value = []
  ElMessage.info(t('liveLog.cleared'))
}

onUnmounted(() => {
  isStreaming.value = false
  closeStream()
})
</script>

<style scoped>
.live-log-container {
  padding: 20px;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.log-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 4px;
  background-color: var(--bg-card);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  padding: 18px 20px;
  box-sizing: border-box;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.log-content {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 10px;
  border-radius: 5px;
}

.log-scrollbar {
  flex: 1;
  min-height: 0;
}

.log-scrollbar :deep(.el-scrollbar__wrap) {
  overflow-x: hidden;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.log-scrollbar :deep(.el-scrollbar__view) {
  flex: 1;
}

.log-lines {
  padding: 5px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.4;
  flex: 1;
  box-sizing: border-box;
}

.log-lines.real-time-mode {
  background-color: #000;
  border-radius: 5px;
  color: #0f0;
  min-height: 100%;
  position: relative;
}

.real-time-mode .log-error {
  color: #ff6b6b;
  background-color: #3a0000;
}

.real-time-mode .log-warn {
  color: #ffd166;
  background-color: #3a2a00;
}

.real-time-mode .log-info {
  color: #999999;
}

.real-time-mode .log-debug {
  color: #66d9ef;
  background-color: #002b36;
}

.empty-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1;
}

.placeholder-text {
  color: var(--text-muted);
  font-size: 14px;
  text-align: center;
}
</style>
