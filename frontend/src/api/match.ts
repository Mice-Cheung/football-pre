import request from './request'
import type { Match, MatchDetail } from '@/types/match'
import type { PageResponse } from '@/types/api'

export function getMatches(params?: { league?: string; status?: string; page?: number; size?: number }) {
  return request.get<any, PageResponse<Match>>('/matches', { params })
}

export function getMatchById(id: number) {
  return request.get<any, Match>(`/matches/${id}`)
}

export function getMatchDetail(id: number) {
  return request.get<any, MatchDetail>(`/matches/${id}/detail`)
}

export function getLeagues() {
  return request.get<any, string[]>('/matches/leagues')
}
