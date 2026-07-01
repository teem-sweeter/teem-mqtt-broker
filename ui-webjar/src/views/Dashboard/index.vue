<template>
  <div class="dashboard-page">
    <div class="container">
      <div class="dashboard-header">
        <div class="range-switch">
          <el-radio-group v-model="timeRange" @change="reconnect">
            <el-radio-button value="5m">5 {{ t('dashboard.minutes') }}</el-radio-button>
            <el-radio-button value="15m">15 {{ t('dashboard.minutes') }}</el-radio-button>
            <el-radio-button value="60m">60 {{ t('dashboard.minutes') }}</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div class="overview-cards">
        <MetricCards :mqtt-info="mqttInfo" :has-mqtt="hasMqtt" />
      </div>

      <div class="charts-grid">
        <MessageChart :data="chartData" />
        <ClientChart :data="chartData" />
        <BandwidthChart :data="chartData" />
        <TopicChart :data="chartData" />
        <QosChart :data="chartData" />
        <ErrorChart :data="chartData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getHealth } from '@/api/mqtt'
import MetricCards from './components/MetricCards.vue'
import MessageChart from './components/MessageChart.vue'
import ClientChart from './components/ClientChart.vue'
import BandwidthChart from './components/BandwidthChart.vue'
import TopicChart from './components/TopicChart.vue'
import QosChart from './components/QosChart.vue'
import ErrorChart from './components/ErrorChart.vue'

const { t } = useI18n()

const timeRange = ref('15m')
const chartData = ref([])
const mqttInfo = ref({})
const hasMqtt = ref(false)
let eventSource = null

function connectSse() {
  if (eventSource) {
    eventSource.close()
  }
  eventSource = new EventSource(`/api/dashboard/stream?range=${timeRange.value}`)
  eventSource.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      chartData.value = data
    } catch (e) {
      console.error('SSE parse error:', e)
    }
  }
  eventSource.onerror = () => {
    console.error('SSE connection error')
  }
}

function reconnect() {
  connectSse()
}

async function loadHealth() {
  try {
    mqttInfo.value = await getHealth()
    hasMqtt.value = true
  } catch (e) {
    console.error('Failed to load health:', e)
  }
}

onMounted(() => {
  connectSse()
  loadHealth()
})

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
.dashboard-page {
  background: var(--bg-secondary);
  padding: 12px;
  box-sizing: border-box;
  height: 100%;
  overflow: hidden;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.dashboard-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.overview-cards {
  margin-bottom: 12px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  min-width: 0;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.chart-card {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 16px;
  box-shadow: var(--el-box-shadow-light);
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--el-text-color-primary);
}

/* 大屏 2K+ */
@media (min-width: 2560px) {
  .charts-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

/* 小屏 */
@media (max-width: 768px) {
  .dashboard-page {
    padding: 8px;
  }
  .charts-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }
}
</style>
