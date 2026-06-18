<script setup lang="ts">
import { computed, ref } from 'vue'
import type { KellyItem } from '@/types/odds'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { BarChart3 } from 'lucide-vue-next'

use([CanvasRenderer, BarChart, TooltipComponent, LegendComponent, GridComponent])

const props = defineProps<{
  kellyData: KellyItem[]
}>()

const companies = computed(() => props.kellyData.map(d => d.company))

const option = computed(() => ({
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'axis',
    backgroundColor: '#22252d',
    borderColor: '#2a2d35',
    textStyle: { color: '#e8eaed' },
  },
  legend: {
    data: ['主胜凯利', '平局凯利', '客胜凯利'],
    textStyle: { color: '#9aa0a6' },
    top: 0,
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '8%',
    top: '12%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: companies.value,
    axisLabel: { color: '#9aa0a6', fontSize: 12 },
    axisLine: { lineStyle: { color: '#2a2d35' } },
  },
  yAxis: {
    type: 'value',
    min: 0.8,
    max: 1.0,
    axisLabel: { color: '#9aa0a6', fontSize: 11 },
    splitLine: { lineStyle: { color: '#2a2d35' } },
  },
  series: [
    {
      name: '主胜凯利',
      type: 'bar',
      data: props.kellyData.map(d => d.homeKelly),
      itemStyle: {
        color: '#e05555',
        borderRadius: [4, 4, 0, 0],
      },
    },
    {
      name: '平局凯利',
      type: 'bar',
      data: props.kellyData.map(d => d.drawKelly),
      itemStyle: {
        color: '#f0c040',
        borderRadius: [4, 4, 0, 0],
      },
    },
    {
      name: '客胜凯利',
      type: 'bar',
      data: props.kellyData.map(d => d.awayKelly),
      itemStyle: {
        color: '#3cb371',
        borderRadius: [4, 4, 0, 0],
      },
    },
  ],
}))
</script>

<template>
  <div>
    <div v-if="kellyData.length === 0" class="text-center py-12 text-text-muted">
      <BarChart3 class="w-12 h-12 mx-auto mb-3 opacity-40" />
      <p>暂无凯利指数数据</p>
    </div>

    <div v-else>
      <div class="mb-6" style="height: 350px;">
        <VChart :option="option" autoresize />
      </div>

      <!-- Data Table -->
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-football-card-hover">
              <th class="text-left py-3 px-4 text-text-muted font-medium">博彩公司</th>
              <th class="text-center py-3 px-4 text-football-red font-medium">主胜凯利</th>
              <th class="text-center py-3 px-4 text-football-gold font-medium">平局凯利</th>
              <th class="text-center py-3 px-4 text-football-primary-light font-medium">客胜凯利</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in kellyData" :key="item.id" class="border-b border-football-card-hover hover:bg-football-bg transition-colors">
              <td class="py-3 px-4 text-text-primary">{{ item.company }}</td>
              <td class="text-center py-3 px-4 text-text-primary font-mono">{{ item.homeKelly.toFixed(4) }}</td>
              <td class="text-center py-3 px-4 text-text-primary font-mono">{{ item.drawKelly.toFixed(4) }}</td>
              <td class="text-center py-3 px-4 text-text-primary font-mono">{{ item.awayKelly.toFixed(4) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
