import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import client from '@/api/client'
import type { MerchantMe, R, Shop } from '@/types'

const ACTIVE_SHOP_KEY = 'merchant_active_shop_id'

export const useMerchantStore = defineStore('merchant', () => {
  const me = ref<MerchantMe | null>(null)
  const loaded = ref(false)
  const activeShopId = ref<number | null>(readStoredShopId())

  const currentShop = computed(() => me.value?.currentShop ?? null)
  const shops = computed(() => me.value?.shops ?? [])
  const openShops = computed(() => shops.value.filter((s) => s.status === 1))

  function readStoredShopId(): number | null {
    const raw = localStorage.getItem(ACTIVE_SHOP_KEY)
    if (!raw) return null
    const n = Number(raw)
    return Number.isFinite(n) && n > 0 ? n : null
  }

  function persistActiveShop(id: number | null) {
    activeShopId.value = id
    if (id != null) localStorage.setItem(ACTIVE_SHOP_KEY, String(id))
    else localStorage.removeItem(ACTIVE_SHOP_KEY)
  }

  function syncActiveShopFromMe() {
    const list = me.value?.shops || []
    const open = list.filter((s) => s.status === 1)
    if (activeShopId.value != null) {
      const stillValid = open.some((s) => s.id === activeShopId.value)
      if (stillValid) return
    }
    const preferred = me.value?.currentShop?.status === 1
      ? me.value.currentShop
      : open[0]
    persistActiveShop(preferred?.id ?? null)
  }

  async function fetchMe() {
    const res = await client.get('/merchant/me') as R<MerchantMe | null>
    me.value = res.data ?? null
    loaded.value = true
    syncActiveShopFromMe()
    return me.value
  }

  function setActiveShop(shop: Shop | number) {
    const id = typeof shop === 'number' ? shop : shop.id
    persistActiveShop(id)
    if (me.value) {
      const found = me.value.shops?.find((s) => s.id === id)
      if (found) me.value.currentShop = found
    }
  }

  function clear() {
    me.value = null
    loaded.value = false
    persistActiveShop(null)
  }

  const isApproved = () =>
    me.value?.merchant?.status === 1 && openShops.value.length > 0

  return {
    me,
    loaded,
    activeShopId,
    currentShop,
    shops,
    openShops,
    fetchMe,
    setActiveShop,
    clear,
    isApproved,
  }
})
