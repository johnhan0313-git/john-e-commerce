import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('@/pages/Home.vue') },
    { path: '/products', component: () => import('@/pages/Products.vue') },
    { path: '/cart', component: () => import('@/pages/Cart.vue') },
    { path: '/checkout', component: () => import('@/pages/Checkout.vue') },
    { path: '/orders', component: () => import('@/pages/Orders.vue') },
  ]
})

export default router
