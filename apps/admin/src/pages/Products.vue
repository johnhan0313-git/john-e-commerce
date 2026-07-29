<template>
  <div>
    <h1>商品管理</h1>
    <div v-for="p in products" :key="p.id">{{ p.name }} - ¥{{ p.price }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '@/api/client'

const products = ref<any[]>([])
onMounted(async () => {
  const res: any = await client.get('/product', { params: { page: 1, size: 20 } })
  products.value = res.data?.records || []
})
</script>
