<template>
  <div class="chart-card">
    <div class="chart-title">{{ t('dashboard.topicChart') }}</div>
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

const chartOption = computed(() => {
  const latest = props.data.length > 0 ? props.data[props.data.length - 1] : null
  const topics = latest?.topTopics || []
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: topics.map(tp => tp.topic), axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', name: t('dashboard.messageCount') },
    series: [
      { name: t('dashboard.messageCount'), type: 'bar', data: topics.map(tp => tp.count) }
    ]
  }
})
</script>
