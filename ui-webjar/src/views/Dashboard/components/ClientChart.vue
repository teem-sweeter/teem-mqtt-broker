<template>
  <div class="chart-card">
    <div class="chart-header">
      <div class="chart-title">{{ t('dashboard.clientChart') }}</div>
      <div class="chart-badge" v-if="hasData">{{ latestClients }} {{ t('dashboard.activeClients') }}</div>
    </div>
    <div class="chart-body" v-if="hasData">
      <v-chart :option="chartOption" autoresize style="height: 260px" />
    </div>
    <div class="chart-empty" v-else>
      <el-icon :size="40" color="#C0C4CC"><User /></el-icon>
      <span>{{ t('dashboard.noData') }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { User } from '@element-plus/icons-vue'
import './echarts-setup'
import VChart from 'vue-echarts'
import { colors, makeAreaStyle, baseAxis, baseTooltip } from './chart-theme'

const { t } = useI18n()

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const hasData = computed(() => props.data.length > 0)

const latestClients = computed(() => {
  const last = props.data[props.data.length - 1]
  return last ? last.activeClients : 0
})

const chartOption = computed(() => ({
  color: colors,
  tooltip: { ...baseTooltip, trigger: 'axis' },
  grid: { top: 20, right: 16, bottom: 20, left: 50, containLabel: false },
  xAxis: { type: 'category', data: props.data.map(d => new Date(d.timestamp).toLocaleTimeString()), ...baseAxis, boundaryGap: false },
  yAxis: { type: 'value', ...baseAxis },
  series: [{
    name: t('dashboard.activeClients'),
    type: 'line',
    smooth: true,
    showSymbol: false,
    lineStyle: { width: 2.5, color: colors[2] },
    areaStyle: makeAreaStyle(colors[2]),
    data: props.data.map(d => d.activeClients)
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
.chart-empty {
  height: 260px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--el-text-color-placeholder);
  font-size: 13px;
}
</style>
