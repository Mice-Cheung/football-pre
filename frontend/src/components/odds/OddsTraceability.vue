<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { OddsTraceability, CompanyOddsHistory, OddsAlert, OddsHistoryPoint } from '@/types/odds'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, GridComponent, MarkLineComponent } from 'echarts/components'
import {
  TrendingUp, TrendingDown, AlertTriangle, ShieldAlert, History, Filter, Eye, Landmark, Globe, CalendarClock
} from 'lucide-vue-next'

use([CanvasRenderer, LineChart, TooltipComponent, LegendComponent, GridComponent, MarkLineComponent])

const props = defineProps<{
  traceability: OddsTraceability | null
  homeTeamName?: string
  awayTeamName?: string
}>()

const sourceFilter = ref<'ALL' | 'NATIONAL_LOTTERY' | 'INTERNATIONAL'>('ALL')
const selectedCompany = ref<string | null>(null)
const showAllAlerts = ref(false)

const setSourceFilter = (key: string) => {
  sourceFilter.value = key as typeof sourceFilter.value
}

// 过滤后的公司列表
const filteredCompanies = computed(() => {
  if (!props.traceability?.companies) return []
  if (sourceFilter.value === 'ALL') return props.traceability.companies
  return props.traceability.companies.filter(c => c.sourceType === sourceFilter.value)
})

// 当公司列表变化时，自动选第一个
watch(filteredCompanies, (list) => {
  if (list.length > 0) {
    selectedCompany.value = list[0].company
  }
}, { immediate: true })

// 当前选中的公司
const currentCompany = computed(() =>
  filteredCompanies.value.find(c => c.company === selectedCompany.value) || null
)

// 告警过滤
const filteredAlerts = computed(() => {
  if (!props.traceability?.alerts) return []
  let list = props.traceability.alerts
  if (sourceFilter.value !== 'ALL') {
    list = list.filter(a => a.sourceType === sourceFilter.value)
  }
  return list.sort((a, b) => new Date(b.changeTime).getTime() - new Date(a.changeTime).getTime())
})

const displayedAlerts = computed(() => {
  if (showAllAlerts.value) return filteredAlerts.value
  return filteredAlerts.value.slice(0, 5)
})

const criticalCount = computed(() => filteredAlerts.value.filter(a => a.severity === 'CRITICAL').length)
const warnCount = computed(() => filteredAlerts.value.filter(a => a.severity === 'WARN').length)

