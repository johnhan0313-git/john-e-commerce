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
          <span class="price">¥{{ formatCents((Number(item.price) || 0) * item.quantity) }}</span>
        </div>
      </div>
      <div class="summary card">
        <div>
          <span class="muted">应付</span>
          <strong class="price">¥{{ totalLabel }}</strong>
        </div>
        <button class="btn" type="button" :disabled="submitting" @click="submit">
          {{ submitting ? '提交中…' : '提交订单' }}
        </button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useCheckout } from '@/features/checkout/useCheckout'

const { items, loading, submitting, totalLabel, load, submit, formatCents } = useCheckout()
onMounted(load)
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
