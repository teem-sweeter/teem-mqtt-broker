<template>
  <div class="message-record">
    <!-- 统计卡片区域 -->
    <div class="stats-section">
      <div class="stat-cards">
        <div class="stat-card stat-card-blue">
          <div class="stat-icon">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.last1Hour || 0 }}</div>
            <div class="stat-label">最近1小时</div>
          </div>
        </div>
        <div class="stat-card stat-card-green">
          <div class="stat-icon">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.last30Min || 0 }}</div>
            <div class="stat-label">最近30分钟</div>
          </div>
        </div>
        <div class="stat-card stat-card-purple">
          <div class="stat-icon">
            <el-icon><Lightning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.last5Min || 0 }}</div>
            <div class="stat-label">最近5分钟</div>
          </div>
        </div>
        <div class="stat-card stat-card-orange">
          <div class="trend-header">
            <span class="trend-title">消息趋势</span>
            <el-button type="primary" size="small" text @click="fetchTrend">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
          <div class="trend-chart" ref="trendChartRef">
            <svg v-if="trendData.length > 0" :viewBox="`0 0 ${trendChartWidth} ${trendChartHeight}`" class="trend-svg">
              <defs>
                <linearGradient id="trendGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#409eff" stop-opacity="0.5"/>
                  <stop offset="100%" stop-color="#409eff" stop-opacity="0.08"/>
                </linearGradient>
              </defs>
              <path :d="areaPath" fill="url(#trendGradient)" />
            </svg>
            <div v-if="tooltip.visible" class="trend-tooltip" :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }">
              <div class="tooltip-time">{{ tooltip.time }}</div>
              <div class="tooltip-value">{{ tooltip.value }} 条消息</div>
            </div>
            <div class="trend-labels" v-if="trendData.length > 0">
              <span>{{ formatTrendTime(trendData[0]?.time) }}</span>
              <span>{{ formatTrendTime(trendData[trendData.length - 1]?.time) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 查询和表格区域 -->
    <div class="main-section">
      <div class="main-card">
        <div class="query-section">
          <div class="query-title">
            <el-icon><Search /></el-icon>
            <span>消息查询</span>
          </div>
          <div class="query-filters">
            <div class="filter-item">
              <label>主题</label>
              <el-input v-model="filterForm.topic" placeholder="按主题搜索" clearable />
            </div>
            <div class="filter-item">
              <label>客户端ID</label>
              <el-input v-model="filterForm.clientId" placeholder="按客户端ID搜索" clearable />
            </div>
            <div class="filter-item">
              <label>时间范围</label>
              <el-date-picker v-model="filterForm.timeRange" type="datetimerange"
                range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间"
                value-format="YYYY-MM-DDTHH:mm:ss" />
            </div>
            <div class="filter-item">
              <label>关键词</label>
              <el-input v-model="filterForm.keyword" placeholder="搜索消息内容" clearable />
            </div>
            <div class="filter-actions">
              <el-button type="primary" @click="handleQuery">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="handleReset">
                <el-icon><RefreshRight /></el-icon>
                重置
              </el-button>
            </div>
          </div>
        </div>

        <el-table :data="messages" v-loading="loading" row-key="id" stripe class="message-table">
        <el-table-column prop="timestamp" label="时间" width="180">
          <template #default="{ row }">
            <div class="time-cell">
              <span class="time-date">{{ formatDate(row.timestamp) }}</span>
              <span class="time-clock">{{ formatClock(row.timestamp) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="topic" label="主题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="topic-cell">
              <el-icon><ChatLineSquare /></el-icon>
              <span>{{ row.topic }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="clientId" label="客户端ID" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.clientId || 'N/A' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="direction" label="方向" width="100">
          <template #default="{ row }">
            <el-tag :type="row.direction === 'INBOUND' ? 'success' : 'warning'" size="small" effect="dark">
              {{ row.direction === 'INBOUND' ? '接收' : '发送' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="qos" label="QoS" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="primary" size="small" effect="light">Q{{ row.qos }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="retained" label="保留" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.retained ? 'warning' : 'info'" size="small" effect="light">
              {{ row.retained ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payload" label="内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="payload-text">{{ row.payload }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="消息详情" width="800px" class="detail-dialog">
      <el-descriptions :column="2" border v-if="selectedMessage">
        <el-descriptions-item label="消息ID" :span="2">
          <span class="id-value">{{ selectedMessage.id }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="主题">{{ selectedMessage.topic }}</el-descriptions-item>
        <el-descriptions-item label="客户端ID">{{ selectedMessage.clientId || 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="方向">
          <el-tag :type="selectedMessage.direction === 'INBOUND' ? 'success' : 'warning'" size="small">
            {{ selectedMessage.direction || 'N/A' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="QoS">
          <el-tag type="primary" size="small">Q{{ selectedMessage.qos }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="保留消息">
          <el-tag :type="selectedMessage.retained ? 'warning' : 'info'" size="small">
            {{ selectedMessage.retained ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="时间">
          {{ formatTime(selectedMessage.timestamp) }}
        </el-descriptions-item>
        <el-descriptions-item label="消息ID">{{ selectedMessage.messageId || 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="负载内容" :span="2">
          <div class="payload-viewer">
            <pre>{{ selectedMessage.payload }}</pre>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh, Search, RefreshRight, View, Delete, Clock, Timer, Lightning, ChatLineSquare
} from '@element-plus/icons-vue'
import { getMessages, deleteMessage, getMessageStats, getMessageTrend } from '@/api/message'

const messages = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const selectedMessage = ref(null)
const stats = ref({})
const trendData = ref([])
const trendChartRef = ref(null)
const trendChartWidth = 280
const trendChartHeight = 60
const tooltip = reactive({
  visible: false,
  x: 0,
  y: 0,
  time: '',
  value: 0
})

const filterForm = reactive({
  topic: '',
  clientId: '',
  timeRange: null,
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

async function fetchStats() {
  try {
    stats.value = await getMessageStats()
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

async function fetchTrend() {
  try {
    trendData.value = await getMessageTrend(60)
  } catch (error) {
    console.error('获取趋势失败', error)
  }
}

function getPointX(index) {
  if (trendData.value.length <= 1) return trendChartWidth / 2
  return (index / (trendData.value.length - 1)) * trendChartWidth
}

function getPointY(count) {
  if (!trendData.value || trendData.value.length === 0) return trendChartHeight
  const maxCount = Math.max(...trendData.value.map(item => item.count), 1)
  const ratio = count / maxCount
  return trendChartHeight - (ratio * (trendChartHeight - 10)) - 5
}

const linePath = computed(() => {
  if (!trendData.value || trendData.value.length === 0) return ''
  return trendData.value.map((item, index) => {
    const x = getPointX(index)
    const y = getPointY(item.count)
    return index === 0 ? `M ${x} ${y}` : `L ${x} ${y}`
  }).join(' ')
})

const smoothLinePath = computed(() => {
  if (!trendData.value || trendData.value.length < 2) return ''
  const points = trendData.value.map((item, index) => ({
    x: getPointX(index),
    y: getPointY(item.count)
  }))

  let path = `M ${points[0].x} ${points[0].y}`

  for (let i = 0; i < points.length - 1; i++) {
    const current = points[i]
    const next = points[i + 1]
    const midX = (current.x + next.x) / 2
    path += ` Q ${midX} ${current.y}, ${midX} ${(current.y + next.y) / 2}`
    path += ` T ${next.x} ${next.y}`
  }

  return path
})

const areaPath = computed(() => {
  if (!trendData.value || trendData.value.length === 0) return ''
  const linePart = smoothLinePath.value
  const lastX = getPointX(trendData.value.length - 1)
  const firstX = getPointX(0)
  return `${linePart} L ${lastX} ${trendChartHeight} L ${firstX} ${trendChartHeight} Z`
})

function showTooltip(index, event) {
  const item = trendData.value[index]
  if (!item) return
  const rect = trendChartRef.value.getBoundingClientRect()
  tooltip.visible = true
  tooltip.time = formatTrendTimeFull(item.time)
  tooltip.value = item.count
  tooltip.x = event.clientX - rect.left + 10
  tooltip.y = event.clientY - rect.top - 40
}

function hideTooltip() {
  tooltip.visible = false
}

function formatTrendTimeFull(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString()
}

function formatTrendTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.getHours() + ':' + String(date.getMinutes()).padStart(2, '0')
}

async function fetchMessages() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      topic: filterForm.topic || undefined,
      clientId: filterForm.clientId || undefined,
      keyword: filterForm.keyword || undefined,
      startTime: filterForm.timeRange?.[0] || undefined,
      endTime: filterForm.timeRange?.[1] || undefined
    }
    const response = await getMessages(params)
    messages.value = response.content
    pagination.total = response.total
  } catch (error) {
    ElMessage.error('获取消息列表失败')
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  pagination.page = 1
  fetchMessages()
}

function handlePageChange() {
  fetchMessages()
}

function handleSizeChange() {
  pagination.page = 1
  fetchMessages()
}

function handleReset() {
  filterForm.topic = ''
  filterForm.clientId = ''
  filterForm.timeRange = null
  filterForm.keyword = ''
  handleQuery()
}

function handleViewDetail(row) {
  selectedMessage.value = row
  detailVisible.value = true
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除此消息?', '提示', { type: 'warning' })
    await deleteMessage(row.id)
    ElMessage.success('删除成功')
    fetchMessages()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleString()
}

function formatDate(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleDateString()
}

function formatClock(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString()
}

onMounted(() => {
  fetchStats()
  fetchTrend()
  fetchMessages()
})
</script>

<style scoped>
.message-record {
  padding: 16px;
  background: var(--bg-secondary);
  min-height: 100%;
}

/* 统计卡片区域 */
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

.stat-card-blue {
  border-left: 4px solid #409eff;
}

.stat-card-green {
  border-left: 4px solid #67c23a;
}

.stat-card-purple {
  border-left: 4px solid #a855f7;
}

.stat-card-orange {
  border-left: 4px solid #f97316;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-card-blue .stat-icon {
  background: linear-gradient(135deg, #409eff20, #409eff10);
  color: #409eff;
}

.stat-card-green .stat-icon {
  background: linear-gradient(135deg, #67c23a20, #67c23a10);
  color: #67c23a;
}

.stat-card-purple .stat-icon {
  background: linear-gradient(135deg, #a855f720, #a855f710);
  color: #a855f7;
}

.stat-card-orange .stat-icon {
  background: linear-gradient(135deg, #f9731620, #f9731610);
  color: #f97316;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-placeholder);
  margin-top: 4px;
}

.trend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.trend-title {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

/* 主区域（查询+表格） */
.main-section {
  margin-bottom: 20px;
}

.main-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.query-section {
  margin-bottom: 16px;
}

.query-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.query-title .el-icon {
  color: #409eff;
}

.query-filters {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-item label {
  font-size: 12px;
  color: var(--text-placeholder);
}

.filter-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.message-table {
  border-radius: 8px;
  overflow: hidden;
}

.time-cell {
  display: flex;
  flex-direction: column;
}

.time-date {
  font-size: 12px;
  color: var(--text-placeholder);
}

.time-clock {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.topic-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #409eff;
}

.payload-text {
  font-family: 'Courier New', Consolas, monospace;
  font-size: 12px;
  color: var(--text-secondary);
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 详情弹窗 */
.detail-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
  padding: 16px 20px;
  margin: 0;
}

.detail-dialog :deep(.el-dialog__title) {
  color: #fff;
}

.detail-dialog :deep(.el-dialog__close) {
  color: #fff;
}

.id-value {
  font-family: 'Courier New', Consolas, monospace;
  color: #409eff;
}

.payload-viewer {
  max-height: 250px;
  overflow: auto;
  background: var(--bg-secondary);
  padding: 16px;
  border-radius: 8px;
}

.payload-viewer pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: 'Courier New', Consolas, monospace;
  font-size: 13px;
  color: var(--text-primary);
}

/* 趋势图 */
.trend-chart {
  height: 90px;
  position: relative;
}

.trend-svg {
  width: 100%;
  height: 60px;
  overflow: visible;
}

.trend-dot {
  cursor: pointer;
  transition: r 0.2s ease, stroke-width 0.2s ease;
}

.trend-labels {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: var(--text-placeholder);
  padding: 4px 0 0 0;
}

.trend-tooltip {
  position: absolute;
  background: rgba(0, 0, 0, 0.85);
  color: #fff;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 12px;
  pointer-events: none;
  z-index: 10;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.tooltip-time {
  color: var(--text-placeholder);
  margin-bottom: 4px;
}

.tooltip-value {
  font-weight: bold;
  color: #409eff;
  font-size: 14px;
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

@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .message-record {
    padding: 12px;
  }
  .stat-cards {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  .stat-card {
    padding: 14px;
  }
  .stat-value {
    font-size: 22px;
  }
  .main-card {
    padding: 14px;
  }
  .query-filters {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
