<script setup lang="ts">
import { Frown } from 'lucide-vue-next'
</script>

<template>
  <div class="flex flex-col items-center justify-center py-20 px-4">
    <Frown class="w-20 h-20 text-text-muted mb-6" />
    <h1 class="text-4xl font-bold text-text-primary mb-3">404</h1>
    <p class="text-text-muted text-lg mb-8">页面未找到</p>
    <router-link to="/"
      class="px-6 py-2.5 bg-football-primary hover:bg-football-primary-light text-white rounded-lg transition-colors text-sm font-medium">
      返回首页
    </router-link>
  </div>
</template>
