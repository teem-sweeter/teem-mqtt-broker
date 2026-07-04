<template>
  <div class="live-log-container">
    <div class="log-card">
      <div class="card-header">
        <span>{{ t('liveLog.title') }}</span>
        <div class="header-actions" style="display: flex; align-items: center; gap: 10px;">
          <div class="log-level-settings" style="display: inline-flex; align-items: center; gap: 8px; margin-right: 16px;">
            <span style="font-size: 13px; color: var(--el-text-color-secondary);">{{ t('liveLog.loggerName') }}:</span>
            <el-select v-model="selectedLogger" size="small" style="width: 140px;" @change="handleLoggerChange">
              <el-option value="ROOT" label="Global (ROOT)" />
              <el-option value="com.jjc.mqtt" label="MQTT Broker" />
              <el-option value="com.jjc.mqtt.admin" label="Admin Web" />
            </el-select>

            <span style="font-size: 13px; color: var(--el-text-color-secondary); margin-left: 8px;">{{ t('liveLog.level') }}:</span>
            <el-select v-model="currentLevel" size="small" style="width: 100px;" @change="handleLevelChange">
              <el-option value="DEBUG" label="DEBUG" />
              <el-option value="INFO" label="INFO" />
              <el-option value="WARN" label="WARN" />
              <el-option value="ERROR" label="ERROR" />
            </el-select>
          </div>
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
import { getLogLevels, updateLogLevel } from '@/api/log'

const { t } = useI18n()

const logs = ref([])
const scrollbarRef = ref(null)
const isStreaming = ref(false)

const selectedLogger = ref('ROOT')
const currentLevel = ref('INFO')
const loggerLevelsMap = ref({})

let eventSource = null

onMounted(async () => {
  startStreaming()
  await fetchLogLevels()
})

const fetchLogLevels = async () => {
  try {
    const data = await getLogLevels()
    loggerLevelsMap.value = data || {}
    currentLevel.value = loggerLevelsMap.value[selectedLogger.value] || 'INFO'
  } catch (e) {
    console.error('Failed to fetch log levels', e)
  }
}

const handleLoggerChange = (val) => {
  currentLevel.value = loggerLevelsMap.value[val] || 'INFO'
}

const handleLevelChange = async (val) => {
  try {
    await updateLogLevel(selectedLogger.value, val)
    ElMessage.success(t('liveLog.updateSuccess'))
    loggerLevelsMap.value[selectedLogger.value] = val
  } catch (e) {
    console.error('Failed to update log level', e)
    ElMessage.error(t('liveLog.updateFailed'))
    currentLevel.value = loggerLevelsMap.value[selectedLogger.value] || 'INFO'
  }
}

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

let flushTimer = null
let buffer = []

const closeStream = () => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
  if (flushTimer) {
    clearInterval(flushTimer)
    flushTimer = null
  }
  buffer = []
}

const startStreaming = () => {
  isStreaming.value = true
  closeStream()

  const token = sessionStorage.token || ''
  eventSource = new EventSource(`/v1/logs/sse?token=${encodeURIComponent(token)}`)

  buffer = []
  flushTimer = setInterval(() => {
    if (buffer.length > 0) {
      const maxLogs = 300
      const combined = [...logs.value, ...buffer]
      if (combined.length > maxLogs) {
        logs.value = combined.slice(combined.length - maxLogs)
      } else {
        logs.value = combined
      }
      buffer = []
      scrollToBottom()
    }
  }, 200)

  eventSource.onmessage = (event) => {
    buffer.push(event.data)
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
