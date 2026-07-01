<template>
  <div class="chart-card">
    <div class="chart-title">{{ t('dashboard.qosChart') }}</div>
    <v-chart :option="chartOption" autoresize style="height: 280px" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, PieChart, TooltipComponent, LegendComponent])

const { t } = useI18n()

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const chartOption = computed(() => {
  const totals = { qos0: 0, qos1: 0, qos2: 0 }
  props.data.forEach(d => {
    totals.qos0 += d.qos0Delta || 0
    totals.qos1 += d.qos1Delta || 0
    totals.qos2 += d.qos2Delta || 0
  })
  return {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { name: 'QoS 0', value: totals.qos0 },
        { name: 'QoS 1', value: totals.qos1 },
        { name: 'QoS 2', value: totals.qos2 }
      ]
    }]
  }
})
</script>
