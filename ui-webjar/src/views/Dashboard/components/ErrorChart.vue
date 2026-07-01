<template>
  <div class="chart-card">
    <div class="chart-title">{{ t('dashboard.errorChart') }}</div>
    <v-chart :option="chartOption" autoresize style="height: 280px" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import VChart from 'vue-echarts'

const { t } = useI18n()

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: props.data.map(d => new Date(d.timestamp).toLocaleTimeString()) },
  yAxis: { type: 'value', name: t('dashboard.errorsPerSec') },
  series: [
    { name: t('dashboard.errorRate'), type: 'line', smooth: true, data: props.data.map(d => d.errorsDelta) }
  ]
}))
</script>
