<template>
  <div>
    <h1>仪表盘</h1>
    <div v-if="stats">
      <p>GMV: ¥{{ stats.gmv }}</p>
      <h3>订单状态分布</h3>
      <ul>
        <li v-for="(cnt, status) in stats.orderCountByStatus" :key="status">状态{{ status }}: {{ cnt }}</li>
      </ul>
      <h3>热销 SKU</h3>
      <ul>
        <li v-for="s in stats.topSkus" :key="s.skuId">{{ s.skuName }} - {{ s.totalQty }}件</li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '@/api/client'

const stats = ref<any>(null)
onMounted(async () => {
  try {
    const res: any = await client.get('/statistics/overview')
    stats.value = res.data
  } catch { /* module may not be enabled */ }
})
</script>
