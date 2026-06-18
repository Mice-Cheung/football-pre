import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Match, MatchDetail } from '@/types/match'
import { getMatches, getMatchById, getMatchDetail, getLeagues } from '@/api/match'

export const useMatchStore = defineStore('match', () => {
  const matches = ref<Match[]>([])
  const currentMatch = ref<Match | null>(null)
  const currentDetail = ref<MatchDetail | null>(null)
  const leagues = ref<string[]>([])
  const loading = ref(false)
  const totalPages = ref(0)
  const currentPage = ref(0)

  async function fetchMatches(params?: { league?: string; status?: string; page?: number; size?: number }) {
    loading.value = true
    const data = await getMatches({ ...params, size: params?.size || 20 })
    matches.value = data.content
    totalPages.value = data.totalPages
    currentPage.value = data.number
    loading.value = false
  }

  async function fetchMatchById(id: number) {
    currentMatch.value = await getMatchById(id)
  }

  async function fetchMatchDetail(id: number) {
    currentDetail.value = await getMatchDetail(id)
  }

  async function fetchLeagues() {
    leagues.value = await getLeagues()
  }

  return {
    matches, currentMatch, currentDetail, leagues, loading, totalPages, currentPage,
    fetchMatches, fetchMatchById, fetchMatchDetail, fetchLeagues,
  }
})
