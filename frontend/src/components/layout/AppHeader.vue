<script setup lang="ts">
import { Search, TrendingUp } from 'lucide-vue-next'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchKeyword = ref('')
const isScrolled = ref(false)

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/', query: { keyword: searchKeyword.value } })
  }
}

function checkScroll() {
  isScrolled.value = window.scrollY > 10
}
if (typeof window !== 'undefined') {
  window.addEventListener('scroll', checkScroll)
}
</script>

<template>
  <header
    class="fixed top-0 left-0 right-0 z-50 transition-all duration-300"
    :class="isScrolled ? 'bg-football-bg/95 backdrop-blur-md shadow-lg' : 'bg-football-bg/80 backdrop-blur-sm'"
  >
    <div class="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
      <router-link to="/" class="flex items-center gap-2.5 group cursor-pointer">
        <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-football-primary to-football-primary-light flex items-center justify-center group-hover:shadow-lg group-hover:shadow-football-primary/30 transition-all">
          <TrendingUp class="w-5 h-5 text-white" />
        </div>
        <span class="text-lg font-bold text-text-primary">
          Football<span class="text-football-primary-light">Pred</span>
        </span>
      </router-link>

      <nav class="hidden md:flex items-center gap-8">
        <router-link to="/" class="text-text-secondary hover:text-football-primary-light transition-colors text-sm font-medium">
          首页
        </router-link>
        <router-link to="/matches" class="text-text-secondary hover:text-football-primary-light transition-colors text-sm font-medium">
          比赛
        </router-link>
      </nav>

      <div class="flex items-center gap-4">
        <div class="hidden sm:flex items-center bg-football-card rounded-lg px-3 py-1.5 border border-football-card-hover">
          <Search class="w-4 h-4 text-text-muted" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索球队..."
            class="bg-transparent text-text-primary text-sm ml-2 outline-none w-32 focus:w-48 transition-all placeholder-text-muted"
            @keyup.enter="handleSearch"
          />
        </div>
      </div>
    </div>
  </header>
</template>
