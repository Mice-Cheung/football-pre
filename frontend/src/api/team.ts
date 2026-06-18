import request from './request'
import type { Team, LineupItem } from '@/types/match'

export function getTeamById(id: number) {
  return request.get<any, Team>(`/teams/${id}`)
}

export function getTeamsByLeague(league: string) {
  return request.get<any, Team[]>('/teams', { params: { league } })
}

export function getLineupsByMatch(matchId: number) {
  return request.get<any, LineupItem[]>(`/teams/lineups/match/${matchId}`)
}

export function getLineupsByMatchAndTeam(matchId: number, teamId: number) {
  return request.get<any, LineupItem[]>(`/teams/lineups/match/${matchId}/team/${teamId}`)
}
