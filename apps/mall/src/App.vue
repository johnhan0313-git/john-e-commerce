<template>
  <div id="mall-app">
    <header class="topbar">
      <div class="container topbar-inner">
        <router-link to="/" class="brand">
          <img v-if="branding.logo" :src="branding.logo" alt="" class="brand-logo" />
          <span>{{ branding.displayName }}</span>
        </router-link>
        <nav class="nav">
          <router-link to="/">首页</router-link>
          <router-link to="/products">商品</router-link>
          <router-link to="/cart">购物车</router-link>
          <router-link to="/orders">订单</router-link>
        </nav>
        <div class="auth">
          <template v-if="auth.isLoggedIn">
            <span class="muted">已登录</span>
            <button class="btn btn-ghost" type="button" @click="onLogout">退出</button>
          </template>
          <router-link v-else class="btn" to="/login">登录 / 注册</router-link>
        </div>
      </div>
    </header>
    <main class="main">
      <div class="container">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useBrandingStore } from '@/stores/branding'
import { useModulesStore } from '@/stores/modules'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const branding = useBrandingStore()
const modules = useModulesStore()
const router = useRouter()

onMounted(() => {
  if (!branding.loaded) branding.fetch()
})

if (auth.isLoggedIn) {
  modules.fetch()
}

function onLogout() {
  auth.logout()
  modules.list = []
  router.push('/')
}
</script>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  height: var(--nav-h);
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-border);
}

.topbar-inner {
  height: 100%;
  display: flex;
  align-items: center;
  gap: var(--space-5);
}

.brand {
  font-family: var(--font-display);
  font-size: 1.25rem;
  font-weight: 600;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: inherit;
  text-decoration: none;
}

.brand-logo {
  width: 28px;
  height: 28px;
  object-fit: contain;
  border-radius: 6px;
  flex-shrink: 0;
}

.nav {
  display: flex;
  gap: var(--space-4);
  flex: 1;
}

.nav a {
  color: var(--color-muted);
  font-weight: 500;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
}

.nav a.router-link-active {
  color: var(--color-ink);
  border-bottom-color: var(--color-accent);
}

.auth {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.main {
  padding: var(--space-5) 0 var(--space-6);
  min-height: calc(100vh - var(--nav-h));
}

@media (max-width: 720px) {
  .topbar-inner {
    gap: var(--space-3);
  }
  .nav {
    gap: var(--space-3);
    overflow-x: auto;
  }
  .auth .muted {
    display: none;
  }
}
</style>