// ===== ECharts 配置 =====
const chartOption = computed(() => {
  if (!currentCompany.value) return {}

  const history = currentCompany.value.history
  const times = history.map(p => {
    const d = new Date(p.recordedAt)
    return d.toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
  })

  const markAreas: any[] = []
  // 标注变化显著区域
  history.forEach((p) => {
    if (!p.isInitial) {
      const hasSignificant = (p.homeChange && Math.abs(p.homeChange) >= (props.traceability?.alertThreshold || 0.1)) ||
        (p.drawChange && Math.abs(p.drawChange) >= (props.traceability?.alertThreshold || 0.1)) ||
        (p.awayChange && Math.abs(p.awayChange) >= (props.traceability?.alertThreshold || 0.1))
      if (hasSignificant) {
        const idx = history.indexOf(p)
        if (idx > 0) {
          markAreas.push({
            name: '显著变化',
            itemStyle: { color: 'rgba(224, 85, 85, 0.08)' },
            data: [[{ xAxis: times[idx - 1] }, { xAxis: times[idx] }]]
          })
        }
      }
    }
  })

  return {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#22252d',
      borderColor: '#2a2d35',
      textStyle: { color: '#e8eaed', fontSize: 12 },
      formatter: (params: any[]) => {
        let html = `<div style="font-weight:bold;margin-bottom:4px">${params[0].axisValue}</div>`
        params.forEach(p => {
          html += `<div style="display:flex;align-items:center;gap:6px;margin:2px 0">
            <span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:${p.color}"></span>
            <span>${p.seriesName}: <b>${p.value.toFixed(2)}</b></span>
          </div>`
        })
        return html
      },
    },
    legend: {
      data: ['主胜', '平局', '客胜'],
      textStyle: { color: '#9aa0a6', fontSize: 11 },
      top: 0,
    },
    grid: {
      left: '8%',
      right: '8%',
      bottom: '12%',
      top: '15%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: times,
      boundaryGap: false,
      axisLabel: { color: '#9aa0a6', fontSize: 10, rotate: times.length > 6 ? 30 : 0 },
      axisLine: { lineStyle: { color: '#2a2d35' } },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#9aa0a6', fontSize: 11 },
      splitLine: { lineStyle: { color: '#2a2d35', type: 'dashed' } },
    },
    series: [
      {
        name: '主胜',
        type: 'line',
        data: history.map(p => p.homeWin),
        smooth: true,
        symbol: 'circle',
        symbolSize: history.map(p => p.isInitial ? 10 : 5),
        lineStyle: { color: '#e05555', width: 2 },
        itemStyle: { color: '#e05555' },
        markLine: {
          silent: true,
          symbol: 'none',
          lineStyle: { color: '#e05555', type: 'dashed', width: 1, opacity: 0.5 },
          label: { color: '#e05555', fontSize: 10, formatter: '初 {c}' },
          data: [{ name: '初盘', xAxis: 0 }],
        },
        markArea: { silent: true, data: markAreas },
      },
      {
        name: '平局',
        type: 'line',
        data: history.map(p => p.draw),
        smooth: true,
        symbol: 'circle',
        symbolSize: history.map(p => p.isInitial ? 10 : 5),
        lineStyle: { color: '#f0c040', width: 2 },
        itemStyle: { color: '#f0c040' },
        markLine: {
          silent: true,
          symbol: 'none',
          lineStyle: { color: '#f0c040', type: 'dashed', width: 1, opacity: 0.5 },
          label: { color: '#f0c040', fontSize: 10, formatter: '初 {c}' },
          data: [{ name: '初盘', xAxis: 0 }],
        },
      },
      {
        name: '客胜',
        type: 'line',
        data: history.map(p => p.awayWin),
        smooth: true,
        symbol: 'circle',
        symbolSize: history.map(p => p.isInitial ? 10 : 5),
        lineStyle: { color: '#3cb371', width: 2 },
        itemStyle: { color: '#3cb371' },
        markLine: {
          silent: true,
          symbol: 'none',
          lineStyle: { color: '#3cb371', type: 'dashed', width: 1, opacity: 0.5 },
          label: { color: '#3cb371', fontSize: 10, formatter: '初 {c}' },
          data: [{ name: '初盘', xAxis: 0 }],
        },
      },
    ],
  }
})

// ===== 工具函数 =====
function formatChange(val: number | null): string {
  if (val === null) return '-'
  if (val === 0) return '0.00'
  return val > 0 ? `+${val.toFixed(2)}` : val.toFixed(2)
}

function changeColor(val: number | null): string {
  if (val === null || val === 0) return 'text-text-muted'
  return val > 0 ? 'text-football-red' : 'text-football-primary-light'
}

function severityClass(severity: string): string {
  return severity === 'CRITICAL'
    ? 'bg-football-red/20 text-football-red border-football-red/30'
    : 'bg-football-gold/20 text-football-gold border-football-gold/30'
}

function alertTypeLabel(type: string): string {
  const map: Record<string, string> = { home: '主胜', draw: '平局', away: '客胜' }
  return map[type] || type
}

function formatTime(isoStr: string): string {
  const d = new Date(isoStr)
  return d.toLocaleString('zh-CN', {
    month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
  })
}

const hasData = computed(() => props.traceability && props.traceability.companies.length > 0)
const hasAlerts = computed(() => filteredAlerts.value.length > 0)
</script>

