<script setup lang="ts">
import { computed } from 'vue'
import type { OddsComparison } from '@/types/odds'
import { Landmark, Globe, TrendingDown, TrendingUp, Minus } from 'lucide-vue-next'

const props = defineProps<{
  comparison: OddsComparison | null
}>()

const national = computed(() => props.comparison?.nationalOdds?.[0] || null)
const international = computed(() => props.comparison?.internationalOdds || [])

function getTrend(init: number, current: number): 'up' | 'down' | 'same' {
  const diff = current - init
  if (Math.abs(diff) < 0.02) return 'same'
  return diff > 0 ? 'up' : 'down'
}

function getDiffClass(diff: number): string {
  if (Math.abs(diff) > 0.15) return 'text-football-red'
  if (Math.abs(diff) > 0.08) return 'text-football-gold'
  return 'text-football-primary-light'
}

function formatOddsChange(init: number, current: number): string {
  const diff = (current - init).toFixed(2)
  return diff.startsWith('-') ? `${diff}` : `+${diff}`
}
</script>

<template>
  <div>
    <div v-if="!comparison || (!national && international.length === 0)" class="text-center py-12 text-text-muted">
      <Globe class="w-12 h-12 mx-auto mb-3 opacity-40" />
      <p>暂无赔率对比数据</p>
    </div>

    <template v-else>
      <!-- Summary: China Lottery vs International -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-6">
        <!-- China Sports Lottery -->
        <div class="bg-football-bg rounded-xl border-2 border-football-gold/40 p-4">
          <div class="flex items-center gap-2 mb-3">
            <Landmark class="w-5 h-5 text-football-gold" />
            <span class="text-football-gold font-semibold text-sm">中国体育彩票</span>
          </div>
          <div v-if="national" class="grid grid-cols-3 gap-2">
            <div class="text-center bg-football-gold/5 rounded-lg p-3">
              <div class="text-text-muted text-xs mb-1">主胜</div>
              <div class="text-xl font-bold text-football-gold font-mono">{{ national.homeWin.toFixed(2) }}</div>
              <div class="text-xs mt-1" :class="getTrend(national.homeWinInit, national.homeWin) === 'up' ? 'text-football-red' : 'text-football-primary-light'">
                <TrendingUp v-if="getTrend(national.homeWinInit, national.homeWin) === 'up'" class="w-3 h-3 inline" />
                <TrendingDown v-else-if="getTrend(national.homeWinInit, national.homeWin) === 'down'" class="w-3 h-3 inline" />
                <Minus v-else class="w-3 h-3 inline" />
                初 {{ national.homeWinInit.toFixed(2) }}
              </div>
            </div>
            <div class="text-center bg-football-gold/5 rounded-lg p-3">
              <div class="text-text-muted text-xs mb-1">平局</div>
              <div class="text-xl font-bold text-football-gold font-mono">{{ national.draw.toFixed(2) }}</div>
              <div class="text-xs mt-1" :class="getTrend(national.drawInit, national.draw) === 'up' ? 'text-football-red' : 'text-football-primary-light'">
                <TrendingUp v-if="getTrend(national.drawInit, national.draw) === 'up'" class="w-3 h-3 inline" />
                <TrendingDown v-else-if="getTrend(national.drawInit, national.draw) === 'down'" class="w-3 h-3 inline" />
                <Minus v-else class="w-3 h-3 inline" />
                初 {{ national.drawInit.toFixed(2) }}
              </div>
            </div>
            <div class="text-center bg-football-gold/5 rounded-lg p-3">
              <div class="text-text-muted text-xs mb-1">客胜</div>
              <div class="text-xl font-bold text-football-gold font-mono">{{ national.awayWin.toFixed(2) }}</div>
              <div class="text-xs mt-1" :class="getTrend(national.awayWinInit, national.awayWin) === 'up' ? 'text-football-red' : 'text-football-primary-light'">
                <TrendingUp v-if="getTrend(national.awayWinInit, national.awayWin) === 'up'" class="w-3 h-3 inline" />
                <TrendingDown v-else-if="getTrend(national.awayWinInit, national.awayWin) === 'down'" class="w-3 h-3 inline" />
                <Minus v-else class="w-3 h-3 inline" />
                初 {{ national.awayWinInit.toFixed(2) }}
              </div>
            </div>
          </div>
        </div>

        <!-- Difference Summary -->
        <div class="bg-football-bg rounded-xl border border-football-card-hover p-4">
          <div class="flex items-center gap-2 mb-3">
            <Globe class="w-5 h-5 text-football-blue" />
            <span class="text-football-blue font-semibold text-sm">中外赔率最大差异</span>
          </div>
          <div class="grid grid-cols-3 gap-2">
            <div class="text-center bg-football-card rounded-lg p-3">
              <div class="text-text-muted text-xs mb-1">主胜差异</div>
              <div :class="['text-xl font-bold font-mono', getDiffClass(comparison.homeWinDiff)]">
                {{ comparison.homeWinDiff.toFixed(2) }}
              </div>
            </div>
            <div class="text-center bg-football-card rounded-lg p-3">
              <div class="text-text-muted text-xs mb-1">平局差异</div>
              <div :class="['text-xl font-bold font-mono', getDiffClass(comparison.drawDiff)]">
                {{ comparison.drawDiff.toFixed(2) }}
              </div>
            </div>
            <div class="text-center bg-football-card rounded-lg p-3">
              <div class="text-text-muted text-xs mb-1">客胜差异</div>
              <div :class="['text-xl font-bold font-mono', getDiffClass(comparison.awayWinDiff)]">
                {{ comparison.awayWinDiff.toFixed(2) }}
              </div>
            </div>
          </div>
          <div class="mt-3 text-xs text-text-muted text-center">
            差异值越大，表示中外赔率分歧越大
          </div>
        </div>
      </div>

      <!-- International Institutions Table -->
      <div class="bg-football-bg rounded-xl border border-football-card-hover overflow-hidden">
        <div class="px-4 py-3 border-b border-football-card-hover flex items-center gap-2">
          <Globe class="w-4 h-4 text-football-blue" />
          <span class="text-sm font-semibold text-text-primary">海外主流机构赔率对比</span>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-football-card-hover">
                <th class="text-left py-3 px-4 text-text-muted font-medium">机构</th>
                <th class="text-center py-3 px-4 text-text-muted font-medium">主胜</th>
                <th class="text-center py-3 px-4 text-text-muted font-medium">变化</th>
                <th class="text-center py-3 px-4 text-text-muted font-medium">平局</th>
                <th class="text-center py-3 px-4 text-text-muted font-medium">变化</th>
                <th class="text-center py-3 px-4 text-text-muted font-medium">客胜</th>
                <th class="text-center py-3 px-4 text-text-muted font-medium">变化</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in international" :key="item.id"
                  class="border-b border-football-card-hover hover:bg-football-card transition-colors">
                <td class="py-3 px-4 text-text-primary font-medium">{{ item.company }}</td>
                <td class="text-center py-3 px-4 text-text-primary font-mono">{{ item.homeWin.toFixed(2) }}</td>
                <td class="text-center py-3 px-4 font-mono text-xs"
                    :class="getTrend(item.homeWinInit, item.homeWin) === 'up' ? 'text-football-red' : getTrend(item.homeWinInit, item.homeWin) === 'down' ? 'text-football-primary-light' : 'text-text-muted'">
                  {{ formatOddsChange(item.homeWinInit, item.homeWin) }}
                </td>
                <td class="text-center py-3 px-4 text-text-primary font-mono">{{ item.draw.toFixed(2) }}</td>
                <td class="text-center py-3 px-4 font-mono text-xs"
                    :class="getTrend(item.drawInit, item.draw) === 'up' ? 'text-football-red' : getTrend(item.drawInit, item.draw) === 'down' ? 'text-football-primary-light' : 'text-text-muted'">
                  {{ formatOddsChange(item.drawInit, item.draw) }}
                </td>
                <td class="text-center py-3 px-4 text-text-primary font-mono">{{ item.awayWin.toFixed(2) }}</td>
                <td class="text-center py-3 px-4 font-mono text-xs"
                    :class="getTrend(item.awayWinInit, item.awayWin) === 'up' ? 'text-football-red' : getTrend(item.awayWinInit, item.awayWin) === 'down' ? 'text-football-primary-light' : 'text-text-muted'">
                  {{ formatOddsChange(item.awayWinInit, item.awayWin) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
