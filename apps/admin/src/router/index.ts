import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/pages/Login.vue') },
    {
      path: '/',
      component: () => import('@/layout/Layout.vue'),
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: () => import('@/pages/Dashboard.vue') },
        { path: 'products', component: () => import('@/pages/Products.vue') },
        { path: 'orders', component: () => import('@/pages/Orders.vue') },
        { path: 'purchase', component: () => import('@/pages/Purchase.vue'), meta: { module: 'purchase' } },
        { path: 'payment', component: () => import('@/pages/Payment.vue'), meta: { module: 'payment' } },
        { path: 'tenant/modules', component: () => import('@/pages/TenantModules.vue') },
      ]
    }
  ]
})

router.beforeEach((to) => {
  const token = localStorage.getItem('admin_token')
  if (!token && to.path !== '/login') return '/login'
})

export default router
