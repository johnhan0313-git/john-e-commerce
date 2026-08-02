import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('@/pages/Home.vue') },
    { path: '/login', component: () => import('@/pages/Login.vue'), meta: { guest: true } },
    { path: '/products', component: () => import('@/pages/Products.vue') },
    { path: '/products/:id', component: () => import('@/pages/ProductDetail.vue') },
    { path: '/shops/:id', component: () => import('@/pages/Shop.vue') },
    { path: '/cart', component: () => import('@/pages/Cart.vue'), meta: { auth: true } },
    { path: '/checkout', component: () => import('@/pages/Checkout.vue'), meta: { auth: true } },
    { path: '/pay', component: () => import('@/pages/Pay.vue'), meta: { auth: true } },
    { path: '/orders', component: () => import('@/pages/Orders.vue'), meta: { auth: true } },
    { path: '/orders/:id', component: () => import('@/pages/OrderDetail.vue'), meta: { auth: true } },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.auth && !auth.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guest && auth.isLoggedIn) {
    return (to.query.redirect as string) || '/'
  }
})

export default router
