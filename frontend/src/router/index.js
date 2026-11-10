import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/home', component: () => import('../views/Home.vue') },
  { path: '/goods/:id', component: () => import('../views/GoodsDetail.vue') },
  { path: '/publish', component: () => import('../views/Publish.vue') },
  { path: '/seckill', component: () => import('../views/Seckill.vue') },
  { path: '/orders', component: () => import('../views/MyOrders.vue') },
  { path: '/feed', component: () => import('../views/Feed.vue') },
  { path: '/nearby', component: () => import('../views/Nearby.vue') },
  { path: '/sign', component: () => import('../views/Sign.vue') },
  { path: '/agent', component: () => import('../views/Agent.vue') },
  { path: '/favorites', component: () => import('../views/Favorites.vue') },
  { path: '/messages', component: () => import('../views/Messages.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
