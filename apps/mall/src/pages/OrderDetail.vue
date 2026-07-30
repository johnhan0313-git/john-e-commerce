<template>
  <section v-if="loading" class="empty">加载中…</section>
  <section v-else-if="!order" class="empty card">订单不存在</section>
  <section v-else>
    <div class="head">
      <div>
        <h1>{{ order.orderNo || `订单 #${order.id}` }}</h1>
        <p class="muted">
          {{ order.statusLabel || `状态 ${order.status}` }}
          · {{ order.payStatusLabel || `支付 ${order.payStatus}` }}
        </p>
      </div>
      <router-link class="btn btn-ghost" to="/orders">返回列表</router-link>
    </div>

    <div class="card block">
      <div class="kv"><span>应付</span><strong class="price">¥{{ order.payAmount ?? 0 }}</strong></div>
      <div class="kv"><span>已付</span><strong>¥{{ order.paidAmount ?? 0 }}</strong></div>
      <div class="kv"><span>订单组</span><span>{{ order.orderGroupNo || '—' }}</span></div>
    </div>

    <h2>商品</h2>
    <div class="card items">
      <div v-for="item in order.items || []" :key="item.id" class="item">
        <div>
          <strong>{{ item.skuName || item.skuId }}</strong>
          <p class="muted">× {{ item.quantity }}</p>
        </div>
        <span class="price">¥{{ item.payAmount ?? item.subtotal ?? item.price }}</span>
      </div>
      <div v-if="!(order.items || []).length" class="empty">无明细</div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import client from '@/api/client'
import type { Order, R } from '@/types'

const route = useRoute()
const order = ref<Order | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await client.get(`/order/${route.params.id}`) as R<Order>
    order.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.block {
  padding: var(--space-4);
  margin-bottom: var(--space-5);
}

.kv {
  display: flex;
  justify-content: space-between;
  padding: var(--space-2) 0;
}

.items {
  padding: var(--space-2) var(--space-4);
}

.item {
  display: flex;
  justify-content: space-between;
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--color-border);
}

.item:last-child {
  border-bottom: none;
}
</style>
