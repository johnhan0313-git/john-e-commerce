<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="brand">John Admin</div>
      <el-menu :default-active="active" router>
        <el-menu-item index="/dashboard">仪表盘</el-menu-item>
        <el-menu-item v-if="modules.isEnabled('merchant')" index="/merchants">卖家</el-menu-item>
        <el-menu-item v-if="modules.isEnabled('merchant')" index="/shops">店铺</el-menu-item>
        <el-menu-item v-if="modules.isEnabled('product')" index="/products">商品</el-menu-item>
        <el-menu-item v-if="modules.isEnabled('trade')" index="/orders">订单</el-menu-item>
        <el-menu-item index="/tenant/modules">模块配置</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="muted">租户运营后台</span>
        <el-button type="danger" link @click="logout">退出</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useModulesStore } from '@/stores/modules'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const modules = useModulesStore()

const active = computed(() => route.path)

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
  background: #f5f7fa;
}

.aside {
  background: #fff;
  border-right: 1px solid #ebeef5;
}

.brand {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-weight: 700;
  font-size: 16px;
  border-bottom: 1px solid #ebeef5;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.muted {
  color: #909399;
}
</style>
