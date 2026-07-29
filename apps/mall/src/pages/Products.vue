<template>
  <div>
    <h1>商品列表</h1>
    <div v-for="p in products" :key="p.id">{{ p.name }}</div>
    <button @click="loadMore">加载更多</button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '@/api/client'

const products = ref<any[]>([])
const page = ref(1)

async function load() {
  const res: any = await client.get('/product', { params: { page: page.value, size: 20 } })
  products.value = res.data?.records || []
}

function loadMore() {
  page.value++
  load()
}

onMounted(load)
</script>
