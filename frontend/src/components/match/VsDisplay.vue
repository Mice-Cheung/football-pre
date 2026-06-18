<script setup lang="ts">
import type { Team } from '@/types/match'

defineProps<{
  homeTeam: Team
  awayTeam: Team
}>()
</script>

<template>
  <div class="flex items-center justify-between gap-4">
    <div class="flex flex-col items-center gap-1.5">
      <img :src="homeTeam.logoUrl" class="w-10 h-10 object-contain" />
      <span class="text-xs text-text-secondary">{{ homeTeam.shortName }}</span>
    </div>
    <span class="text-text-muted text-lg font-black">VS</span>
    <div class="flex flex-col items-center gap-1.5">
      <img :src="awayTeam.logoUrl" class="w-10 h-10 object-contain" />
      <span class="text-xs text-text-secondary">{{ awayTeam.shortName }}</span>
    </div>
  </div>
</template>
