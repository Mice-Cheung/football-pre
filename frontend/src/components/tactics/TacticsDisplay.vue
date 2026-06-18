<script setup lang="ts">
import type { TacticsItem } from '@/types/odds'
import { Shield, Zap, Target, TrendingUp, Activity } from 'lucide-vue-next'

defineProps<{
  tactics: TacticsItem[]
}>()

function getRatingColor(rating: number): string {
  if (rating >= 90) return 'text-football-gold'
  if (rating >= 80) return 'text-football-primary-light'
  return 'text-text-secondary'
}

function getRatingBg(rating: number): string {
  if (rating >= 90) return 'bg-football-gold/10'
  if (rating >= 80) return 'bg-football-primary/10'
  return 'bg-football-card'
}
</script>

<template>
  <div v-if="tactics.length === 0" class="text-center py-12 text-text-muted">
    <Shield class="w-12 h-12 mx-auto mb-3 opacity-40" />
    <p>暂无技战术数据</p>
  </div>

  <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-6">
    <div v-for="t in tactics" :key="t.id"
         class="bg-football-bg rounded-xl border border-football-card-hover p-5">
      <!-- Header -->
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center gap-3">
          <div :class="['w-12 h-12 rounded-xl flex items-center justify-center text-lg font-black', getRatingBg(t.strengthRating), getRatingColor(t.strengthRating)]">
            {{ t.strengthRating }}
          </div>
          <div>
            <div class="text-text-primary font-semibold">{{ t.teamName }}</div>
            <div class="text-text-muted text-xs">{{ t.formation }}</div>
          </div>
        </div>
      </div>

      <!-- Stats Grid -->
      <div class="grid grid-cols-2 gap-3 mb-4">
        <div class="bg-football-card rounded-lg p-3">
          <div class="flex items-center gap-2 text-text-muted text-xs mb-1">
            <Zap class="w-3.5 h-3.5 text-orange-400" /> 进攻风格
          </div>
          <div class="text-text-primary text-sm font-medium">{{ t.attackStyle }}</div>
        </div>
        <div class="bg-football-card rounded-lg p-3">
          <div class="flex items-center gap-2 text-text-muted text-xs mb-1">
            <Shield class="w-3.5 h-3.5 text-blue-400" /> 防守风格
          </div>
          <div class="text-text-primary text-sm font-medium">{{ t.defenseStyle }}</div>
        </div>
        <div class="bg-football-card rounded-lg p-3">
          <div class="flex items-center gap-2 text-text-muted text-xs mb-1">
            <Activity class="w-3.5 h-3.5 text-football-primary-light" /> 场均控球率
          </div>
          <div class="text-text-primary text-sm font-medium">{{ t.possessionAvg }}%</div>
        </div>
        <div class="bg-football-card rounded-lg p-3">
          <div class="flex items-center gap-2 text-text-muted text-xs mb-1">
            <TrendingUp class="w-3.5 h-3.5 text-football-gold" /> 近期战绩
          </div>
          <div class="flex items-center gap-1">
            <span v-for="(ch, i) in (t.recentForm || '').split('')" :key="i"
                  :class="['text-xs font-bold px-1 py-0.5 rounded', ch === 'W' ? 'bg-football-primary/20 text-football-primary-light' : ch === 'D' ? 'bg-yellow-500/20 text-yellow-400' : 'bg-red-500/20 text-red-400']">
              {{ ch }}
            </span>
          </div>
        </div>
      </div>

      <!-- Description -->
      <div class="bg-football-card rounded-lg p-4">
        <p class="text-text-secondary text-sm leading-relaxed">{{ t.description }}</p>
      </div>
    </div>
  </div>
</template>
