<template>
  <el-container class="layout">
    <el-aside :width="asideWidth" class="aside">
      <div class="brand">
        <span class="brand-mark">J</span>
        <div class="brand-text">
          <strong>John Admin</strong>
          <span>运营控制台</span>
        </div>
      </div>
      <el-menu :default-active="active" router class="side-menu">
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item v-if="modules.isEnabled('merchant')" index="/merchants">
          <el-icon><UserFilled /></el-icon>
          <span>卖家</span>
        </el-menu-item>
        <el-menu-item v-if="modules.isEnabled('merchant')" index="/shops">
          <el-icon><Shop /></el-icon>
          <span>店铺</span>
        </el-menu-item>
        <el-menu-item v-if="modules.isEnabled('product')" index="/products">
          <el-icon><Goods /></el-icon>
          <span>商品</span>
        </el-menu-item>
        <el-menu-item v-if="modules.isEnabled('trade')" index="/orders">
          <el-icon><List /></el-icon>
          <span>订单</span>
        </el-menu-item>
        <el-menu-item v-if="modules.isEnabled('settle')" index="/settlements">
          <el-icon><Wallet /></el-icon>
          <span>结算</span>
        </el-menu-item>
        <el-menu-item index="/tenant/modules">
          <el-icon><SetUp /></el-icon>
          <span>模块配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="main-wrap">
      <el-header class="header" height="56px">
        <div class="header-left">
          <span class="crumb">租户运营后台</span>
        </div>
        <div class="header-right">
          <span class="user-chip">{{ auth.email || '管理员' }}</span>
          <el-button text class="logout-btn" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <div class="page">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Goods, List, Odometer, SetUp, Shop, UserFilled, Wallet } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useModulesStore } from '@/stores/modules'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const modules = useModulesStore()

const active = computed(() => route.path)
const asideWidth = '232px'

onMounted(() => {
  modules.fetch()
})

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: var(--color-bg);
}

.aside {
  background: linear-gradient(180deg, var(--color-sidebar) 0%, #111827 100%);
  border-right: 1px solid rgba(148, 163, 184, 0.08);
  display: flex;
  flex-direction: column;
}

.brand {
  height: var(--header-h);
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
}

.brand-mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 14px;
  color: #fff;
  background: linear-gradient(135deg, #0ea5e9, #0369a1);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.35);
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.brand-text strong {
  color: #f8fafc;
  font-size: 14px;
}

.brand-text span {
  color: var(--color-sidebar-text);
  font-size: 11px;
  margin-top: 2px;
}

.side-menu {
  padding: 12px 0;
  flex: 1;
}

.main-wrap {
  min-width: 0;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-border);
  padding: 0 24px;
}

.crumb {
  font-size: 13px;
  color: var(--color-muted);
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-chip {
  font-size: 13px;
  color: var(--color-ink);
  background: #f1f5f9;
  border: 1px solid var(--color-border);
  border-radius: 99px;
  padding: 4px 12px;
  max-width: 220px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logout-btn {
  color: var(--color-muted) !important;
  font-weight: 600;
}

.logout-btn:hover {
  color: var(--color-danger) !important;
}

.main {
  padding: 24px;
}
</style>
