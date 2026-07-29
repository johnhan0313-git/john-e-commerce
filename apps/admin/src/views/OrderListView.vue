<template>
  <div>
    <h2>订单管理</h2>
    <ul>
      <li v-for="o in orders" :key="o.id">{{ o.orderNo }} - {{ o.status }} - ¥{{ o.payAmount }}</li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '../api/client'

const orders = ref<any[]>([])

onMounted(async () => {
  try {
    const res = await client.get('/order')
    orders.value = res.data.data?.records || []
  } catch { /* ignore */ }
})
</script>
