<template>
  <div class="dashboard-page">
    <div class="container">
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
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getHealth } from '@/api/mqtt'
import MetricCards from './components/MetricCards.vue'
import MqttBrokerCard from './components/MqttBrokerCard.vue'
import JvmMemoryCard from './components/JvmMemoryCard.vue'
import ThreadCard from './components/ThreadCard.vue'
import GcCard from './components/GcCard.vue'
import SystemCard from './components/SystemCard.vue'
import DiskCard from './components/DiskCard.vue'

const healthData = ref({})

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
})
</script>

<style scoped>
.dashboard-page {
  background: var(--bg-secondary);
  padding: 16px;
  box-sizing: border-box;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  min-width: 0;
}

@media (max-width: 1400px) {
  .dashboard-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 900px) {
  .dashboard-page {
    padding: 12px;
  }
  .dashboard-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>