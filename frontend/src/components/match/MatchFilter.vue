<script setup lang="ts">
import { ListFilter } from 'lucide-vue-next'

defineProps<{
  leagues: string[]
  activeLeague: string
}>()

const emit = defineEmits<{
  change: [league: string]
}>()

const popularLeagues = ['英超', '西甲', '德甲', '意甲', '法甲', '欧冠']
</script>

<template>
  <div class="flex items-center gap-2 mb-6 overflow-x-auto pb-2 scrollbar-hide">
    <div class="flex items-center gap-1.5 mr-2 shrink-0">
      <ListFilter class="w-4 h-4 text-text-muted" />
      <span class="text-text-muted text-sm">筛选：</span>
    </div>

    <button
      class="shrink-0 px-4 py-1.5 rounded-full text-sm font-medium transition-all duration-200"
      :class="activeLeague === '' ? 'bg-football-primary text-white shadow-lg shadow-football-primary/20' : 'bg-football-card text-text-secondary hover:bg-football-card-hover hover:text-text-primary'"
      @click="emit('change', '')"
    >
      全部
    </button>

    <template v-for="league in leagues" :key="league">
      <button
        v-if="popularLeagues.includes(league)"
        class="shrink-0 px-4 py-1.5 rounded-full text-sm font-medium transition-all duration-200"
        :class="activeLeague === league ? 'bg-football-primary text-white shadow-lg shadow-football-primary/20' : 'bg-football-card text-text-secondary hover:bg-football-card-hover hover:text-text-primary'"
        @click="emit('change', league)"
      >
        {{ league }}
      </button>
    </template>

    <template v-for="league in leagues" :key="'other-' + league">
      <button
        v-if="!popularLeagues.includes(league)"
        class="shrink-0 px-4 py-1.5 rounded-full text-sm font-medium transition-all duration-200"
        :class="activeLeague === league ? 'bg-football-primary text-white shadow-lg shadow-football-primary/20' : 'bg-football-card text-text-secondary hover:bg-football-card-hover hover:text-text-primary'"
        @click="emit('change', league)"
      >
        {{ league }}
      </button>
    </template>
  </div>
</template>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
