<template>
  <div>
    <h2>商品管理</h2>
    <ul>
      <li v-for="p in products" :key="p.id">{{ p.name }} - {{ p.status }}</li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '../api/client'

const products = ref<any[]>([])

onMounted(async () => {
  try {
    const res = await client.get('/product')
    products.value = res.data.data?.records || []
  } catch { /* ignore */ }
})
</script>
