<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Match } from '@/types/match'
import { Clock, MapPin, ChevronRight } from 'lucide-vue-next'

const props = defineProps<{ match: Match }>()
const router = useRouter()

const formattedDate = computed(() => {
  const d = new Date(props.match.matchDate)
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
})

const formattedTime = computed(() => {
  const d = new Date(props.match.matchDate)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
})

const handicapText = computed(() => {
  if (!props.match.handicap || props.match.handicap === 0) return ''
  const val = props.match.handicap
  if (val < 0) return `主队让${Math.abs(val)}球`
  return `客队让${val}球`
})

const statusClass = computed(() => {
  switch (props.match.status) {
    case 'LIVE': return 'bg-football-red text-white animate-pulse'
    case 'FINISHED': return 'bg-text-muted text-white'
    default: return 'bg-football-primary text-white'
  }
})

const statusLabel = computed(() => {
  switch (props.match.status) {
    case 'LIVE': return '进行中'
    case 'FINISHED': return '已结束'
    default: return '未开赛'
  }
})

function goDetail() {
  router.push(`/matches/${props.match.id}`)
}
</script>

<template>
  <div
    class="group bg-football-card rounded-xl border border-football-card-hover overflow-hidden cursor-pointer transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:shadow-football-primary/5 hover:border-football-primary/30"
    @click="goDetail"
  >
    <!-- Card Header -->
    <div class="px-5 py-3 flex items-center justify-between border-b border-football-card-hover">
      <div class="flex items-center gap-2">
        <span class="text-xs font-mono text-football-primary-light bg-football-primary/10 px-2 py-0.5 rounded">
          {{ match.matchNo }}
        </span>
        <div class="flex items-center gap-1 text-text-muted text-xs">
          <MapPin class="w-3 h-3" />
          <span>{{ match.league }}</span>
        </div>
      </div>
      <span :class="['text-xs px-2 py-0.5 rounded-full', statusClass]">
        {{ statusLabel }}
      </span>
    </div>

    <!-- VS Section -->
    <div class="px-5 py-4">
      <div class="flex items-center justify-between mb-3">
        <span class="text-text-muted text-xs">{{ formattedDate }}</span>
        <span class="text-text-muted text-xs flex items-center gap-1">
          <Clock class="w-3 h-3" /> {{ formattedTime }}
        </span>
      </div>

      <div class="flex items-center justify-between">
        <!-- Home -->
        <div class="flex flex-col items-center gap-2 flex-1">
          <div class="w-12 h-12 rounded-full bg-football-bg border border-football-card-hover flex items-center justify-center overflow-hidden">
            <img :src="match.homeTeam.logoUrl" :alt="match.homeTeam.name" class="w-8 h-8 object-contain" />
          </div>
          <span class="text-sm font-semibold text-text-primary text-center leading-tight">{{ match.homeTeam.shortName }}</span>
        </div>

        <!-- VS -->
        <div class="flex flex-col items-center px-3">
          <div class="text-2xl font-black text-text-muted mb-1 animate-pulse-vs">VS</div>
          <div v-if="handicapText" class="text-xs text-football-gold bg-football-gold/10 px-1.5 py-0.5 rounded">
            {{ handicapText }}
          </div>
        </div>

        <!-- Away -->
        <div class="flex flex-col items-center gap-2 flex-1">
          <div class="w-12 h-12 rounded-full bg-football-bg border border-football-card-hover flex items-center justify-center overflow-hidden">
            <img :src="match.awayTeam.logoUrl" :alt="match.awayTeam.name" class="w-8 h-8 object-contain" />
          </div>
          <span class="text-sm font-semibold text-text-primary text-center leading-tight">{{ match.awayTeam.shortName }}</span>
        </div>
      </div>
    </div>

    <!-- Card Footer -->
    <div class="px-5 py-2.5 bg-football-bg/50 border-t border-football-card-hover flex items-center justify-between">
      <span class="text-xs text-text-muted">点击查看详情</span>
      <ChevronRight class="w-4 h-4 text-text-muted transition-transform group-hover:translate-x-1" />
    </div>
  </div>
</template>
