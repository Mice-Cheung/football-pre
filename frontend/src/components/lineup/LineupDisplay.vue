<script setup lang="ts">
import { computed } from 'vue'
import type { LineupItem, Team } from '@/types/match'
import { Users } from 'lucide-vue-next'

const props = defineProps<{
  homeLineups: LineupItem[]
  awayLineups: LineupItem[]
  homeTeam: Team
  awayTeam: Team
}>()

const homeStarters = computed(() => props.homeLineups.filter(l => l.isStarter).sort((a, b) => a.sortOrder - b.sortOrder))
const awayStarters = computed(() => props.awayLineups.filter(l => l.isStarter).sort((a, b) => a.sortOrder - b.sortOrder))

const posLabels: Record<string, string> = { GK: '守门员', DF: '后卫', MF: '中场', FW: '前锋' }
const posColors: Record<string, string> = {
  GK: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/40',
  DF: 'bg-blue-500/20 text-blue-400 border-blue-500/40',
  MF: 'bg-green-500/20 text-green-400 border-green-500/40',
  FW: 'bg-red-500/20 text-red-400 border-red-500/40',
}

function getPlayerStyle(index: number, total: number): string {
  // Simulated formation positions based on 4-3-3
  if (index === 0) return 'col-start-2 col-span-1' // GK
  if (index <= 4) return `col-span-1` // Defenders
  if (index <= 7) return `col-span-1` // Midfielders
  return `col-span-1` // Forwards
}

function getGridStyle(starters: LineupItem[]): string {
  if (starters.length >= 11) return 'grid-cols-5'
  return 'grid-cols-4'
}
</script>

<template>
  <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
    <!-- Home Team -->
    <div class="bg-football-bg rounded-xl border border-football-card-hover p-4">
      <div class="flex items-center gap-3 mb-4">
        <img :src="homeTeam.logoUrl" class="w-10 h-10 object-contain" />
        <div>
          <div class="text-text-primary font-semibold">{{ homeTeam.name }}</div>
          <div class="text-text-muted text-xs">{{ homeTeam.defaultFormation }} · {{ homeTeam.coach }}</div>
        </div>
      </div>

      <!-- Formation Pitch -->
      <div class="relative rounded-lg overflow-hidden mb-4" style="background: linear-gradient(180deg, #1a3a1a 0%, #2d5a2d 50%, #1a3a1a 100%); min-height: 320px;">
        <div class="absolute inset-0 flex flex-col justify-between p-3">
          <div v-for="(row, rowIdx) in [[0], [1,2,3,4], [5,6,7], [8,9,10]]" :key="rowIdx"
               class="flex justify-around items-center"
               :class="rowIdx === 0 ? 'mb-8' : rowIdx === 1 ? 'mb-6' : rowIdx === 2 ? 'mb-4' : ''">
            <div v-for="playerIdx in row" :key="playerIdx"
                 class="flex flex-col items-center gap-1">
              <div v-if="homeStarters[playerIdx]"
                   :class="['w-10 h-10 rounded-full flex items-center justify-center text-xs font-bold border-2', posColors[homeStarters[playerIdx]?.position || 'MF']]">
                {{ homeStarters[playerIdx]?.number }}
              </div>
              <span v-if="homeStarters[playerIdx]" class="text-white text-xs max-w-[60px] text-center leading-tight">
                {{ homeStarters[playerIdx]?.playerName }}
              </span>
            </div>
          </div>
        </div>
        <!-- Pitch lines -->
        <div class="absolute top-1/2 left-0 right-0 h-px bg-white/20" />
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-16 h-16 rounded-full border border-white/20" />
      </div>

      <!-- Player List -->
      <div class="space-y-1.5 mt-3">
        <div v-for="player in homeStarters" :key="player.id"
             class="flex items-center gap-3 px-2 py-1.5 rounded-lg hover:bg-football-card transition-colors text-sm">
          <span class="w-6 h-6 rounded-full bg-football-primary/20 text-football-primary-light flex items-center justify-center text-xs font-bold">
            {{ player.number }}
          </span>
          <span class="text-text-primary flex-1">{{ player.playerName }}</span>
          <span :class="['text-xs px-2 py-0.5 rounded-full border', posColors[player.position]]">
            {{ posLabels[player.position] }}
          </span>
        </div>
      </div>
    </div>

    <!-- Away Team -->
    <div class="bg-football-bg rounded-xl border border-football-card-hover p-4">
      <div class="flex items-center gap-3 mb-4">
        <img :src="awayTeam.logoUrl" class="w-10 h-10 object-contain" />
        <div>
          <div class="text-text-primary font-semibold">{{ awayTeam.name }}</div>
          <div class="text-text-muted text-xs">{{ awayTeam.defaultFormation }} · {{ awayTeam.coach }}</div>
        </div>
      </div>

      <!-- Formation Pitch -->
      <div class="relative rounded-lg overflow-hidden mb-4" style="background: linear-gradient(180deg, #1a3a1a 0%, #2d5a2d 50%, #1a3a1a 100%); min-height: 320px;">
        <div class="absolute inset-0 flex flex-col justify-between p-3">
          <div v-for="(row, rowIdx) in [[0], [1,2,3,4], [5,6,7], [8,9,10]]" :key="rowIdx"
               class="flex justify-around items-center"
               :class="rowIdx === 0 ? 'mb-8' : rowIdx === 1 ? 'mb-6' : rowIdx === 2 ? 'mb-4' : ''">
            <div v-for="playerIdx in row" :key="playerIdx"
                 class="flex flex-col items-center gap-1">
              <div v-if="awayStarters[playerIdx]"
                   :class="['w-10 h-10 rounded-full flex items-center justify-center text-xs font-bold border-2', posColors[awayStarters[playerIdx]?.position || 'MF']]">
                {{ awayStarters[playerIdx]?.number }}
              </div>
              <span v-if="awayStarters[playerIdx]" class="text-white text-xs max-w-[60px] text-center leading-tight">
                {{ awayStarters[playerIdx]?.playerName }}
              </span>
            </div>
          </div>
        </div>
        <div class="absolute top-1/2 left-0 right-0 h-px bg-white/20" />
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-16 h-16 rounded-full border border-white/20" />
      </div>

      <!-- Player List -->
      <div class="space-y-1.5 mt-3">
        <div v-for="player in awayStarters" :key="player.id"
             class="flex items-center gap-3 px-2 py-1.5 rounded-lg hover:bg-football-card transition-colors text-sm">
          <span class="w-6 h-6 rounded-full bg-football-primary/20 text-football-primary-light flex items-center justify-center text-xs font-bold">
            {{ player.number }}
          </span>
          <span class="text-text-primary flex-1">{{ player.playerName }}</span>
          <span :class="['text-xs px-2 py-0.5 rounded-full border', posColors[player.position]]">
            {{ posLabels[player.position] }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
