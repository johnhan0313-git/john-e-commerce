<template>
  <section>
    <h1>我的订单</h1>
    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!orders.length" class="empty card">暂无订单</div>
    <div v-else class="list">
      <router-link
        v-for="o in orders"
        :key="o.id"
        class="card row"
        :to="`/orders/${o.id}`"
      >
        <div>
          <strong>{{ o.orderNo || `订单 #${o.id}` }}</strong>
          <p class="muted">
            {{ o.statusLabel || `状态 ${o.status}` }}
            · {{ o.payStatusLabel || `支付 ${o.payStatus}` }}
          </p>
        </div>
        <span class="price">¥{{ formatCents(o.payAmount ?? o.totalAmount ?? 0) }}</span>
      </router-link>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import client from '@/api/client'
import { formatCents } from '@john/fe-shared/money'
import type { Order, PageResult, R } from '@/types'

const orders = ref<Order[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await client.get('/order', { params: { page: 1, size: 50 } }) as R<PageResult<Order>>
    orders.value = res.data?.records || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  transition: transform 0.15s ease;
}

.row:hover {
  transform: translateY(-2px);
}
</style>
