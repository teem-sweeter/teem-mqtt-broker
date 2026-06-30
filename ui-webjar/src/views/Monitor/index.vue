<template>
  <div class="mqtt-monitor">
    <div class="stats-card">
      <div class="card-header">
        <span>MQTT Broker 监控</span>
        <el-tag :type="wsConnected ? 'success' : 'danger'" size="small">
          {{ wsConnected ? 'WS已连接' : 'WS未连接' }}
        </el-tag>
      </div>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">{{ stats.currentConnections || 0 }}</div>
          <div class="stat-label">当前连接</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalConnections || 0 }}</div>
          <div class="stat-label">总连接数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalMessagesReceived || 0 }}</div>
          <div class="stat-label">总消息数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.messagesPerSecond || 0 }}/s</div>
          <div class="stat-label">消息速率</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalSubscriptions || 0 }}</div>
          <div class="stat-label">订阅数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalTopics || 0 }}</div>
          <div class="stat-label">主题数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ formatUptime(stats.uptime) }}</div>
          <div class="stat-label">运行时间</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ health.status || 'checking...' }}</div>
          <div class="stat-label">健康状态</div>
        </div>
      </div>
    </div>

    <div class="monitor-body">
      <div class="table-row">
        <el-card class="table-card">
          <template #header>
            <span>已连接客户端</span>
          </template>
          <el-table :data="clients" style="width: 100%" max-height="360" @row-click="showClientDetail">
            <el-table-column prop="clientId" label="客户端ID" show-overflow-tooltip />
            <el-table-column prop="username" label="用户名" show-overflow-tooltip />
            <el-table-column prop="ipAddress" label="IP地址" width="130" />
            <el-table-column label="订阅数" width="70" align="center">
              <template #default="{ row }">
                {{ row.subscriptions ? row.subscriptions.length : 0 }}
              </template>
            </el-table-column>
            <el-table-column label="连接时间" width="160">
              <template #default="{ row }">
                {{ formatTime(row.connectTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="table-card">
          <template #header>
            <span>主题统计</span>
          </template>
          <el-table :data="topicList" style="width: 100%" max-height="360">
            <el-table-column prop="topic" label="主题" show-overflow-tooltip />
            <el-table-column prop="count" label="消息数" width="90" align="right" />
          </el-table>
        </el-card>
      </div>

      <el-card class="message-card">
        <template #header>
          <div class="card-header">
            <span>最近消息</span>
            <div class="header-actions">
              <el-input v-model="messageFilter.topic" placeholder="按主题过滤" style="width: 140px;" size="small" />
              <el-select v-model="messageFilter.qos" placeholder="QoS" style="width: 90px;" size="small" clearable>
                <el-option :value="0" label="QoS 0" />
                <el-option :value="1" label="QoS 1" />
                <el-option :value="2" label="QoS 2" />
              </el-select>
              <el-button size="small" @click="togglePause">
                {{ paused ? '继续' : '暂停' }}
              </el-button>
            </div>
          </div>
        </template>
        <el-table :data="filteredMessages" style="width: 100%" max-height="360">
          <el-table-column prop="timestamp" label="时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.timestamp) }}
            </template>
          </el-table-column>
          <el-table-column prop="topic" label="主题" show-overflow-tooltip />
          <el-table-column prop="clientId" label="客户端ID" show-overflow-tooltip />
          <el-table-column prop="qos" label="QoS" width="60" align="center" />
          <el-table-column prop="direction" label="方向" width="60" align="center" />
          <el-table-column prop="payload" label="内容" show-overflow-tooltip />
        </el-table>
      </el-card>
    </div>

    <!-- 客户端详情对话框 -->
    <el-dialog v-model="clientDetailVisible" title="客户端详情" width="500px" align-center>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="客户端ID">{{ selectedClient.clientId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ selectedClient.username }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ selectedClient.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="端口">{{ selectedClient.port }}</el-descriptions-item>
        <el-descriptions-item label="KeepAlive">{{ selectedClient.keepAlive }}s</el-descriptions-item>
        <el-descriptions-item label="连接时间">{{ formatTime(selectedClient.connectTime) }}</el-descriptions-item>
        <el-descriptions-item label="订阅列表">
          <el-tag v-for="topic in selectedClient.subscriptions" :key="topic" style="margin: 2px;">
            {{ topic }}
          </el-tag>
          <span v-if="!selectedClient.subscriptions || selectedClient.subscriptions.length === 0">无订阅</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getMqttStats,
  getMqttClients,
  getMqttMessages,
  getMqttTopics,
  getHealth
} from '@/api/mqtt'

const stats = ref({})
const clients = ref([])
const messages = ref([])
const topics = ref({})
const health = ref({})
const wsConnected = ref(false)
const paused = ref(false)
const clientDetailVisible = ref(false)
const selectedClient = ref({})

const messageFilter = ref({
  topic: '',
  qos: null
})


let ws = null
let refreshTimer = null
let reconnectAttempts = 0
const maxReconnectDelay = 30000

const topicList = computed(() => {
  return Object.entries(topics.value).map(([topic, count]) => ({
    topic,
    count
  })).sort((a, b) => b.count - a.count)
})

const filteredMessages = computed(() => {
  let result = messages.value
  if (messageFilter.value.topic) {
    result = result.filter(m => m.topic.includes(messageFilter.value.topic))
  }
  if (messageFilter.value.qos !== null && messageFilter.value.qos !== '') {
    result = result.filter(m => m.qos === messageFilter.value.qos)
  }
  return result
})

function formatUptime(ms) {
  if (!ms) return '0s'
  const seconds = Math.floor(ms / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (days > 0) return `${days}d ${hours % 24}h`
  if (hours > 0) return `${hours}h ${minutes % 60}m`
  if (minutes > 0) return `${minutes}m ${seconds % 60}s`
  return `${seconds}s`
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleString()
}

async function refreshAll() {
  await Promise.all([
    refreshStats(),
    refreshClients(),
    refreshMessages(),
    refreshTopics(),
    refreshHealth()
  ])
}

async function refreshStats() {
  try {
    stats.value = await getMqttStats()
  } catch (error) {
    console.error('获取MQTT统计失败:', error)
  }
}

async function refreshClients() {
  try {
    clients.value = await getMqttClients()
  } catch (error) {
    console.error('获取客户端列表失败:', error)
  }
}

async function refreshMessages() {
  if (paused.value) return
  try {
    messages.value = await getMqttMessages(100)
  } catch (error) {
    console.error('获取消息列表失败:', error)
  }
}

async function refreshTopics() {
  try {
    topics.value = await getMqttTopics()
  } catch (error) {
    console.error('获取主题统计失败:', error)
  }
}

async function refreshHealth() {
  try {
    health.value = await getHealth()
  } catch (error) {
    console.error('获取健康状态失败:', error)
  }
}

function showClientDetail(row) {
  selectedClient.value = row
  clientDetailVisible.value = true
}

function togglePause() {
  paused.value = !paused.value
  if (!paused.value) {
    refreshMessages()
  }
}

function connectWebSocket() {
  if (ws && ws.readyState === WebSocket.OPEN) return

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const token = sessionStorage.token
  const wsUrl = `${protocol}//${window.location.host}/ws/monitor${token ? '?token=' + token : ''}`

  ws = new WebSocket(wsUrl)

  ws.onopen = () => {
    wsConnected.value = true
    reconnectAttempts = 0
    console.log('MQTT监控WebSocket已连接')
  }

  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      handleWebSocketMessage(data)
    } catch (error) {
      console.error('解析WebSocket消息失败:', error)
    }
  }

  ws.onclose = () => {
    wsConnected.value = false
    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), maxReconnectDelay)
    reconnectAttempts++
    console.log(`MQTT监控WebSocket已断开，${delay / 1000}秒后重连...`)
    setTimeout(connectWebSocket, delay)
  }

  ws.onerror = (error) => {
    console.error('WebSocket错误:', error)
  }
}

