<template>
  <div>
    <h2>数据统计</h2>
    <div v-if="overview">
      <p>订单总数: {{ overview.orderCount }}</p>
      <p>GMV: ¥{{ overview.gmv }}</p>
      <p>已支付订单: {{ overview.paidOrderCount }}</p>
      <h3>热销SKU TOP10</h3>
      <table>
        <tr><th>SKU</th><th>数量</th><th>金额</th></tr>
        <tr v-for="s in overview.topSkus" :key="s.skuId">
          <td>{{ s.skuName }}</td><td>{{ s.totalQty }}</td><td>¥{{ s.totalAmount }}</td>
        </tr>
      </table>
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
