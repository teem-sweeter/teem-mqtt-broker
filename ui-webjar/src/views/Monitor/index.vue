<template>
  <div class="monitor-page">
    <div class="container">
      <section class="stats-grid">
        <div class="stat-card" v-for="stat in statCards" :key="stat.key">
          <div class="stat-icon" :style="{ background: stat.bg, color: stat.color }">
            <el-icon :size="22"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
        </div>
      </section>

      <section class="content-grid">
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="card-header">
              <div class="card-header-left">
                <el-icon :size="16" color="var(--color-primary)"><Monitor /></el-icon>
                <span>{{ t('monitor.connectedClients') }}</span>
                <el-tag size="small" round>{{ clients.length }}</el-tag>
              </div>
            </div>
          </template>
          <el-table
            :data="clients"
            style="width: 100%"
            max-height="320"
            @row-click="showClientDetail"
            class="monitor-table"
            size="small"
          >
            <el-table-column prop="clientId" :label="t('monitor.clientId')" min-width="140" show-overflow-tooltip />
            <el-table-column prop="username" :label="t('monitor.username')" min-width="90" show-overflow-tooltip />
            <el-table-column prop="ipAddress" :label="t('monitor.ipAddress')" width="130" />
            <el-table-column :label="t('dashboard.subscriptionCount')" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small" effect="plain" round>
                  {{ row.subscriptions ? row.subscriptions.length : 0 }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="t('monitor.connectedAt')" width="160">
              <template #default="{ row }">
                <span class="cell-time">{{ formatTime(row.connectTime) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="card-header">
              <div class="card-header-left">
                <el-icon :size="16" color="var(--color-primary)"><DataAnalysis /></el-icon>
                <span>{{ t('monitor.topicStats') }}</span>
                <el-tag size="small" round>{{ topicList.length }}</el-tag>
              </div>
            </div>
          </template>
          <el-table
            :data="topicList"
            style="width: 100%"
            max-height="320"
            class="monitor-table"
            size="small"
          >
            <el-table-column prop="topic" :label="t('monitor.topic')" min-width="180" show-overflow-tooltip />
            <el-table-column prop="count" :label="t('monitor.messageCount')" width="120" align="right">
              <template #default="{ row }">
                <div class="topic-count-bar">
                  <span class="count-value">{{ formatCount(row.count) }}</span>
                  <div class="count-bar-track">
                    <div class="count-bar-fill" :style="{ width: (row.count / maxTopicCount * 100) + '%' }" />
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </section>

      <section class="message-section">
        <el-card shadow="never" class="message-card">
          <template #header>
            <div class="card-header">
              <div class="card-header-left">
                <el-icon :size="16" color="var(--color-primary)"><ChatDotRound /></el-icon>
                <span>{{ t('monitor.recentMessages') }}</span>
                <el-tag size="small" round>{{ filteredMessages.length }}</el-tag>
              </div>
              <div class="card-header-actions">
                <el-input
                  v-model="messageFilter.topic"
                  :placeholder="t('monitor.filterByTopic')"
                  size="small"
                  clearable
                  class="filter-input"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-select v-model="messageFilter.qos" placeholder="QoS" size="small" clearable class="filter-select">
                  <el-option :value="0" label="QoS 0" />
                  <el-option :value="1" label="QoS 1" />
                  <el-option :value="2" label="QoS 2" />
                </el-select>
                <el-button
                  :type="paused ? 'warning' : 'default'"
                  size="small"
                  @click="togglePause"
                  circle
                >
                  <el-icon :size="14">
                    <VideoPause v-if="!paused" />
                    <VideoPlay v-else />
                  </el-icon>
                </el-button>
              </div>
            </div>
          </template>
          <div class="message-list" v-if="filteredMessages.length > 0">
            <TransitionGroup name="msg">
              <div v-for="(msg, idx) in filteredMessages" :key="idx" class="message-row">
                <div class="msg-direction-tag" :class="msg.direction === 'INBOUND' ? 'in' : 'out'">
                  <span>{{ msg.direction === 'INBOUND' ? 'RX' : 'TX' }}</span>
                </div>
                <div class="msg-content">
                  <div class="msg-header">
                    <span class="msg-topic">{{ msg.topic }}</span>
                    <span class="msg-meta">
                      <el-tag size="small" effect="plain" round>QoS {{ msg.qos }}</el-tag>
                      <span class="msg-time">{{ formatTime(msg.timestamp) }}</span>
                    </span>
                  </div>
                  <div class="msg-payload">{{ msg.payload }}</div>
                  <div class="msg-client" v-if="msg.clientId">{{ msg.clientId }}</div>
                </div>
              </div>
            </TransitionGroup>
          </div>
          <el-empty v-else :description="t('monitor.noSubscriptions')" :image-size="60" />
        </el-card>
      </section>

      <section class="delayed-section">
        <el-card shadow="never" class="delayed-card">
          <template #header>
            <div class="card-header">
              <div class="card-header-left">
                <el-icon :size="16" color="var(--color-primary)"><Timer /></el-icon>
                <span>{{ t('monitor.delayedMessages') }}</span>
              </div>
              <div class="card-header-actions">
                <div class="delayed-stats">
                  <el-tag size="small" type="warning" effect="light" round>
                    {{ t('monitor.pending') }}: {{ delayedStats.pending || 0 }}
                  </el-tag>
                  <el-tag size="small" type="success" effect="light" round>
                    {{ t('monitor.delivered') }}: {{ delayedStats.delivered || 0 }}
                  </el-tag>
                  <el-tag size="small" type="info" effect="light" round>
                    {{ t('monitor.cancelled') }}: {{ delayedStats.cancelled || 0 }}
                  </el-tag>
                </div>
                <el-button size="small" text @click="refreshDelayedMessages">
                  <el-icon :size="14"><Refresh /></el-icon>
                </el-button>
              </div>
            </div>
          </template>
          <el-table :data="delayedMessages" style="width: 100%" class="monitor-table" size="small" max-height="280">
            <template #empty>
              <el-empty :description="t('common.noData')" :image-size="50" />
            </template>
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="topic" :label="t('monitor.topic')" min-width="160" show-overflow-tooltip />
            <el-table-column prop="qos" label="QoS" width="60" align="center">
              <template #default="{ row }">
                <el-tag size="small" effect="plain" round>QoS {{ row.qos }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="t('monitor.delay')" width="70" align="center">
              <template #default="{ row }">{{ row.delaySeconds }}s</template>
            </el-table-column>
            <el-table-column :label="t('monitor.scheduledTime')" width="160">
              <template #default="{ row }">
                <span class="cell-time">{{ formatTime(new Date(row.scheduledTime).getTime()) }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="t('common.status')" width="90" align="center">
              <template #default="{ row }">
                <el-tag
                  :type="row.status === 'PENDING' ? 'warning' : row.status === 'DELIVERED' ? 'success' : 'info'"
                  effect="light"
                  size="small"
                  round
                >
                  <span class="status-dot-inline" :class="row.status.toLowerCase()" />
                  {{ t(`monitor.${row.status.toLowerCase()}`) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="t('common.operation')" width="80" align="center" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'PENDING'"
                  type="danger"
                  text
                  size="small"
                  @click="handleCancelDelayed(row.id)"
                >
                  <el-icon><Close /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </section>
    </div>

    <el-dialog v-model="clientDetailVisible" :title="t('monitor.clientDetail')" width="540px" align-center destroy-on-close>
      <div class="detail-body">
        <div class="detail-section">
          <div class="detail-section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ t('client.connectionInfo') }}</span>
          </div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item :label="t('monitor.clientId')">
              <code class="detail-code">{{ selectedClient.clientId }}</code>
            </el-descriptions-item>
            <el-descriptions-item :label="t('monitor.username')">{{ selectedClient.username || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.ipAddress')">{{ selectedClient.ipAddress || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.port')">{{ selectedClient.port || '-' }}</el-descriptions-item>
            <el-descriptions-item label="KeepAlive">{{ selectedClient.keepAlive }}s</el-descriptions-item>
            <el-descriptions-item :label="t('monitor.connectedAt')">{{ formatTime(selectedClient.connectTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-section">
          <div class="detail-section-title">
            <el-icon><List /></el-icon>
            <span>{{ t('monitor.subscriptionList') }}</span>
            <el-tag size="small" round>{{ selectedClient.subscriptions?.length || 0 }}</el-tag>
          </div>
          <div class="subscription-tags" v-if="selectedClient.subscriptions?.length">
            <el-tag
              v-for="topic in selectedClient.subscriptions"
              :key="topic"
              size="small"
              effect="plain"
              class="sub-tag"
            >
              {{ topic }}
            </el-tag>
          </div>
          <span v-else class="no-data-text">{{ t('monitor.noSubscriptions') }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  Refresh, Connection, Monitor, DataAnalysis, ChatDotRound, Timer,
  VideoPlay, VideoPause, Search, Close, InfoFilled, List,
  User, Message, Document, Bell, Rank, Clock, TrendCharts, Link
} from '@element-plus/icons-vue'

const { t } = useI18n()
import {
  getMqttStats,
  getMqttClients,
  getMqttMessages,
  getMqttTopics,
  getHealth
} from '@/api/mqtt'
import { getDelayedMessages, cancelDelayedMessage, getDelayedMessageStats } from '@/api/delayed'

const stats = ref({})
const clients = ref([])
const messages = ref([])
const topics = ref({})
const health = ref({})
const wsConnected = ref(false)
const paused = ref(false)
const clientDetailVisible = ref(false)
const selectedClient = ref({})
const delayedMessages = ref([])
const delayedStats = ref({})
const messageFilter = ref({ topic: '', qos: null })

let ws = null
let refreshTimer = null
let reconnectAttempts = 0
const maxReconnectDelay = 30000

const topicList = computed(() => {
  return Object.entries(topics.value).map(([topic, count]) => ({ topic, count }))
    .sort((a, b) => b.count - a.count)
})

const maxTopicCount = computed(() => {
  return topicList.value.length > 0 ? topicList.value[0].count : 1
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

const statCards = computed(() => [
  { key: 'connections', icon: User, label: t('dashboard.currentConnections'), value: stats.value.currentConnections || 0, bg: 'rgba(43,224,140,0.1)', color: '#2BE08C' },
  { key: 'totalConns', icon: Link, label: t('dashboard.totalConnections'), value: stats.value.totalConnections || 0, bg: 'rgba(64,158,255,0.1)', color: '#409eff' },
  { key: 'totalMessages', icon: Message, label: t('monitor.totalMessages'), value: formatCount(stats.value.totalMessagesReceived || 0), bg: 'rgba(103,194,58,0.1)', color: '#67c23a' },
  { key: 'msgRate', icon: TrendCharts, label: t('dashboard.messageRate'), value: (stats.value.messagesPerSecond || 0) + '/s', bg: 'rgba(230,162,60,0.1)', color: '#e6a23c' },
  { key: 'subscriptions', icon: Bell, label: t('dashboard.subscriptionCount'), value: stats.value.totalSubscriptions || 0, bg: 'rgba(144,147,153,0.1)', color: '#909399' },
  { key: 'topics', icon: Document, label: t('dashboard.topicCount'), value: stats.value.totalTopics || 0, bg: 'rgba(43,224,140,0.1)', color: '#2BE08C' },
  { key: 'uptime', icon: Clock, label: t('dashboard.uptime'), value: formatUptime(stats.value.uptime), bg: 'rgba(64,158,255,0.1)', color: '#409eff' },
  { key: 'health', icon: Rank, label: t('monitor.healthStatus'), value: health.value.status || '-', bg: 'rgba(103,194,58,0.1)', color: '#67c23a' },
])

function formatCount(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

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
  const d = new Date(timestamp)
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

async function refreshAll() {
  await Promise.all([
    refreshStats(),
    refreshClients(),
    refreshMessages(),
    refreshTopics(),
    refreshHealth(),
    refreshDelayedMessages()
  ])
}

async function refreshStats() {
  try { stats.value = await getMqttStats() } catch (e) { console.error(e) }
}
async function refreshClients() {
  try { clients.value = await getMqttClients() } catch (e) { console.error(e) }
}
async function refreshMessages() {
  if (paused.value) return
  try { messages.value = await getMqttMessages(100) } catch (e) { console.error(e) }
}
async function refreshTopics() {
  try { topics.value = await getMqttTopics() } catch (e) { console.error(e) }
}
async function refreshHealth() {
  try { health.value = await getHealth() } catch (e) { console.error(e) }
}
async function refreshDelayedMessages() {
  try {
    const [list, s] = await Promise.all([
      getDelayedMessages('PENDING'),
      getDelayedMessageStats()
    ])
    delayedMessages.value = list
    delayedStats.value = s
  } catch (e) { console.error(e) }
}

async function handleCancelDelayed(id) {
  try {
    const res = await cancelDelayedMessage(id)
    if (res.success) {
      ElMessage.success(t('monitor.delayedCancelSuccess'))
      refreshDelayedMessages()
    } else {
      ElMessage.error(res.message || t('monitor.delayedCancelFailed'))
    }
  } catch (e) {
    ElMessage.error(t('monitor.delayedCancelFailed'))
  }
}

function showClientDetail(row) {
  selectedClient.value = row
  clientDetailVisible.value = true
}

function togglePause() {
  paused.value = !paused.value
  if (!paused.value) refreshMessages()
}

function connectWebSocket() {
  if (ws && ws.readyState === WebSocket.OPEN) return
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const token = sessionStorage.token
  const wsUrl = `${protocol}//${window.location.host}/ws/monitor${token ? '?token=' + token : ''}`
  ws = new WebSocket(wsUrl)
  ws.onopen = () => { wsConnected.value = true; reconnectAttempts = 0 }
  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      switch (data.type) {
        case 'STATS_UPDATE':
        case 'STATS':
          stats.value = data.data; break
        case 'CLIENT_CONNECTED':
        case 'CLIENT_DISCONNECTED':
          refreshClients(); break
        case 'MESSAGE_RECEIVED':
          if (!paused.value) {
            messages.value.unshift(data.data)
            if (messages.value.length > 100) messages.value.pop()
          }
          break
        case 'TOPIC_SUBSCRIBED':
        case 'TOPIC_UNSUBSCRIBED':
          refreshTopics(); break
        case 'CLIENTS':
          clients.value = data.data; break
        case 'MESSAGES':
          if (!paused.value) messages.value = data.data; break
        case 'PONG': break
        case 'ERROR':
          ElMessage.error(data.message || t('monitor.wsError')); break
      }
    } catch (e) { console.error(e) }
  }
  ws.onclose = () => {
    wsConnected.value = false
    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), maxReconnectDelay)
    reconnectAttempts++
    setTimeout(connectWebSocket, delay)
  }
  ws.onerror = () => {}
}

onMounted(() => {
  refreshAll()
  connectWebSocket()
  refreshTimer = setInterval(refreshAll, 30000)
})

onUnmounted(() => {
  if (ws) ws.close()
  if (refreshTimer) { clearInterval(refreshTimer); refreshTimer = null }
})
</script>

<style scoped>
.monitor-page {
  height: 100%;
  padding: 0;
  box-sizing: border-box;
  overflow-y: auto;
  overflow-x: hidden;
}

.container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: var(--shadow-sm);
  transition: all 0.25s ease;
  cursor: default;
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
  white-space: nowrap;
}

.stat-label {
  font-size: 11px;
  color: var(--text-placeholder);
  margin-top: 2px;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.card-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-input {
  width: 170px;
}
.filter-select {
  width: 90px;
}

.table-card,
.message-card,
.delayed-card {
  min-width: 0;
}

.table-card :deep(.el-card),
.message-card :deep(.el-card),
.delayed-card :deep(.el-card) {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
  --el-card-border-color: var(--border-light);
  --el-card-border-radius: 12px;
}

.table-card :deep(.el-card__header),
.message-card :deep(.el-card__header),
.delayed-card :deep(.el-card__header) {
  padding: 14px 18px;
  border-bottom: 1px solid var(--border-light);
  border-radius: 12px 12px 0 0;
}

.table-card :deep(.el-card__body),
.message-card :deep(.el-card__body),
.delayed-card :deep(.el-card__body) {
  padding: 0 14px 14px;
}

.monitor-table {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-tr-bg-color: var(--bg-card);
  --el-table-row-hover-bg-color: var(--bg-hover);
  --el-table-current-row-bg-color: var(--table-current-row-bg);
}

.monitor-table :deep(.el-table th) {
  color: var(--text-placeholder);
  font-weight: 500;
  font-size: 12px;
  border-bottom: 1px solid var(--border-light);
  padding: 10px 8px;
}

.monitor-table :deep(.el-table td) {
  border-bottom: 1px solid var(--border-light);
  padding: 8px;
  color: var(--text-primary);
  font-size: 13px;
}

.monitor-table :deep(.el-table__row:hover td) {
  background: var(--bg-hover);
  cursor: pointer;
}

.cell-time {
  font-size: 12px;
  color: var(--text-placeholder);
  font-family: 'SF Mono', 'Courier New', monospace;
}

.topic-count-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: flex-end;
}

.count-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  min-width: 36px;
  text-align: right;
}

.count-bar-track {
  width: 80px;
  height: 4px;
  background: var(--border-light);
  border-radius: 2px;
  overflow: hidden;
}

.count-bar-fill {
  height: 100%;
  background: var(--color-primary-gradient);
  border-radius: 2px;
  transition: width 0.5s ease;
}

.message-section {
  flex-shrink: 0;
}

.message-list {
  padding: 8px;
  max-height: 360px;
  overflow-y: auto;
}

.message-row {
  display: flex;
  gap: 8px;
  padding: 6px 10px;
  margin-bottom: 4px;
  background: var(--bg-secondary);
  border-radius: 8px;
  transition: background 0.2s;
}

.message-row:hover {
  background: var(--bg-hover);
}

.msg-direction-tag {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 10px;
  font-weight: 700;
}
.msg-direction-tag.in {
  background: rgba(103, 194, 58, 0.12);
  color: #67c23a;
}
.msg-direction-tag.out {
  background: rgba(230, 162, 60, 0.12);
  color: #e6a23c;
}

.msg-content {
  flex: 1;
  min-width: 0;
  line-height: 1.3;
}

.msg-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  margin-bottom: 1px;
}

.msg-topic {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.msg-time {
  font-size: 10px;
  color: var(--text-placeholder);
  font-family: 'SF Mono', 'Courier New', monospace;
}

.msg-payload {
  font-size: 11px;
  color: var(--text-secondary);
  font-family: 'SF Mono', 'Courier New', monospace;
  word-break: break-all;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-height: none;
}

.msg-client {
  display: none;
}

.delayed-section {
  flex-shrink: 0;
}

.delayed-stats {
  display: flex;
  align-items: center;
  gap: 6px;
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
.detail-section-title .el-icon {
  color: var(--color-primary);
}

.detail-code {
  font-size: 13px;
  background: var(--bg-secondary);
  padding: 2px 8px;
  border-radius: 4px;
  color: var(--text-primary);
}

.subscription-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.sub-tag {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
}

.no-data-text {
  font-size: 13px;
  color: var(--text-placeholder);
}

.msg-enter-active {
  transition: all 0.3s ease;
}
.msg-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.status-dot-inline {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;
}
.status-dot-inline.pending { background: #e6a23c; }
.status-dot-inline.delivered { background: #67c23a; }
.status-dot-inline.cancelled { background: #909399; }

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .container { padding: 10px; gap: 10px; }
  .monitor-header { flex-direction: column; align-items: flex-start; gap: 10px; }
  .header-right { width: 100%; justify-content: flex-end; }
  .stats-grid { grid-template-columns: repeat(2, 1fr); gap: 8px; }
  .stat-card { padding: 10px 12px; }
  .stat-icon { width: 36px; height: 36px; }
  .stat-value { font-size: 16px; }
  .card-header-actions { flex-wrap: wrap; }
  .filter-input { width: 130px; }
}
</style>
