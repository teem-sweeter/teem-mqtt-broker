<template>
  <div class="chart-card">
    <div class="chart-title">{{ t('dashboard.bandwidthChart') }}</div>
    <v-chart :option="chartOption" autoresize style="height: 280px" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import './echarts-setup'
import VChart from 'vue-echarts'

const { t } = useI18n()

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: [t('dashboard.bytesIn'), t('dashboard.bytesOut')], bottom: 0 },
  xAxis: { type: 'category', data: props.data.map(d => new Date(d.timestamp).toLocaleTimeString()) },
  yAxis: { type: 'value', name: t('dashboard.bytesPerSec') },
  series: [
    { name: t('dashboard.bytesIn'), type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, data: props.data.map(d => d.bytesInDelta) },
    { name: t('dashboard.bytesOut'), type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, data: props.data.map(d => d.bytesOutDelta) }
  ]
}))
</script>

<style scoped>
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
</style>
