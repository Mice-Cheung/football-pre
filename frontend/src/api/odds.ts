import request from './request'
import type { OddsItem, OddsComparison, KellyItem, TacticsItem, OddsTraceability } from '@/types/odds'

export function getOddsByMatch(matchId: number) {
  return request.get<any, OddsItem[]>(`/odds/match/${matchId}`)
}

export function getOddsComparison(matchId: number) {
  return request.get<any, OddsComparison>(`/odds/match/${matchId}/comparison`)
}

export function getKellyByMatch(matchId: number) {
  return request.get<any, KellyItem[]>(`/odds/kelly/match/${matchId}`)
}

export function getTacticsByMatch(matchId: number) {
  return request.get<any, TacticsItem[]>(`/tactics/match/${matchId}`)
}

/** 赔率追溯：完整历史变化 + 显著变化告警 */
export function getOddsTraceability(matchId: number, sourceType?: string) {
  const params = sourceType ? { sourceType } : {}
  return request.get<any, OddsTraceability>(`/odds/match/${matchId}/traceability`, { params })
}
