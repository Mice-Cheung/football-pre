import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { OddsItem, OddsComparison, KellyItem, TacticsItem, OddsTraceability } from '@/types/odds'
import { getOddsComparison, getKellyByMatch, getTacticsByMatch, getOddsTraceability } from '@/api/odds'

export const useOddsStore = defineStore('odds', () => {
  const oddsComparison = ref<OddsComparison | null>(null)
  const kellyData = ref<KellyItem[]>([])
  const tacticsData = ref<TacticsItem[]>([])
  const traceability = ref<OddsTraceability | null>(null)
  const loading = ref(false)
  const traceLoading = ref(false)

  async function fetchOddsComparison(matchId: number) {
    loading.value = true
    oddsComparison.value = await getOddsComparison(matchId)
    loading.value = false
  }

  async function fetchKelly(matchId: number) {
    kellyData.value = await getKellyByMatch(matchId)
  }

  async function fetchTactics(matchId: number) {
    tacticsData.value = await getTacticsByMatch(matchId)
  }

  async function fetchTraceability(matchId: number, sourceType?: string) {
    traceLoading.value = true
    traceability.value = await getOddsTraceability(matchId, sourceType)
    traceLoading.value = false
  }

  return {
    oddsComparison, kellyData, tacticsData, traceability, loading, traceLoading,
    fetchOddsComparison, fetchKelly, fetchTactics, fetchTraceability,
  }
})
