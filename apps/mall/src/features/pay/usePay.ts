import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import client from '@/api/client'
import { toast } from '@/utils/toast'
import { formatCents } from '@john/fe-shared/money'
import type { OrderGroup, PayMethod, Payment, R } from '@/types'

export function usePay() {
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

  async function resolveFromGroup() {
    if (!groupNo.value) {
      toast('缺少订单组编号', 'error')
      return
    }
    const res = (await client.get(`/order/group/${groupNo.value}`)) as R<OrderGroup>
    const group = res.data
    payItems.value = (group.orders || []).map((o) => ({
      orderId: o.id,
      amount: Number(o.payAmount ?? o.totalAmount ?? 0),
    }))
    payAmount.value = formatCents(group.payAmount || 0)
  }

  async function load() {
    loading.value = true
    try {
      await resolveFromGroup()
      const res = (await client.get('/cashier/methods')) as R<PayMethod[]>
      methods.value = (res.data || []).filter((m) => m.status !== 0)
      methodCode.value = methods.value[0]?.methodCode || ''
    } finally {
      loading.value = false
    }
  }

  async function pay() {
    if (!methodCode.value || !payItems.value.length) {
      toast('缺少支付项或支付方式', 'error')
      return
    }
    paying.value = true
    try {
      const res = (await client.post('/payment', {
        methodCode: methodCode.value,
        items: payItems.value,
      })) as R<Payment>
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

  return {
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
  }
}
