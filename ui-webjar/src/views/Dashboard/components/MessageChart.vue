<template>
  <div class="chart-card">
    <div class="chart-title">{{ t('dashboard.messageChart') }}</div>
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
  legend: { data: [t('dashboard.publishRate'), t('dashboard.receiveRate')], bottom: 0 },
  xAxis: { type: 'category', data: props.data.map(d => new Date(d.timestamp).toLocaleTimeString()) },
  yAxis: { type: 'value', name: t('dashboard.msgPerSec') },
  series: [
    { name: t('dashboard.publishRate'), type: 'line', smooth: true, data: props.data.map(d => d.publishDelta) },
    { name: t('dashboard.receiveRate'), type: 'line', smooth: true, data: props.data.map(d => d.receiveDelta) }
  ]
}))
</script>
