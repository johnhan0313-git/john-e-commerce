<template>
  <div v-if="order">
    <h2>订单 {{ order.orderNo }}</h2>
    <p>状态: {{ order.status }}</p>
    <p>金额: ¥{{ order.payAmount }}</p>
    <h3>商品</h3>
    <ul>
      <li v-for="item in order.items" :key="item.id">{{ item.skuName }} x{{ item.quantity }} ¥{{ item.subtotal }}</li>
    </ul>
  </div>
  <div v-else><p>加载中...</p></div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import client from '../api/client'

const route = useRoute()
const order = ref<any>(null)

onMounted(async () => {
  try {
    const res = await client.get(`/order/${route.params.id}`)
    order.value = res.data.data
  } catch { /* ignore */ }
})
</script>
