<template>
  <div class="client-page">
    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-cards">
        <div class="stat-card stat-card-blue">
          <div class="stat-icon"><el-icon><User /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ clients.length || 0 }}</div>
            <div class="stat-label">{{ t('client.onlineClients') }}</div>
          </div>
        </div>
        <div class="stat-card stat-card-green">
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ normalCount }}</div>
            <div class="stat-label">{{ t('client.normalComm') }}</div>
          </div>
        </div>
        <div class="stat-card stat-card-purple">
          <div class="stat-icon"><el-icon><Mute /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ sendDisabledCount }}</div>
            <div class="stat-label">{{ t('client.sendDisabled') }}</div>
          </div>
        </div>
        <div class="stat-card stat-card-orange">
          <div class="stat-icon"><el-icon><CloseBold /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ receiveDisabledCount }}</div>
            <div class="stat-label">{{ t('client.receiveDisabled') }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 客户端详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="t('client.clientDetail')"
      width="620px"
      class="detail-dialog"
      destroy-on-close
    >
      <template v-if="detailClient">
        <!-- 连接信息 -->
        <div class="detail-section">
          <div class="detail-section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ t('client.connectionInfo') }}</span>
          </div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item :label="t('client.clientId')" :span="2">
              <span class="mono-text">{{ detailClient.clientId }}</span>
            </el-descriptions-item>
            <el-descriptions-item :label="t('client.username')">
              {{ detailClient.username || '-' }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('client.ipAddress')">
              <span class="mono-text">{{ detailClient.ipAddress || '-' }}:{{ detailClient.port || '' }}</span>
            </el-descriptions-item>
            <el-descriptions-item :label="t('client.protocolVersion')">
              <el-tag size="small" effect="plain">{{ detailClient.protocolVersion || '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Clean Session">
              <el-tag :type="detailClient.cleanSession ? 'warning' : 'info'" size="small" effect="plain">
                {{ detailClient.cleanSession ? t('common.yes') : t('common.no') }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="t('client.connectedAt')">
              {{ formatTime(detailClient.connectTime) }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('client.duration')">
              {{ formatDuration(detailClient.connectTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="Keep Alive">
              {{ detailClient.keepAlive || '-' }} {{ t('client.seconds') }}
            </el-descriptions-item>
            <el-descriptions-item :label="t('client.controlStatus')">
              <div class="status-tags">
                <el-tag v-if="!detailClient.sendDisabled && !detailClient.receiveDisabled" type="success" size="small" effect="plain">{{ t('client.normal') }}</el-tag>
                <el-tag v-if="detailClient.sendDisabled" type="danger" size="small" effect="plain">{{ t('client.sendDisabled') }}</el-tag>
                <el-tag v-if="detailClient.receiveDisabled" type="warning" size="small" effect="plain">{{ t('client.receiveDisabled') }}</el-tag>
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 遗嘱配置 -->
        <div class="detail-section" v-if="detailClient.willFlag">
          <div class="detail-section-title">
            <el-icon><WarningFilled /></el-icon>
            <span>{{ t('client.willMessage') }}</span>
          </div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="Will Topic" :span="2">
              <span class="mono-text">{{ detailClient.willTopic || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="Will QoS">
              <el-tag size="small" effect="plain">QoS {{ detailClient.willQos }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Will Retain">
              <el-tag :type="detailClient.willRetain ? 'warning' : 'info'" size="small" effect="plain">
                {{ detailClient.willRetain ? t('common.yes') : t('common.no') }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Will Payload" :span="2">
              <div class="will-payload">{{ detailClient.willMessage || '-' }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 运行指标 -->
        <div class="detail-section">
          <div class="detail-section-title">
            <el-icon><DataAnalysis /></el-icon>
            <span>{{ t('client.metrics') }}</span>
          </div>
          <div class="metrics-grid">
            <div class="metric-card">
              <div class="metric-val">{{ detailClient.inflightCount || 0 }}</div>
              <div class="metric-lbl">{{ t('client.inflight') }}</div>
              <div class="metric-desc">{{ t('client.inflightDesc') }}</div>
            </div>
            <div class="metric-card">
              <div class="metric-val">{{ detailClient.queuedCount || 0 }}</div>
              <div class="metric-lbl">{{ t('client.queueBacklog') }}</div>
              <div class="metric-desc">{{ t('client.queueBacklogDesc') }}</div>
            </div>
            <div class="metric-card">
              <div class="metric-val">{{ detailClient.subscriptions ? detailClient.subscriptions.length : 0 }}</div>
              <div class="metric-lbl">{{ t('client.subscriptions') }}</div>
              <div class="metric-desc">{{ t('client.subscriptionsDesc') }}</div>
            </div>
          </div>
        </div>

        <!-- 订阅详情 -->
        <div class="detail-section">
          <div class="detail-section-title">
            <el-icon><List /></el-icon>
            <span>{{ t('client.subscriptionDetails') }}</span>
            <el-tag size="small" type="info" class="sub-count">
              {{ detailClient.subscriptionDetails ? detailClient.subscriptionDetails.length : 0 }}
            </el-tag>
          </div>
          <el-table
            v-if="detailClient.subscriptionDetails && detailClient.subscriptionDetails.length > 0"
            :data="detailClient.subscriptionDetails"
            size="small"
            border
            class="sub-table"
          >
            <el-table-column prop="topic" :label="t('client.topic')" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="mono-text">{{ row.topic }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="qos" label="QoS" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small" effect="plain">QoS {{ row.qos }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="noLocal" label="No Local" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.noLocal ? 'success' : 'info'" size="small" effect="plain">
                  {{ row.noLocal ? t('common.yes') : t('common.no') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="retainAsPublished" :label="t('client.retainPublished')" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.retainAsPublished ? 'success' : 'info'" size="small" effect="plain">
                  {{ row.retainAsPublished ? t('common.yes') : t('common.no') }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else :description="t('client.noSubscriptions')" :image-size="40" />
        </div>

        <!-- 备注 -->
        <div class="detail-section" v-if="detailClient.remark">
          <div class="detail-section-title">
            <el-icon><Document /></el-icon>
            <span>{{ t('client.remark') }}</span>
          </div>
          <div class="remark-text">{{ detailClient.remark }}</div>
        </div>
      </template>
    </el-dialog>

    <!-- 主内容区 -->
    <div class="main-section">
      <div class="main-card">
        <!-- 操作栏 -->
        <div class="toolbar">
          <div class="toolbar-title">
            <el-icon><UserFilled /></el-icon>
            <span>{{ t('client.clientManagement') }}</span>
          </div>
          <div class="toolbar-actions">
            <el-input
              v-model="searchText"
              :placeholder="t('client.searchPlaceholder')"
              clearable
              prefix-icon="Search"
              style="width: 240px;"
            />
          </div>
        </div>

        <!-- 数据表格 -->
        <el-table :data="filteredClients" v-loading="initialLoading" row-key="clientId" stripe class="client-table">
          <el-table-column prop="clientId" :label="t('client.clientId')" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="client-id-cell">
                <span class="client-id-text">{{ row.clientId }}</span>
                <el-tag v-if="row.sendDisabled || row.receiveDisabled" type="warning" size="small" effect="plain">
                  {{ t('client.controlled') }}
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="username" :label="t('client.username')" width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ row.username || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="ipAddress" :label="t('client.ipAddress')" width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="mono-text">{{ row.ipAddress || '-' }}:{{ row.port || '' }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="connectTime" :label="t('client.connectedAt')" width="170">
            <template #default="{ row }">
              <span class="time-text">{{ formatTime(row.connectTime) }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="subscriptions" :label="t('client.subscriptions')" width="80" align="center">
            <template #default="{ row }">
              <el-tag type="info" size="small">{{ row.subscriptions ? row.subscriptions.length : 0 }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column :label="t('client.allowSend')" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.sendDisabled"
                :active-value="false"
                :inactive-value="true"
                @change="(val) => handleSendToggle(row, val)"
                :loading="row._sendLoading"
                inline-prompt
                :active-text="t('client.on')"
                :inactive-text="t('client.off')"
                :active-color="'#67c23a'"
                :inactive-color="'#f56c6c'"
              />
            </template>
          </el-table-column>

          <el-table-column :label="t('client.allowReceive')" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.receiveDisabled"
                :active-value="false"
                :inactive-value="true"
                @change="(val) => handleReceiveToggle(row, val)"
                :loading="row._receiveLoading"
                inline-prompt
                :active-text="t('client.on')"
                :inactive-text="t('client.off')"
                :active-color="'#67c23a'"
                :inactive-color="'#f56c6c'"
              />
            </template>
          </el-table-column>

          <el-table-column :label="t('client.action')" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openDetail(row)">
                <el-icon><View /></el-icon>
                {{ t('client.detail') }}
              </el-button>
              <el-popconfirm
                :title="t('client.kickConfirm')"
                :confirm-button-text="t('client.kick')"
                :cancel-button-text="t('common.cancel')"
                @confirm="handleKick(row)"
              >
                <template #reference>
                  <el-button link type="danger" size="small">
                    <el-icon><SwitchButton /></el-icon>
                    {{ t('client.kick') }}
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!initialLoading && filteredClients.length === 0" :description="t('client.noOnlineClients')" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
import {
  User, UserFilled, CircleCheck, Mute, CloseBold,
  SwitchButton, Search, View, InfoFilled, List, Tickets, Document,
  WarningFilled, DataAnalysis
} from '@element-plus/icons-vue'
import { getClients, kickClient, setSendDisabled, setReceiveDisabled } from '@/api/client'

const clients = ref([])
const initialLoading = ref(true)
const searchText = ref('')
const detailVisible = ref(false)
const detailClient = ref(null)
let refreshTimer = null

const normalCount = computed(() =>
  clients.value.filter(c => !c.sendDisabled && !c.receiveDisabled).length
)
const sendDisabledCount = computed(() =>
  clients.value.filter(c => c.sendDisabled).length
)
const receiveDisabledCount = computed(() =>
  clients.value.filter(c => c.receiveDisabled).length
)

const filteredClients = computed(() => {
  if (!searchText.value) return clients.value
  const q = searchText.value.toLowerCase()
  return clients.value.filter(c =>
    (c.clientId && c.clientId.toLowerCase().includes(q)) ||
    (c.username && c.username.toLowerCase().includes(q))
  )
})

async function fetchClients(silent = false) {
  try {
    clients.value = await getClients()
  } catch {
    if (!silent) ElMessage.error(t('client.fetchFailed'))
  } finally {
    initialLoading.value = false
  }
}

async function handleSendToggle(row, val) {
  // val 是 el-switch 的新值，active-value=false / inactive-value=true
  // 所以 val=true 表示禁止发送，val=false 表示允许发送
  const disabled = val
  row._sendLoading = true
  try {
    await setSendDisabled(row.clientId, disabled)
    row.sendDisabled = disabled
    ElMessage.success(disabled ? t('client.sendDisabledSuccess') : t('client.sendEnabledSuccess'))
  } catch {
    row.sendDisabled = !disabled
    ElMessage.error(t('client.operationFailed'))
  } finally {
    row._sendLoading = false
  }
}

async function handleReceiveToggle(row, val) {
  const disabled = val
  row._receiveLoading = true
  try {
    await setReceiveDisabled(row.clientId, disabled)
    row.receiveDisabled = disabled
    ElMessage.success(disabled ? t('client.receiveDisabledSuccess') : t('client.receiveEnabledSuccess'))
  } catch {
    row.receiveDisabled = !disabled
    ElMessage.error(t('client.operationFailed'))
  } finally {
    row._receiveLoading = false
  }
}

async function handleKick(row) {
  try {
    const res = await kickClient(row.clientId)
    if (res.success) {
      ElMessage.success(t('client.kickSuccess') + ': ' + row.clientId)
      fetchClients(true)
    } else {
      ElMessage.error(res.message || t('client.kickFailed'))
    }
  } catch {
    ElMessage.error(t('client.operationFailed'))
  }
}

function openDetail(row) {
  detailClient.value = row
  detailVisible.value = true
}

function formatTime(ts) {
  if (!ts) return '-'
  return new Date(ts).toLocaleString()
}

function formatDuration(ts) {
  if (!ts) return '-'
  const diff = Date.now() - ts
  if (diff < 0) return '-'
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  if (days > 0) return `${days}${t('client.days')} ${hours % 24}${t('client.hours')} ${minutes % 60}${t('client.minutes')}`
  if (hours > 0) return `${hours}${t('client.hours')} ${minutes % 60}${t('client.minutes')}`
  if (minutes > 0) return `${minutes}${t('client.minutes')} ${seconds % 60}${t('client.seconds')}`
  return `${seconds}${t('client.seconds')}`
}

onMounted(() => {
  fetchClients()
  refreshTimer = setInterval(() => fetchClients(true), 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped>
.client-page {
  padding: 12px;
  background: var(--bg-secondary);
  height: 100%;
  overflow: hidden;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

/* 统计卡片 */
.stats-section {
  flex-shrink: 0;
  margin-bottom: 10px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: 10px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.stat-card-blue { border-left: 4px solid #409eff; }
.stat-card-green { border-left: 4px solid #67c23a; }
.stat-card-purple { border-left: 4px solid #a855f7; }
.stat-card-orange { border-left: 4px solid #f97316; }

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.stat-card-blue .stat-icon { background: linear-gradient(135deg, #409eff20, #409eff10); color: #409eff; }
.stat-card-green .stat-icon { background: linear-gradient(135deg, #67c23a20, #67c23a10); color: #67c23a; }
.stat-card-purple .stat-icon { background: linear-gradient(135deg, #a855f720, #a855f710); color: #a855f7; }
.stat-card-orange .stat-icon { background: linear-gradient(135deg, #f9731620, #f9731610); color: #f97316; }

.stat-content { flex: 1; }
.stat-value { font-size: 22px; font-weight: bold; color: var(--text-primary); line-height: 1.2; }
.stat-label { font-size: 12px; color: var(--text-placeholder); margin-top: 2px; }

/* 主内容区 */
.main-section {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.main-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 14px;
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.toolbar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.toolbar-title .el-icon { color: #409eff; }

.toolbar-actions {
  display: flex;
  gap: 8px;
}

/* 表格 */
.client-table { border-radius: 8px; overflow: hidden; }

.client-id-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.client-id-text {
  font-family: 'Courier New', Consolas, monospace;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.mono-text {
  font-family: 'Courier New', Consolas, monospace;
  font-size: 12px;
  color: var(--text-secondary);
}

.time-text {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 详情弹窗 */
.detail-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
  padding: 16px 20px;
  margin: 0;
}

.detail-dialog :deep(.el-dialog__title) { color: #fff; }
.detail-dialog :deep(.el-dialog__close) { color: #fff; }

.detail-section {
  margin-bottom: 20px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.detail-section-title .el-icon {
  color: #409eff;
}

.sub-count {
  margin-left: 4px;
}

.status-tags {
  display: flex;
  gap: 6px;
}

.sub-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid var(--border-light);
  border-radius: 6px;
}

.sub-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-bottom: 1px solid var(--border-light);
  font-size: 13px;
}

.sub-item:last-child {
  border-bottom: none;
}

.sub-item .el-icon {
  color: #409eff;
  flex-shrink: 0;
}

.sub-topic {
  font-family: 'Courier New', Consolas, monospace;
  color: var(--text-primary);
  word-break: break-all;
}

.remark-text {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  padding: 10px 12px;
  background: var(--bg-secondary);
  border-radius: 6px;
}

.will-payload {
  font-family: 'Courier New', Consolas, monospace;
  font-size: 12px;
  color: var(--text-secondary);
  word-break: break-all;
  max-height: 80px;
  overflow-y: auto;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.metric-card {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 14px;
  text-align: center;
}

.metric-val {
  font-size: 24px;
  font-weight: bold;
  color: var(--text-primary);
}

.metric-lbl {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-top: 4px;
}

.metric-desc {
  font-size: 11px;
  color: var(--text-placeholder);
  margin-top: 2px;
}

.sub-table {
  border-radius: 6px;
  overflow: hidden;
}

/* Element Plus 覆盖 */
:deep(.el-table) {
  --el-table-border-color: var(--border-light);
  --el-table-header-bg-color: var(--bg-input);
}

:deep(.el-table th) {
  font-weight: 600;
  color: var(--text-secondary);
}

:deep(.el-table__row:hover > td) {
  background: var(--bg-hover) !important;
}

:deep(.el-button + .el-button) {
  margin-left: 4px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stat-cards { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .client-page { padding: 8px; }
  .stat-cards { grid-template-columns: 1fr; gap: 8px; }
  .stat-card { padding: 10px 12px; }
  .stat-value { font-size: 20px; }
  .main-card { padding: 10px; }
  .toolbar { flex-direction: column; gap: 10px; align-items: stretch; }
  .toolbar-actions { justify-content: flex-end; }
}
</style>
