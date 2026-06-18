<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getTeamById } from '@/api/team'
import type { Team } from '@/types/match'
import { ArrowLeft, User, MapPin, Award } from 'lucide-vue-next'

const route = useRoute()
const team = ref<Team | null>(null)
const loading = ref(true)

onMounted(async () => {
  const id = Number(route.params.id)
  team.value = await getTeamById(id)
  loading.value = false
})
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-6">
    <router-link to="/" class="flex items-center gap-1.5 text-text-muted hover:text-football-primary-light transition-colors mb-6 text-sm">
      <ArrowLeft class="w-4 h-4" /> 返回首页
    </router-link>

    <div v-if="loading" class="flex justify-center py-20">
      <el-icon class="is-loading text-football-primary-light" :size="32"><Loading /></el-icon>
    </div>

    <div v-else-if="team" class="bg-football-card rounded-2xl border border-football-card-hover overflow-hidden">
      <div class="p-8 flex flex-col items-center text-center">
        <img :src="team.logoUrl" class="w-24 h-24 mb-4 object-contain" />
        <h1 class="text-2xl font-bold text-text-primary mb-1">{{ team.name }}</h1>
        <p class="text-text-muted text-sm mb-4">{{ team.nameEn }}</p>

        <div class="flex flex-wrap justify-center gap-3">
          <div class="flex items-center gap-1.5 bg-football-bg rounded-lg px-3 py-1.5 text-sm text-text-secondary">
            <User class="w-4 h-4 text-football-primary-light" /> {{ team.coach }}
          </div>
          <div class="flex items-center gap-1.5 bg-football-bg rounded-lg px-3 py-1.5 text-sm text-text-secondary">
            <MapPin class="w-4 h-4 text-football-primary-light" /> {{ team.league }}
          </div>
          <div class="flex items-center gap-1.5 bg-football-bg rounded-lg px-3 py-1.5 text-sm text-text-secondary">
            <Award class="w-4 h-4 text-football-gold" /> {{ team.country }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
