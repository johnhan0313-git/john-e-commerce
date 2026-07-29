<template>
  <div style="display:flex">
    <aside style="width:200px; border-right:1px solid #ccc; padding:16px">
      <h3>Admin</h3>
      <ul style="list-style:none; padding:0">
        <li><router-link to="/dashboard">仪表盘</router-link></li>
        <li><router-link to="/products">商品</router-link></li>
        <li><router-link to="/orders">订单</router-link></li>
        <li v-if="modules.isEnabled('purchase')"><router-link to="/purchase">采购</router-link></li>
        <li v-if="modules.isEnabled('payment')"><router-link to="/payment">支付</router-link></li>
        <li><router-link to="/tenant/modules">模块配置</router-link></li>
      </ul>
    </aside>
    <main style="flex:1; padding:16px">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useModulesStore } from '@/stores/modules'

const modules = useModulesStore()
modules.fetch()
</script>
