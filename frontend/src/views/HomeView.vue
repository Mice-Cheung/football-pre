<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useMatchStore } from '@/stores/match'
import { useOddsStore } from '@/stores/odds'
import MatchCard from '@/components/match/MatchCard.vue'
import MatchFilter from '@/components/match/MatchFilter.vue'
import { Flame, Trophy } from 'lucide-vue-next'

const matchStore = useMatchStore()
const oddsStore = useOddsStore()
const currentLeague = ref('')
const currentPage = ref(1)
const pageSize = 12

onMounted(async () => {
  await matchStore.fetchLeagues()
  await loadMatches()
})

async function loadMatches() {
  const params: any = { page: currentPage.value - 1, size: pageSize }
  if (currentLeague.value) params.league = currentLeague.value
  await matchStore.fetchMatches(params)
}

function handleLeagueChange(league: string) {
  currentLeague.value = league
  currentPage.value = 1
  loadMatches()
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadMatches()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(currentPage, () => {}, { immediate: true })
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 py-6">
    <!-- Hero Banner -->
    <div class="relative mb-8 rounded-2xl overflow-hidden bg-gradient-to-r from-football-primary-dark via-football-primary to-football-primary-light p-8">
      <div class="absolute inset-0 opacity-20">
        <div class="absolute top-0 right-0 w-64 h-64 bg-white/20 rounded-full -translate-y-1/2 translate-x-1/4" />
        <div class="absolute bottom-0 left-10 w-48 h-48 bg-football-gold/30 rounded-full translate-y-1/3" />
      </div>
      <div class="relative z-10 flex items-center justify-between">
        <div>
          <div class="flex items-center gap-2 mb-2">
            <Flame class="w-5 h-5 text-football-gold" />
            <span class="text-football-gold text-sm font-semibold tracking-wide">竞彩足球 · 今日赛程</span>
          </div>
          <h1 class="text-2xl md:text-3xl font-bold text-white mb-2">足球赛事数据平台</h1>
          <p class="text-white/70 text-sm max-w-lg">对阵阵容 · 技战术分析 · 凯利指数 · 中外赔率对比 — 全面覆盖竞彩足球赛事数据</p>
        </div>
        <div class="hidden lg:flex items-center gap-2">
          <Trophy class="w-10 h-10 text-football-gold opacity-80" />
        </div>
      </div>
    </div>

    <!-- League Filter -->
    <MatchFilter
      :leagues="matchStore.leagues"
      :active-league="currentLeague"
      @change="handleLeagueChange"
    />

    <!-- Match Cards Grid -->
    <div v-if="matchStore.loading" class="flex justify-center py-20">
      <el-icon class="is-loading text-football-primary-light" :size="32"><Loading /></el-icon>
    </div>

    <div v-else-if="matchStore.matches.length === 0" class="text-center py-20">
      <p class="text-text-muted text-lg">暂无比赛数据</p>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
      <MatchCard v-for="match in matchStore.matches" :key="match.id" :match="match" />
    </div>

    <!-- Pagination -->
    <div v-if="matchStore.totalPages > 1" class="flex justify-center mt-8">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="matchStore.totalPages * pageSize"
        layout="prev, pager, next"
        background
        @current-change="handlePageChange"
        class="football-pagination"
      />
    </div>
  </div>
</template>

<style scoped>
.football-pagination :deep(.el-pager li) {
  background: #22252d !important;
  color: #e8eaed !important;
  border-radius: 8px !important;
}
.football-pagination :deep(.el-pager li.is-active) {
  background: #2d8c4e !important;
}
.football-pagination :deep(.btn-prev),
.football-pagination :deep(.btn-next) {
  background: #22252d !important;
  color: #e8eaed !important;
  border-radius: 8px !important;
}
</style>
