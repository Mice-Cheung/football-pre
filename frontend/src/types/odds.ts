export type OddsSourceType = 'NATIONAL_LOTTERY' | 'INTERNATIONAL'

export interface OddsItem {
  id: number
  matchId: number
  company: string
  sourceType: OddsSourceType
  homeWin: number
  draw: number
  awayWin: number
  homeWinInit: number
  drawInit: number
  awayWinInit: number
  updatedAt: string
}

export interface OddsComparison {
  nationalOdds: OddsItem[]
  internationalOdds: OddsItem[]
  homeWinDiff: number
  drawDiff: number
  awayWinDiff: number
}

export interface KellyItem {
  id: number
  matchId: number
  company: string
  homeKelly: number
  drawKelly: number
  awayKelly: number
  updatedAt: string
}

export interface TacticsItem {
  id: number
  matchId: number
  teamId: number
  teamName: string
  attackStyle: string
  defenseStyle: string
  formation: string
  description: string
  strengthRating: number
  possessionAvg: number
  recentForm: string
}

// ========== 赔率追溯 ==========

export interface OddsHistoryPoint {
  id: number
  recordedAt: string
  homeWin: number
  draw: number
  awayWin: number
  homeChange: number | null
  drawChange: number | null
  awayChange: number | null
  isInitial: boolean
}

export interface CompanyOddsHistory {
  company: string
  sourceType: OddsSourceType
  history: OddsHistoryPoint[]
  homeWinInit: number
  drawInit: number
  awayWinInit: number
  homeWinLive: number
  drawLive: number
  awayWinLive: number
  homeWinTotalChange: number
  drawTotalChange: number
  awayWinTotalChange: number
}

export interface OddsAlert {
  id: number
  matchId: number
  company: string
  sourceType: OddsSourceType
  alertType: 'home' | 'draw' | 'away'
  severity: 'WARN' | 'CRITICAL'
  oldOdds: number
  newOdds: number
  changeAmount: number
  changePercent: number
  changeTime: string
}

export interface OddsTraceability {
  companies: CompanyOddsHistory[]
  alerts: OddsAlert[]
  alertThreshold: number
}
