<template>
  <div v-if="product">
    <h2>{{ product.name }}</h2>
    <p>{{ product.description }}</p>
    <p>¥{{ product.price }}</p>
    <button @click="addToCart">加入购物车</button>
  </div>
  <div v-else><p>加载中...</p></div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import client from '../api/client'

const route = useRoute()
const product = ref<any>(null)

onMounted(async () => {
  try {
    const res = await client.get(`/product/${route.params.id}`)
    product.value = res.data.data
  } catch { /* ignore */ }
})

async function addToCart() {
  try {
    await client.post('/cart', { spuId: product.value.id, skuId: product.value.skuId, quantity: 1 })
    alert('已添加')
  } catch { /* ignore */ }
}
</script>
