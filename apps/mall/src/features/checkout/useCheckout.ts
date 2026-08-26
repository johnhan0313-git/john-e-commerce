import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import client from '@/api/client'
import { toast } from '@/utils/toast'
import { formatCents } from '@john/fe-shared/money'
import type { CartItem, OrderGroup, R, Sku } from '@/types'

export function useCheckout() {
  const router = useRouter()
  const items = ref<CartItem[]>([])
  const loading = ref(true)
  const submitting = ref(false)

  const totalLabel = computed(() =>
    formatCents(items.value.reduce((s, i) => s + (Number(i.price) || 0) * i.quantity, 0)),
  )

  async function enrichPrices(list: CartItem[]) {
    const map = new Map<string, number>()
    await Promise.all(
      [...new Set(list.map((i) => String(i.skuId)))].map(async (skuId) => {
        try {
          const res = (await client.get(`/sku/${skuId}`)) as R<Sku>
          if (res.data?.price != null) map.set(skuId, Number(res.data.price))
        } catch {
          /* ignore */
        }
      }),
    )
    return list.map((i) => ({ ...i, price: map.get(String(i.skuId)) }))
  }

  async function load() {
    loading.value = true
    try {
      const res = (await client.get('/cart')) as R<CartItem[]>
      const all = await enrichPrices(res.data || [])
      items.value = all.filter((i) => i.selected === 1)
    } finally {
      loading.value = false
    }
  }

  async function submit() {
    if (!items.value.length) return
    submitting.value = true
    try {
      const res = (await client.post('/order', {
        items: items.value.map((i) => ({ skuId: i.skuId, quantity: i.quantity })),
      })) as R<OrderGroup>
      const group = res.data
      if (!group?.orderGroupNo) {
        toast('下单失败：未返回订单组', 'error')
        return
      }
      toast('下单成功', 'success')
      router.replace({
        path: '/pay',
        query: { groupNo: group.orderGroupNo },
      })
    } catch {
      /* interceptor */
    } finally {
      submitting.value = false
    }
  }

  return { items, loading, submitting, totalLabel, load, submit, formatCents }
}
