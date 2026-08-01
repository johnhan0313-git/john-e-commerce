import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useModulesStore } from '@/stores/modules'

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
        { path: 'merchants', component: () => import('@/pages/Merchants.vue'), meta: { module: 'merchant' } },
        { path: 'shops', component: () => import('@/pages/Shops.vue'), meta: { module: 'merchant' } },
        { path: 'products', component: () => import('@/pages/Products.vue'), meta: { module: 'product' } },
        { path: 'orders', component: () => import('@/pages/Orders.vue'), meta: { module: 'trade' } },
        { path: 'settlements', component: () => import('@/pages/Settlements.vue'), meta: { module: 'settle' } },
        { path: 'tenant/modules', component: () => import('@/pages/TenantModules.vue') },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (!auth.isLoggedIn && to.path !== '/login') return '/login'
  if (auth.isLoggedIn && to.meta.guest) return '/dashboard'

  if (to.meta.module && auth.isLoggedIn) {
    const modules = useModulesStore()
    if (!modules.loaded) await modules.fetch()
    if (!modules.isEnabled(to.meta.module as string)) {
      return '/tenant/modules'
    }
  }
})

export default router
