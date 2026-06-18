<script setup lang="ts">
import { Trophy } from 'lucide-vue-next'
</script>

<template>
  <footer class="bg-football-card border-t border-football-card-hover mt-auto">
    <div class="max-w-7xl mx-auto px-4 py-6 flex flex-col md:flex-row items-center justify-between gap-4">
      <div class="flex items-center gap-2 text-text-muted text-sm">
        <Trophy class="w-4 h-4 text-football-gold" />
        <span>FootballPred - 足球数据与预测平台</span>
      </div>
      <div class="flex items-center gap-6 text-text-muted text-sm">
        <span>数据对齐中国竞彩足球</span>
        <span class="hidden sm:inline">|</span>
        <span class="hidden sm:inline">仅供数据分析参考</span>
      </div>
      <span class="text-text-muted text-xs">© 2026 FootballPred</span>
    </div>
  </footer>
</template>
