<template>
  <section>
    <h1>收银台</h1>
    <p class="muted">订单组 {{ groupNo || '—' }} · 应付 <span class="price">¥{{ payAmount }}</span></p>

    <div v-if="loading" class="empty">加载支付方式…</div>
    <div v-else class="methods">
      <label
        v-for="m in methods"
        :key="m.methodCode"
        class="card method"
        :class="{ active: methodCode === m.methodCode }"
      >
        <input v-model="methodCode" type="radio" :value="m.methodCode" />
        <div>
          <strong>{{ m.name }}</strong>
          <p class="muted">{{ m.methodCode }}</p>
        </div>
      </label>
      <div v-if="!methods.length" class="empty card">暂无可用支付方式，请确认 PAYMENT 模块已开通</div>
    </div>

    <div class="actions">
      <button class="btn" type="button" :disabled="!methodCode || paying" @click="pay">
        {{ paying ? '支付中…' : '确认支付' }}
      </button>
      <router-link class="btn btn-ghost" to="/orders">稍后支付</router-link>
    </div>

    <p v-if="payResult" class="card result">
      支付单 {{ payResult.payNo }} · 状态 {{ payResult.status }}
      <button
        v-if="isDev"
        class="btn btn-ghost"
        type="button"
        style="margin-left: 12px"
        @click="mockCallback"
      >
        开发态 Mock 成功
      </button>
    </p>
  </section>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { usePay } from '@/features/pay/usePay'

const {
  groupNo,
  methods,
  methodCode,
  loading,
  paying,
  payResult,
  payAmount,
  isDev,
  load,
  pay,
  mockCallback,
} = usePay()

onMounted(load)
</script>

<style scoped>
.methods {
  display: grid;
  gap: var(--space-3);
  margin: var(--space-4) 0;
}

.method {
  display: flex;
  gap: var(--space-3);
  align-items: center;
  padding: var(--space-4);
  cursor: pointer;
}

.method.active {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 1px var(--color-accent);
}

.actions {
  display: flex;
  gap: var(--space-3);
}

.result {
  margin-top: var(--space-4);
  padding: var(--space-4);
}
</style>
