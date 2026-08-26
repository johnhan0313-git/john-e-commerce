<template>
  <section>
    <div class="head">
      <h1>购物车</h1>
      <button v-if="items.length" class="btn btn-ghost" type="button" @click="clearAll">清空</button>
    </div>

    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!items.length" class="empty card">
      购物车为空，<router-link to="/products">去选购</router-link>
    </div>
    <div v-else class="list">
      <div v-for="item in items" :key="item.id" class="card row">
        <label class="check">
          <input
            type="checkbox"
            :checked="item.selected === 1"
            @change="toggleSelect(item)"
          />
        </label>
        <div class="meta">
          <strong>{{ item.spuName || item.skuName }}</strong>
          <p class="muted">{{ item.skuName }}</p>
          <p class="price">¥{{ item.price != null ? formatCents(item.price) : '—' }}</p>
        </div>
        <div class="ops">
          <input
            class="qty"
            type="number"
            min="1"
            :value="item.quantity"
            @change="onQty(item, ($event.target as HTMLInputElement).value)"
          />
          <button class="btn btn-ghost" type="button" @click="remove(item.id)">删除</button>
        </div>
      </div>

      <div class="footer card">
        <div>
          <span class="muted">已选 {{ selectedCount }} 件</span>
          <strong class="price total">合计 ¥{{ selectedTotal }}</strong>
        </div>
        <router-link class="btn" to="/checkout">去结算</router-link>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import client from '@/api/client'
import { toast } from '@/utils/toast'
import { formatCents } from '@john/fe-shared/money'
import type { CartItem, R, Sku } from '@/types'

const items = ref<CartItem[]>([])
const loading = ref(true)

const selectedItems = computed(() => items.value.filter((i) => i.selected === 1))
const selectedCount = computed(() => selectedItems.value.reduce((s, i) => s + i.quantity, 0))
const selectedTotal = computed(() =>
  formatCents(selectedItems.value.reduce((s, i) => s + (Number(i.price) || 0) * i.quantity, 0))
)

async function enrichPrices(list: CartItem[]) {
  const uniqueSkuIds = [...new Set(list.map((i) => String(i.skuId)))]
  const priceMap = new Map<string, number>()
  await Promise.all(
    uniqueSkuIds.map(async (skuId) => {
      try {
        const res = await client.get(`/sku/${skuId}`) as R<Sku>
        if (res.data?.price != null) priceMap.set(skuId, Number(res.data.price))
      } catch {
        /* ignore */
      }
    })
  )
  return list.map((i) => ({ ...i, price: priceMap.get(String(i.skuId)) }))
}

async function load() {
  loading.value = true
  try {
    const res = await client.get('/cart') as R<CartItem[]>
    items.value = await enrichPrices(res.data || [])
  } finally {
    loading.value = false
  }
}

async function toggleSelect(item: CartItem) {
  const next = item.selected === 1 ? 0 : 1
  await client.put(`/cart/${item.id}/selected`, null, { params: { selected: next } })
  item.selected = next
}

async function onQty(item: CartItem, value: string) {
  const quantity = Math.max(1, Number(value) || 1)
  await client.put(`/cart/${item.id}/quantity`, null, { params: { quantity } })
  item.quantity = quantity
}

async function remove(id: number | string) {
  await client.delete(`/cart/${id}`)
  items.value = items.value.filter((i) => i.id !== id)
  toast('已删除', 'success')
}

async function clearAll() {
  await client.delete('/cart')
  items.value = []
  toast('已清空', 'success')
}

onMounted(load)
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-4);
}

.ops {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.qty {
  width: 72px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 8px;
}

.footer {
  margin-top: var(--space-3);
  padding: var(--space-4);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total {
  margin-left: var(--space-3);
  font-size: 1.2rem;
}

@media (max-width: 640px) {
  .row {
    grid-template-columns: auto 1fr;
  }
  .ops {
    grid-column: 1 / -1;
    justify-content: flex-end;
  }
}
</style>
