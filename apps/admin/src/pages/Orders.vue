<template>
  <div>
    <h1>订单管理</h1>
    <div v-for="o in orders" :key="o.id">{{ o.orderNo }} - ¥{{ o.payAmount }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '@/api/client'

const orders = ref<any[]>([])
onMounted(async () => {
  const res: any = await client.get('/order', { params: { page: 1, size: 20 } })
  orders.value = res.data?.records || []
})
</script>
