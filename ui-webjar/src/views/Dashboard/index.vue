<template>
  <div class="dashboard-page">
    <div class="container">
      <div class="dashboard-body">
        <!-- 核心指标卡片 -->
        <MetricCards :mqtt-info="mqttInfo" :has-mqtt="hasMqtt" />

        <div class="dashboard-grid">
          <!-- MQTT Broker 信息 -->
          <MqttBrokerCard :mqtt-info="mqttInfo" :has-mqtt="hasMqtt" />

          <!-- JVM 内存 -->
          <JvmMemoryCard :jvm-info="jvmInfo" :has-jvm-memory="hasJvmMemory" />

          <!-- 线程信息 -->
          <ThreadCard :jvm-info="jvmInfo" :has-threads="hasThreads" />

          <!-- GC 信息 -->
          <GcCard :jvm-info="jvmInfo" :has-gc="hasGc" />

          <!-- 系统信息 -->
          <SystemCard :system-info="systemInfo" :has-system="hasSystem" />

          <!-- 磁盘信息 -->
          <DiskCard :disk-info="diskInfo" :has-disk="hasDisk" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getHealth } from '@/api/mqtt'
import MetricCards from './components/MetricCards.vue'
import MqttBrokerCard from './components/MqttBrokerCard.vue'
import JvmMemoryCard from './components/JvmMemoryCard.vue'
import ThreadCard from './components/ThreadCard.vue'
import GcCard from './components/GcCard.vue'
import SystemCard from './components/SystemCard.vue'
import DiskCard from './components/DiskCard.vue'

const healthData = ref({})
let refreshTimer = null

const mqttInfo = computed(() => healthData.value.mqtt || {})
const jvmInfo = computed(() => healthData.value.jvm || {})
const systemInfo = computed(() => healthData.value.system || {})
const diskInfo = computed(() => healthData.value.disk || {})

const hasMqtt = computed(() => Object.keys(mqttInfo.value).length > 0)
const hasJvmMemory = computed(() => !!(jvmInfo.value.heapMemory && jvmInfo.value.heapMemory.used))
const hasThreads = computed(() => jvmInfo.value.threadCount > 0)
const hasGc = computed(() => jvmInfo.value.gc && jvmInfo.value.gc.length > 0)
const hasSystem = computed(() => !!(systemInfo.value.name || systemInfo.value.arch))
const hasDisk = computed(() => !!(diskInfo.value.totalSpace && diskInfo.value.totalSpace > 0))

async function refreshData() {
  try {
    healthData.value = await getHealth()
  } catch (error) {
    console.error('获取健康状态失败:', error)
  }
}

onMounted(() => {
  refreshData()
  refreshTimer = setInterval(refreshData, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
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

.dashboard-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  min-width: 0;
}

/* 大屏 2K+ */
@media (min-width: 2560px) {
  .dashboard-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

/* 中屏 */
@media (max-width: 1400px) {
  .dashboard-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 小屏 */
@media (max-width: 900px) {
  .dashboard-page {
    padding: 8px;
  }
  .dashboard-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }
}
</style>