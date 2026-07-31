import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useMerchantStore } from '@/stores/merchant'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/pages/Login.vue'), meta: { guest: true } },
    {
      path: '/',
      component: () => import('@/layout/Layout.vue'),
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: () => import('@/pages/Dashboard.vue') },
        { path: 'apply', component: () => import('@/pages/Apply.vue') },
        { path: 'products', component: () => import('@/pages/Products.vue'), meta: { shop: true } },
        { path: 'orders', component: () => import('@/pages/Orders.vue'), meta: { shop: true } },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (!auth.isLoggedIn && to.path !== '/login') return '/login'
  if (auth.isLoggedIn && to.meta.guest) return '/dashboard'

  if (auth.isLoggedIn && to.path !== '/login') {
    const merchant = useMerchantStore()
    if (!merchant.loaded) {
      try {
        await merchant.fetchMe()
      } catch {
        merchant.clear()
      }
    }
    if (to.meta.shop && !merchant.isApproved()) {
      return '/apply'
    }
  }
})

export default router
