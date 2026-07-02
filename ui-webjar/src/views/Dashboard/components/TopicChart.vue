<template>
  <div class="chart-card">
    <div class="chart-header">
      <div class="chart-title">{{ t('dashboard.topicChart') }}</div>
      <div class="chart-badge" v-if="hasData">Top {{ topics.length }}</div>
    </div>
    <div class="chart-body" v-if="hasData">
      <v-chart :option="chartOption" autoresize style="height: 260px" />
    </div>
    <div class="chart-empty" v-else>
      <el-icon :size="40" color="#C0C4CC"><Histogram /></el-icon>
      <span>{{ t('dashboard.noData') }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Histogram } from '@element-plus/icons-vue'
import './echarts-setup'
import VChart from 'vue-echarts'
import { colors, baseAxis, baseTooltip } from './chart-theme'

const { t } = useI18n()

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const hasData = computed(() => {
  const latest = props.data.length > 0 ? props.data[props.data.length - 1] : null
  return latest?.topTopics?.length > 0
})

const topics = computed(() => {
  const latest = props.data.length > 0 ? props.data[props.data.length - 1] : null
  return latest?.topTopics || []
})

const chartOption = computed(() => ({
  color: colors,
  tooltip: { ...baseTooltip, trigger: 'axis' },
  grid: { top: 16, right: 16, bottom: 20, left: 50, containLabel: false },
  xAxis: { type: 'category', data: topics.value.map(tp => tp.topic), ...baseAxis, axisLabel: { ...baseAxis.axisLabel, rotate: 20, fontSize: 10 } },
  yAxis: { type: 'value', ...baseAxis },
  series: [{
    name: t('dashboard.messageCount'),
    type: 'bar',
    barWidth: '50%',
    itemStyle: {
      borderRadius: [4, 4, 0, 0],
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: colors[0] },
          { offset: 1, color: colors[0] + '60' }
        ]
      }
    },
    data: topics.value.map(tp => tp.count)
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