function handleWebSocketMessage(data) {
  switch (data.type) {
    case 'STATS_UPDATE':
      stats.value = data.data
      break
    case 'CLIENT_CONNECTED':
    case 'CLIENT_DISCONNECTED':
      refreshClients()
      break
    case 'MESSAGE_RECEIVED':
      if (!paused.value) {
        messages.value.unshift(data.data)
        if (messages.value.length > 100) {
          messages.value.pop()
        }
      }
      break
    case 'TOPIC_SUBSCRIBED':
    case 'TOPIC_UNSUBSCRIBED':
      refreshTopics()
      break
    case 'STATS':
      stats.value = data.data
      break
    case 'CLIENTS':
      clients.value = data.data
      break
    case 'MESSAGES':
      if (!paused.value) {
        messages.value = data.data
      }
      break
    case 'PONG':
      break
    case 'ERROR':
      ElMessage.error(data.message || 'WebSocket错误')
      break
    default:
      console.log('未知消息类型:', data.type)
  }
}

onMounted(() => {
  refreshAll()
  connectWebSocket()
  refreshTimer = setInterval(refreshAll, 30000)
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped>
.mqtt-monitor {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
  overflow: hidden;
  box-sizing: border-box;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 统计卡片 */
.stats-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 10px 14px;
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.25s ease, border-color 0.25s ease;
  flex-shrink: 0;
}

.stats-card:hover {
  box-shadow: var(--shadow-card-hover) !important;
  border-color: var(--color-primary) !important;
}

/* 表格卡片统一样式 */
.table-card,
.message-card {
  min-width: 0;
}

.table-card :deep(.el-card),
.message-card :deep(.el-card) {
  background: var(--bg-card) !important;
  border: 1px solid var(--border-light) !important;
  border-radius: 12px !important;
  box-shadow: var(--shadow-sm) !important;
  transition: box-shadow 0.25s ease, border-color 0.25s ease;
  --el-card-border-color: var(--border-light);
  --el-card-border-radius: 12px;
}

.table-card :deep(.el-card__header),
.message-card :deep(.el-card__header) {
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
  border-radius: 12px 12px 0 0;
  padding: 10px 14px;
}

.table-card :deep(.el-card__body),
.message-card :deep(.el-card__body) {
  padding: 10px 14px;
}

.table-card:hover :deep(.el-card),
.message-card:hover :deep(.el-card) {
  box-shadow: var(--shadow-card-hover) !important;
  border-color: var(--color-primary) !important;
  --el-card-border-color: var(--color-primary);
}

.monitor-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  margin-top: 8px;
}

.stat-item {
  text-align: center;
  padding: 6px 0;
  border-radius: 8px;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 11px;
  color: var(--text-placeholder);
  margin-top: 2px;
}

/* 表格区域 */
.table-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.table-card {
  min-width: 0;
}

.message-card {
  min-width: 0;
}

/* 表格样式 */
.table-card :deep(.el-table),
.message-card :deep(.el-table) {
  --el-table-border-color: var(--border-light);
  --el-table-header-bg-color: var(--bg-input);
  --el-table-tr-bg-color: var(--bg-card);
  --el-table-row-hover-bg-color: var(--bg-hover);
  --el-table-current-row-bg-color: var(--table-current-row-bg);
  background: var(--bg-card);
  color: var(--text-primary);
}

.table-card :deep(.el-table th),
.message-card :deep(.el-table th) {
  background: var(--bg-input);
  color: var(--text-secondary);
  font-weight: 600;
  border-bottom: 1px solid var(--border-light);
}

.table-card :deep(.el-table td),
.message-card :deep(.el-table td) {
  border-bottom: 1px solid var(--border-light);
  color: var(--text-primary);
}

.table-card :deep(.el-table__row:hover > td),
.message-card :deep(.el-table__row:hover > td) {
  background: var(--bg-hover) !important;
}

.table-card :deep(.el-table__row.current-row > td),
.message-card :deep(.el-table__row.current-row > td) {
  background: var(--table-current-row-bg) !important;
}

/* 响应式 */
@media (max-width: 1400px) {
  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 900px) {
  .mqtt-monitor {
    padding: 8px;
    gap: 8px;
  }
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 2px;
  }
  .stat-value {
    font-size: 16px;
  }
  .table-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .stat-value {
    font-size: 14px;
  }
  .header-actions {
    flex-wrap: wrap;
  }
}
</style>
