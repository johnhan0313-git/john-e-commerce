<template>
  <div>
    <h2>购物车</h2>
    <ul>
      <li v-for="item in items" :key="item.id">{{ item.skuName }} x{{ item.quantity }}</li>
    </ul>
    <p v-if="!items.length">购物车为空</p>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '../api/client'

const items = ref<any[]>([])

onMounted(async () => {
  try {
    const res = await client.get('/cart')
    items.value = res.data.data || []
  } catch { /* ignore */ }
})
</script>
