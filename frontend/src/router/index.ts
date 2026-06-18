import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
    },
    {
      path: '/matches/:id',
      name: 'match-detail',
      component: () => import('../views/MatchDetailView.vue'),
    },
    {
      path: '/team/:id',
      name: 'team',
      component: () => import('../views/TeamView.vue'),
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue'),
    },
  ],
})

export default router
