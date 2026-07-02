<template>
  <div class="chart-card">
    <div class="chart-header">
      <div class="chart-title">{{ t('dashboard.errorChart') }}</div>
      <div class="chart-badge" :class="{ 'badge-warn': latestErrors > 0 }">{{ latestErrors }} {{ t('dashboard.errorsPerSec') }}</div>
    </div>
    <v-chart :option="chartOption" autoresize style="height: 260px" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import './echarts-setup'
import VChart from 'vue-echarts'
import { colors, makeAreaStyle, baseAxis, baseTooltip } from './chart-theme'

const { t } = useI18n()

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const latestErrors = computed(() => {
  const last = props.data[props.data.length - 1]
  return last ? last.errorsDelta : 0
})

const chartOption = computed(() => ({
  color: [colors[3]],
  tooltip: { ...baseTooltip, trigger: 'axis' },
  grid: { top: 20, right: 16, bottom: 20, left: 50, containLabel: false },
  xAxis: { type: 'category', data: props.data.map(d => new Date(d.timestamp).toLocaleTimeString()), ...baseAxis, boundaryGap: false },
  yAxis: { type: 'value', ...baseAxis },
  series: [{
    name: t('dashboard.errorRate'),
    type: 'line',
    smooth: true,
    showSymbol: false,
    lineStyle: { width: 2.5, color: colors[3] },
    areaStyle: makeAreaStyle(colors[3]),
    data: props.data.map(d => d.errorsDelta)
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
.badge-warn {
  color: #F56C6C;
  background: #FEF0F0;
}
</style>
