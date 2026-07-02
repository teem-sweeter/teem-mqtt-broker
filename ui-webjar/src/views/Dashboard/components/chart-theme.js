export const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#b37feb']

export function makeAreaStyle(color) {
  return {
    color: {
      type: 'linear',
      x: 0, y: 0, x2: 0, y2: 1,
      colorStops: [
        { offset: 0, color: color + '40' },
        { offset: 1, color: color + '05' }
      ]
    }
  }
}

export const baseAxis = {
  axisLine: { lineStyle: { color: '#E4E7ED' } },
  axisTick: { show: false },
  axisLabel: { color: '#909399', fontSize: 11 },
  splitLine: { lineStyle: { color: '#F2F3F5', type: 'dashed' } }
}

export const baseTooltip = {
  backgroundColor: 'rgba(255,255,255,0.95)',
  borderColor: '#E4E7ED',
  borderWidth: 1,
  textStyle: { color: '#303133', fontSize: 12 },
  extraCssText: 'box-shadow: 0 2px 12px rgba(0,0,0,0.1);border-radius: 8px;'
}

export const baseLegend = {
  textStyle: { color: '#606266', fontSize: 11 },
  itemWidth: 16,
  itemHeight: 8,
  itemGap: 20
}
