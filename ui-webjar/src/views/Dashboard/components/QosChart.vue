<template>
  <div class="chart-card">
    <div class="chart-header">
      <div class="chart-title">{{ t('dashboard.qosChart') }}</div>
      <div class="chart-badge">{{ totalMsg }} {{ t('dashboard.messageCount') }}</div>
    </div>
    <v-chart :option="chartOption" autoresize style="height: 260px" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import './echarts-setup'
import VChart from 'vue-echarts'
import { colors, baseTooltip, baseLegend } from './chart-theme'

const { t } = useI18n()

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const totals = computed(() => {
  const t = { qos0: 0, qos1: 0, qos2: 0 }
  props.data.forEach(d => {
    t.qos0 += d.qos0Delta || 0
    t.qos1 += d.qos1Delta || 0
    t.qos2 += d.qos2Delta || 0
  })
  return t
})

const totalMsg = computed(() => totals.value.qos0 + totals.value.qos1 + totals.value.qos2)

const chartOption = computed(() => ({
  color: [colors[0], colors[2], colors[3]],
  tooltip: { ...baseTooltip, trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { ...baseLegend, bottom: 0 },
  series: [{
    type: 'pie',
    radius: ['45%', '70%'],
    center: ['50%', '45%'],
    avoidLabelOverlap: true,
    itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
    label: { show: false },
    emphasis: {
      label: { show: true, fontSize: 14, fontWeight: 'bold' },
      itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.2)' }
    },
    data: [
      { name: 'QoS 0', value: totals.value.qos0 },
      { name: 'QoS 1', value: totals.value.qos1 },
      { name: 'QoS 2', value: totals.value.qos2 }
    ]
  }]
}))
</script>

<style scoped>
.chart-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid var(--el-border-color-lighter);
  transition: box-shadow 0.3s;
}
.chart-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.chart-badge {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-light);
  padding: 2px 10px;
  border-radius: 12px;
}
</style>
