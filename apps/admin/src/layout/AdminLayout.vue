<template>
  <div style="display:flex; min-height:100vh">
    <aside style="width:200px; background:#f5f5f5; padding:10px">
      <h3>后台管理</h3>
      <ul style="list-style:none; padding:0">
        <li><router-link to="/">Dashboard</router-link></li>
        <li v-if="m.has('product')"><router-link to="/products">商品管理</router-link></li>
        <li v-if="m.has('trade')"><router-link to="/orders">订单管理</router-link></li>
        <li v-if="m.has('activity')"><router-link to="/activities">营销活动</router-link></li>
        <li v-if="m.has('purchase')"><router-link to="/purchase">采购管理</router-link></li>
        <li v-if="m.has('fulfillment')"><router-link to="/warehouse">仓储</router-link></li>
        <li v-if="m.has('payment')"><router-link to="/payment">支付</router-link></li>
        <li v-if="m.has('settle')"><router-link to="/settlement">结算</router-link></li>
        <li v-if="m.has('content')"><router-link to="/content">内容管理</router-link></li>
        <li v-if="m.has('statistics')"><router-link to="/statistics">统计</router-link></li>
        <li><router-link to="/tenant">租户模块</router-link></li>
      </ul>
      <button @click="authStore.logout()">退出</button>
    </aside>
    <main style="flex:1; padding:20px">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { modulesStore as m } from '../stores/modules'
import { authStore } from '../stores/auth'

onMounted(() => m.fetch())
</script>
