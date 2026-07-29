<template>
  <div>
    <h1>购物车</h1>
    <div v-for="item in items" :key="item.id">
      {{ item.skuName }} x {{ item.quantity }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '@/api/client'

const items = ref<any[]>([])
onMounted(async () => {
  const res: any = await client.get('/cart')
  items.value = res.data || []
})
</script>
