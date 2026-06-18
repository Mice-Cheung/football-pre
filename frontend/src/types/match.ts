export interface Team {
  id: number
  name: string
  nameEn: string
  shortName: string
  logoUrl: string
  coach: string
  defaultFormation: string
  league: string
  country: string
  teamColor: string
}

export interface Match {
  id: number
  matchNo: string
  league: string
  homeTeam: Team
  awayTeam: Team
  matchDate: string
  handicap: number
  status: 'PENDING' | 'LIVE' | 'FINISHED'
  homeScore: number | null
  awayScore: number | null
}

export interface MatchDetail {
  match: Match
  homeLineups: LineupItem[]
  awayLineups: LineupItem[]
}

export interface LineupItem {
  id: number
  matchId: number
  teamId: number
  playerName: string
  number: number
  position: 'GK' | 'DF' | 'MF' | 'FW'
  isStarter: boolean
  sortOrder: number
}
