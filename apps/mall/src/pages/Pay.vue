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
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import client from '@/api/client'
import { toast } from '@/utils/toast'
import type { OrderGroup, PayMethod, Payment, R } from '@/types'

const route = useRoute()
const router = useRouter()
const isDev = import.meta.env.DEV

const groupNo = computed(() => (route.query.groupNo as string) || '')
const methods = ref<PayMethod[]>([])
const methodCode = ref('')
const loading = ref(true)
const paying = ref(false)
const payResult = ref<Payment | null>(null)
const payItems = ref<{ orderId: number; amount: number }[]>([])
const payAmount = ref('0.00')

async function resolvePayload() {
  if (route.query.payload) {
    try {
      const raw = JSON.parse(decodeURIComponent(route.query.payload as string))
      payItems.value = raw.orders || []
      payAmount.value = Number(raw.payAmount || 0).toFixed(2)
      return
    } catch { /* fallthrough */ }
  }
  if (groupNo.value) {
    const res = await client.get(`/order/group/${groupNo.value}`) as R<OrderGroup>
    const group = res.data
    payItems.value = (group.orders || []).map((o) => ({
      orderId: o.id,
      amount: Number(o.payAmount ?? o.totalAmount ?? 0),
    }))
    payAmount.value = Number(group.payAmount || 0).toFixed(2)
  }
}

onMounted(async () => {
  try {
    await resolvePayload()
    const res = await client.get('/cashier/methods') as R<PayMethod[]>
    methods.value = (res.data || []).filter((m) => m.status !== 0)
    methodCode.value = methods.value[0]?.methodCode || ''
  } finally {
    loading.value = false
  }
})

async function pay() {
  if (!methodCode.value || !payItems.value.length) {
    toast('缺少支付项或支付方式', 'error')
    return
  }
  paying.value = true
  try {
    const res = await client.post('/payment', {
      methodCode: methodCode.value,
      items: payItems.value,
    }) as R<Payment>
    payResult.value = res.data
    toast('支付单已创建', 'success')
    if (isDev && res.data?.payNo) {
      await client.post('/payment/mock-callback', null, { params: { payNo: res.data.payNo } })
      toast('Mock 支付成功', 'success')
      const firstOrderId = payItems.value[0]?.orderId
      router.replace(firstOrderId ? `/orders/${firstOrderId}` : '/orders')
    }
  } finally {
    paying.value = false
  }
}

async function mockCallback() {
  if (!payResult.value?.payNo) return
  await client.post('/payment/mock-callback', null, { params: { payNo: payResult.value.payNo } })
  toast('Mock 支付成功', 'success')
  const firstOrderId = payItems.value[0]?.orderId
  router.replace(firstOrderId ? `/orders/${firstOrderId}` : '/orders')
}
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
