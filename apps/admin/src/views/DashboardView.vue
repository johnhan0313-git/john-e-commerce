<template>
  <div>
    <h2>Dashboard</h2>
    <div v-if="overview">
      <p>订单总数: {{ overview.orderCount }}</p>
      <p>GMV: ¥{{ overview.gmv }}</p>
      <p>已支付订单: {{ overview.paidOrderCount }}</p>
      <h3>热销SKU</h3>
      <ul>
        <li v-for="s in overview.topSkus" :key="s.skuId">{{ s.skuName }} - {{ s.totalQty }}件 ¥{{ s.totalAmount }}</li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '../api/client'

const overview = ref<any>(null)

onMounted(async () => {
  try {
    const res = await client.get('/statistics/overview')
    overview.value = res.data.data
  } catch { /* ignore */ }
})
</script>
