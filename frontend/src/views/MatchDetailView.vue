<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMatchStore } from '@/stores/match'
import { useOddsStore } from '@/stores/odds'
import { ArrowLeft, Clock, MapPin } from 'lucide-vue-next'
import LineupDisplay from '@/components/lineup/LineupDisplay.vue'
import TacticsDisplay from '@/components/tactics/TacticsDisplay.vue'
import KellyChart from '@/components/odds/KellyChart.vue'
import OddsComparisonView from '@/components/odds/OddsComparison.vue'
import OddsTraceabilityView from '@/components/odds/OddsTraceability.vue'

const route = useRoute()
const router = useRouter()
const matchStore = useMatchStore()
const oddsStore = useOddsStore()

const activeTab = ref('lineup')
const matchId = ref(Number(route.params.id))

onMounted(async () => {
  await matchStore.fetchMatchById(matchId.value)
  await matchStore.fetchMatchDetail(matchId.value)
  await oddsStore.fetchOddsComparison(matchId.value)
  await oddsStore.fetchKelly(matchId.value)
  await oddsStore.fetchTactics(matchId.value)
  await oddsStore.fetchTraceability(matchId.value)
})

function goBack() {
  router.push('/')
}

const tabs = [
  { key: 'lineup', label: '对阵阵容' },
  { key: 'tactics', label: '技战术打法' },
  { key: 'traceability', label: '赔率追溯' },
  { key: 'kelly', label: '凯利指数' },
  { key: 'odds', label: '赔率对比' },
]
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 py-6">
    <!-- Back -->
    <button @click="goBack" class="flex items-center gap-1.5 text-text-muted hover:text-football-primary-light transition-colors mb-4 text-sm">
      <ArrowLeft class="w-4 h-4" /> 返回比赛列表
    </button>

    <!-- Match Banner -->
    <div v-if="matchStore.currentMatch" class="relative rounded-2xl overflow-hidden mb-6">
      <div class="absolute inset-0 bg-gradient-to-r from-football-card via-football-card/95 to-football-card" />
      <div class="relative z-10 p-6">
        <div class="flex items-center justify-center gap-2 mb-3 text-text-muted text-sm">
          <MapPin class="w-4 h-4" />
          <span>{{ matchStore.currentMatch.league }}</span>
          <span class="mx-1">·</span>
          <span class="text-football-primary-light font-mono">{{ matchStore.currentMatch.matchNo }}</span>
          <span class="mx-1">·</span>
          <Clock class="w-4 h-4" />
          <span>{{ new Date(matchStore.currentMatch.matchDate).toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }) }}</span>
        </div>

        <div class="flex items-center justify-center gap-6 md:gap-12">
          <div class="flex flex-col items-center gap-2">
            <img :src="matchStore.currentMatch.homeTeam.logoUrl" class="w-16 h-16 md:w-20 md:h-20 object-contain" />
            <span class="text-text-primary font-bold text-lg md:text-xl">{{ matchStore.currentMatch.homeTeam.shortName }}</span>
          </div>

          <div class="flex flex-col items-center">
            <div class="text-3xl md:text-4xl font-black text-text-muted mb-1">VS</div>
            <div v-if="matchStore.currentMatch.handicap" class="text-sm text-football-gold bg-football-gold/10 px-2 py-0.5 rounded-full">
              让球 {{ matchStore.currentMatch.handicap > 0 ? '+' : '' }}{{ matchStore.currentMatch.handicap }}
            </div>
            <div v-if="matchStore.currentMatch.status !== 'PENDING'" class="text-2xl font-bold text-text-primary mt-2">
              {{ matchStore.currentMatch.homeScore }} - {{ matchStore.currentMatch.awayScore }}
            </div>
          </div>

          <div class="flex flex-col items-center gap-2">
            <img :src="matchStore.currentMatch.awayTeam.logoUrl" class="w-16 h-16 md:w-20 md:h-20 object-contain" />
            <span class="text-text-primary font-bold text-lg md:text-xl">{{ matchStore.currentMatch.awayTeam.shortName }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="bg-football-card rounded-xl border border-football-card-hover overflow-hidden">
      <div class="flex border-b border-football-card-hover overflow-x-auto">
        <button
          v-for="tab in tabs" :key="tab.key"
          @click="activeTab = tab.key"
          :class="[
            'px-5 py-3 text-sm font-medium transition-all duration-200 whitespace-nowrap border-b-2',
            activeTab === tab.key
              ? 'text-football-primary-light border-football-primary-light'
              : 'text-text-muted border-transparent hover:text-text-secondary'
          ]"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="p-5">
        <transition name="fade" mode="out-in">
          <LineupDisplay
            v-if="activeTab === 'lineup' && matchStore.currentDetail"
            :key="'lineup'"
            :home-lineups="matchStore.currentDetail.homeLineups"
            :away-lineups="matchStore.currentDetail.awayLineups"
            :home-team="matchStore.currentMatch!.homeTeam"
            :away-team="matchStore.currentMatch!.awayTeam"
          />
          <TacticsDisplay
            v-else-if="activeTab === 'tactics'"
            :key="'tactics'"
            :tactics="oddsStore.tacticsData"
          />
          <OddsTraceabilityView
            v-else-if="activeTab === 'traceability' && matchStore.currentMatch"
            :key="'traceability'"
            :traceability="oddsStore.traceability"
            :home-team-name="matchStore.currentMatch.homeTeam.name"
            :away-team-name="matchStore.currentMatch.awayTeam.name"
          />
          <KellyChart
            v-else-if="activeTab === 'kelly'"
            :key="'kelly'"
            :kelly-data="oddsStore.kellyData"
          />
          <OddsComparisonView
            v-else-if="activeTab === 'odds'"
            :key="'odds'"
            :comparison="oddsStore.oddsComparison"
          />
        </transition>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
