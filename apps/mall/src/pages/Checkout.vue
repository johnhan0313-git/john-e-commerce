<template>
  <div>
    <h1>结算</h1>
    <button @click="submit">提交订单</button>
  </div>
</template>

<script setup lang="ts">
import client from '@/api/client'
import { useRouter } from 'vue-router'

const router = useRouter()

async function submit() {
  // Minimal: gather cart items and submit
  const cartRes: any = await client.get('/cart')
  const items = (cartRes.data || []).map((c: any) => ({ skuId: c.skuId, quantity: c.quantity }))
  if (!items.length) return
  await client.post('/order', { items })
  router.push('/orders')
}
</script>
