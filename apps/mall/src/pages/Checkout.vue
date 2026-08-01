<template>
  <section>
    <h1>结算</h1>
    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!items.length" class="empty card">
      没有可结算商品，<router-link to="/cart">返回购物车</router-link>
    </div>
    <div v-else>
      <div class="card list">
        <div v-for="item in items" :key="item.id" class="row">
          <div>
            <strong>{{ item.spuName || item.skuName }}</strong>
            <p class="muted">{{ item.skuName }} × {{ item.quantity }}</p>
          </div>
          <span class="price">¥{{ ((Number(item.price) || 0) * item.quantity).toFixed(2) }}</span>
        </div>
      </div>
      <div class="summary card">
        <div>
          <span class="muted">应付</span>
          <strong class="price">¥{{ total }}</strong>
        </div>
        <button class="btn" type="button" :disabled="submitting" @click="submit">
          {{ submitting ? '提交中…' : '提交订单' }}
        </button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import client from '@/api/client'
import { toast } from '@/utils/toast'
import type { CartItem, OrderGroup, R, Sku } from '@/types'

const router = useRouter()
const items = ref<CartItem[]>([])
const loading = ref(true)
const submitting = ref(false)

const total = computed(() =>
  items.value.reduce((s, i) => s + (Number(i.price) || 0) * i.quantity, 0).toFixed(2)
)

async function enrichPrices(list: CartItem[]) {
  const map = new Map<string, number>()
  await Promise.all(
    [...new Set(list.map((i) => String(i.skuId)))].map(async (skuId) => {
      try {
        const res = await client.get(`/sku/${skuId}`) as R<Sku>
        if (res.data?.price != null) map.set(skuId, Number(res.data.price))
      } catch { /* ignore */ }
    })
  )
  return list.map((i) => ({ ...i, price: map.get(String(i.skuId)) }))
}

onMounted(async () => {
  try {
    const res = await client.get('/cart') as R<CartItem[]>
    const all = await enrichPrices(res.data || [])
    items.value = all.filter((i) => i.selected === 1)
  } finally {
    loading.value = false
  }
})

async function submit() {
  if (!items.value.length) return
  submitting.value = true
  try {
    const res = await client.post('/order', {
      items: items.value.map((i) => ({ skuId: i.skuId, quantity: i.quantity })),
    }) as R<OrderGroup>
    const group = res.data
    if (!group?.orderGroupNo) {
      toast('下单失败：未返回订单组', 'error')
      return
    }
    toast('下单成功', 'success')
    router.replace({
      path: '/pay',
      query: {
        groupNo: group.orderGroupNo,
        payload: encodeURIComponent(JSON.stringify({
          orders: (group.orders || []).map((o) => ({
            orderId: o.id,
            amount: o.payAmount ?? o.totalAmount,
          })),
          payAmount: group.payAmount,
        })),
      },
    })
  } catch {
    /* interceptor already toasted */
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.list {
  padding: var(--space-2) var(--space-4);
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--color-border);
}

.row:last-child {
  border-bottom: none;
}

.summary {
  margin-top: var(--space-4);
  padding: var(--space-4);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary .price {
  margin-left: var(--space-3);
  font-size: 1.25rem;
}
</style>