<template>
  <div>
    <!-- Empty State -->
    <div v-if="!hasData" class="text-center py-16 text-text-muted">
      <History class="w-14 h-14 mx-auto mb-4 opacity-30" />
      <p class="text-lg font-medium mb-1">暂无赔率追溯数据</p>
      <p class="text-sm text-text-muted">系统尚未采集到赔率历史变化记录</p>
      <p class="text-xs text-text-muted mt-2">需要先执行数据同步，等待多次拉取后生成变化记录</p>
    </div>

    <template v-else>
      <!-- Alert Banner -->
      <div v-if="hasAlerts" class="mb-5 space-y-2">
        <div
          v-for="alert in filteredAlerts.filter(a => a.severity === 'CRITICAL').slice(0, 3)"
          :key="'crit-' + alert.id"
          class="flex items-start gap-3 px-4 py-3 rounded-lg bg-football-red/10 border border-football-red/30 animate-fade-in"
        >
          <ShieldAlert class="w-5 h-5 text-football-red flex-shrink-0 mt-0.5" />
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 flex-wrap">
              <span class="text-football-red font-semibold text-sm">重大赔率异动</span>
              <span class="text-xs text-football-red/70 font-mono">{{ alert.company }}</span>
              <span class="text-xs bg-football-red/20 text-football-red px-1.5 py-0.5 rounded font-mono">
                {{ alertTypeLabel(alert.alertType) }}
              </span>
            </div>
            <div class="text-xs text-text-secondary mt-1">
              赔率从 <span class="font-mono text-text-primary">{{ alert.oldOdds.toFixed(2) }}</span>
              <TrendingUp v-if="alert.changeAmount > 0" class="w-3 h-3 inline text-football-red mx-1" />
              <TrendingDown v-else class="w-3 h-3 inline text-football-primary-light mx-1" />
              变为 <span class="font-mono text-text-primary font-bold">{{ alert.newOdds.toFixed(2) }}</span>
              <span class="ml-1">({{ formatChange(alert.changeAmount) }}, {{ alert.changePercent.toFixed(1) }}%)</span>
            </div>
            <div class="text-xs text-text-muted mt-1 flex items-center gap-1">
              <CalendarClock class="w-3 h-3" /> {{ formatTime(alert.changeTime) }}
            </div>
          </div>
        </div>
      </div>

      <!-- Filter Bar -->
      <div class="flex flex-wrap items-center gap-3 mb-5">
        <!-- Source type filter -->
        <div class="flex items-center gap-1 bg-football-bg rounded-lg p-1 border border-football-card-hover">
          <button
            v-for="filter in [
              { key: 'ALL', label: '全部机构' },
              { key: 'NATIONAL_LOTTERY', label: '国内彩票', icon: Landmark },
              { key: 'INTERNATIONAL', label: '海外机构', icon: Globe }
            ]"
            :key="filter.key"
            @click="setSourceFilter(filter.key)"
            :class="[
              'flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium transition-all duration-200',
              sourceFilter === (filter.key as typeof sourceFilter.value)
                ? 'bg-football-primary text-white shadow-sm'
                : 'text-text-secondary hover:text-text-primary'
            ]"
          >
            <component :is="filter.icon" v-if="filter.icon" class="w-3.5 h-3.5" />
            {{ filter.label }}
          </button>
        </div>

        <!-- Company selector -->
        <div class="flex items-center gap-2 ml-auto">
          <Eye class="w-4 h-4 text-text-muted" />
          <select
            v-model="selectedCompany"
            class="bg-football-bg border border-football-card-hover rounded-lg px-3 py-1.5 text-xs text-text-primary focus:outline-none focus:border-football-primary-light"
          >
            <option v-for="c in filteredCompanies" :key="c.company" :value="c.company">
              {{ c.company }}
            </option>
          </select>
        </div>
      </div>

      <!-- Company Summary Cards -->
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3 mb-5">
        <div
          v-for="c in filteredCompanies"
          :key="c.company"
          @click="selectedCompany = c.company"
          :class="[
            'bg-football-card rounded-xl border p-3 cursor-pointer transition-all duration-200 hover:-translate-y-0.5',
            selectedCompany === c.company
              ? 'border-football-primary-light shadow-lg shadow-football-primary-light/10'
              : 'border-football-card-hover hover:border-text-muted/30'
          ]"
        >
          <div class="flex items-center gap-1.5 mb-2">
            <Landmark v-if="c.sourceType === 'NATIONAL_LOTTERY'" class="w-3.5 h-3.5 text-football-gold" />
            <Globe v-else class="w-3.5 h-3.5 text-football-blue" />
            <span class="text-xs font-medium text-text-primary truncate">{{ c.company }}</span>
          </div>
          <div class="grid grid-cols-3 gap-1 text-center">
            <div>
              <div class="text-[10px] text-text-muted">主</div>
              <div class="text-xs font-mono font-bold" :class="changeColor(c.homeWinTotalChange)">
                {{ c.homeWinLive.toFixed(2) }}
              </div>
            </div>
            <div>
              <div class="text-[10px] text-text-muted">平</div>
              <div class="text-xs font-mono font-bold" :class="changeColor(c.drawTotalChange)">
                {{ c.drawLive.toFixed(2) }}
              </div>
            </div>
            <div>
              <div class="text-[10px] text-text-muted">客</div>
              <div class="text-xs font-mono font-bold" :class="changeColor(c.awayWinTotalChange)">
                {{ c.awayWinLive.toFixed(2) }}
              </div>
            </div>
          </div>
          <!-- Change indicator -->
          <div v-if="c.history.length > 1" class="mt-2 pt-2 border-t border-football-card-hover flex items-center justify-center gap-2 text-[10px]">
            <span class="text-text-muted">{{ c.history.length }}次记录</span>
            <span class="text-text-muted opacity-50">|</span>
            <span :class="changeColor(c.homeWinTotalChange)">
              <TrendingUp v-if="c.homeWinTotalChange > 0" class="w-3 h-3 inline" />
              <TrendingDown v-else-if="c.homeWinTotalChange < 0" class="w-3 h-3 inline" />
              {{ formatChange(c.homeWinTotalChange) }}
            </span>
          </div>
        </div>
      </div>

      <!-- Trend Chart -->
      <div class="bg-football-card rounded-xl border border-football-card-hover overflow-hidden mb-5">
        <div class="px-4 py-3 border-b border-football-card-hover flex items-center gap-2">
          <History class="w-4 h-4 text-football-primary-light" />
          <span class="text-sm font-semibold text-text-primary">
            {{ currentCompany?.company || '' }} — 赔率变化趋势
          </span>
          <span class="text-xs text-text-muted ml-auto">
            初→终: {{ currentCompany?.history.length || 0 }}个快照
          </span>
        </div>

        <div v-if="currentCompany && currentCompany.history.length >= 2" class="p-4" style="height: 350px;">
          <VChart :option="chartOption" autoresize />
        </div>
        <div v-else class="p-8 text-center text-text-muted text-sm">
          <p>数据点不足，无法绘制趋势图</p>
          <p class="text-xs mt-1">当前仅 {{ currentCompany?.history.length || 0 }} 个快照（需要 ≥2 个）</p>
        </div>
      </div>

      <!-- Initial vs Live Summary -->
      <div v-if="currentCompany && currentCompany.history.length > 0" class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-5">
        <!-- Initial Odds -->
        <div class="bg-football-bg rounded-xl border border-football-card-hover p-4">
          <div class="flex items-center gap-2 mb-3">
            <div class="w-3 h-3 rounded-full bg-football-gold"></div>
            <span class="text-xs font-semibold text-football-gold">初盘赔率</span>
          </div>
          <div class="grid grid-cols-3 gap-3 text-center">
            <div>
              <div class="text-xs text-text-muted mb-1">主胜</div>
              <div class="text-xl font-bold text-text-primary font-mono">{{ currentCompany.homeWinInit.toFixed(2) }}</div>
            </div>
            <div>
              <div class="text-xs text-text-muted mb-1">平局</div>
              <div class="text-xl font-bold text-text-primary font-mono">{{ currentCompany.drawInit.toFixed(2) }}</div>
            </div>
            <div>
              <div class="text-xs text-text-muted mb-1">客胜</div>
              <div class="text-xl font-bold text-text-primary font-mono">{{ currentCompany.awayWinInit.toFixed(2) }}</div>
            </div>
          </div>
          <div class="mt-2 text-center text-xs text-text-muted">
            {{ formatTime(currentCompany.history[0].recordedAt) }}
          </div>
        </div>

        <!-- Live Odds -->
        <div class="bg-football-bg rounded-xl border border-football-primary-light/30 p-4">
          <div class="flex items-center gap-2 mb-3">
            <div class="w-3 h-3 rounded-full bg-football-primary-light animate-pulse"></div>
            <span class="text-xs font-semibold text-football-primary-light">实时赔率</span>
          </div>
          <div class="grid grid-cols-3 gap-3 text-center">
            <div>
              <div class="text-xs text-text-muted mb-1">主胜</div>
              <div class="text-xl font-bold font-mono" :class="changeColor(currentCompany.homeWinTotalChange)">
                {{ currentCompany.homeWinLive.toFixed(2) }}
              </div>
              <div class="text-[10px] font-mono mt-0.5" :class="changeColor(currentCompany.homeWinTotalChange)">
                {{ formatChange(currentCompany.homeWinTotalChange) }}
              </div>
            </div>
            <div>
              <div class="text-xs text-text-muted mb-1">平局</div>
              <div class="text-xl font-bold font-mono" :class="changeColor(currentCompany.drawTotalChange)">
                {{ currentCompany.drawLive.toFixed(2) }}
              </div>
              <div class="text-[10px] font-mono mt-0.5" :class="changeColor(currentCompany.drawTotalChange)">
                {{ formatChange(currentCompany.drawTotalChange) }}
              </div>
            </div>
            <div>
              <div class="text-xs text-text-muted mb-1">客胜</div>
              <div class="text-xl font-bold font-mono" :class="changeColor(currentCompany.awayWinTotalChange)">
                {{ currentCompany.awayWinLive.toFixed(2) }}
              </div>
              <div class="text-[10px] font-mono mt-0.5" :class="changeColor(currentCompany.awayWinTotalChange)">
                {{ formatChange(currentCompany.awayWinTotalChange) }}
              </div>
            </div>
          </div>
          <div class="mt-2 text-center text-xs text-text-muted">
            {{ formatTime(currentCompany.history[currentCompany.history.length - 1].recordedAt) }}
          </div>
        </div>
      </div>

      <!-- All Alerts Table -->
      <div v-if="hasAlerts" class="bg-football-card rounded-xl border border-football-card-hover overflow-hidden">
        <div class="px-4 py-3 border-b border-football-card-hover flex items-center gap-2">
          <AlertTriangle class="w-4 h-4 text-football-gold" />
          <span class="text-sm font-semibold text-text-primary">赔率异动告警</span>
          <span class="text-xs ml-auto text-text-muted">
            <span v-if="criticalCount" class="text-football-red mr-2">严重 {{ criticalCount }}</span>
            <span v-if="warnCount" class="text-football-gold">提示 {{ warnCount }}</span>
          </span>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-xs">
            <thead>
              <tr class="border-b border-football-card-hover">
                <th class="text-left py-2.5 px-4 text-text-muted font-medium w-16">级别</th>
                <th class="text-left py-2.5 px-4 text-text-muted font-medium">机构</th>
                <th class="text-center py-2.5 px-4 text-text-muted font-medium">类型</th>
                <th class="text-right py-2.5 px-4 text-text-muted font-medium">变化前</th>
                <th class="text-right py-2.5 px-4 text-text-muted font-medium">变化后</th>
                <th class="text-right py-2.5 px-4 text-text-muted font-medium">变化</th>
                <th class="text-right py-2.5 px-4 text-text-muted font-medium">幅度%</th>
                <th class="text-right py-2.5 px-4 text-text-muted font-medium">时间</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="alert in displayedAlerts"
                :key="alert.id"
                class="border-b border-football-card-hover hover:bg-football-bg transition-colors"
              >
                <td class="py-2.5 px-4">
                  <span :class="['inline-block px-2 py-0.5 rounded text-[10px] font-semibold border', severityClass(alert.severity)]">
                    {{ alert.severity === 'CRITICAL' ? '严重' : '提示' }}
                  </span>
                </td>
                <td class="py-2.5 px-4 text-text-primary font-medium">{{ alert.company }}</td>
                <td class="py-2.5 px-4 text-center text-text-secondary">{{ alertTypeLabel(alert.alertType) }}</td>
                <td class="py-2.5 px-4 text-right font-mono text-text-secondary">{{ alert.oldOdds.toFixed(2) }}</td>
                <td class="py-2.5 px-4 text-right font-mono text-text-primary font-bold">{{ alert.newOdds.toFixed(2) }}</td>
                <td class="py-2.5 px-4 text-right font-mono" :class="changeColor(alert.changeAmount)">
                  {{ formatChange(alert.changeAmount) }}
                </td>
                <td class="py-2.5 px-4 text-right font-mono text-text-muted">{{ alert.changePercent.toFixed(1) }}%</td>
                <td class="py-2.5 px-4 text-right text-text-muted whitespace-nowrap">{{ formatTime(alert.changeTime) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div
          v-if="filteredAlerts.length > 5 && !showAllAlerts"
          class="px-4 py-2.5 border-t border-football-card-hover text-center"
        >
          <button
            @click="showAllAlerts = true"
            class="text-xs text-football-primary-light hover:text-football-blue transition-colors"
          >
            查看全部 {{ filteredAlerts.length }} 条告警
          </button>
        </div>
      </div>

      <!-- Info footer -->
      <div class="mt-4 text-center text-xs text-text-muted opacity-60">
        告警阈值: 赔率变化 ≥ {{ props.traceability?.alertThreshold || 0.1 }} 触发提示，≥ {{ ((props.traceability?.alertThreshold || 0.1) * 2).toFixed(2) }} 触发严重告警。
        每次数据同步自动检测赔率变化并记录。
      </div>
    </template>
  </div>
</template>
