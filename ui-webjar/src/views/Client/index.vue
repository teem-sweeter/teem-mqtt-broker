<template>
  <div class="client-page">
    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-cards">
        <div class="stat-card stat-card-blue">
          <div class="stat-icon"><el-icon><User /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ clients.length || 0 }}</div>
            <div class="stat-label">在线客户端</div>
          </div>
        </div>
        <div class="stat-card stat-card-green">
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ normalCount }}</div>
            <div class="stat-label">正常通信</div>
          </div>
        </div>
        <div class="stat-card stat-card-purple">
          <div class="stat-icon"><el-icon><Mute /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ sendDisabledCount }}</div>
            <div class="stat-label">禁止发送</div>
          </div>
        </div>
        <div class="stat-card stat-card-orange">
          <div class="stat-icon"><el-icon><CloseBold /></el-icon></div>
          <div class="stat-content">
            <div class="stat-value">{{ receiveDisabledCount }}</div>
            <div class="stat-label">禁止接收</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 客户端详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="客户端详情"
      width="620px"
      class="detail-dialog"
      destroy-on-close
    >
      <template v-if="detailClient">
        <!-- 连接信息 -->
        <div class="detail-section">
          <div class="detail-section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>连接信息</span>
          </div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="客户端 ID" :span="2">
              <span class="mono-text">{{ detailClient.clientId }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="用户名">
              {{ detailClient.username || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="IP 地址">
              <span class="mono-text">{{ detailClient.ipAddress || '-' }}:{{ detailClient.port || '' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="协议版本">
              <el-tag size="small" effect="plain">{{ detailClient.protocolVersion || '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="Clean Session">
              <el-tag :type="detailClient.cleanSession ? 'warning' : 'info'" size="small" effect="plain">
                {{ detailClient.cleanSession ? '是' : '否' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="连接时间">
              {{ formatTime(detailClient.connectTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="在线时长">
              {{ formatDuration(detailClient.connectTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="Keep Alive">
              {{ detailClient.keepAlive || '-' }} 秒
            </el-descriptions-item>
            <el-descriptions-item label="管控状态">
              <div class="status-tags">
                <el-tag v-if="!detailClient.sendDisabled && !detailClient.receiveDisabled" type="success" size="small" effect="plain">正常</el-tag>
                <el-tag v-if="detailClient.sendDisabled" type="danger" size="small" effect="plain">禁止发送</el-tag>
                <el-tag v-if="detailClient.receiveDisabled" type="warning" size="small" effect="plain">禁止接收</el-tag>
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 遗嘱配置 -->
        <div class="detail-section" v-if="detailClient.willFlag">
          <div class="detail-section-title">
            <el-icon><WarningFilled /></el-icon>
            <span>遗嘱消息（Will）</span>
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
                {{ detailClient.willRetain ? '是' : '否' }}
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
            <span>运行指标</span>
          </div>
          <div class="metrics-grid">
            <div class="metric-card">
              <div class="metric-val">{{ detailClient.inflightCount || 0 }}</div>
              <div class="metric-lbl">飞行窗口</div>
              <div class="metric-desc">正在传输的 QoS1/2 消息</div>
            </div>
            <div class="metric-card">
              <div class="metric-val">{{ detailClient.queuedCount || 0 }}</div>
              <div class="metric-lbl">队列积压</div>
              <div class="metric-desc">断线期间积压的消息</div>
            </div>
            <div class="metric-card">
              <div class="metric-val">{{ detailClient.subscriptions ? detailClient.subscriptions.length : 0 }}</div>
              <div class="metric-lbl">订阅数</div>
              <div class="metric-desc">当前活跃的订阅</div>
            </div>
          </div>
        </div>

        <!-- 订阅详情 -->
        <div class="detail-section">
          <div class="detail-section-title">
            <el-icon><List /></el-icon>
            <span>订阅详情</span>
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
            <el-table-column prop="topic" label="主题" min-width="180" show-overflow-tooltip>
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
                  {{ row.noLocal ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="retainAsPublished" label="Retain 发布" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.retainAsPublished ? 'success' : 'info'" size="small" effect="plain">
                  {{ row.retainAsPublished ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无订阅" :image-size="40" />
        </div>

        <!-- 备注 -->
        <div class="detail-section" v-if="detailClient.remark">
          <div class="detail-section-title">
            <el-icon><Document /></el-icon>
            <span>备注</span>
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
            <span>客户端管理</span>
          </div>
          <div class="toolbar-actions">
            <el-input
              v-model="searchText"
              placeholder="搜索客户端ID / 用户名"
              clearable
              prefix-icon="Search"
              style="width: 240px;"
            />
            <el-button @click="fetchClients" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>

        <!-- 数据表格 -->
        <el-table :data="filteredClients" v-loading="loading" row-key="clientId" stripe class="client-table">
          <el-table-column prop="clientId" label="客户端 ID" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="client-id-cell">
                <span class="client-id-text">{{ row.clientId }}</span>
                <el-tag v-if="row.sendDisabled || row.receiveDisabled" type="warning" size="small" effect="plain">
                  已管控
                </el-tag>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="username" label="用户名" width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ row.username || '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="ipAddress" label="IP 地址" width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="mono-text">{{ row.ipAddress || '-' }}:{{ row.port || '' }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="connectTime" label="连接时间" width="170">
            <template #default="{ row }">
              <span class="time-text">{{ formatTime(row.connectTime) }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="subscriptions" label="订阅数" width="80" align="center">
            <template #default="{ row }">
              <el-tag type="info" size="small">{{ row.subscriptions ? row.subscriptions.length : 0 }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="允许发送" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.sendDisabled"
                :active-value="false"
                :inactive-value="true"
                @change="(val) => handleSendToggle(row, val)"
                :loading="row._sendLoading"
                inline-prompt
                active-text="开"
                inactive-text="关"
                :active-color="'#67c23a'"
                :inactive-color="'#f56c6c'"
              />
            </template>
          </el-table-column>

          <el-table-column label="允许接收" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.receiveDisabled"
                :active-value="false"
                :inactive-value="true"
                @change="(val) => handleReceiveToggle(row, val)"
                :loading="row._receiveLoading"
                inline-prompt
                active-text="开"
                inactive-text="关"
                :active-color="'#67c23a'"
                :inactive-color="'#f56c6c'"
              />
            </template>
          </el-table-column>

          <el-table-column label="操作" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openDetail(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-popconfirm
                title="确定踢出该客户端？"
                confirm-button-text="踢出"
                cancel-button-text="取消"
                @confirm="handleKick(row)"
              >
                <template #reference>
                  <el-button link type="danger" size="small">
                    <el-icon><SwitchButton /></el-icon>
                    踢出
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!loading && filteredClients.length === 0" description="暂无在线客户端" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User, UserFilled, CircleCheck, Mute, CloseBold,
  Refresh, SwitchButton, Search, View, InfoFilled, List, Tickets, Document,
  WarningFilled, DataAnalysis
} from '@element-plus/icons-vue'
import { getClients, kickClient, setSendDisabled, setReceiveDisabled } from '@/api/client'

const clients = ref([])
const loading = ref(false)
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

async function fetchClients() {
  loading.value = true
  try {
    clients.value = await getClients()
  } catch {
    ElMessage.error('获取客户端列表失败')
  } finally {
    loading.value = false
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
    ElMessage.success(disabled ? '已禁止发送' : '已允许发送')
  } catch {
    row.sendDisabled = !disabled
    ElMessage.error('操作失败')
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
    ElMessage.success(disabled ? '已禁止接收' : '已允许接收')
  } catch {
    row.receiveDisabled = !disabled
    ElMessage.error('操作失败')
  } finally {
    row._receiveLoading = false
  }
}

async function handleKick(row) {
  try {
    const res = await kickClient(row.clientId)
    if (res.success) {
      ElMessage.success('已踢出客户端: ' + row.clientId)
      fetchClients()
    } else {
      ElMessage.error(res.message || '踢出失败')
    }
  } catch {
    ElMessage.error('操作失败')
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
  if (days > 0) return `${days}天 ${hours % 24}小时 ${minutes % 60}分钟`
  if (hours > 0) return `${hours}小时 ${minutes % 60}分钟`
  if (minutes > 0) return `${minutes}分钟 ${seconds % 60}秒`
  return `${seconds}秒`
}

onMounted(() => {
  fetchClients()
  refreshTimer = setInterval(fetchClients, 10000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.client-page {
  padding: 16px;
  background: var(--bg-secondary);
  min-height: 100%;
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 16px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
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
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-card-blue .stat-icon { background: linear-gradient(135deg, #409eff20, #409eff10); color: #409eff; }
.stat-card-green .stat-icon { background: linear-gradient(135deg, #67c23a20, #67c23a10); color: #67c23a; }
.stat-card-purple .stat-icon { background: linear-gradient(135deg, #a855f720, #a855f710); color: #a855f7; }
.stat-card-orange .stat-icon { background: linear-gradient(135deg, #f9731620, #f9731610); color: #f97316; }

.stat-content { flex: 1; }
.stat-value { font-size: 28px; font-weight: bold; color: var(--text-primary); line-height: 1.2; }
.stat-label { font-size: 13px; color: var(--text-placeholder); margin-top: 4px; }

/* 主内容区 */
.main-section { margin-bottom: 20px; }

.main-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
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
  .client-page { padding: 12px; }
  .stat-cards { grid-template-columns: 1fr; gap: 8px; }
  .stat-card { padding: 14px; }
  .stat-value { font-size: 22px; }
  .main-card { padding: 14px; }
  .toolbar { flex-direction: column; gap: 12px; align-items: stretch; }
  .toolbar-actions { justify-content: flex-end; }
}
</style>
