<template>
  <el-container class="layout">
    <el-aside :width="asideWidth" class="aside">
      <div class="brand">
        <span class="brand-mark">M</span>
        <div class="brand-text">
          <strong>John Merchant</strong>
          <span>卖家工作台</span>
        </div>
      </div>
      <el-menu :default-active="active" router class="side-menu">
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>概览</span>
        </el-menu-item>
        <el-menu-item index="/apply">
          <el-icon><Document /></el-icon>
          <span>主体 / 店铺</span>
        </el-menu-item>
        <el-menu-item v-if="approved" index="/products">
          <el-icon><Goods /></el-icon>
          <span>本店商品</span>
        </el-menu-item>
        <el-menu-item v-if="approved" index="/orders">
          <el-icon><List /></el-icon>
          <span>本店订单</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="main-wrap">
      <el-header class="header" height="56px">
        <div class="header-left">
          <span class="crumb">卖家管理端</span>
          <el-select
            v-if="openShops.length"
            :model-value="activeShopId ?? undefined"
            placeholder="选择店铺"
            size="small"
            style="width: 180px; margin-left: 12px"
            @change="onShopChange"
          >
            <el-option
              v-for="s in openShops"
              :key="String(s.id)"
              :label="s.name"
              :value="String(s.id)"
            />
          </el-select>
        </div>
        <div class="header-right">
          <el-button v-if="merchantApproved" text type="primary" @click="$router.push('/apply')">
            申请新店
          </el-button>
          <span class="user-chip">{{ auth.email || '卖家账号' }}</span>
          <el-button text class="logout-btn" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <div class="page">
          <router-view :key="viewKey" />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, Goods, List, Odometer } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useMerchantStore } from '@/stores/merchant'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const merchant = useMerchantStore()

const active = computed(() => route.path)
const approved = computed(() => merchant.isApproved())
const merchantApproved = computed(() => merchant.me?.merchant?.status === 1)
const openShops = computed(() => merchant.openShops)
const activeShopId = computed(() => merchant.activeShopId)
/** Remount page content when shop switches so lists reload with new X-Shop-Id. */
const viewKey = computed(() => `${route.path}::${activeShopId.value ?? 'none'}`)
const asideWidth = '232px'

onMounted(() => {
  if (auth.isLoggedIn && !merchant.loaded) merchant.fetchMe()
})

function onShopChange(id: string | number) {
  merchant.setActiveShop(id)
}

function logout() {
  auth.logout()
  merchant.clear()
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
  background: linear-gradient(135deg, #14b8a6, #0f766e);
  box-shadow: 0 4px 12px rgba(20, 184, 166, 0.35);
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

.header-left {
  display: flex;
  align-items: center;
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
